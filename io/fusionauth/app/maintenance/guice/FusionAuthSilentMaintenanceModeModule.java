package io.fusionauth.app.maintenance.guice;

import com.inversoft.configuration.InversoftConfiguration;
import com.inversoft.maintenance.BaseMaintenanceModeSilentModeModule;
import com.inversoft.maintenance.MaintenanceModeModules;
import com.inversoft.maintenance.db.JDBCMaintenanceModeDatabaseService;
import com.inversoft.maintenance.db.MaintenanceModeDatabaseService;
import com.inversoft.maintenance.search.MaintenanceModeSearchService;
import io.fusionauth.api.configuration.DefaultFusionAuthConfiguration;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.app.maintenance.FusionAuthElasticSearchMaintenanceModeService;
import io.fusionauth.app.maintenance.FusionAuthMaintenanceModeMVCConfiguration;
import io.fusionauth.app.maintenance.FusionAuthMaintenanceModeModules;
import io.fusionauth.app.primeframework.FusionAuthCORSConfigurationProvider;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.cors.CORSConfigurationProvider;

public class FusionAuthSilentMaintenanceModeModule extends BaseMaintenanceModeSilentModeModule {
  protected void bindServices() {
    bind(MaintenanceModeDatabaseService.class).to(JDBCMaintenanceModeDatabaseService.class);
    bind(MaintenanceModeSearchService.class).to(FusionAuthElasticSearchMaintenanceModeService.class);
  }
  
  protected void configure() {
    super.configure();
    bind(CORSConfigurationProvider.class).to(FusionAuthCORSConfigurationProvider.class);
    bind(InversoftConfiguration.class).to(DefaultFusionAuthConfiguration.class).asEagerSingleton();
    bind(FusionAuthConfiguration.class).to(DefaultFusionAuthConfiguration.class).asEagerSingleton();
    bind(MVCConfiguration.class).to(FusionAuthMaintenanceModeMVCConfiguration.class);
  }
  
  protected Class<? extends MaintenanceModeModules> maintenanceModeModulesType() {
    return (Class)FusionAuthMaintenanceModeModules.class;
  }
}
