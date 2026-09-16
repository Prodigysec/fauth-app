package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.util.NetworkTools;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.security.SecurityScheme;
import org.primeframework.mvc.security.UnauthenticatedException;

public class FusionAuthLocalMetricsSecurityScheme implements SecurityScheme {
  private final FusionAuthConfiguration configuration;
  
  private final HTTPRequest request;
  
  @Inject
  public FusionAuthLocalMetricsSecurityScheme(FusionAuthConfiguration paramFusionAuthConfiguration, HTTPRequest paramHTTPRequest) {
    this.configuration = paramFusionAuthConfiguration;
    this.request = paramHTTPRequest;
  }
  
  public void handle(String[] paramArrayOfString) {
    if (!this.configuration.localMetricsEnabled())
      throw new UnauthenticatedException(); 
    if (this.request.getHeader("X-Forwarded-For") != null || !NetworkTools.isLoopbackAddress(this.request.getRawIPAddress()))
      throw new UnauthenticatedException(); 
  }
}
