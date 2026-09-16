package io.fusionauth.app.maintenance;

import com.google.inject.Module;
import com.inversoft.maintenance.MaintenanceModeModules;
import io.fusionauth.app.guice.FusionAuthModule;
import io.fusionauth.app.maintenance.guice.FusionAuthInitialMaintenanceModeModule;
import io.fusionauth.app.maintenance.guice.FusionAuthInteractiveMaintenanceModeModule;
import io.fusionauth.app.maintenance.guice.FusionAuthSilentMaintenanceModeModule;

public class FusionAuthMaintenanceModeModules implements MaintenanceModeModules {
  public Module initialModule() {
    return (Module)new FusionAuthInitialMaintenanceModeModule();
  }
  
  public Module interactiveModeModule(boolean paramBoolean) {
    return (Module)new FusionAuthInteractiveMaintenanceModeModule(paramBoolean);
  }
  
  public Module mainModule() {
    return (Module)new FusionAuthModule(true);
  }
  
  public Module silentModeModule() {
    return (Module)new FusionAuthSilentMaintenanceModeModule();
  }
}
