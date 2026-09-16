package io.fusionauth.api.guice;

import com.google.inject.Module;
import com.inversoft.license.v2.LicenseManager;
import com.inversoft.license.v2.LicenseMetaDataManager;
import com.inversoft.license.v2.LicenseProvider;
import com.inversoft.license.v2.guice.LicenseModule;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.support.service.guice.SupportModule;
import io.fusionauth.api.configuration.guice.ConfigurationModule;
import io.fusionauth.api.domain.guice.FusionAuthClientModule;
import io.fusionauth.api.domain.guice.mybatis.BackgroundDataSourceProvider;
import io.fusionauth.api.domain.guice.mybatis.DataSourceName;
import io.fusionauth.api.domain.guice.mybatis.FusionAuthPrivateMyBatisModule;
import io.fusionauth.api.domain.guice.mybatis.FusionAuthPublicMyBatisModule;
import io.fusionauth.api.domain.guice.mybatis.PrimaryDataSourceProvider;
import io.fusionauth.api.domain.guice.mybatis.SecondaryDataSourceProvider;
import io.fusionauth.api.json.guice.FusionAuthJSONModule;
import io.fusionauth.api.license.FusionAuthLicenseManager;
import io.fusionauth.api.license.FusionAuthLicenseMetaDataManager;
import io.fusionauth.api.license.FusionAuthLicenseProvider;
import io.fusionauth.api.license.FusionAuthLicenseServerURLProvider;
import io.fusionauth.api.metrics.FusionAuthMetricModule;
import io.fusionauth.api.network.FusionAuthProxyProvider;
import io.fusionauth.api.plugin.DefaultPluginTestService;
import io.fusionauth.api.plugin.PluginTestService;
import io.fusionauth.api.security.guice.ConnectorModule;
import io.fusionauth.api.security.guice.IdentityProviderModule;
import io.fusionauth.api.security.guice.SecurityModule;
import io.fusionauth.api.service.guice.FusionAuthCacheModule;
import io.fusionauth.api.service.guice.ServiceModule;
import io.fusionauth.api.service.messenger.guice.MessengerModule;
import io.fusionauth.api.system.guice.ProductVersionModule;

public class APIModule extends LicenseModule {
  private final boolean runSchedules;
  
  public APIModule(boolean paramBoolean) {
    this.runSchedules = paramBoolean;
  }
  
  protected void configure() {
    super.configure();
    install((Module)new ConfigurationModule());
    install((Module)new ConnectorModule());
    install((Module)new FusionAuthCacheModule());
    install((Module)new FusionAuthClientModule());
    install((Module)new FusionAuthJSONModule());
    install((Module)new FusionAuthMetricModule());
    install((Module)new IdentityProviderModule());
    install((Module)new MessengerModule());
    install((Module)new FusionAuthPrivateMyBatisModule((Class)PrimaryDataSourceProvider.class, DataSourceName.primary.name()));
    install((Module)new FusionAuthPrivateMyBatisModule((Class)SecondaryDataSourceProvider.class, DataSourceName.secondary.name()));
    install((Module)new FusionAuthPrivateMyBatisModule((Class)BackgroundDataSourceProvider.class, DataSourceName.background.name()));
    install((Module)new FusionAuthPublicMyBatisModule());
    install((Module)new ProductVersionModule());
    install((Module)new SecurityModule());
    install((Module)new ServiceModule(this.runSchedules));
    install((Module)new SupportModule());
    bind(PluginTestService.class).to(DefaultPluginTestService.class);
    bind(ProxyInfoSupplier.class).toProvider(FusionAuthProxyProvider.class);
  }
  
  protected int licenseLocalReloadMilliseconds() {
    return 37000;
  }
  
  protected Class<? extends LicenseManager> licenseManagerType() {
    return (Class)FusionAuthLicenseManager.class;
  }
  
  protected Class<? extends LicenseMetaDataManager> licenseMetaDataManagerType() {
    return (Class)FusionAuthLicenseMetaDataManager.class;
  }
  
  protected int licenseNetworkReloadMilliseconds() {
    return 660000;
  }
  
  protected Class<? extends LicenseProvider> licenseProviderType() {
    return (Class)FusionAuthLicenseProvider.class;
  }
  
  protected Class<FusionAuthLicenseServerURLProvider> licenseServerURLProviderType() {
    return FusionAuthLicenseServerURLProvider.class;
  }
}
