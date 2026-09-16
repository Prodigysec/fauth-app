package io.fusionauth.domain.api.identityProvider;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.api.BaseEventRequest;
import java.util.UUID;

public class IdentityProviderLinkRequest extends BaseEventRequest implements Buildable<IdentityProviderLinkRequest> {
  public IdentityProviderLink identityProviderLink = new IdentityProviderLink();
  
  public String pendingIdPLinkId;
  
  @Deprecated
  public void setDisplayName(String paramString) {
    this.identityProviderLink.displayName = paramString;
  }
  
  @Deprecated
  public void setIdentityProviderId(UUID paramUUID) {
    this.identityProviderLink.identityProviderId = paramUUID;
  }
  
  @Deprecated
  public void setIdentityProviderUserId(String paramString) {
    this.identityProviderLink.identityProviderUserId = paramString;
  }
  
  @Deprecated
  public void setUserId(UUID paramUUID) {
    this.identityProviderLink.userId = paramUUID;
  }
}
