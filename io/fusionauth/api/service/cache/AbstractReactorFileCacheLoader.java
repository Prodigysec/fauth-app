package io.fusionauth.api.service.cache;

import com.inversoft.cache.CacheLoader;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.jdbc.Select;
import com.inversoft.license.v2.LicenseProvider;
import io.fusionauth.api.domain.api.FusionAuthEnvironment;
import io.fusionauth.api.service.reactor.ReactorCore;
import io.fusionauth.api.service.system.NodeService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import javax.sql.DataSource;
import org.slf4j.Logger;

public abstract class AbstractReactorFileCacheLoader implements CacheLoader, Runnable {
  protected static final DateTimeFormatter ISO_FILE_SYSTEM_FRIENDLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmssZZZZZ");
  
  private final CacheNotifier cacheNotifier;
  
  private final LicenseProvider licenseProvider;
  
  private final NodeService nodeService;
  
  private final ReactorCore reactorCore;
  
  protected AbstractReactorFileCacheLoader(CacheNotifier paramCacheNotifier, LicenseProvider paramLicenseProvider, NodeService paramNodeService, ReactorCore paramReactorCore) {
    this.cacheNotifier = paramCacheNotifier;
    this.licenseProvider = paramLicenseProvider;
    this.nodeService = paramNodeService;
    this.reactorCore = paramReactorCore;
  }
  
  public final void load() {
    runLocked();
  }
  
  public final void run() {
    boolean bool = runLocked();
    if (bool && this.nodeService != null && this.nodeService.isMaster() && this.cacheNotifier != null)
      this.cacheNotifier.reload(cacheName()); 
  }
  
  protected Path getDataDirectory() {
    Path path = Paths.get(System.getProperty(FusionAuthEnvironment.DATA_DIRECTORY), new String[0]);
    if (!Files.isDirectory(path, new java.nio.file.LinkOption[0]))
      try {
        Files.createDirectories(path, (FileAttribute<?>[])new FileAttribute[0]);
      } catch (IOException iOException) {
        throw new RuntimeException(iOException);
      }  
    return path;
  }
  
  protected List<Path> getDataFiles(String paramString1, String paramString2) {
    try {
      Stream<Path> stream = Files.list(getDataDirectory());
      try {
        List<Path> list = stream.filter(paramPath -> paramPath.getFileName().toString().startsWith(paramString)).filter(paramPath -> paramPath.getFileName().toString().endsWith(paramString)).sorted().toList();
        if (stream != null)
          stream.close(); 
        return list;
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
      logger().error(iOException.getMessage(), iOException);
      return Collections.emptyList();
    } 
  }
  
  protected MessageDigest getDigest(String paramString) {
    try {
      return MessageDigest.getInstance(paramString);
    } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
      throw new RuntimeException(noSuchAlgorithmException);
    } 
  }
  
  protected LicenseProvider licenseProvider() {
    return this.licenseProvider;
  }
  
  protected ZonedDateTime mostRecentFileLastModified(String paramString1, String paramString2) {
    List<Path> list = getDataFiles(paramString1, paramString2);
    if (!list.isEmpty()) {
      String str = ((Path)list.getLast()).getFileName().toString().replace(paramString1, "").replace(paramString2, "");
      return ZonedDateTime.from(ISO_FILE_SYSTEM_FRIENDLY_FORMATTER.parse(str));
    } 
    return null;
  }
  
  protected NodeService nodeService() {
    return this.nodeService;
  }
  
  protected ReactorCore reactorCore() {
    return this.reactorCore;
  }
  
  protected void streamFileFromDB(DataSource paramDataSource, String paramString, ZonedDateTime paramZonedDateTime, Consumer<byte[]> paramConsumer) {
    try {
      Connection connection = paramDataSource.getConnection();
      try {
        connection.setAutoCommit(false);
        ((Select)(new Select(connection))
          .in(paramString)
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
  
  protected File writeFileVerifyAndUnzip(Path paramPath, String paramString1, Consumer<Consumer<byte[]>> paramConsumer, Supplier<List<Path>> paramSupplier, String paramString2) throws IOException {
    Path path = Paths.get(paramPath.toString() + ".gz", new String[0]);
    File file = path.toFile();
    Files.deleteIfExists(paramPath);
    Files.deleteIfExists(path);
    Files.createFile(path, (FileAttribute<?>[])new FileAttribute[0]);
    String[] arrayOfString = paramString1.split("=", 2);
    String str1 = arrayOfString[1];
    String str2 = arrayOfString[0];
    MessageDigest messageDigest = getDigest(str2);
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      try {
        paramConsumer.accept(paramArrayOfbyte -> byteHandler(paramArrayOfbyte, paramMessageDigest, paramFileOutputStream));
        String str = HexFormat.of().formatHex(messageDigest.digest());
        if (!str1.equals(str)) {
          logger().error("File [{}] did not match [{}] checksum. Expected [{}] but found [{}]. Delete the file [{}]", new Object[] { file.getName(), str2, str1, str, file
                .getName() });
          File file1 = null;
          fileOutputStream.close();
          return file1;
        } 
        logger().info("Decompress [{}] file [{}] to [{}]", new Object[] { paramString2, file.getName(), paramPath.toFile().getName() });
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
        Files.deleteIfExists(path);
      } catch (IOException iOException) {
        logger().debug("Unable to delete temporary gz file [{}]", path.getFileName(), iOException);
      } 
    } 
    List<Path> list = paramSupplier.get();
    byte b = 2;
    if (list.size() > b)
      for (byte b1 = 0; b1 < list.size() - b; b1++) {
        Path path1 = list.get(b1);
        logger().info("Delete old [{}] file [{}]", paramString2, path1.getFileName());
        Files.delete(path1);
      }  
    return paramPath.toFile();
  }
  
  private void byteHandler(byte[] paramArrayOfbyte, MessageDigest paramMessageDigest, OutputStream paramOutputStream) {
    try {
      paramOutputStream.write(paramArrayOfbyte);
      paramMessageDigest.update(paramArrayOfbyte);
    } catch (IOException iOException) {
      throw new RuntimeException(iOException);
    } 
  }
  
  private boolean runLocked() {
    boolean bool = (this.nodeService != null && this.nodeService.isMaster()) ? true : false;
    boolean bool1 = false;
    try {
      if (bool) {
        bool1 = masterLock().tryLock();
        if (!bool1) {
          logger().debug("[{}] Already locked. Retry later. [{}]", Long.valueOf(Thread.currentThread().threadId()), Integer.valueOf(masterLock().getHoldCount()));
          return false;
        } 
        if (masterLock().getHoldCount() != 1)
          logger().debug("[{}] Locked [{}]", Long.valueOf(Thread.currentThread().threadId()), Integer.valueOf(masterLock().getHoldCount())); 
      } 
      return runInternal();
    } finally {
      if (bool1) {
        masterLock().unlock();
        if (masterLock().getHoldCount() != 0)
          logger().debug("[{}] Unlock [{}]", Long.valueOf(Thread.currentThread().threadId()), Integer.valueOf(masterLock().getHoldCount())); 
      } 
    } 
  }
  
  protected abstract String cacheName();
  
  protected abstract Logger logger();
  
  protected abstract ReentrantLock masterLock();
  
  protected abstract boolean runInternal();
}
