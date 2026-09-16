package io.fusionauth.app.action.api.identityProvider;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.identity.IdentityProviderUserService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLinkRequest;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLinkResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class LinkAction extends BaseTenantAPIAction {
  @JSONRequest
  public final IdentityProviderLinkRequest request = new IdentityProviderLinkRequest();
  
  private final IdentityProviderUserService identityProviderUserService;
  
  public UUID identityProviderId;
  
  public String identityProviderUserId;
  
  @JSONResponse
  public IdentityProviderLinkResponse response;
  
  public UUID userId;
  
  private IdentityProviderUserService.ValidationResult result;
  
  @Inject
  public LinkAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, IdentityProviderUserService paramIdentityProviderUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.identityProviderUserService = paramIdentityProviderUserService;
  }
  
  public String delete() {
    if (this.result.link == null)
      return "missing"; 
    this.identityProviderUserService.unlink(this.frontEndSupport.buildEventInfo(null), getTenant(), this.result.identityProvider, this.result.link, this.result.user);
    return "success";
  }
  
  public String get() {
    if (this.result.identityProvider != null && this.identityProviderUserId != null) {
      this.response = new IdentityProviderLinkResponse(this.identityProviderUserService.retrieveIdentityProviderUser(getTenant(), this.result.identityProvider, this.identityProviderUserId, this.result.user));
      if (this.response.identityProviderLink == null)
        return "missing"; 
      return "render";
    } 
    this.response = new IdentityProviderLinkResponse(this.identityProviderUserService.retrieveIdentityProviderUsersByUser(getTenant(), this.result.identityProvider, this.result.user.id));
    return "render";
  }
  
  public String post() {
    String str1 = (this.result.externalIdentifier != null) ? this.result.externalIdentifier.getAttribute("identityProviderDisplayName") : this.request.identityProviderLink.displayName;
    String str2 = (this.result.externalIdentifier != null) ? this.result.externalIdentifier.id : null;
    String str3 = (this.result.externalIdentifier != null) ? this.result.externalIdentifier.getAttribute("identityProviderToken") : this.request.identityProviderLink.token;
    this.response = new IdentityProviderLinkResponse(this.identityProviderUserService.link(this.frontEndSupport.buildEventInfo(null), getTenant(), this.result.user, this.result.identityProvider, this.result.identityProviderUserId, str1, str3, str2));
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validateLink() {
    this.result = this.identityProviderUserService.validateLink(getOptionalTenant(), this.request.identityProviderLink.identityProviderId, this.request.identityProviderLink.identityProviderUserId, this.request.pendingIdPLinkId, this.request.identityProviderLink.userId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateRetrieve() {
    this.result = this.identityProviderUserService.validateRetrieve(getOptionalTenant(), this.identityProviderId, this.identityProviderUserId, this.userId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateUnlink() {
    this.result = this.identityProviderUserService.validateUnlink(getOptionalTenant(), this.identityProviderId, this.identityProviderUserId, this.userId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
