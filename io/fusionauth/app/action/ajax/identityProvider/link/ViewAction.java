package io.fusionauth.app.action.ajax.identityProvider.link;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLinkResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
public class ViewAction extends BaseAJAXAction {
  public BaseIdentityProvider<?> identityProvider;
  
  public UUID identityProviderId;
  
  public IdentityProviderLink identityProviderLink;
  
  public String identityProviderUserId;
  
  public UUID userId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.identityProvider = ((IdentityProviderResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIdentityProvider(this.identityProviderId))).identityProvider;
    this.identityProviderLink = ((IdentityProviderLinkResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserLink(this.identityProviderId, this.identityProviderUserId, this.userId))).identityProviderLink;
    return "render";
  }
}
