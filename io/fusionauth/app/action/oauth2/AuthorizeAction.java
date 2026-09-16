package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.SecurityTools;
import com.inversoft.util.StringTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.user.BotDetectionScoreValidator;
import io.fusionauth.api.util.ActionTools;
import io.fusionauth.api.util.OAuthTools;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.identityProvider.IdentityProviderFrontendService;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.LoginRequest;
import io.fusionauth.domain.api.identityProvider.IdentityProviderStartLoginRequest;
import io.fusionauth.domain.api.identityProvider.IdentityProviderStartLoginResponse;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.BaseIdentityProviderApplicationConfiguration;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;
import org.primeframework.mvc.scope.annotation.ManagedSessionCookie;
import org.primeframework.mvc.util.QueryStringBuilder;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;

@Action
@Redirect(code = "redirect-to-wait", uri = "${redirectToWait}")
public class AuthorizeAction extends BaseOAuthAuthenticationAction {
  private final IdentityProviderCache identityProviderCache;
  
  public Double botDetectionScore;
  
  @ManagedSessionCookie(name = "federated.csrf", encrypt = false)
  public Cookie federatedCSRF;
  
  @FTLVariable
  public String federatedCSRFToken;
  
  public boolean hasDomainBasedIdentityProviders;
  
  public Map<String, List<BaseIdentityProvider<?>>> identityProviders;
  
  public boolean rememberDevice = true;
  
  public boolean showPasswordField;
  
  public boolean showWebAuthnReauthLink;
  
