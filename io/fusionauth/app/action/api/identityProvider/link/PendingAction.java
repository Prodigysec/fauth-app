package io.fusionauth.app.action.api.identityProvider.link;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.identity.IdentityProviderUserService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.identityProvider.IdentityProviderPendingLinkResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{pendingLinkId}", requiresAuthentication = true, scheme = {"api"})
public class PendingAction extends BaseTenantAPIAction {
  private final IdentityProviderUserService identityProviderUserService;
  
  public String pendingLinkId;
  
  @JSONResponse
  public IdentityProviderPendingLinkResponse response;
  
  public UUID userId;
  
  private IdentityProviderUserService.ValidationResult result;
  
  @Inject
  public PendingAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, IdentityProviderUserService paramIdentityProviderUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.identityProviderUserService = paramIdentityProviderUserService;
  }
  
  public String get() {
    if (this.result.externalIdentifier == null)
      return "missing"; 
    this

      
      .response = (new IdentityProviderPendingLinkResponse()).with(paramIdentityProviderPendingLinkResponse -> paramIdentityProviderPendingLinkResponse.pendingIdPLink = this.result.externalIdentifier.buildPendingIdpLink()).with(paramIdentityProviderPendingLinkResponse -> paramIdentityProviderPendingLinkResponse.identityProviderTenantConfiguration = this.result.identityProvider.tenantConfiguration.get(this.result.tenant.id)).with(paramIdentityProviderPendingLinkResponse -> paramIdentityProviderPendingLinkResponse.linkCount = (this.result.userLinks != null) ? Integer.valueOf(this.result.userLinks.size()) : null);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateRetrieve() {
    this.result = this.identityProviderUserService.validatePendingLinkRetrieve(getOptionalTenant(), this.pendingLinkId, this.userId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
