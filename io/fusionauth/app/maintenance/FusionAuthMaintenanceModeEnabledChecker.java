package io.fusionauth.app.maintenance;

import com.google.inject.Inject;
import com.inversoft.maintenance.MaintenanceModeEnabledChecker;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.RuntimeMode;

public class FusionAuthMaintenanceModeEnabledChecker implements MaintenanceModeEnabledChecker {
  private final FusionAuthConfiguration configuration;
  
  @Inject
  public FusionAuthMaintenanceModeEnabledChecker(FusionAuthConfiguration paramFusionAuthConfiguration) {
    this.configuration = paramFusionAuthConfiguration;
  }
  
  public boolean isEnabled() {
    return (this.configuration.runtimeMode() != RuntimeMode.Production || this.configuration.silentMode());
  }
}
