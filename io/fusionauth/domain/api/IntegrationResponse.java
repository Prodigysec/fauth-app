package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Integrations;

public class IntegrationResponse {
  public Integrations integrations;
  
  @JacksonConstructor
  public IntegrationResponse() {}
  
  public IntegrationResponse(Integrations paramIntegrations) {
    this.integrations = paramIntegrations;
  }
}
