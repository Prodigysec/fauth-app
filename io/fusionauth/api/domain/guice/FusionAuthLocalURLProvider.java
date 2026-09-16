package io.fusionauth.api.domain.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.fusionauth.api.configuration.FusionAuthConfiguration;

public class FusionAuthLocalURLProvider implements Provider<String> {
  public final String host = "localhost";
  
  public final int port;
  
  @Inject
  public FusionAuthLocalURLProvider(FusionAuthConfiguration paramFusionAuthConfiguration) {
    this.port = paramFusionAuthConfiguration.appHTTPLocalPort();
  }
  
  public String get() {
    return "http://localhost:" + this.port;
  }
}
