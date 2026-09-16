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
import org.primeframework.mvc.validation.annotation.PostValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
public class DeleteAction extends BaseAJAXAction {
  public BaseIdentityProvider<?> identityProvider;
  
  public UUID identityProviderId;
  
  public IdentityProviderLink identityProviderUser;
  
  public String identityProviderUserId;
  
  public UUID userId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.deleteUserLink(this.identityProviderId, this.identityProviderUserId, this.userId));
    writeAuditLog("Deleted the user account link with Id of [" + this.identityProviderUserId + "] in Identity Provider [" + String.valueOf(this.identityProviderId) + "] for user with Id [" + String.valueOf(this.userId) + "]");
    return "success";
  }
  
  @PostValidationMethod
  public void postValidate() {
    this.identityProvider = ((IdentityProviderResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIdentityProvider(this.identityProviderId))).identityProvider;
    this.identityProviderUser = ((IdentityProviderLinkResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserLink(this.identityProviderId, this.identityProviderUserId, this.userId))).identityProviderLink;
  }
}
