package io.fusionauth.app.maintenance.guice;

import com.inversoft.configuration.InversoftConfiguration;
import com.inversoft.maintenance.BaseMaintenanceModeInitialModule;
import com.inversoft.maintenance.MaintenanceModeEnabledChecker;
import io.fusionauth.api.configuration.DefaultFusionAuthConfiguration;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.app.maintenance.FusionAuthMaintenanceModeEnabledChecker;
import io.fusionauth.app.primeframework.FusionAuthCORSConfigurationProvider;
import org.primeframework.mvc.cors.CORSConfigurationProvider;

public class FusionAuthInitialMaintenanceModeModule extends BaseMaintenanceModeInitialModule {
  protected void configure() {
    super.configure();
    bind(CORSConfigurationProvider.class).to(FusionAuthCORSConfigurationProvider.class);
    bind(InversoftConfiguration.class).to(DefaultFusionAuthConfiguration.class).asEagerSingleton();
    bind(FusionAuthConfiguration.class).to(DefaultFusionAuthConfiguration.class).asEagerSingleton();
  }
  
  protected Class<? extends MaintenanceModeEnabledChecker> maintenanceModeEnabledChecker() {
    return (Class)FusionAuthMaintenanceModeEnabledChecker.class;
  }
}
