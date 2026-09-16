package io.fusionauth.api.license;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.fusionauth.api.configuration.FusionAuthConfiguration;

public class FusionAuthLicenseServerURLProvider implements Provider<String> {
  private final FusionAuthConfiguration configuration;
  
  @Inject
  public FusionAuthLicenseServerURLProvider(FusionAuthConfiguration paramFusionAuthConfiguration) {
    this.configuration = paramFusionAuthConfiguration;
  }
  
  public String get() {
    return this.configuration.licenseBaseURL() + "/api/license/v2";
  }
}
