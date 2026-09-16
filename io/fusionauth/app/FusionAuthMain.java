package io.fusionauth.app;

import com.inversoft.maintenance.BaseMaintenanceModePrimeMain;
import com.inversoft.maintenance.MaintenanceModeModules;
import com.inversoft.util.StringTools;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.app.maintenance.FusionAuthMaintenanceModeModules;
import io.fusionauth.http.io.MultipartConfiguration;
import io.fusionauth.http.server.HTTPListenerConfiguration;
import io.fusionauth.http.server.HTTPServerConfiguration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FusionAuthMain extends BaseMaintenanceModePrimeMain {
  private static final Logger logger = LoggerFactory.getLogger(FusionAuthMain.class);
  
  public static void main(String[] paramArrayOfString) {
    String str = System.getProperty("fusionauth.home.directory");
    if (str == null) {
      logger.error("\n\n------------------------------------------------------------------------------------------------\n                                       !!!FATAL ERROR!!!\n\nFusionAuth is missing the [fusionauth.home.directory] property. This is normally passed in by\nthe start.sh or start.ps1 script. This must have been removed, which means that FusionAuth\ncannot be started.\n------------------------------------------------------------------------------------------------\n");
      System.exit(1);
    } 
    FIPSActivator.optionallyEnable(logger);
    BaseMaintenanceModePrimeMain.Instance = new FusionAuthMain();
    BaseMaintenanceModePrimeMain.Instance.registerShutdownHook();
    BaseMaintenanceModePrimeMain.Instance.start();
  }
  
  public HTTPServerConfiguration[] configuration() {
    FusionAuthConfiguration fusionAuthConfiguration = (FusionAuthConfiguration)this.configuration;
    HTTPServerConfiguration hTTPServerConfiguration1 = baseHTTPServerConfiguration().withListener(new HTTPListenerConfiguration(fusionAuthConfiguration.appHTTPPort()));
    HTTPServerConfiguration hTTPServerConfiguration2 = getHttpsConfiguration();
    HTTPServerConfiguration hTTPServerConfiguration3 = (new HTTPServerConfiguration()).withListener(new HTTPListenerConfiguration(fusionAuthConfiguration.appHTTPLocalPort())).withProcessingTimeoutDuration(hTTPServerConfiguration1.getProcessingTimeoutDuration()).withInitialReadTimeout(Duration.ofSeconds(5L)).withKeepAliveTimeoutDuration(Duration.ofSeconds(60L)).withMaxRequestHeaderSize(hTTPServerConfiguration1.getMaxRequestHeaderSize() + 32768).withMaxRequestBodySize(Map.of("*", Integer.valueOf(((Integer)hTTPServerConfiguration1.getMaxRequestBodySize().get("*")).intValue() + 16777216)));
    if (hTTPServerConfiguration2 != null)
      return new HTTPServerConfiguration[] { hTTPServerConfiguration1, hTTPServerConfiguration3, hTTPServerConfiguration2 }; 
    return new HTTPServerConfiguration[] { hTTPServerConfiguration1, hTTPServerConfiguration3 };
  }
  
  protected MaintenanceModeModules getMaintenanceModeModules() {
    return new FusionAuthMaintenanceModeModules();
  }
  
  private HTTPServerConfiguration baseHTTPServerConfiguration() {
    HTTPServerConfiguration hTTPServerConfiguration = (new HTTPServerConfiguration()).withMinimumReadThroughput(-1L).withMinimumWriteThroughput(-1L).withProcessingTimeoutDuration(Duration.ofMinutes(5L)).withInitialReadTimeout(Duration.ofMillis(this.configuration.httpReadTimeout())).withKeepAliveTimeoutDuration(Duration.ofMillis(this.configuration.httpReadTimeout())).withMaxRequestHeaderSize(this.configuration.httpMaxHeaderSize());
    Map map = this.configuration.httpMaxRequestSize();
    if (!map.isEmpty())
      hTTPServerConfiguration.withMaxRequestBodySize(map); 
    Integer integer = (Integer)hTTPServerConfiguration.getMaxRequestBodySize().get("application/x-www-form-urlencoded");
    if (integer != null) {
      int j = (int)(integer.intValue() * 1.15F);
      hTTPServerConfiguration.withMultipartConfiguration((new MultipartConfiguration()).withMaxRequestSize(j));
    } 
    int i = this.configuration.httpKeepAliveTimeout();
    if (i != -1)
      hTTPServerConfiguration.withKeepAliveTimeoutDuration(Duration.ofMillis(i)); 
    return hTTPServerConfiguration;
  }
  
  private HTTPServerConfiguration getHttpsConfiguration() {
    FusionAuthConfiguration fusionAuthConfiguration = (FusionAuthConfiguration)this.configuration;
    if (fusionAuthConfiguration.httpsEnabled()) {
      int i = fusionAuthConfiguration.appHTTPSPort();
      String str1 = fusionAuthConfiguration.httpsCertificate();
      String str2 = fusionAuthConfiguration.httpsPrivateKey();
      Path path1 = fusionAuthConfiguration.httpsCertificateFile();
      Path path2 = fusionAuthConfiguration.httpsPrivateKeyFile();
      if (StringTools.isNotBlank(str1) && StringTools.isNotBlank(str2))
        try {
          return baseHTTPServerConfiguration().withListener(new HTTPListenerConfiguration(i, str1, str2));
        } catch (Exception exception) {
          throw new RuntimeException(exception);
        }  
      if (path1 != null && path2 != null)
        try {
          String str3 = Files.readString(path1);
          String str4 = Files.readString(path2);
          return baseHTTPServerConfiguration().withListener(new HTTPListenerConfiguration(i, str3, str4));
        } catch (Exception exception) {
          throw new RuntimeException(exception);
        }  
      throw new RuntimeException("HTTPS is enabled, but no certificate or key provided");
    } 
    return null;
  }
}