  @Inject
  public AuthorizeAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, IdentityProviderCache paramIdentityProviderCache, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.identityProviderCache = paramIdentityProviderCache;
  }
  
  @PostValidationMethod
  public void bootstrapUserSessionIfAvailable() {
    if (this.codeTenant.ssoConfiguration.allowAccessTokenBootstrap && this.frontEndSupport.isGET() && this.frontEndSupport.hasNoErrorMessages())
      ActionTools.extractBearerTokenFromAuthorizationHeader(this.frontEndSupport.request.getHeader("Authorization"))
        .ifPresent(paramString -> {
            OAuthService.BootstrapValidationResult bootstrapValidationResult = this.oauthService.validateBootstrapJWT(paramString, this.codeTenant);
            User user = bootstrapValidationResult.user;
            ZonedDateTime zonedDateTime = bootstrapValidationResult.originalAuthTime;
            AuthenticationType authenticationType = bootstrapValidationResult.authenticationType;
            if (user != null && this.ssoSession.user == null) {
              this.ssoService.login(this.ssoSession, this.ssoCookie, this.codeTenant, user, this.metaData, zonedDateTime, authenticationType, this.frontEndSupport.getTrustedClientIPAddress());
            } else if (user == null && this.ssoSession.user != null) {
              this.ssoService.logout(this.ssoSession, this.ssoCookie, this.codeTenant, this.frontEndSupport.buildEventInfo());
              this.ssoSession = this.ssoService.getSession(this.codeTenant, this.ssoCookie);
            } else if (user != null && !this.ssoSession.user.id.equals(user.id)) {
              this.ssoService.logout(this.ssoSession, this.ssoCookie, this.codeTenant, this.frontEndSupport.buildEventInfo());
              this.ssoService.login(this.ssoSession, this.ssoCookie, this.codeTenant, user, this.metaData, zonedDateTime, authenticationType, this.frontEndSupport.getTrustedClientIPAddress());
            } 
          }); 
  }
  
  public String get() {
    User user = this.ssoSession.user;
    boolean bool1 = (user == null || OAuthTools.forceLoginRequested(this.prompts, this.max_age, this.ssoSession)) ? true : false;
    boolean bool2 = false;
    UserRegistration userRegistration = (user != null && this.codeApplication != null) ? user.getRegistrationForApplication(this.codeApplication.id) : null;
    if (userRegistration == null && this.identityProviderId != null && this.codeApplication != null) {
      BaseIdentityProvider baseIdentityProvider = (BaseIdentityProvider)this.identityProviderCache.get(this.identityProviderId);
      if (baseIdentityProvider != null && baseIdentityProvider.applicationConfiguration != null) {
        BaseIdentityProviderApplicationConfiguration baseIdentityProviderApplicationConfiguration = (BaseIdentityProviderApplicationConfiguration)baseIdentityProvider.applicationConfiguration.get(this.codeApplication.id);
        bool2 = (baseIdentityProviderApplicationConfiguration != null && baseIdentityProviderApplicationConfiguration.createRegistration) ? true : false;
      } 
    } 
    if (!bool1 && !bool2) {
      String str = handleTwoFactorDuringSSO(user, AuthenticationType.PING);
      if (str != null)
        return str; 
      this.oauth_context = new BaseOAuthAction.OAuthContext();
      this.oauth_context.addAuthenticationType(AuthenticationType.PING);
      return (handlePostAuthenticationRedirect(user, getRememberDeviceState())).step.getResultCode();
    } 
    if (this.prompts.contains("none")) {
      buildAuthorizedRedirectWithError((new OAuthService.OAuthValidationResult()).withError(new OAuthError(OAuthError.OAuthErrorType.login_required, OAuthError.OAuthErrorReason.authentication_required, "The user is required to complete authentication.")));
      return "authorized-redirect-with-error";
    } 
    if (this.pendingIdPLinkId != null) {
      this.showPasswordField = true;
      this.hasDomainBasedIdentityProviders = false;
      this.identityProviders.clear();
      addSessionExpiredMessage();
      return "input";
    } 
    if (this.identityProviderId != null)
      return buildRedirectToRedirect(); 
    if (this.hasDomainBasedIdentityProviders && this.loginId != null) {
      String str = resolveIdentityProviderOrShowPasswordField();
      if (str != null)
        return str; 
    } 
    boolean bool = ReactorStatusValidator.isLicensedFor(this.frontEndSupport.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.webAuthn);
    if (bool) {
      String str = handleWebAuthnReauth();
      if (str != null)
        return str; 
      this




        
        .showWebAuthnReauthLink = (this.codeTenant.webAuthnConfiguration.enabled && (this.codeApplication.webAuthnConfiguration.enabled ? this.codeApplication.webAuthnConfiguration.reauthenticationWorkflow.enabled : this.codeTenant.webAuthnConfiguration.reauthenticationWorkflow.enabled) && this.ssoService.getWebAuthnReAuthenticationCredentials(this.frontEndSupport.request.getCookies(), this.codeTenant).stream().anyMatch(paramWebAuthnReAuthenticationCredential -> (paramWebAuthnReAuthenticationCredential.credentialId != null)));
    } 
    addSessionExpiredMessage();
    return "input";
  }
  
  @PostParameterMethod
  public void initialize() {
    persistRememberDeviceChoice(this.rememberDevice, this.frontEndSupport.isPOST());
    UUID uUID = StringTools.parseUUID(this.client_id);
    this


      
      .identityProviders = (Map<String, List<BaseIdentityProvider<?>>>)this.identityProviderCache.getByApplicationId((this.codeTenant != null) ? this.codeTenant.id : null, uUID).stream().filter(paramBaseIdentityProvider -> paramBaseIdentityProvider.isEnabledForApplicationId(paramUUID)).filter(paramBaseIdentityProvider -> !isDomainBasedOrPasswordless(paramBaseIdentityProvider)).collect(Collectors.groupingBy(paramBaseIdentityProvider -> paramBaseIdentityProvider.getType().toString()));
    this.federatedCSRFToken = SecurityTools.secureRandom(12);
    boolean bool = this.identityProviderCache.getByApplicationId((this.codeTenant != null) ? this.codeTenant.id : null, uUID).stream().anyMatch(paramBaseIdentityProvider -> paramBaseIdentityProvider.isEnabledForApplicationId(paramUUID));
    boolean bool1 = (this.identityProviderId != null && this.connectionTestId != null) ? true : false;
    if (bool || bool1)
      this.federatedCSRF.value = this.federatedCSRFToken; 
    this.idpRedirectState = this.oauthService.encodeStateForRedirect(paramQueryStringBuilder -> addBaseParameters(paramQueryStringBuilder).with("csrf", this.federatedCSRFToken));
    if (this.showPasswordField && this.loginId == null)
      return; 
    this

      
      .hasDomainBasedIdentityProviders = this.identityProviderCache.getByApplicationId((this.codeTenant != null) ? this.codeTenant.id : null, uUID).stream().filter(paramBaseIdentityProvider -> paramBaseIdentityProvider.isEnabledForApplicationId(paramUUID)).anyMatch(this::isDomainBasedOrPasswordless);
    if (this.showPasswordField && this.hasDomainBasedIdentityProviders && this.loginId != null)
      return; 
    this.showPasswordField = !this.hasDomainBasedIdentityProviders;
  }
  
  public String post() {
    if (!this.showPasswordField && this.hasDomainBasedIdentityProviders && this.loginId != null) {
      String str = resolveIdentityProviderOrShowPasswordField();
      return (str != null) ? str : "input";
    } 
    LoginRequest loginRequest = (new LoginRequest(this.frontEndSupport.buildEventInfo(this.metaData), this.codeApplication.id, this.loginId, this.password)).with(paramLoginRequest -> paramLoginRequest.botDetectionScore = normalizeBotDetectionScore()).with(paramLoginRequest -> paramLoginRequest.loginIdTypes = StandardLoginIdTypes);
    return callLogin(loginRequest, this.rememberDevice ? SSOService.RememberDeviceState.Remember : SSOService.RememberDeviceState.Forget, AuthenticationType.PASSWORD, false);
  }
  
  @PreRenderMethod({ThemedForward.class})
  public void prePageRenderSetup() {
    this.rememberDevice = getRememberDeviceCookieValue(this.rememberDevice, this.frontEndSupport.isGET());
    this.showCaptcha = (this.showCaptcha || (this.showPasswordField && showCaptchaOnInitialPageRender(null)));
  }
  
  public void setIdp_hint(String paramString) {
    this.identityProviderId = StringTools.parseUUID(paramString);
  }
  
  @ValidationMethod(httpMethods = {"POST", "GET"})
  public void validateOAuth() {
    String str = validateAndHandleErrors(true);
    if (str != null)
      throw new ErrorException(str, false); 
    if (this.devicePendingIdPLink != null && this.identityProviders != null)
      this.identityProviders.remove(this.devicePendingIdPLink.identityProviderType.toString()); 
    if (this.frontEndSupport.isPOST() && this.showPasswordField)
      validateCaptchaAfterAttemptingToResolveUser(this.captcha_token, this.loginId, null, () -> this.showCaptcha = true); 
  }
  
  protected String buildRedirectToRedirect() {
    this


















      
      .redirectToRedirectURI = QueryStringBuilder.builder("/oauth2/redirect").with("client_id", this.client_id).with("connectionTestId", this.connectionTestId).with("identityProviderId", this.identityProviderId).with("loginId", this.loginId).with("max_age", this.max_age).with("prompt", this.prompt).with("state", Base64.getUrlEncoder().withoutPadding().encodeToString(baseQueryBuilder((String)null).with("connectionTestId", this.connectionTestId).with("identityProviderId", this.identityProviderId).with("csrf", this.federatedCSRFToken).with("bypassedAuthorize", Boolean.valueOf(true)).with("rememberDevice", Boolean.valueOf(this.rememberDevice)).build().getBytes(StandardCharsets.UTF_8))).with("tenantId", this.codeTenant.id).build();
    return "redirect-to-redirect";
  }
  
  protected void buildRedirectToWait(String paramString, UUID paramUUID) {
    this

      
      .redirectToWait = baseQueryBuilder("/oauth2/wait").with("code", paramString).with("identityProviderId", paramUUID).build();
  }
  
  private void addSessionExpiredMessage() {
    if (this.ssoSession.loggedOut)
      addGeneralInfo("[SSOSessionDeletedOrExpired]", new Object[0]); 
  }
  
  private Double normalizeBotDetectionScore() {
    boolean bool = (new Validator()).validate(paramValidator -> BotDetectionScoreValidator.validate(paramValidator, this.botDetectionScore, "botDetectionScore")).done().empty();
    if (bool)
      return this.botDetectionScore; 
    EventLogHelper.create(new EventLog(EventLogType.Debug, "Invalid botDetectionScore value [%s] received during hosted login attempt. Treating botDetectionScore as [1.0]."
          .formatted(new Object[] { this.botDetectionScore })));
    return Double.valueOf(1.0D);
  }
  
  private String resolveIdentityProviderOrShowPasswordField() {
    BaseIdentityProvider<?> baseIdentityProvider = this.identityProviderCache.lookup(this.codeTenant.id, this.loginId);
    if (baseIdentityProvider != null && baseIdentityProvider.isEnabledForApplicationId(this.codeApplication.id)) {
      IdentityProviderFrontendService identityProviderFrontendService = this.frontEndSupport.identityProviderFrontendServices.get(baseIdentityProvider.getType());
      if (identityProviderFrontendService != null) {
        UUID uUID = baseIdentityProvider.id;
        this.federatedCSRF.value = SecurityTools.secureRandom(12);
        this.state = this.oauthService.encodeStateForRedirect(paramQueryStringBuilder -> addBaseParameters(paramQueryStringBuilder).with("bypassedAuthorize", Boolean.valueOf(true)).with("rememberDevice", Boolean.valueOf(this.rememberDevice)).with("csrf", this.federatedCSRF.value).with("identityProviderId", paramUUID));
        BaseIdentityProvider<?> baseIdentityProvider1 = baseIdentityProvider;
        this.redirectToExternalIdPURI = identityProviderFrontendService.buildRedirectURI(this.codeTenant, (new IdentityProviderFrontendService.FrontendRequestContext()).with(paramFrontendRequestContext -> paramFrontendRequestContext.client_id = this.client_id)
            .with(paramFrontendRequestContext -> paramFrontendRequestContext.fusionAuthURI = this.frontEndSupport.getFusionAuthBaseURL())
            .with(paramFrontendRequestContext -> paramFrontendRequestContext.identityProvider = paramBaseIdentityProvider)
            .with(paramFrontendRequestContext -> paramFrontendRequestContext.loginId = this.loginId)
            .with(paramFrontendRequestContext -> paramFrontendRequestContext.prompt = OAuthTools.convertMaxAgeToPrompt(this.prompt, this.max_age, this.ssoSession))
            .with(paramFrontendRequestContext -> paramFrontendRequestContext.state = this.state));
        return "redirect-to-idp";
      } 
    } else {
      baseIdentityProvider = this.identityProviderCache.lookupPasswordlessProvider(this.codeApplication.id, this.loginId);
      if (baseIdentityProvider != null) {
        IdentityProviderStartLoginRequest identityProviderStartLoginRequest = new IdentityProviderStartLoginRequest(this.codeApplication.id, baseIdentityProvider.id, this.loginId, this.frontEndSupport.getTrustedClientIPAddress());
        ClientResponse<IdentityProviderStartLoginResponse, Errors> clientResponse = this.client.startIdentityProviderLogin(identityProviderStartLoginRequest);
        if (clientResponse.wasSuccessful()) {
          buildRedirectToWait(((IdentityProviderStartLoginResponse)clientResponse.successResponse).code, baseIdentityProvider.id);
          return "redirect-to-wait";
        } 
      } 
    } 
    this.showPasswordField = true;
    return null;
  }
}
