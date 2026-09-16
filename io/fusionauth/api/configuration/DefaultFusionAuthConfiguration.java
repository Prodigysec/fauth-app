package io.fusionauth.api.configuration;

import com.google.inject.Singleton;
import com.inversoft.configuration.BasePropertiesFileInversoftConfiguration;
import io.fusionauth.api.domain.InstallationType;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.api.domain.SearchEngineType;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DefaultFusionAuthConfiguration extends BasePropertiesFileInversoftConfiguration implements FusionAuthConfiguration {
  private static final Logger logger = LoggerFactory.getLogger(DefaultFusionAuthConfiguration.class);
  
  private boolean adminStrictRefererProtectionEnabled;
  
  private boolean allowSubClaimOverride;
  
  private int appHTTPLocalPort;
  
  private int appHTTPPort;
  
  private int appHTTPSPort;
  
  private String appURL;
  
  private boolean appURLComputed;
  
  private int collectionSizeLimit;
  
  private String entitySearchIndexName;
  
  private String httpsCertificate;
  
  private Path httpsCertificateFile;
  
  private boolean httpsEnabled;
  
  private String httpsPrivateKey;
  
  private Path httpsPrivateKeyFile;
  
  private String installationSource;
  
  private InstallationType installationType;
  
  private UUID instanceId;
  
  private int internalApplicationReaderBatchSize;
  
  private int internalSCIMSearchBatchSize;
  
  private int internalUserReaderExpansionBatchSize;
  
  private String kickstartFile;
  
  private String licenseBaseURL;
  
  private boolean localMetricsEnabled;
  
  private int loginQueueSize;
  
  private String metricsBaseURL;
  
  private String reactorBaseURL;
  
  private int reindexBatchSize;
  
  private int reindexThreadCount;
  
  private RuntimeMode runtimeMode;
  
  private int searchEngineDefaultMaxHitCount;
  
  private String searchEngineDefaultRefreshInterval;
  
  private SearchEngineType searchEngineType;
  
  private String setupToken;
  
  private String statsBaseURL;
  
  private boolean supportLoginEnabled;
  
  private String[] trustedProxies;
  
  private boolean usageDataManaged;
  
  private int usageDataThrottleTime;
  
  private String userSearchIndexName;
  
  public DefaultFusionAuthConfiguration() {
    super((new BasePropertiesFileInversoftConfiguration.ConfigurationParameters()).withConfigurationDirectoryProperty("fusionauth.config.directory")
        .withDatabaseConfigRequired(true)
        .withDefaultSearchServers(new URI[] { URI.create("http://localhost:9021") }).withDefaultDatabaseConnectionMinimumIdle(10)
        .withDefaultDatabaseMaximumPoolSize(20)
        .withFileName("fusionauth.properties")
        .withHomeDirectoryProperty("fusionauth.home.directory")
        .withLicenseIdRequired(false)
        .withProductName("FusionAuth")
        .withPropertyPrefix("fusionauth-app"));
  }
  
  public boolean adminStrictRefererProtectionEnabled() {
    return this.adminStrictRefererProtectionEnabled;
  }
  
  public boolean allowSubClaimOverride() {
    return this.allowSubClaimOverride;
  }
  
  public int appHTTPLocalPort() {
    return this.appHTTPLocalPort;
  }
  
  public int appHTTPPort() {
    return this.appHTTPPort;
  }
  
  public int appHTTPSPort() {
    return this.appHTTPSPort;
  }
  
  public String appURL() {
    return this.appURL;
  }
  
  public boolean appURLComputed() {
    return this.appURLComputed;
  }
  
  public int collectionSizeLimit() {
    return this.collectionSizeLimit;
  }
  
  public String entitySearchIndexName() {
    return this.entitySearchIndexName;
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
    return true;
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
    return this.loginQueueSize;
  }
  
  public String metricsBaseURL() {
    return this.metricsBaseURL;
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
  
  public Map<String, String> searchIndexConfiguration() {
    return Map.of(this.userSearchIndexName, "user", this.entitySearchIndexName, "entity");
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
  
  protected void initialize() {
    super.initialize();
    try {
      logger.info("Loading FusionAuth configuration file [{}]", this.configurationFile.toFile().getCanonicalPath());
    } catch (IOException iOException) {
      throw new RuntimeException(iOException);
    } 
    this
      
      .adminStrictRefererProtectionEnabled = resolveProperty().withNames(new String[] { "fusionauth-app.admin.strict-referer-protection.enabled" }).withDefaultBooleanValue(true).asBoolean();
    this
      
      .allowSubClaimOverride = resolveProperty().withNames(new String[] { "fusionauth-app.allow-sub-claim-override" }).withDefaultBooleanValue(false).asBoolean();
    this
      
      .collectionSizeLimit = resolveProperty().withNames(new String[] { "fusionauth-app.collection-size-limit" }).withDefaultIntValue(10000).asInt();
    this

      
      .appHTTPPort = resolveProperty().withNames(new String[] { "fusionauth-app.http.port" }).withDeprecatedNames(new String[] { "fusionauth-app.http-port" }).withDefaultIntValue(9011).asInt();
    this
      
      .appHTTPLocalPort = resolveProperty().withNames(new String[] { "fusionauth-app.http-local.port" }).withDefaultIntValue(9012).asInt();
    this
      
      .appHTTPSPort = resolveProperty().withNames(new String[] { "fusionauth-app.https.port" }).withDefaultIntValue(9013).asInt();
    this
      
      .httpsEnabled = resolveProperty().withNames(new String[] { "fusionauth-app.https.enabled" }).withDefaultBooleanValue(false).asBoolean();
    this
      .httpsCertificateFile = resolveProperty().withNames(new String[] { "fusionauth-app.https.certificate-file" }).asPath();
    this
      .httpsCertificate = resolveProperty().withNames(new String[] { "fusionauth-app.https.certificate" }).asString();
    this
      .httpsPrivateKeyFile = resolveProperty().withNames(new String[] { "fusionauth-app.https.private-key-file" }).asPath();
    this
      .httpsPrivateKey = resolveProperty().withNames(new String[] { "fusionauth-app.https.private-key" }).asString();
    URI uRI = resolveProperty().withNames(new String[] { "fusionauth-app.url" }).withDeprecatedNames(new String[] { "fusionauth-app.public-url", "FUSIONAUTH_URL" }).asURI(new String[] { "http", "https" });
    if (uRI != null) {
      this.appURL = uRI.toString();
      logger.info("Set property [fusionauth-app.url] set to [" + this.appURL + "] using configured value.");
    } else {
      this.appURL = computeAppURL();
      this.appURLComputed = true;
      logger.info("Dynamically set property [fusionauth-app.url] set to [" + this.appURL + "]");
    } 
    this.runtimeMode = RuntimeMode.fromConfiguration(resolveProperty().withNames(new String[] { "fusionauth-app.runtime-mode" }).withDeprecatedNames(new String[] { "fusionauth.runtime-mode" }).withDefaultStringValue("development")
        .withLogOverride(true)
        .asString());
    this


      
      .searchEngineType = (SearchEngineType)resolveProperty().withNames(new String[] { "search.type" }).withDeprecatedNames(new String[] { "FUSIONAUTH_SEARCH_ENGINE_TYPE", "fusionauth-app.search-engine-type", "fusionauth.search.engine" }).withDefaultStringValue("elasticsearch").withLogOverride(true).asEnum(SearchEngineType.class);
    this
      
      .reindexBatchSize = resolveProperty().withNames(new String[] { "fusionauth-app.reindex-batch-size" }).withDefaultIntValue(1000).asInt();
    this
      
      .reindexThreadCount = resolveProperty().withNames(new String[] { "fusionauth-app.reindex-thread-count" }).withDefaultIntValue(4).asInt();
    this

      
      .licenseBaseURL = resolveProperty().withNames(new String[] { "fusionauth-app.license.url" }).withDeprecatedNames(new String[] { "fusionauth-license-url" }).withDefaultStringValue("https://license.fusionauth.io").asString();
    this

      
      .metricsBaseURL = resolveProperty().withNames(new String[] { "fusionauth-app.metrics.url" }).withDeprecatedNames(new String[] { "fusionauth-metrics-url" }).withDefaultStringValue("https://metrics.fusionauth.io").asString();
    this

      
      .reactorBaseURL = resolveProperty().withNames(new String[] { "fusionauth-app.reactor.url" }).withDeprecatedNames(new String[] { "fusionauth-reactor-url" }).withDefaultStringValue("https://reactor.fusionauth.io").asString();
    this

      
      .statsBaseURL = resolveProperty().withNames(new String[] { "fusionauth-app.stats.url" }).withDeprecatedNames(new String[] { "fusionauth-stats-url" }).withDefaultStringValue("https://usage-stats.fusionauth.io").asString();
    this
      
      .supportLoginEnabled = resolveProperty().withNames(new String[] { "fusionauth-app.support-login.enabled" }).withDefaultBooleanValue(false).asBoolean();
    this
      
      .localMetricsEnabled = resolveProperty().withNames(new String[] { "fusionauth-app.local-metrics.enabled" }).withDefaultBooleanValue(false).asBoolean();
    this

      
      .loginQueueSize = resolveProperty().withNames(new String[] { "fusionauth-app.login-queue.size" }).withDeprecatedNames(new String[] { "fusionauth.login-queue-size" }).withDefaultIntValue(1000000).asInt();
    this

      
      .kickstartFile = resolveProperty().withNames(new String[] { "fusionauth-app.kickstart.file" }).withDeprecatedNames(new String[] { "FUSIONAUTH_KICKSTART" }).withRequired(false).asString();
    this


      
      .entitySearchIndexName = resolveProperty().withNames(new String[] { "fusionauth-app.entity-search-index.name" }).withRequired(false).withLogOverride(true).withDefaultStringValue("fusionauth_entity").asString();
    this


      
      .userSearchIndexName = resolveProperty().withNames(new String[] { "fusionauth-app.user-search-index.name" }).withRequired(false).withLogOverride(true).withDefaultStringValue("fusionauth_user").asString();
    this


      
      .searchEngineDefaultMaxHitCount = resolveProperty().withNames(new String[] { "fusionauth-app.search.default-max-hit-count" }).withRequired(false).withLogOverride(true).withDefaultIntValue(10000).asInt();
    this


      
      .searchEngineDefaultRefreshInterval = resolveProperty().withNames(new String[] { "fusionauth-app.search.default-refresh-interval" }).withRequired(false).withLogOverride(true).withDefaultStringValue("1s").asString();
    this


      
      .internalApplicationReaderBatchSize = resolveProperty().withNames(new String[] { "fusionauth-app.internal.application.reader-batch-size" }).withRequired(false).withLogOverride(true).withDefaultIntValue(32000).asInt();
    if (this.internalApplicationReaderBatchSize > 64000) {
      logger.warn("The configured fusionauth-app.internal.application.reader-batch-size of {} exceeds the maximum of 64,000. Using 64,000.", 
          Integer.valueOf(this.internalApplicationReaderBatchSize));
      this.internalApplicationReaderBatchSize = 64000;
    } 
    this


      
      .internalUserReaderExpansionBatchSize = resolveProperty().withNames(new String[] { "fusionauth-app.internal.user.reader-expansion-batch-size" }).withRequired(false).withLogOverride(true).withDefaultIntValue(25).asInt();
    if (this.internalUserReaderExpansionBatchSize > 100) {
      logger.warn("The configured fusionauth-app.internal.user.reader-batch-size of {} exceeds the maximum of 100. Using 100.", 
          Integer.valueOf(this.internalUserReaderExpansionBatchSize));
      this.internalUserReaderExpansionBatchSize = 100;
    } 
    this

      
      .internalSCIMSearchBatchSize = resolveProperty().withNames(new String[] { "fusionauth-app.internal.scim.search-batch-size" }).withRequired(false).withDefaultIntValue(1000000).asInt();
    this
      
      .setupToken = resolveProperty().withNames(new String[] { "fusionauth-app.setup-token" }).withRequired(false).asString();
    this

      
      .trustedProxies = resolveProperty().withNames(new String[] { "fusionauth-app.trusted-proxies" }).withRequired(false).withLogOverride(true).asStrings();
    this
      
      .usageDataManaged = resolveProperty().withNames(new String[] { "fusionauth-app.usage-data-managed" }).withDefaultBooleanValue(false).asBoolean();
    this.usageDataThrottleTime = Integer.min(60000, 
        resolveProperty().withNames(new String[] { "fusionauth-app.usage-data-throttle-time" }).withDefaultIntValue(2000)
        .asInt());
    this
      
      .installationSource = resolveProperty().withNames(new String[] { "fusionauth-app.installation-source" }).withRequired(false).asString();
    this
      
      .installationType = (InstallationType)resolveProperty().withNames(new String[] { "fusionauth-app.installation-type" }).withDefaultStringValue("standard").asEnum(InstallationType.class);
    String str = resolveProperty().withNames(new String[] { "fusionauth-app.instance-id" }).asString();
    if (str != null) {
      logger.info("Overriding instance id from the configuration. The new instance id is [{}]", str);
      this.instanceId = UUID.fromString(str);
    } 
  }
}
