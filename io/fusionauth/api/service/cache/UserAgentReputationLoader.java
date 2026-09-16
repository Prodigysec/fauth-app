package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.license.v2.LicenseProvider;
import com.inversoft.license.v2.domain.LicenseContainer;
import io.fusionauth.api.domain.SequencedData;
import io.fusionauth.api.domain.SequencedMetaData;
import io.fusionauth.api.domain.UserAgentReputationDatabaseMapper;
import io.fusionauth.api.domain.api.FusionAuthEnvironment;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAgentReputationLoader extends AbstractReactorFileCacheLoader {
  public static final String BAD_USER_AGENTS_FILE = "bad-user-agents.txt";
  
  public static final String USER_AGENT_REPUTATION = "user-agent-reputation-";
  
  public static final ReentrantLock masterLock = new ReentrantLock();
  
  private static final Logger logger = LoggerFactory.getLogger(UserAgentReputationLoader.class);
  
  private final DataSource backgroundDataSource;
  
  private final UserAgentReputationDatabaseMapper backgroundUserAgentReputationDatabaseMapper;
  
  private final UserAgentReputationCache cache;
  
  @Inject
  public UserAgentReputationLoader(UserAgentReputationCache paramUserAgentReputationCache, CacheNotifier paramCacheNotifier, @Named("background") UserAgentReputationDatabaseMapper paramUserAgentReputationDatabaseMapper, @Named("background") DataSource paramDataSource, LicenseProvider paramLicenseProvider, NodeService paramNodeService, ReactorCore paramReactorCore) {
    super(paramCacheNotifier, paramLicenseProvider, paramNodeService, paramReactorCore);
    this.cache = paramUserAgentReputationCache;
    this.backgroundUserAgentReputationDatabaseMapper = paramUserAgentReputationDatabaseMapper;
    this.backgroundDataSource = paramDataSource;
  }
  
  UserAgentReputationLoader(UserAgentReputationCache paramUserAgentReputationCache) {
    super(null, null, null, null);
    this.cache = paramUserAgentReputationCache;
    this.backgroundUserAgentReputationDatabaseMapper = null;
    this.backgroundDataSource = null;
  }
  
  public static boolean isLicensed(LicenseContainer paramLicenseContainer) {
    if (ReactorService.isLicenseContainerInvalid(paramLicenseContainer))
      return false; 
    return DefaultReactorStatusService.isLicensedForUserAgentReputation(paramLicenseContainer.license());
  }
  
  public void removeCache() {
    if (this.cache.get() != null) {
      logger.info("Removing cache");
      this.cache.update(null, null);
    } 
  }
  
  protected String cacheName() {
    return "UserAgentReputation";
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
      logger.debug("Not licensed for User Agent Reputation loader, removing cache and returning");
      removeCache();
      return false;
    } 
    ListAndTime listAndTime = null;
    if (nodeService().isMaster())
      listAndTime = downloadUpdatesFromReactor(); 
    if (listAndTime == null)
      listAndTime = loadFromDatabaseOrFile(); 
    if (listAndTime == null && this.cache.get() == null)
      listAndTime = loadFromBundledFile(); 
    if (listAndTime != null) {
      logger.debug("Calling update on UserAgentReputationCache");
      this.cache.update(listAndTime.badUserAgents, listAndTime.lastModified);
      logger.info("Loaded [{}] bad user agents into the user agent reputation cache.", Integer.valueOf(listAndTime.badUserAgents.size()));
      return true;
    } 
    return false;
  }
  
  List<String> parse(InputStream paramInputStream) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(paramInputStream, StandardCharsets.UTF_8));
    try {
      List<String> list = (List)bufferedReader.lines().filter(StringUtils::isNotBlank).collect(Collectors.toList());
      bufferedReader.close();
      return list;
    } catch (Throwable throwable) {
      try {
        bufferedReader.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  private ListAndTime downloadFromReactor(SequencedMetaData paramSequencedMetaData) {
    try {
      Path path = getLastModifiedFilePath(paramSequencedMetaData.lastModified);
      File file = writeFileVerifyAndUnzip(path, paramSequencedMetaData.digest, paramConsumer -> reactorCore().retrieveUserAgentReputationFile(()), this::getUserAgentReputationPaths, "user agent reputation");
      logger.info("Download complete. Update user agent reputation meta-data.");
      this.backgroundUserAgentReputationDatabaseMapper.createUserAgentReputationMetaData(paramSequencedMetaData);
      List<String> list = tryParseFile(file);
      if (list == null) {
        this.backgroundUserAgentReputationDatabaseMapper.deleteUserAgentReputationData(paramSequencedMetaData.lastModified);
        this.backgroundUserAgentReputationDatabaseMapper.deleteUserAgentReputationMetaData(paramSequencedMetaData.lastModified);
        String str = "Failed to parse user agent reputation data file with last modified timestamp [" + String.valueOf(paramSequencedMetaData.lastModified) + "] at file [" + String.valueOf(file) + "]. The user agent reputation data file has been deleted. We will attempt to download the file from Reactor again at the next update interval.";
        logger.error(str);
        EventLogHelper.create(new EventLog(EventLogType.Error, str));
        return null;
      } 
      return new ListAndTime(list, paramSequencedMetaData.lastModified);
    } catch (Exception exception) {
      logger.error(exception.getMessage(), exception);
      return null;
    } 
  }
  
  private ListAndTime downloadUpdatesFromReactor() {
    ListAndTime listAndTime = null;
    SequencedMetaData sequencedMetaData1 = this.backgroundUserAgentReputationDatabaseMapper.getLatestUserAgentReputationMetaData();
    boolean bool = (sequencedMetaData1 != null) ? true : false;
    SequencedMetaData sequencedMetaData2 = reactorCore().retrieveUserAgentReputationFileLastModified();
    if (sequencedMetaData2 != null && (!bool || sequencedMetaData2.lastModified
      .isAfter(sequencedMetaData1.lastModified))) {
      logger.info("New version of user agent reputation data available from FusionAuth Reactor.");
      listAndTime = downloadFromReactor(sequencedMetaData2);
      if (listAndTime != null && bool) {
        this.backgroundUserAgentReputationDatabaseMapper.deleteUserAgentReputationData(sequencedMetaData1.lastModified);
        this.backgroundUserAgentReputationDatabaseMapper.deleteUserAgentReputationMetaData(sequencedMetaData1.lastModified);
      } 
    } else {
      logger.debug("We are the master node and no new metadata is available");
    } 
    return listAndTime;
  }
  
  private Path getLastModifiedFilePath(ZonedDateTime paramZonedDateTime) {
    Path path = getDataDirectory();
    return path.resolve("user-agent-reputation-" + ISO_FILE_SYSTEM_FRIENDLY_FORMATTER.format(paramZonedDateTime) + ".txt");
  }
  
  private List<Path> getUserAgentReputationPaths() {
    return getDataFiles("user-agent-reputation-", ".txt");
  }
  
  private ListAndTime loadFromBundledFile() {
    Path path = Paths.get(System.getProperty(FusionAuthEnvironment.DATA_DIRECTORY), new String[0]).resolve("bad-user-agents.txt");
    if (!Files.exists(path, new java.nio.file.LinkOption[0])) {
      String str = "User agent reputation file [" + String.valueOf(path) + "] not found. User agent reputation data will not be available.";
      logger.error(str);
      EventLogHelper.create(new EventLog(EventLogType.Error, str));
      return null;
    } 
    try {
      InputStream inputStream = Files.newInputStream(path, new java.nio.file.OpenOption[0]);
      try {
        List<String> list = parse(inputStream);
        ListAndTime listAndTime = new ListAndTime(list, ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));
        if (inputStream != null)
          inputStream.close(); 
        return listAndTime;
      } catch (Throwable throwable) {
        if (inputStream != null)
          try {
            inputStream.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (IOException iOException) {
      String str = "Failed to read user agent reputation file [" + String.valueOf(path) + "].";
      logger.error(str, iOException);
      EventLogHelper.create(new EventLog(EventLogType.Error, str));
      return null;
    } 
  }
  
  private ListAndTime loadFromDatabaseOrFile() {
    ZonedDateTime zonedDateTime;
    Path path;
    SequencedMetaData sequencedMetaData = this.backgroundUserAgentReputationDatabaseMapper.getLatestUserAgentReputationMetaData();
    if (sequencedMetaData != null) {
      zonedDateTime = sequencedMetaData.lastModified;
      logger.debug("User agent reputation database last modified [{}]", zonedDateTime);
      path = getLastModifiedFilePath(zonedDateTime);
    } else {
      zonedDateTime = mostRecentFileLastModified("user-agent-reputation-", ".txt");
      if (zonedDateTime == null) {
        logger.debug("No user agent reputation data found in database or file system.");
        return null;
      } 
      logger.debug("User agent reputation file data last modified [{}]", zonedDateTime);
      path = getDataDirectory().resolve("user-agent-reputation-" + ISO_FILE_SYSTEM_FRIENDLY_FORMATTER.format(zonedDateTime) + ".txt");
    } 
    try {
      ZonedDateTime zonedDateTime1 = this.cache.lastModified();
      if (zonedDateTime1 != null && !zonedDateTime.isAfter(zonedDateTime1))
        return null; 
      if (Files.exists(path, new java.nio.file.LinkOption[0])) {
        List<String> list = tryParseFile(path.toFile());
        if (list != null) {
          logger.info("Use existing user agent reputation data file [{}]", path.getFileName());
          return new ListAndTime(list, zonedDateTime);
        } 
      } 
      if (sequencedMetaData != null) {
        logger.info("Write the user agent reputation data from DB to file [{}]", String.valueOf(path.getFileName()) + ".gz");
        File file = writeFileVerifyAndUnzip(path, sequencedMetaData.digest, paramConsumer -> streamFileFromDB(paramZonedDateTime, paramConsumer), this::getUserAgentReputationPaths, "user agent reputation");
        List<String> list = tryParseFile(file);
        if (list != null)
          return new ListAndTime(list, zonedDateTime); 
      } 
    } catch (Exception exception) {
      logger.error(exception.getMessage(), exception);
    } 
    return null;
  }
  
  private void streamFileFromDB(ZonedDateTime paramZonedDateTime, Consumer<byte[]> paramConsumer) {
    streamFileFromDB(this.backgroundDataSource, "SELECT data FROM user_agent_reputation_data WHERE last_modified = ? ORDER BY seq", paramZonedDateTime, paramConsumer);
  }
  
  private List<String> tryParseFile(File paramFile) {
    if (paramFile == null)
      return null; 
    try {
      FileInputStream fileInputStream = new FileInputStream(paramFile);
      try {
        List<String> list1 = parse(fileInputStream);
        if (list1.isEmpty()) {
          logger.warn("User agent reputation file [{}] parsed to an empty list.", paramFile.getName());
          try {
            Files.deleteIfExists(paramFile.toPath());
          } catch (IOException iOException) {
            logger.debug("Unable to delete invalid user agent reputation file [{}]", paramFile.getName(), iOException);
          } 
          List list = null;
          fileInputStream.close();
          return list;
        } 
        List<String> list2 = list1;
        fileInputStream.close();
        return list2;
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
      } catch (IOException iOException1) {
        logger.debug("Unable to delete invalid user agent reputation file [{}]", paramFile.getName(), iOException1);
      } 
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
            this.backgroundUserAgentReputationDatabaseMapper.createUserAgentReputationData(arrayList); 
          break;
        } 
        byte[] arrayOfByte1 = (i == c1) ? arrayOfByte[b1] : Arrays.copyOfRange(arrayOfByte[b1], 0, i);
        arrayList.add(new SequencedData(arrayOfByte1, paramSequencedMetaData.lastModified, Integer.valueOf(b++)));
        if (paramConsumer != null)
          paramConsumer.accept(arrayOfByte1); 
        b1++;
        if (arrayList.size() == c2) {
          this.backgroundUserAgentReputationDatabaseMapper.createUserAgentReputationData(arrayList);
          arrayList.clear();
          b1 = 0;
        } 
      } 
    } catch (Exception exception) {
      logger.error("Error downloading user agent reputation data file. Message [{}]", exception.getMessage(), exception);
      this.backgroundUserAgentReputationDatabaseMapper.deleteUserAgentReputationData(paramSequencedMetaData.lastModified);
      throw new RuntimeException(exception);
    } 
  }
  
  public static final class ListAndTime extends Record {
    private final List<String> badUserAgents;
    
    private final ZonedDateTime lastModified;
    
    public ListAndTime(List<String> param1List, ZonedDateTime param1ZonedDateTime) {
      this.badUserAgents = param1List;
      this.lastModified = param1ZonedDateTime;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/cache/UserAgentReputationLoader$ListAndTime;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #413	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/cache/UserAgentReputationLoader$ListAndTime;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #413	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/cache/UserAgentReputationLoader$ListAndTime;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #413	-> 0
    }
    
    public List<String> badUserAgents() {
      return this.badUserAgents;
    }
    
    public ZonedDateTime lastModified() {
      return this.lastModified;
    }
  }
}
