package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import com.inversoft.util.StringTools;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.identity.IdentityProviderHelper;
import io.fusionauth.api.service.identity.IdentityProviderReaderService;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.util.OAuthTools;
import io.fusionauth.app.action.oauth1.RequestTokenAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.identityProvider.IdentityProviderFrontendService;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.SupportsPostBindings;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.scope.annotation.BrowserActionSession;

@Action
public class RedirectAction extends BaseOAuthAuthenticationAction {
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final IdentityProviderCache identityProviderCache;
  
  private final IdentityProviderReaderService identityProviderReader;
  
  @BrowserActionSession(action = RequestTokenAction.class, name = "state")
  public RequestTokenAction.OAuth1 oauth1;
  
  public IdentityProviderFrontendService.PostDataToExternalIDP postDataToExternalIDP;
  
  @Inject
  public RedirectAction(ExternalIdentifierReaderService paramExternalIdentifierReaderService, FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderReaderService paramIdentityProviderReaderService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.identityProviderCache = paramIdentityProviderCache;
    this.identityProviderReader = paramIdentityProviderReaderService;
  }
  
  @PostParameterMethod
  public void clearOAuth1() {
    this.oauth1 = null;
  }
  
  public String get() {
    SupportsPostBindings supportsPostBindings;
    IdentityProviderHelper.ResolvedIdentityProviderResult resolvedIdentityProviderResult = IdentityProviderHelper.resolveIdentityProvider(this.identityProviderId, this.connectionTestId, this.codeTenant, this.externalIdentifierReader, this.identityProviderCache, this.identityProviderReader);
    BaseIdentityProvider<?> baseIdentityProvider = resolvedIdentityProviderResult.identityProvider;
    if (baseIdentityProvider instanceof SupportsPostBindings) {
      supportsPostBindings = (SupportsPostBindings)baseIdentityProvider;
    } else {
      addGeneralError("[IdentityProviderDoesNotSupportRedirect]", new Object[0]);
      return "render-error";
    } 
    if (!baseIdentityProvider.isEnabledForApplicationId(StringTools.parseUUID(this.client_id))) {
      addGeneralError("[IdentityProviderDoesNotSupportRedirect]", new Object[0]);
      return "render-error";
    } 
    if (baseIdentityProvider.tenantId != null && !baseIdentityProvider.tenantId.equals(this.codeTenant.id)) {
      addGeneralError("[IdentityProviderDoesNotSupportRedirect]", new Object[0]);
      return "render-error";
    } 
    IdentityProviderFrontendService identityProviderFrontendService = this.frontEndSupport.identityProviderFrontendServices.get(baseIdentityProvider.getType());
    if (identityProviderFrontendService == null)
      return "render-error"; 
    IdentityProviderFrontendService.FrontendRequestContext frontendRequestContext = (new IdentityProviderFrontendService.FrontendRequestContext()).with(paramFrontendRequestContext -> paramFrontendRequestContext.client_id = this.client_id).with(paramFrontendRequestContext -> paramFrontendRequestContext.connectionTestId = this.connectionTestId).with(paramFrontendRequestContext -> paramFrontendRequestContext.fusionAuthURI = this.frontEndSupport.getFusionAuthBaseURL()).with(paramFrontendRequestContext -> paramFrontendRequestContext.identityProvider = paramBaseIdentityProvider).with(paramFrontendRequestContext -> paramFrontendRequestContext.loginId = this.loginId).with(paramFrontendRequestContext -> paramFrontendRequestContext.prompt = OAuthTools.convertMaxAgeToPrompt(this.prompt, this.max_age, this.ssoSession)).with(paramFrontendRequestContext -> paramFrontendRequestContext.state = this.state);
    if (supportsPostBindings.postRequestEnabled()) {
      this.postDataToExternalIDP = identityProviderFrontendService.buildPostData(this.codeTenant, frontendRequestContext);
      return "post-to-idp";
    } 
    this.redirectToExternalIdPURI = identityProviderFrontendService.buildRedirectURI(this.codeTenant, frontendRequestContext);
    return "redirect-to-idp";
  }
}
