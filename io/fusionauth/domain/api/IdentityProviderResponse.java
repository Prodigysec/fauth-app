package io.fusionauth.domain.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.client.json.IdentityProviderResponseDeserializer;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import java.util.List;

@JsonDeserialize(using = IdentityProviderResponseDeserializer.class)
public class IdentityProviderResponse {
  public BaseIdentityProvider<?> identityProvider;
  
  public List<BaseIdentityProvider<?>> identityProviders;
  
  @JacksonConstructor
  public IdentityProviderResponse() {}
  
  public IdentityProviderResponse(List<BaseIdentityProvider<?>> paramList) {
    this.identityProviders = paramList;
  }
  
  public IdentityProviderResponse(BaseIdentityProvider<?> paramBaseIdentityProvider) {
    this.identityProvider = paramBaseIdentityProvider;
  }
}
