package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.cache.CacheLoader;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.jdbc.Select;
import com.inversoft.license.v2.LicenseProvider;
import com.inversoft.license.v2.domain.LicenseContainer;
import com.maxmind.db.CHMCache;
import com.maxmind.db.NodeCache;
import com.maxmind.db.Reader;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.IPLocationDatabaseMapper;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.api.domain.api.FusionAuthEnvironment;
import io.fusionauth.api.domain.ip.ipinfo.Response;
import io.fusionauth.api.domain.ip.maxmind.IPLocation;
import io.fusionauth.api.domain.ip.maxmind.IPLocationMetaData;
import io.fusionauth.api.service.reactor.DefaultReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorCore;
import io.fusionauth.api.service.reactor.ReactorService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.NodeService;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaxMindDatabaseLoader implements CacheLoader, Runnable {
  public static final ReentrantLock MasterLock = new ReentrantLock();
  
  private static final DateTimeFormatter ISO_FileSystem_Friendly = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmssZZZZZ");
  
  private static final Logger logger = LoggerFactory.getLogger(MaxMindDatabaseLoader.class);
  
  private final DataSource backgroundDataSource;
  
  private final IPLocationDatabaseMapper backgroundIPLocationDatabaseMapper;
  
  private final CacheNotifier cacheNotifier;
  
  private final FusionAuthConfiguration configuration;
  
  private final LicenseProvider licenseProvider;
  
  private final MaxMindDatabaseCache maxMindDatabaseCache;
  
  private final NodeService nodeService;
  
  private final ReactorCore reactorCore;
  
  @Inject
  public MaxMindDatabaseLoader(CacheNotifier paramCacheNotifier, FusionAuthConfiguration paramFusionAuthConfiguration, @Named("background") IPLocationDatabaseMapper paramIPLocationDatabaseMapper, @Named("background") DataSource paramDataSource, LicenseProvider paramLicenseProvider, MaxMindDatabaseCache paramMaxMindDatabaseCache, NodeService paramNodeService, ReactorCore paramReactorCore) {
    this.cacheNotifier = paramCacheNotifier;
    this.configuration = paramFusionAuthConfiguration;
    this.backgroundIPLocationDatabaseMapper = paramIPLocationDatabaseMapper;
    this.backgroundDataSource = paramDataSource;
    this.licenseProvider = paramLicenseProvider;
    this.maxMindDatabaseCache = paramMaxMindDatabaseCache;
    this.nodeService = paramNodeService;
    this.reactorCore = paramReactorCore;
  }
  
  public static boolean isLicensed(LicenseContainer paramLicenseContainer) {
    if (ReactorService.isLicenseContainerInvalid(paramLicenseContainer))
      return false; 
    return DefaultReactorStatusService.isLicensedForIPGeoLocation(paramLicenseContainer.license());
  }
  
  private static List<Path> getMaxmindPaths(Stream<Path> paramStream) {
    return paramStream.filter(paramPath -> paramPath.getFileName().toString().startsWith("ip-location-"))
      .filter(paramPath -> paramPath.getFileName().toString().endsWith("mmdb"))
      .sorted()
      .toList();
  }
  
  public boolean _run() {
    LicenseContainer licenseContainer = this.licenseProvider.getLicense();
    boolean bool1 = isLicensed(licenseContainer);
    if (!bool1) {
      logger.debug("Not licensed for Maxmind loader, removing cache and returning");
      removeCache();
      return false;
    } 
    IPLocationMetaData iPLocationMetaData = this.backgroundIPLocationDatabaseMapper.getLatestIPLocationMetaData();
    boolean bool = (iPLocationMetaData != null) ? true : false;
    ZonedDateTime zonedDateTime = bool ? iPLocationMetaData.lastModified : null;
    boolean bool2 = checkDatabaseFile();
    ReaderAndTime readerAndTime = null;
    if (!bool && !bool2) {
      if (this.nodeService.isMaster()) {
        logger.info("IP location database not present. Attempting to download database from FusionAuth Reactor.");
        if (this.configuration.runtimeMode() == RuntimeMode.FusionAuth_Development || this.configuration.runtimeMode() == RuntimeMode.Testing) {
          LocalPath localPath = latestVersionOnDisk();
          if (localPath != null) {
            IPLocationMetaData iPLocationMetaData1 = new IPLocationMetaData(null, localPath.lastModified);
            readerAndTime = developmentFastPath(iPLocationMetaData1, localPath.path);
          } 
        } 
        if (readerAndTime == null) {
          IPLocationMetaData iPLocationMetaData1 = this.reactorCore.retrieveIPLocationDatabaseLastModified();
          if (iPLocationMetaData1 != null) {
            readerAndTime = downloadFromReactor(iPLocationMetaData1);
          } else {
            logger.info("IP location data from FusionAuth Reactor not currently available.");
          } 
        } 
      } else {
        logger.info("IP location data not ready. Waiting for cache reload or recheck.");
      } 
    } else if (!bool) {
      logger.info("The IP location data has been removed from the database. Clear the local IP location cache.");
      this.maxMindDatabaseCache.update((Reader)null, (ZonedDateTime)null);
    } else if (!bool2) {
      readerAndTime = loadFromDatabase();
    } else if (this.nodeService.isMaster()) {
      IPLocationMetaData iPLocationMetaData1 = this.reactorCore.retrieveIPLocationDatabaseLastModified();
      if (iPLocationMetaData1 != null && iPLocationMetaData1.lastModified.isAfter(zonedDateTime)) {
        logger.info("New version of IP location data available.");
        readerAndTime = downloadFromReactor(iPLocationMetaData1);
        if (readerAndTime != null) {
          this.backgroundIPLocationDatabaseMapper.deleteIPLocationData(zonedDateTime);
          this.backgroundIPLocationDatabaseMapper.deleteIPLocationMetaData(zonedDateTime);
        } 
      } else {
        logger.debug("We are the master node and no new metadata is available");
      } 
    } else {
      logger.debug("We have the DB and cache loaded but are not the master node");
    } 
    if (readerAndTime != null) {
      logger.debug("Calling update on maxMindDatabaseCache");
      this.maxMindDatabaseCache.update(readerAndTime.dbReader, readerAndTime.lastModified);
      return true;
    } 
    return false;
  }
  
  public boolean _runLocked() {
    boolean bool1 = this.nodeService.isMaster();
    boolean bool2 = false;
    try {
      if (bool1) {
        bool2 = MasterLock.tryLock();
        if (!bool2) {
          logger.debug("[{}] Already locked. Retry later. [{}]", Long.valueOf(Thread.currentThread().threadId()), Integer.valueOf(MasterLock.getHoldCount()));
          return false;
        } 
        if (MasterLock.getHoldCount() != 1)
          logger.debug("[{}] Locked [{}]", Long.valueOf(Thread.currentThread().threadId()), Integer.valueOf(MasterLock.getHoldCount())); 
      } 
      return _run();
    } finally {
      if (bool2) {
        MasterLock.unlock();
        if (MasterLock.getHoldCount() != 0)
          logger.debug("[{}] Unlock [{}]", Long.valueOf(Thread.currentThread().threadId()), Integer.valueOf(MasterLock.getHoldCount())); 
      } 
    } 
  }
  
  public void load() {
    _runLocked();
  }
  
  public void removeCache() {
    if (this.maxMindDatabaseCache.get() != null) {
      logger.info("Removing cache");
      this.maxMindDatabaseCache.update((Reader)null, (ZonedDateTime)null);
    } 
  }
  
  public void run() {
    boolean bool = _runLocked();
    if (bool && this.nodeService.isMaster())
      this.cacheNotifier.reload("MaxMindDatabase"); 
  }
  
  private void byteHandler(byte[] paramArrayOfbyte, MessageDigest paramMessageDigest, OutputStream paramOutputStream) {
    try {
      paramOutputStream.write(paramArrayOfbyte);
      paramMessageDigest.update(paramArrayOfbyte);
    } catch (IOException iOException) {
      throw new RuntimeException(iOException);
    } 
  }
  
  private boolean canRead(Reader paramReader) {
    try {
      paramReader.get(InetAddress.getByName("8.8.8.8"), Response.class);
      return true;
    } catch (IOException iOException) {
      logger.debug(iOException.getMessage(), iOException);
      return false;
    } 
  }
  
  private boolean checkDatabaseFile() {
    Reader reader = (Reader)this.maxMindDatabaseCache.get();
    return (reader != null && canRead(reader));
  }
  
  private ReaderAndTime developmentFastPath(IPLocationMetaData paramIPLocationMetaData, Path paramPath) {
    String str = paramPath.getName(paramPath.getNameCount() - 1).toString();
    Path path = paramPath.getParent().resolve(str + ".gz");
    if (!Files.exists(path, new java.nio.file.LinkOption[0])) {
      logger.info("[{}] The gzipped file does not exist. Unable to use development fast path.", this.configuration.runtimeMode());
      return null;
    } 
    logger.info("[{}] Save some bytes, skip the download and use existing download file [{}].", this.configuration.runtimeMode(), paramPath.getName(paramPath.getNameCount() - 1));
    logger.info("[{}] Write [{}] to the database, this will take 5-10 seconds. Stand by...", this.configuration.runtimeMode(), paramPath.getName(paramPath.getNameCount() - 1));
    MessageDigest messageDigest = getDigest("md5");
    try {
      FileInputStream fileInputStream = new FileInputStream(path.toFile());
      try {
        Objects.requireNonNull(messageDigest);
        writeToDatabase(fileInputStream, paramIPLocationMetaData, messageDigest::update);
        logger.info("[{}] Write to database complete. Update IP location meta-data.", this.configuration.runtimeMode());
        paramIPLocationMetaData.digest = "md5=" + HexFormat.of().formatHex(messageDigest.digest());
        this.backgroundIPLocationDatabaseMapper.createIPLocationMetaData(paramIPLocationMetaData);
        ReaderAndTime readerAndTime = new ReaderAndTime(getReaderForFile(paramPath.toFile()), paramIPLocationMetaData.lastModified);
        fileInputStream.close();
        return readerAndTime;
      } catch (Throwable throwable) {
        try {
          fileInputStream.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (IOException iOException) {
      logger.error("[{}] Unable to stream the compressed file to the database. That kinda sucks.", this.configuration.runtimeMode());
      return null;
    } 
  }
  
  private ReaderAndTime downloadFromReactor(IPLocationMetaData paramIPLocationMetaData) {
    try {
      Path path = getLastModifiedFilePath(paramIPLocationMetaData.lastModified);
      File file = writeFileVerifyAndUnzip(path, paramIPLocationMetaData.digest, paramConsumer -> this.reactorCore.retrieveIPLocationDatabase(()));
      logger.info("Download complete. Update IP location meta-data.");
      this.backgroundIPLocationDatabaseMapper.createIPLocationMetaData(paramIPLocationMetaData);
      return tryReader(file, path, paramIPLocationMetaData.lastModified);
    } catch (Exception exception) {
      logger.error(exception.getMessage(), exception);
      return null;
    } 
  }
  
  private Path getDataDirectory() {
    Path path = Paths.get(System.getProperty(FusionAuthEnvironment.DATA_DIRECTORY), new String[0]);
    if (!Files.isDirectory(path, new java.nio.file.LinkOption[0]))
      try {
        Files.createDirectories(path, (FileAttribute<?>[])new FileAttribute[0]);
      } catch (IOException iOException) {
        throw new RuntimeException(iOException);
      }  
    return path;
  }
  
  private MessageDigest getDigest(String paramString) {
    try {
      return MessageDigest.getInstance(paramString);
    } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
      throw new RuntimeException(noSuchAlgorithmException);
    } 
  }
  
  private Path getLastModifiedFilePath(ZonedDateTime paramZonedDateTime) {
    Path path = getDataDirectory();
    return path.resolve("ip-location-" + ISO_FileSystem_Friendly.format(paramZonedDateTime) + ".mmdb");
  }
  
  private Reader getReaderForFile(File paramFile) {
    if (paramFile == null)
      return null; 
    try {
      Reader reader = new Reader(paramFile, (NodeCache)new CHMCache());
      if (canRead(reader))
        return reader; 
    } catch (IOException iOException) {
      logger.error(iOException.getMessage(), iOException);
    } 
    logger.warn("File [{}] was not readable. Attempting to delete.", paramFile.getName());
    try {
      Files.deleteIfExists(paramFile.toPath());
    } catch (IOException iOException) {}
    return null;
  }
  
  private LocalPath latestVersionOnDisk() {
    try {
      Stream<Path> stream = Files.list(getDataDirectory());
      try {
        List<Path> list = getMaxmindPaths(stream);
        if (list.isEmpty()) {
          LocalPath localPath1 = null;
          if (stream != null)
            stream.close(); 
          return localPath1;
        } 
        Path path1 = list.get(list.size() - 1);
        String str1 = path1.getName(path1.getNameCount() - 1).toString();
        int i = "ip-location-".length();
        String str2 = str1.substring(i, str1.length() - 5);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(str2, ISO_FileSystem_Friendly);
        Path path2 = getLastModifiedFilePath(zonedDateTime);
        LocalPath localPath = new LocalPath(zonedDateTime, path2);
        if (stream != null)
          stream.close(); 
        return localPath;
      } catch (Throwable throwable) {
        if (stream != null)
          try {
            stream.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (IOException iOException) {
      return null;
    } 
  }
  
  private ReaderAndTime loadFromDatabase() {
    IPLocationMetaData iPLocationMetaData = this.backgroundIPLocationDatabaseMapper.getLatestIPLocationMetaData();
    ZonedDateTime zonedDateTime = iPLocationMetaData.lastModified;
    Path path = getLastModifiedFilePath(zonedDateTime);
    logger.info("IP location data last modified [{}]", zonedDateTime);
    try {
      if (Files.exists(path, new java.nio.file.LinkOption[0])) {
        Reader reader = getReaderForFile(path.toFile());
        if (reader != null) {
          logger.info("Use existing IP location data file [{}]", path.getFileName());
          return new ReaderAndTime(reader, zonedDateTime);
        } 
      } 
      logger.info("Write the IP location data to file [{}]", String.valueOf(path.getFileName()) + ".gz");
      File file = writeFileVerifyAndUnzip(path, iPLocationMetaData.digest, paramConsumer -> streamFileFromDB(paramZonedDateTime, paramConsumer));
      return tryReader(file, path, zonedDateTime);
    } catch (Exception exception) {
      logger.error(exception.getMessage(), exception);
      return null;
    } 
  }
  
  private void streamFileFromDB(ZonedDateTime paramZonedDateTime, Consumer<byte[]> paramConsumer) {
    try {
      Connection connection = this.backgroundDataSource.getConnection();
      try {
        connection.setAutoCommit(false);
        ((Select)(new Select(connection))
          .in("SELECT data FROM ip_location_database WHERE last_modified = ? ORDER BY seq")
          .with(new Object[] { Long.valueOf(paramZonedDateTime.toInstant().toEpochMilli()) })).stream()
          .go(paramResultSet -> paramConsumer.accept(paramResultSet.getBytes(1)));
        connection.commit();
        if (connection != null)
          connection.close(); 
      } catch (Throwable throwable) {
        if (connection != null)
          try {
            connection.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (SQLException sQLException) {
      throw new RuntimeException(sQLException);
    } 
  }
  
  private ReaderAndTime tryReader(File paramFile, Path paramPath, ZonedDateTime paramZonedDateTime) {
    Reader reader = getReaderForFile(paramFile);
    if (reader == null) {
      if (this.nodeService.isMaster()) {
        this.backgroundIPLocationDatabaseMapper.deleteIPLocationData(paramZonedDateTime);
        this.backgroundIPLocationDatabaseMapper.deleteIPLocationMetaData(paramZonedDateTime);
        String str = "Failed to write IP location database file with last modified timestamp [" + String.valueOf(paramZonedDateTime) + "] to file [" + String.valueOf(paramPath.getFileName()) + "]. The IP location database file has been deleted. We will attempt to download the file from Reactor again at the next update interval.";
        logger.error(str);
        EventLogHelper.create(new EventLog(EventLogType.Error, str));
      } 
      return null;
    } 
    return new ReaderAndTime(reader, paramZonedDateTime);
  }
  
  private File writeFileVerifyAndUnzip(Path paramPath, String paramString, Consumer<Consumer<byte[]>> paramConsumer) throws IOException {
    Path path = Paths.get(paramPath.toString() + ".gz", new String[0]);
    File file = path.toFile();
    Files.deleteIfExists(paramPath);
    Files.deleteIfExists(path);
    Files.createFile(path, (FileAttribute<?>[])new FileAttribute[0]);
    String[] arrayOfString = paramString.split("=", 2);
    String str1 = arrayOfString[1];
    String str2 = arrayOfString[0];
    MessageDigest messageDigest = getDigest(str2);
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      try {
        paramConsumer.accept(paramArrayOfbyte -> byteHandler(paramArrayOfbyte, paramMessageDigest, paramFileOutputStream));
        String str = HexFormat.of().formatHex(messageDigest.digest());
        if (!str1.equals(str)) {
          logger.error("File [{}] did not match [{}] checksum. Expected [{}] but found [{}]. Delete the file [{}]", new Object[] { file.getName(), str2, str1, str, file.getName() });
          File file1 = null;
          fileOutputStream.close();
          return file1;
        } 
        logger.info("Decompress IP location file [{}] to [{}]", file.getName(), paramPath.toFile().getName());
        GZIPInputStream gZIPInputStream = new GZIPInputStream(new FileInputStream(file));
        try {
          Files.copy(gZIPInputStream, paramPath, new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
          gZIPInputStream.close();
        } catch (Throwable throwable) {
          try {
            gZIPInputStream.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
        fileOutputStream.close();
      } catch (Throwable throwable) {
        try {
          fileOutputStream.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } finally {
      try {
        if (this.configuration.runtimeMode() != RuntimeMode.FusionAuth_Development && this.configuration.runtimeMode() != RuntimeMode.Testing)
          Files.deleteIfExists(path); 
      } catch (IOException iOException) {}
    } 
    Stream<Path> stream = Files.list(getDataDirectory());
    try {
      List<Path> list = stream.filter(paramPath -> paramPath.getFileName().toString().startsWith("ip-location")).filter(paramPath -> paramPath.getFileName().toString().endsWith("mmdb")).sorted().toList();
      byte b = 2;
      if (list.size() > b)
        for (byte b1 = 0; b1 < list.size() - b; b1++) {
          Path path1 = list.get(b1);
          logger.info("Delete old IP location file [" + String.valueOf(path1.getName(path1.getNameCount() - 1)) + "]");
          Files.delete(path1);
        }  
      if (stream != null)
        stream.close(); 
    } catch (Throwable throwable) {
      if (stream != null)
        try {
          stream.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
    return paramPath.toFile();
  }
  
  private void writeToDatabase(InputStream paramInputStream, IPLocationMetaData paramIPLocationMetaData, Consumer<byte[]> paramConsumer) {
    byte b = 1;
    try {
      char c1 = 'ὀ';
      char c2 = 'Ϩ';
      ArrayList<IPLocation> arrayList = new ArrayList(c2);
      byte[][] arrayOfByte = new byte[c2][c1];
      byte b1 = 0;
      while (true) {
        int i = paramInputStream.readNBytes(arrayOfByte[b1], 0, c1);
        if (i == 0) {
          if (arrayList.size() > 0)
            this.backgroundIPLocationDatabaseMapper.createIPLocationData(arrayList); 
          break;
        } 
        byte[] arrayOfByte1 = (i == c1) ? arrayOfByte[b1] : Arrays.copyOfRange(arrayOfByte[b1], 0, i);
        arrayList.add(new IPLocation(arrayOfByte1, paramIPLocationMetaData.lastModified, Integer.valueOf(b++)));
        if (paramConsumer != null)
          paramConsumer.accept(arrayOfByte1); 
        b1++;
        if (arrayList.size() == c2) {
          this.backgroundIPLocationDatabaseMapper.createIPLocationData(arrayList);
          arrayList.clear();
          b1 = 0;
        } 
      } 
    } catch (Exception exception) {
      logger.error("Error downloading file " + exception.getMessage(), exception);
      this.backgroundIPLocationDatabaseMapper.deleteIPLocationData(paramIPLocationMetaData.lastModified);
      throw new RuntimeException(exception);
    } 
  }
  
  public static final class ReaderAndTime extends Record {
    private final Reader dbReader;
    
    private final ZonedDateTime lastModified;
    
    public ReaderAndTime(Reader param1Reader, ZonedDateTime param1ZonedDateTime) {
      this.dbReader = param1Reader;
      this.lastModified = param1ZonedDateTime;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/cache/MaxMindDatabaseLoader$ReaderAndTime;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #663	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/cache/MaxMindDatabaseLoader$ReaderAndTime;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #663	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/cache/MaxMindDatabaseLoader$ReaderAndTime;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #663	-> 0
    }
    
    public Reader dbReader() {
      return this.dbReader;
    }
    
    public ZonedDateTime lastModified() {
      return this.lastModified;
    }
  }
  
  private static class LocalPath {
    public ZonedDateTime lastModified;
    
    public Path path;
    
    public LocalPath(ZonedDateTime param1ZonedDateTime, Path param1Path) {
      this.lastModified = param1ZonedDateTime;
      this.path = param1Path;
    }
  }
}
