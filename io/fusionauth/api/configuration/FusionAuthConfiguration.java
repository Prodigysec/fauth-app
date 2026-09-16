package io.fusionauth.api.configuration;

import com.inversoft.configuration.InversoftConfiguration;
import io.fusionauth.api.domain.InstallationType;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.api.domain.SearchEngineType;
import io.fusionauth.api.util.NetworkTools;
import java.nio.file.Path;
import java.util.UUID;

public interface FusionAuthConfiguration extends InversoftConfiguration {
  boolean adminStrictRefererProtectionEnabled();
  
  boolean allowSubClaimOverride();
  
  int appHTTPLocalPort();
  
  int appHTTPPort();
  
  int appHTTPSPort();
  
  String appURL();
  
  boolean appURLComputed();
  
  default String computeAppURL() {
    String str = NetworkTools.guessSiteIpAddress();
    int i = appHTTPPort();
    return "http://" + str + ":" + i;
  }
  
  int collectionSizeLimit();
  
  String entitySearchIndexName();
  
  String httpsCertificate();
  
  Path httpsCertificateFile();
  
  boolean httpsEnabled();
  
  String httpsPrivateKey();
  
  Path httpsPrivateKeyFile();
  
  boolean ignoreIdentitiesInUserAPIRequests();
  
  String installationSource();
  
  InstallationType installationType();
  
  UUID instanceId();
  
  int internalApplicationReaderBatchSize();
  
  int internalSCIMSearchBatchSize();
  
  int internalUserReaderExpansionBatchSize();
  
  String kickstartFile();
  
  String licenseBaseURL();
  
  boolean localMetricsEnabled();
  
  int loginQueueSize();
  
  String metricsBaseURL();
  
  String reactorBaseURL();
  
  int reindexBatchSize();
  
  int reindexThreadCount();
  
  RuntimeMode runtimeMode();
  
  int searchEngineDefaultMaxHitCount();
  
  String searchEngineDefaultRefreshInterval();
  
  SearchEngineType searchEngineType();
  
  void setAppURL(String paramString);
  
  String setupToken();
  
  String statsBaseURL();
  
  boolean supportLoginEnabled();
  
  String[] trustedProxies();
  
  boolean usageDataManaged();
  
  int usageDataThrottleTime();
  
  String userSearchIndexName();
}
