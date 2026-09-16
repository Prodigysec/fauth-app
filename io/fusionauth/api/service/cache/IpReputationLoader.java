package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.license.v2.LicenseProvider;
import com.inversoft.license.v2.domain.LicenseContainer;
import com.inversoft.validator.IPAddressType;
import com.inversoft.validator.IPValidator;
import io.fusionauth.api.domain.IpReputationDatabaseMapper;
import io.fusionauth.api.domain.SequencedData;
import io.fusionauth.api.domain.SequencedMetaData;
import io.fusionauth.api.service.reactor.DefaultReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorCore;
import io.fusionauth.api.service.reactor.ReactorService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.NodeService;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpReputationLoader extends AbstractReactorFileCacheLoader {
  public static final String IP_REPUTATION = "ip-reputation-";
  
  public static final ReentrantLock masterLock = new ReentrantLock();
  
  private static final int MIN_SUSPICIOUS_LIST_COUNT = 3;
  
  private static final Logger logger = LoggerFactory.getLogger(IpReputationLoader.class);
  
  private final DataSource backgroundDataSource;
  
  private final IpReputationDatabaseMapper backgroundIpReputationDatabaseMapper;
  
  private final IpReputationCache cache;
  
  @Inject
  public IpReputationLoader(IpReputationCache paramIpReputationCache, CacheNotifier paramCacheNotifier, @Named("background") IpReputationDatabaseMapper paramIpReputationDatabaseMapper, @Named("background") DataSource paramDataSource, LicenseProvider paramLicenseProvider, NodeService paramNodeService, ReactorCore paramReactorCore) {
    super(paramCacheNotifier, paramLicenseProvider, paramNodeService, paramReactorCore);
    this.cache = paramIpReputationCache;
    this.backgroundIpReputationDatabaseMapper = paramIpReputationDatabaseMapper;
    this.backgroundDataSource = paramDataSource;
  }
  
  IpReputationLoader(IpReputationCache paramIpReputationCache) {
    super(null, null, null, null);
    this.cache = paramIpReputationCache;
    this.backgroundIpReputationDatabaseMapper = null;
    this.backgroundDataSource = null;
  }
  
  public static boolean isLicensed(LicenseContainer paramLicenseContainer) {
    if (ReactorService.isLicenseContainerInvalid(paramLicenseContainer))
      return false; 
    return DefaultReactorStatusService.isLicensedForIPReputation(paramLicenseContainer.license());
  }
  
  public void removeCache() {
    if (this.cache.get() != null) {
      logger.info("Removing cache");
      this.cache.update(null, null);
    } 
  }
  
  protected String cacheName() {
    return "IpReputation";
  }
  
  protected Logger logger() {
    return logger;
  }
  
  protected ReentrantLock masterLock() {
    return masterLock;
  }
  
  protected boolean runInternal() {
    LicenseContainer licenseContainer = licenseProvider().getLicense();
    boolean bool = isLicensed(licenseContainer);
    if (!bool) {
      logger.debug("Not licensed for IP Reputation loader, removing cache and returning");
      removeCache();
      return false;
    } 
    MapAndTime mapAndTime = null;
    if (nodeService().isMaster())
      mapAndTime = downloadUpdatesFromReactor(); 
    if (mapAndTime == null)
      mapAndTime = loadFromDatabaseOrFile(); 
    if (mapAndTime != null) {
      logger.debug("Calling update on IpReputationCache");
      this.cache.update(mapAndTime.badIps, mapAndTime.lastModified);
      logger.info("Loaded [{}] IP addresses into the IP reputation cache.", Integer.valueOf(mapAndTime.badIps.size()));
      return true;
    } 
    return false;
  }
  
  Map<InetAddress, Integer> parse(InputStream paramInputStream) throws IOException {
    HashMap<Object, Object> hashMap = new HashMap<>();
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(paramInputStream, StandardCharsets.UTF_8));
    try {
      String str;
      while ((str = bufferedReader.readLine()) != null) {
        int i;
        String str1 = str.trim();
        if (str1.isEmpty() || str1.startsWith("#"))
          continue; 
        String[] arrayOfString = str1.split("\\s+");
        if (arrayOfString.length < 2) {
          logger.warn("Unable to parse line [{}], skipping.", str1);
          continue;
        } 
        try {
          i = Integer.parseInt(arrayOfString[1]);
        } catch (NumberFormatException numberFormatException) {
          logger.warn("Unable to parse weight [{}], skipping.", arrayOfString[1]);
          continue;
        } 
        if (i < 3)
          continue; 
        try {
          if (IPAddressType.Unknown.equals(IPValidator.getType(arrayOfString[0]))) {
            logger.warn("IP Address in list [{}] is not properly formatted.", arrayOfString[0]);
            continue;
          } 
          InetAddress inetAddress = InetAddress.getByName(arrayOfString[0]);
          hashMap.put(inetAddress, Integer.valueOf(i));
        } catch (UnknownHostException unknownHostException) {
          logger.warn("Unable to parse IP address [{}], skipping.", arrayOfString[0]);
        } 
      } 
      bufferedReader.close();
    } catch (Throwable throwable) {
      try {
        bufferedReader.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
    return (Map)hashMap;
  }
  
  private MapAndTime downloadFromReactor(SequencedMetaData paramSequencedMetaData) {
    try {
      Path path = getLastModifiedFilePath(paramSequencedMetaData.lastModified);
      File file = writeFileVerifyAndUnzip(path, paramSequencedMetaData.digest, paramConsumer -> reactorCore().retrieveIpReputationFile(()), this::getIpReputationPaths, "IP reputation");
      logger.info("Download complete. Update IP Reputation meta-data.");
      this.backgroundIpReputationDatabaseMapper.createIpReputationMetaData(paramSequencedMetaData);
      Map<InetAddress, Integer> map = tryParseFile(file);
      if (map == null) {
        this.backgroundIpReputationDatabaseMapper.deleteIpReputationData(paramSequencedMetaData.lastModified);
        this.backgroundIpReputationDatabaseMapper.deleteIpReputationMetaData(paramSequencedMetaData.lastModified);
        String str = "Failed to parse IP Reputation data file with last modified timestamp [" + String.valueOf(paramSequencedMetaData.lastModified) + "] at file [" + String.valueOf(file) + "]. The IP Reputation data file has been deleted. We will attempt to download the file from Reactor again at the next update interval.";
        logger.error(str);
        EventLogHelper.create(new EventLog(EventLogType.Error, str));
        return null;
      } 
      return new MapAndTime(map, paramSequencedMetaData.lastModified);
    } catch (Exception exception) {
      logger.error(exception.getMessage(), exception);
      return null;
    } 
  }
  
  private MapAndTime downloadUpdatesFromReactor() {
    MapAndTime mapAndTime = null;
    SequencedMetaData sequencedMetaData1 = this.backgroundIpReputationDatabaseMapper.getLatestIpReputationMetaData();
    boolean bool = (sequencedMetaData1 != null) ? true : false;
    SequencedMetaData sequencedMetaData2 = reactorCore().retrieveIpReputationFileLastModified();
    if (sequencedMetaData2 != null && (!bool || sequencedMetaData2.lastModified
      .isAfter(sequencedMetaData1.lastModified))) {
      logger.info("New version of IP Reputation data available from FusionAuth Reactor.");
      mapAndTime = downloadFromReactor(sequencedMetaData2);
      if (mapAndTime != null && bool) {
        this.backgroundIpReputationDatabaseMapper.deleteIpReputationData(sequencedMetaData1.lastModified);
        this.backgroundIpReputationDatabaseMapper.deleteIpReputationMetaData(sequencedMetaData1.lastModified);
      } 
    } else {
      logger.debug("We are the master node and no new metadata is available");
    } 
    return mapAndTime;
  }
  
  private List<Path> getIpReputationPaths() {
    return getDataFiles("ip-reputation-", ".txt");
  }
  
  private Path getLastModifiedFilePath(ZonedDateTime paramZonedDateTime) {
    Path path = getDataDirectory();
    return path.resolve("ip-reputation-" + ISO_FILE_SYSTEM_FRIENDLY_FORMATTER.format(paramZonedDateTime) + ".txt");
  }
  
  private MapAndTime loadFromDatabaseOrFile() {
    ZonedDateTime zonedDateTime;
    Path path;
    SequencedMetaData sequencedMetaData = this.backgroundIpReputationDatabaseMapper.getLatestIpReputationMetaData();
    if (sequencedMetaData != null) {
      zonedDateTime = sequencedMetaData.lastModified;
      logger.debug("IP Reputation database last modified [{}]", zonedDateTime);
      path = getLastModifiedFilePath(zonedDateTime);
    } else {
      zonedDateTime = mostRecentFileLastModified("ip-reputation-", ".txt");
      if (zonedDateTime == null) {
        logger.info("No IP Reputation data found in database or file system.");
        return null;
      } 
      logger.debug("IP Reputation file data last modified [{}]", zonedDateTime);
      path = getDataDirectory().resolve("ip-reputation-" + ISO_FILE_SYSTEM_FRIENDLY_FORMATTER.format(zonedDateTime) + ".txt");
    } 
    try {
      ZonedDateTime zonedDateTime1 = this.cache.lastModified();
      if (zonedDateTime1 != null && !zonedDateTime.isAfter(zonedDateTime1))
        return null; 
      if (Files.exists(path, new java.nio.file.LinkOption[0])) {
        Map<InetAddress, Integer> map = tryParseFile(path.toFile());
        if (map != null) {
          logger.info("Use existing IP Reputation data file [{}]", path.getFileName());
          return new MapAndTime(map, zonedDateTime);
        } 
      } else if (sequencedMetaData != null) {
        logger.info("Write the IP Reputation data from DB to file [{}]", String.valueOf(path.getFileName()) + ".gz");
        File file = writeFileVerifyAndUnzip(path, sequencedMetaData.digest, paramConsumer -> streamFileFromDB(paramZonedDateTime, paramConsumer), this::getIpReputationPaths, "IP reputation");
        Map<InetAddress, Integer> map = tryParseFile(file);
        if (map != null)
          return new MapAndTime(map, zonedDateTime); 
      } 
    } catch (Exception exception) {
      logger.error(exception.getMessage(), exception);
    } 
    return null;
  }
  
  private void streamFileFromDB(ZonedDateTime paramZonedDateTime, Consumer<byte[]> paramConsumer) {
    streamFileFromDB(this.backgroundDataSource, "SELECT data FROM ip_reputation_data WHERE last_modified = ? ORDER BY seq", paramZonedDateTime, paramConsumer);
  }
  
  private Map<InetAddress, Integer> tryParseFile(File paramFile) {
    if (paramFile == null)
      return null; 
    try {
      FileInputStream fileInputStream = new FileInputStream(paramFile);
      try {
        Map<InetAddress, Integer> map1 = parse(fileInputStream);
        if (map1.isEmpty()) {
          logger.warn("IP Reputation file [{}] parsed to an empty map.", paramFile.getName());
          Map map = null;
          fileInputStream.close();
          return map;
        } 
        Map<InetAddress, Integer> map2 = map1;
        fileInputStream.close();
        return map2;
      } catch (Throwable throwable) {
        try {
          fileInputStream.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (IOException iOException) {
      logger.error(iOException.getMessage(), iOException);
      logger.warn("File [{}] was not parseable. Attempting to delete.", paramFile.getName());
      try {
        Files.deleteIfExists(paramFile.toPath());
      } catch (IOException iOException1) {}
      return null;
    } 
  }
  
  private void writeToDatabase(InputStream paramInputStream, SequencedMetaData paramSequencedMetaData, Consumer<byte[]> paramConsumer) {
    byte b = 1;
    try {
      char c1 = 'ὀ';
      char c2 = 'Ϩ';
      ArrayList<SequencedData> arrayList = new ArrayList(c2);
      byte[][] arrayOfByte = new byte[c2][c1];
      byte b1 = 0;
      while (true) {
        int i = paramInputStream.readNBytes(arrayOfByte[b1], 0, c1);
        if (i == 0) {
          if (!arrayList.isEmpty())
            this.backgroundIpReputationDatabaseMapper.createIpReputationData(arrayList); 
          break;
        } 
        byte[] arrayOfByte1 = (i == c1) ? arrayOfByte[b1] : Arrays.copyOfRange(arrayOfByte[b1], 0, i);
        arrayList.add(new SequencedData(arrayOfByte1, paramSequencedMetaData.lastModified, Integer.valueOf(b++)));
        if (paramConsumer != null)
          paramConsumer.accept(arrayOfByte1); 
        b1++;
        if (arrayList.size() == c2) {
          this.backgroundIpReputationDatabaseMapper.createIpReputationData(arrayList);
          arrayList.clear();
          b1 = 0;
        } 
      } 
    } catch (Exception exception) {
      logger.error("Error downloading IP reputation data file. Message [{}]", exception.getMessage(), exception);
      this.backgroundIpReputationDatabaseMapper.deleteIpReputationData(paramSequencedMetaData.lastModified);
      throw new RuntimeException(exception);
    } 
  }
  
  public static final class MapAndTime extends Record {
    private final Map<InetAddress, Integer> badIps;
    
    private final ZonedDateTime lastModified;
    
    public MapAndTime(Map<InetAddress, Integer> param1Map, ZonedDateTime param1ZonedDateTime) {
      this.badIps = param1Map;
      this.lastModified = param1ZonedDateTime;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/cache/IpReputationLoader$MapAndTime;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #413	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/cache/IpReputationLoader$MapAndTime;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #413	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/cache/IpReputationLoader$MapAndTime;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #413	-> 0
    }
    
    public Map<InetAddress, Integer> badIps() {
      return this.badIps;
    }
    
    public ZonedDateTime lastModified() {
      return this.lastModified;
    }
  }
}
