package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Integrations;

public class IntegrationRequest {
  public Integrations integrations;
  
  @JacksonConstructor
  public IntegrationRequest() {}
  
  public IntegrationRequest(Integrations paramIntegrations) {
    this.integrations = paramIntegrations;
  }
}
