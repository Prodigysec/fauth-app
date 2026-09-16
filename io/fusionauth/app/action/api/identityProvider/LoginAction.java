package io.fusionauth.app.action.api.identityProvider;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.api.service.authentication.ExternalAuthenticationException;
import io.fusionauth.api.service.authentication.IdentityProviderAuthenticationService;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.identity.IdentityProviderHelper;
import io.fusionauth.api.service.identity.IdentityProviderReaderService;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.app.action.api.BaseLoginAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLoginRequest;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.JSON.List;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
@JSON(code = "external-authentication-exception", status = 401)
@Status(code = "user-authentication-pending", status = 204)
@List({@JSON(code = "user-authentication-canceled", status = 400), @JSON(code = "user-authentication-failed", status = 400)})
public class LoginAction extends BaseLoginAction {
  @JSONRequest
  public final IdentityProviderLoginRequest request = new IdentityProviderLoginRequest();
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final Map<IdentityProviderType, IdentityProviderAuthenticationService> identityProviderAuthenticationServices;
  
  private final IdentityProviderCache identityProviderCache;
  
  private final IdentityProviderReaderService identityProviderReader;
  
  private IdentityProviderAuthenticationService.ValidationResult result;
  
  @Inject
  public LoginAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ApplicationReaderService paramApplicationReaderService, ExternalIdentifierReaderService paramExternalIdentifierReaderService, JWTService paramJWTService, IdentityProviderCache paramIdentityProviderCache, Map<IdentityProviderType, IdentityProviderAuthenticationService> paramMap, IdentityProviderReaderService paramIdentityProviderReaderService, RefreshTokenService paramRefreshTokenService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramApplicationReaderService, paramJWTService, paramRefreshTokenService);
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.identityProviderAuthenticationServices = paramMap;
    this.identityProviderCache = paramIdentityProviderCache;
    this.identityProviderReader = paramIdentityProviderReaderService;
  }
  
  public String post() {
    if (!this.result.identityProvider.isEnabledForApplicationId(this.request.applicationId))
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.InvalidApplication); 
    IdentityProviderAuthenticationService identityProviderAuthenticationService = this.identityProviderAuthenticationServices.get(this.result.identityProvider.getType());
    return callLogin(getTenant(), this.result.application, () -> paramIdentityProviderAuthenticationService.login(getTenant(), this.result.application, this.result.identityProvider, this.request, this.result.connectionTestId), this.request);
  }
  
  @ValidationMethod
  public void validate() {
    IdentityProviderHelper.ResolvedIdentityProviderResult resolvedIdentityProviderResult = IdentityProviderHelper.resolveIdentityProvider(this.request.identityProviderId, this.request.connectionTestId, 
        getOptionalTenant(), this.externalIdentifierReader, this.identityProviderCache, this.identityProviderReader);
    if (!resolvedIdentityProviderResult.errors.empty()) {
      this.frontEndSupport.transfer(resolvedIdentityProviderResult.errors);
      return;
    } 
    if (this.request.connectionTestId != null)
      this.request.noJWT = true; 
    if (resolvedIdentityProviderResult.identityProvider == null) {
      this.frontEndSupport.addGeneralError("[InvalidIdentityProviderId]", new Object[0]);
      return;
    } 
    IdentityProviderType identityProviderType = requiredType();
    if (identityProviderType != null && resolvedIdentityProviderResult.identityProvider.getType() != identityProviderType) {
      this.frontEndSupport.addFieldError("identityProviderId", "[type]identityProviderId", new Object[] { resolvedIdentityProviderResult.identityProvider.id, identityProviderType, resolvedIdentityProviderResult.identityProvider.getType() });
      return;
    } 
    IdentityProviderAuthenticationService identityProviderAuthenticationService = this.identityProviderAuthenticationServices.get(resolvedIdentityProviderResult.identityProvider.getType());
    this.result = identityProviderAuthenticationService.validate(getOptionalTenant(), resolvedIdentityProviderResult.identityProvider, this.request, resolvedIdentityProviderResult.connectionTestId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  protected IdentityProviderType requiredType() {
    return null;
  }
}
