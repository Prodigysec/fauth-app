package io.fusionauth.api.configuration;

import com.inversoft.configuration.BaseMockInversoftConfiguration;
import io.fusionauth.api.domain.InstallationType;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.api.domain.SearchEngineType;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public class MockFusionAuthConfiguration extends BaseMockInversoftConfiguration implements FusionAuthConfiguration {
  private final boolean fipsEnabled;
  
  public boolean adminStrictRefererProtectionEnabled = true;
  
  public boolean allowSubClaimOverride = false;
  
  public int appHttpLocalPort = 9012;
  
  public int appHttpPort = 9011;
  
  public int appHttpsPort = 9013;
  
  public String appURL = "http://localhost:9011";
  
  public int collectionSizeLimit = 10000;
  
  public String entitySearchIndexName = "entity";
  
  public int httpKeepAliveTimeout = -1;
  
  public String httpsCertificate;
  
  public Path httpsCertificateFile;
  
  public boolean httpsEnabled;
  
  public String httpsPrivateKey;
  
  public Path httpsPrivateKeyFile;
  
  public boolean ignoreIdentitiesInUserAPIRequests = true;
  
  public String installationSource = "fusionauth-test";
  
  public InstallationType installationType;
  
  public UUID instanceId = UUID.randomUUID();
  
  public int internalApplicationReaderBatchSize = 10;
  
  public int internalSCIMSearchBatchSize = 100;
  
  public int internalUserReaderExpansionBatchSize = 10;
  
  public String kickstartFile;
  
  public String licenseBaseURL = "http://localhost:7011";
  
  public boolean localMetricsEnabled;
  
  public int maxHttpHeaderSize = 8192;
  
  public String metricsBaseURL = "https://metrics.fusionauth.io";
  
  public String reactorBaseURL = "https://reactor.fusionauth.io";
  
  public int reindexBatchSize = 2500;
  
  public int reindexThreadCount = 5;
  
  public RuntimeMode runtimeMode = RuntimeMode.Testing;
  
  public int searchEngineDefaultMaxHitCount = 10000;
  
  public String searchEngineDefaultRefreshInterval = "1s";
  
  public SearchEngineType searchEngineType = SearchEngineType.elasticsearch;
  
  public String setupToken;
  
  public String statsBaseURL = "http://localhost:8581";
  
  public boolean supportLoginEnabled = true;
  
  public String[] trustedProxies = new String[0];
  
  public boolean usageDataManaged = false;
  
  public int usageDataThrottleTime = 0;
  
  public String userSearchIndexName = "user";
  
  public MockFusionAuthConfiguration(String paramString, Path paramPath, boolean paramBoolean) {
    super(paramString, paramPath);
    this.fipsEnabled = paramBoolean;
    this.searchServers = new URI[] { URI.create("http://localhost:9021") };
    this.searchSniffer = false;
    this.entitySearchIndexName = "fusionauth_entity_test";
    this.searchIndexConfiguration = Map.of(this.entitySearchIndexName, "entity", this.userSearchIndexName, "user");
    reset();
  }
  
  public boolean adminStrictRefererProtectionEnabled() {
    return this.adminStrictRefererProtectionEnabled;
  }
  
  public boolean allowSubClaimOverride() {
    return this.allowSubClaimOverride;
  }
  
  public int appHTTPLocalPort() {
    return this.appHttpLocalPort;
  }
  
  public int appHTTPPort() {
    return this.appHttpPort;
  }
  
  public int appHTTPSPort() {
    return this.appHttpsPort;
  }
  
  public String appURL() {
    return this.appURL;
  }
  
  public boolean appURLComputed() {
    return false;
  }
  
  public int collectionSizeLimit() {
    return this.collectionSizeLimit;
  }
  
  public String entitySearchIndexName() {
    return this.entitySearchIndexName;
  }
  
  public int httpKeepAliveTimeout() {
    return this.httpKeepAliveTimeout;
  }
  
  public String httpsCertificate() {
    return this.httpsCertificate;
  }
  
  public Path httpsCertificateFile() {
    return this.httpsCertificateFile;
  }
  
  public boolean httpsEnabled() {
    return this.httpsEnabled;
  }
  
  public String httpsPrivateKey() {
    return this.httpsPrivateKey;
  }
  
  public Path httpsPrivateKeyFile() {
    return this.httpsPrivateKeyFile;
  }
  
  public boolean ignoreIdentitiesInUserAPIRequests() {
    return this.ignoreIdentitiesInUserAPIRequests;
  }
  
  public String installationSource() {
    return this.installationSource;
  }
  
  public InstallationType installationType() {
    return this.installationType;
  }
  
  public UUID instanceId() {
    return this.instanceId;
  }
  
  public int internalApplicationReaderBatchSize() {
    return this.internalApplicationReaderBatchSize;
  }
  
  public int internalSCIMSearchBatchSize() {
    return this.internalSCIMSearchBatchSize;
  }
  
  public int internalUserReaderExpansionBatchSize() {
    return this.internalUserReaderExpansionBatchSize;
  }
  
  public String kickstartFile() {
    return this.kickstartFile;
  }
  
  public String licenseBaseURL() {
    return this.licenseBaseURL;
  }
  
  public boolean localMetricsEnabled() {
    return this.localMetricsEnabled;
  }
  
  public int loginQueueSize() {
    return 1000000;
  }
  
  public String metricsBaseURL() {
    return this.metricsBaseURL;
  }
  
  public String propertyPrefix() {
    return "fusionauth-app";
  }
  
  public String reactorBaseURL() {
    return this.reactorBaseURL;
  }
  
  public int reindexBatchSize() {
    return this.reindexBatchSize;
  }
  
  public int reindexThreadCount() {
    return this.reindexThreadCount;
  }
  
  public void reset() {
    super.reset();
    if (System.getenv("USE_HOST_DB_FROM_CONTAINER") != null)
      this.databaseURL = this.databaseURL.replace("localhost", "host.docker.internal"); 
    this.adminStrictRefererProtectionEnabled = true;
    this.allowSubClaimOverride = false;
    this.searchEngineType = SearchEngineType.elasticsearch;
    this.licenseId = null;
    this.setupToken = null;
    this.usageDataManaged = false;
    this.userSearchIndexName = "fusionauth_user_test";
    this.localMetricsEnabled = false;
    this.ignoreIdentitiesInUserAPIRequests = true;
    this.installationType = InstallationType.standard;
    if (this.fipsEnabled) {
      this.databaseUsername = "dev_fips";
      this.databasePassword = "12345678901234567890";
    } 
  }
  
  public RuntimeMode runtimeMode() {
    return this.runtimeMode;
  }
  
  public int searchEngineDefaultMaxHitCount() {
    return this.searchEngineDefaultMaxHitCount;
  }
  
  public String searchEngineDefaultRefreshInterval() {
    return this.searchEngineDefaultRefreshInterval;
  }
  
  public SearchEngineType searchEngineType() {
    return this.searchEngineType;
  }
  
  public boolean searchSniffer() {
    return this.searchSniffer;
  }
  
  public void setAppURL(String paramString) {
    this.appURL = paramString;
  }
  
  public String setupToken() {
    return this.setupToken;
  }
  
  public String statsBaseURL() {
    return this.statsBaseURL;
  }
  
  public boolean supportLoginEnabled() {
    return this.supportLoginEnabled;
  }
  
  public String[] trustedProxies() {
    return this.trustedProxies;
  }
  
  public boolean usageDataManaged() {
    return this.usageDataManaged;
  }
  
  public int usageDataThrottleTime() {
    return this.usageDataThrottleTime;
  }
  
  public String userSearchIndexName() {
    return this.userSearchIndexName;
  }
}
