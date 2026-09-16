package io.fusionauth.app.action.api.identityProvider;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.authentication.ExternalAuthenticationException;
import io.fusionauth.api.service.authentication.IdentityProviderAuthenticationService;
import io.fusionauth.api.service.authentication.StartResult;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.identity.IdentityProviderHelper;
import io.fusionauth.api.service.identity.IdentityProviderReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.identityProvider.IdentityProviderStartLoginRequest;
import io.fusionauth.domain.api.identityProvider.IdentityProviderStartLoginResponse;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
@JSON(code = "external-authentication-exception", status = 401)
public class StartAction extends BaseTenantAPIAction {
  @JSONRequest
  public final IdentityProviderStartLoginRequest request = new IdentityProviderStartLoginRequest();
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final Map<IdentityProviderType, IdentityProviderAuthenticationService> identityProviderAuthenticationServices;
  
  private final IdentityProviderCache identityProviderCache;
  
  private final IdentityProviderReaderService identityProviderReader;
  
  @JSONResponse
  public IdentityProviderStartLoginResponse response;
  
  private IdentityProviderAuthenticationService.ValidationResult result;
  
  @Inject
  public StartAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ExternalIdentifierReaderService paramExternalIdentifierReaderService, Map<IdentityProviderType, IdentityProviderAuthenticationService> paramMap, IdentityProviderCache paramIdentityProviderCache, IdentityProviderReaderService paramIdentityProviderReaderService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.identityProviderAuthenticationServices = paramMap;
    this.identityProviderCache = paramIdentityProviderCache;
    this.identityProviderReader = paramIdentityProviderReaderService;
  }
  
  public String post() {
    if (!this.result.identityProvider.isEnabledForApplicationId(this.request.applicationId))
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.InvalidApplication); 
    IdentityProviderAuthenticationService identityProviderAuthenticationService = this.identityProviderAuthenticationServices.get(this.result.identityProvider.getType());
    StartResult startResult = identityProviderAuthenticationService.start(getTenant(), this.result.application, this.result.identityProvider, this.result.user, this.request, this.result.connectionTestId);
    if (startResult == null)
      return "missing"; 
    this.response = new IdentityProviderStartLoginResponse(startResult.code);
    return "accepted";
  }
  
  @ValidationMethod
  public void validate() {
    IdentityProviderHelper.ResolvedIdentityProviderResult resolvedIdentityProviderResult = IdentityProviderHelper.resolveIdentityProvider(this.request.identityProviderId, this.request.connectionTestId, 
        getOptionalTenant(), this.externalIdentifierReader, this.identityProviderCache, this.identityProviderReader);
    if (!resolvedIdentityProviderResult.errors.empty()) {
      this.frontEndSupport.transfer(resolvedIdentityProviderResult.errors);
      return;
    } 
    if (resolvedIdentityProviderResult.identityProvider == null)
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.InvalidIdentityProviderId); 
    IdentityProviderAuthenticationService identityProviderAuthenticationService = this.identityProviderAuthenticationServices.get(resolvedIdentityProviderResult.identityProvider.getType());
    this.result = identityProviderAuthenticationService.validateStart(getOptionalTenant(), resolvedIdentityProviderResult.identityProvider, this.request, resolvedIdentityProviderResult.connectionTestId);
    if (this.result.errors.containsError("[InvalidIdentityProviderId]"))
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.InvalidIdentityProviderId); 
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
