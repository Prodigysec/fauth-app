package io.fusionauth.domain.api.identityProvider;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;

public class IdentityProviderStartLoginResponse implements Buildable<IdentityProviderStartLoginResponse> {
  public String code;
  
  @JacksonConstructor
  public IdentityProviderStartLoginResponse() {}
  
  public IdentityProviderStartLoginResponse(String paramString) {
    this.code = paramString;
  }
}
