package io.fusionauth.domain.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.client.json.IdentityProviderRequestDeserializer;
import io.fusionauth.domain.provider.BaseIdentityProvider;

@JsonDeserialize(using = IdentityProviderRequestDeserializer.class)
public class IdentityProviderRequest {
  public BaseIdentityProvider<?> identityProvider;
  
  @JacksonConstructor
  public IdentityProviderRequest() {}
  
  public IdentityProviderRequest(BaseIdentityProvider<?> paramBaseIdentityProvider) {
    this.identityProvider = paramBaseIdentityProvider;
  }
}
