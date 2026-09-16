package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.error.Errors;
import com.inversoft.jdbc.CurrentDatabaseEngine;
import com.inversoft.search.ElasticSearchClient;
import com.inversoft.support.domain.ProductVersion;
import com.inversoft.support.service.SupportService;
import com.inversoft.validator.Validator;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.ProductInformation;
import io.fusionauth.api.domain.SearchEngineType;
import io.fusionauth.api.domain.SystemConfigurationMapper;
import io.fusionauth.api.service.search.UserSearchEngine;
import io.fusionauth.api.util.NetworkTools;
import io.fusionauth.domain.SystemConfiguration;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.mybatis.guice.transactional.Transactional;

public class DefaultSystemConfigurationService implements SystemConfigurationService {
  private final CacheNotifier cacheNotifier;
  
  private final FusionAuthConfiguration configuration;
  
  private final SystemConfigurationMapper mapper;
  
  private final NodeService nodeService;
  
  private final UserSearchEngine searchEngine;
  
  private final SupportService supportService;
  
  @Inject
  public DefaultSystemConfigurationService(CacheNotifier paramCacheNotifier, FusionAuthConfiguration paramFusionAuthConfiguration, NodeService paramNodeService, SupportService paramSupportService, SystemConfigurationMapper paramSystemConfigurationMapper, UserSearchEngine paramUserSearchEngine) {
    this.configuration = paramFusionAuthConfiguration;
    this.nodeService = paramNodeService;
    this.mapper = paramSystemConfigurationMapper;
    this.supportService = paramSupportService;
    this.cacheNotifier = paramCacheNotifier;
    this.searchEngine = paramUserSearchEngine;
  }
  
  @Transactional
  public void _update(SystemConfiguration paramSystemConfiguration1, SystemConfiguration paramSystemConfiguration2) {
    paramSystemConfiguration2.cookieEncryptionKey = paramSystemConfiguration1.cookieEncryptionKey;
    paramSystemConfiguration2.insertInstant = paramSystemConfiguration1.insertInstant;
    paramSystemConfiguration2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.mapper.update(paramSystemConfiguration2);
  }
  
  public boolean isUsageStatsEnabled() {
    return (this.configuration.usageDataManaged() || (retrieve()).usageDataConfiguration.enabled);
  }
  
  public SystemConfiguration retrieve() {
    SystemConfiguration systemConfiguration = this.mapper.retrieve();
    if (this.configuration.usageDataManaged())
      systemConfiguration.usageDataConfiguration.enabled = true; 
    return systemConfiguration;
  }
  
  public ProductInformation retrieveProductInformation() {
    ProductVersion productVersion = this.supportService.getProductVersion();
    ProductInformation productInformation = new ProductInformation();
    productInformation.latestProductVersion = productVersion.latestVersion;
    productInformation.currentProductVersion = productVersion.currentVersion;
    productInformation.productUpdateAvailable = productVersion.updateAvailable;
    productInformation.dbEngine = CurrentDatabaseEngine.current.toString();
    productInformation.dbEngineVersion = CurrentDatabaseEngine.version;
    productInformation.runtimeMode = this.configuration.runtimeMode();
    productInformation.searchEngine = this.configuration.searchEngineType().toString();
    if (productInformation.searchEngine.equals(SearchEngineType.elasticsearch.name())) {
      this.searchEngine.status();
      productInformation.searchEngineVersion = ElasticSearchClient.VERSION;
      productInformation.searchEngineDistribution = ElasticSearchClient.DISTRIBUTION;
    } 
    productInformation.nodes = this.nodeService.retrieveAll();
    return productInformation;
  }
  
  public void update(SystemConfiguration paramSystemConfiguration) {
    SystemConfiguration systemConfiguration = this.mapper.retrieve();
    _update(systemConfiguration, paramSystemConfiguration);
    this.cacheNotifier.reload(new String[] { "CORSConfiguration", "SystemConfiguration" });
  }
  
  public Errors validate(SystemConfiguration paramSystemConfiguration) {
    return (new Validator())
      
      .notMissing(paramSystemConfiguration.reportTimezone, "systemConfiguration.reportTimezone", new Object[0])

      
      .ifTrue(paramSystemConfiguration.auditLogConfiguration.delete.enabled, paramValidator -> paramValidator.ensure((paramSystemConfiguration.auditLogConfiguration.delete.numberOfDaysToRetain > 0), "systemConfiguration.auditLogConfiguration.delete.numberOfDaysToRetain", "[invalid]", new Object[0]))


      
      .ensure((paramSystemConfiguration.eventLogConfiguration.numberToRetain > 0), "systemConfiguration.eventLogConfiguration.numberToRetain", "[invalid]", new Object[0])

      
      .ifTrue(paramSystemConfiguration.loginRecordConfiguration.delete.enabled, paramValidator -> paramValidator.ensure((paramSystemConfiguration.loginRecordConfiguration.delete.numberOfDaysToRetain > 0), "systemConfiguration.loginRecordConfiguration.delete.numberOfDaysToRetain", "[invalid]", new Object[0]))


      
      .ensure((paramSystemConfiguration.corsConfiguration.preflightMaxAgeInSeconds >= -1), "systemConfiguration.corsConfiguration.preflightMaxAgeInSeconds", "[tooSmall]", new Object[0])
      .ifTrue(paramSystemConfiguration.corsConfiguration.allowedOrigins.contains(URI.create("*")), paramValidator -> paramValidator.ensure((paramSystemConfiguration.corsConfiguration.allowedOrigins.size() == 1), "systemConfiguration.corsConfiguration.allowedOrigins", "[invalid]", new Object[0]))


      
      .ifTrue(paramSystemConfiguration.webhookEventLogConfiguration.delete.enabled, paramValidator -> paramValidator.ensure((paramSystemConfiguration.webhookEventLogConfiguration.delete.numberOfDaysToRetain > 0), "systemConfiguration.webhookEventLogConfiguration.delete.numberOfDaysToRetain", "[invalid]", new Object[0]))




      
      .forEach(paramSystemConfiguration.trustedProxyConfiguration.trusted, (paramValidator, paramString, paramInteger) -> paramValidator.ensureWithCode(NetworkTools.isValidCDIR(paramString), "systemConfiguration.trustedProxyConfiguration.trusted[" + paramInteger + "]", "[invalid]systemConfiguration.trustedProxyConfiguration.trusted", new Object[0]))


      
      .ifFalse(paramSystemConfiguration.usageDataConfiguration.enabled, paramValidator -> paramValidator.ensure(!this.configuration.usageDataManaged(), "systemConfiguration.usageDataConfiguration.enabled", "[invalid]", new Object[0]))
      
      .done();
  }
}
