package io.fusionauth.app.maintenance.guice;

import com.inversoft.configuration.InversoftConfiguration;
import com.inversoft.maintenance.BaseMaintenanceModeInteractiveModule;
import com.inversoft.maintenance.MaintenanceModeModules;
import com.inversoft.maintenance.db.JDBCMaintenanceModeDatabaseService;
import com.inversoft.maintenance.db.MaintenanceModeDatabaseService;
import com.inversoft.maintenance.search.MaintenanceModeSearchService;
import com.inversoft.util.LoggerTool;
import io.fusionauth.api.configuration.DefaultFusionAuthConfiguration;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.app.maintenance.FusionAuthElasticSearchMaintenanceModeService;
import io.fusionauth.app.maintenance.FusionAuthMaintenanceModeMVCConfiguration;
import io.fusionauth.app.maintenance.FusionAuthMaintenanceModeModules;
import io.fusionauth.app.primeframework.FusionHTTPContextAuthSetup;
import org.primeframework.mvc.config.MVCConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FusionAuthInteractiveMaintenanceModeModule extends BaseMaintenanceModeInteractiveModule {
  private static final Logger logger = LoggerFactory.getLogger(FusionAuthInteractiveMaintenanceModeModule.class);
  
  private final boolean printAnnouncement;
  
  public FusionAuthInteractiveMaintenanceModeModule(boolean paramBoolean) {
    this.printAnnouncement = paramBoolean;
  }
  
  protected void bindServices() {
    bind(MaintenanceModeDatabaseService.class).to(JDBCMaintenanceModeDatabaseService.class);
    bind(MaintenanceModeSearchService.class).to(FusionAuthElasticSearchMaintenanceModeService.class);
  }
  
  protected void configure() {
    if (this.printAnnouncement)
      LoggerTool.logPrettyInfoMessage(logger, "Entering Maintenance Mode"); 
    super.configure();
    bind(FusionHTTPContextAuthSetup.class).asEagerSingleton();
    bind(InversoftConfiguration.class).to(DefaultFusionAuthConfiguration.class).asEagerSingleton();
    bind(FusionAuthConfiguration.class).to(DefaultFusionAuthConfiguration.class).asEagerSingleton();
    bind(MVCConfiguration.class).to(FusionAuthMaintenanceModeMVCConfiguration.class);
  }
  
  protected Class<? extends MaintenanceModeModules> maintenanceModeModulesType() {
    return (Class)FusionAuthMaintenanceModeModules.class;
  }
}
