package io.fusionauth.domain.api.identityProvider;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.IdentityProviderLink;
import java.util.List;

public class IdentityProviderLinkResponse implements Buildable<IdentityProviderLinkResponse> {
  public IdentityProviderLink identityProviderLink;
  
  public List<IdentityProviderLink> identityProviderLinks;
  
  @JacksonConstructor
  public IdentityProviderLinkResponse() {}
  
  public IdentityProviderLinkResponse(IdentityProviderLink paramIdentityProviderLink) {
    this.identityProviderLink = paramIdentityProviderLink;
  }
  
  public IdentityProviderLinkResponse(List<IdentityProviderLink> paramList) {
    this.identityProviderLinks = paramList;
  }
}
