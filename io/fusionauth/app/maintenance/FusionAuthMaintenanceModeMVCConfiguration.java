package io.fusionauth.app.maintenance;

import com.google.inject.Inject;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.RuntimeMode;
import java.nio.file.Path;
import java.security.Key;
import org.primeframework.mvc.config.AbstractMVCConfiguration;

public class FusionAuthMaintenanceModeMVCConfiguration extends AbstractMVCConfiguration {
  private final Path baseDirectory;
  
  private final FusionAuthConfiguration configuration;
  
  @Inject
  public FusionAuthMaintenanceModeMVCConfiguration(FusionAuthConfiguration paramFusionAuthConfiguration) {
    this.baseDirectory = paramFusionAuthConfiguration.homeDirectory().resolve("web");
    this.configuration = paramFusionAuthConfiguration;
  }
  
  public boolean allowUnknownParameters() {
    return (this.configuration.runtimeMode() == RuntimeMode.Production || this.configuration.runtimeMode() == RuntimeMode.Development || this.configuration.runtimeMode() == RuntimeMode.FusionAuth_Demo);
  }
  
  public Path baseDirectory() {
    return this.baseDirectory;
  }
  
  public Key cookieEncryptionKey() {
    return null;
  }
  
  public int l10nReloadSeconds() {
    return 10;
  }
  
  public int templateCheckSeconds() {
    return 10;
  }
}
