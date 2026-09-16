package io.fusionauth.domain.api.identityProvider;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.provider.IdentityProviderTenantConfiguration;
import io.fusionauth.domain.provider.PendingIdPLink;

public class IdentityProviderPendingLinkResponse implements Buildable<IdentityProviderPendingLinkResponse> {
  public IdentityProviderTenantConfiguration identityProviderTenantConfiguration;
  
  public Integer linkCount;
  
  public PendingIdPLink pendingIdPLink;
}
