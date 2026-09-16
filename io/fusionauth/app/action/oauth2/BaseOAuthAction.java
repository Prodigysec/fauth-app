package io.fusionauth.app.action.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inversoft.error.Error;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.CollectionTools;
import com.inversoft.util.StringTools;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.api.RequestContext;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.user.IdentityHelper;
import io.fusionauth.api.util.OAuthTools;
import io.fusionauth.api.util.URITools;
import io.fusionauth.app.action.BaseThemedAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.PostAuthenticationStep;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.primeframework.ThemedForward.List;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.ChangePasswordReason;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.UnverifiedBehavior;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.VerificationStrategy;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.api.LoginRequest;
import io.fusionauth.domain.api.LoginResponse;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.WebAuthnLoginRequest;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLinkResponse;
import io.fusionauth.domain.api.identityProvider.IdentityProviderPendingLinkResponse;
import io.fusionauth.domain.api.twoFactor.TwoFactorSendRequest;
import io.fusionauth.domain.api.twoFactor.TwoFactorStartRequest;
import io.fusionauth.domain.api.twoFactor.TwoFactorStartResponse;
import io.fusionauth.domain.api.twoFactor.TwoFactorStatusRequest;
import io.fusionauth.domain.api.twoFactor.TwoFactorStatusResponse;
import io.fusionauth.domain.api.user.RegistrationResponse;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.oauth2.AccessToken;
import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.domain.oauth2.OAuthApplicationRelationship;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.domain.oauth2.OAuthScopeConsentMode;
import io.fusionauth.domain.oauth2.TokenType;
import io.fusionauth.domain.oauth2.UserState;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.DomainBasedIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderTenantConfiguration;
import io.fusionauth.domain.provider.PendingIdPLink;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.domain.webauthn.WebAuthnWorkflow;
import io.fusionauth.http.Cookie;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.List;
import org.primeframework.mvc.http.HTTPTools;
import org.primeframework.mvc.parameter.annotation.UnknownParameters;
import org.primeframework.mvc.scope.annotation.ManagedCookie;
import org.primeframework.mvc.util.QueryStringBuilder;

@List({@Status(code = "change-password-status")})
@List({@ThemedForward(code = "post-to-idp", page = "/oauth2/post-to-idp.ftl"), @ThemedForward(code = "post-to-reply", page = "/oauth2/post.ftl")})
@List({@Redirect(code = "authorized-redirect", uri = "${authorizedRedirectURI}"), @Redirect(code = "authorized-redirect-with-error", uri = "${authorizedRedirectURIWithError}"), @Redirect(code = "redirect-to-authorize", uri = "${redirectToAuthorizeURI}"), @Redirect(code = "redirect-to-authorized-not-registered", uri = "${redirectToAuthorizedNotRegisteredURI}"), @Redirect(code = "redirect-to-change-password", uri = "${redirectToChangePassword}"), @Redirect(code = "redirect-to-complete-registration", uri = "${redirectToRegisterURI}"), @Redirect(code = "redirect-to-consent", uri = "${redirectToConsentURI}"), @Redirect(code = "redirect-to-email-verification-required", uri = "${redirectToEmailVerificationRequiredURI}"), @Redirect(code = "redirect-to-phone-verification-required", uri = "${redirectToPhoneVerificationRequiredURI}"), @Redirect(code = "redirect-to-registration-verification-required", uri = "${redirectToRegistrationVerificationRequiredURI}"), @Redirect(code = "redirect-to-start-idp-link", uri = "${redirectToStartIdPLinkURI}"), @Redirect(code = "redirect-to-idp", uri = "${redirectToExternalIdPURI}"), @Redirect(code = "redirect-to-redirect", uri = "${redirectToRedirectURI}"), @Redirect(code = "redirect-to-two-factor", uri = "${redirectToTwoFactorURI}"), @Redirect(code = "redirect-to-two-factor-enable", uri = "${redirectToTwoFactorEnableURI}"), @Redirect(code = "redirect-to-two-factor-enable-complete", uri = "${redirectToTwoFactorEnableCompleteURI}"), @Redirect(code = "redirect-to-two-factor-methods", uri = "${redirectToTwoFactorMethodsURI}"), @Redirect(code = "redirect-to-webauthn", uri = "${redirectToWebAuthnURI}"), @Redirect(code = "redirect-to-webauthn-reauth", uri = "${redirectToWebAuthnReauthURI}"), @Redirect(code = "redirect-to-webauthn-reauth-enable", uri = "${redirectToWebAuthnReauthEnableURI}"), @Redirect(code = "redirect-to-logout", uri = "${redirectToLogoutURI}")})
public abstract class BaseOAuthAction extends BaseThemedAction {
  public static final String USER_SESSION_KEY = "fusionauth.oauth-user";
  
  protected final OAuthService oauthService;
  
  protected final Set<String> prompts = new LinkedHashSet<>();
  
  public String authorizedRedirectURI;
  
  public String authorizedRedirectURIWithError;
  
  @FTLVariable
  public boolean bootstrapWebauthnEnabled;
  
  public boolean cancelPendingIdpLink;
  
  public String captcha_token;
  
  public String client_secret;
  
  public String code_challenge;
  
  public String code_challenge_method;
  
  public String connectionTestId;
  
  @FTLVariable
  public PendingIdPLink devicePendingIdPLink;
  
  public String dpop_jkt;
  
  @FTLVariable
  public FormPostResponse formPostResponse;
  
  public UUID identityProviderId;
  
  @FTLVariable
  public String idpRedirectState;
  
  public String loginId;
  
  public Long max_age;
  
  public RefreshToken.MetaData metaData = new RefreshToken.MetaData();
  
  public String nonce;
  
  public String password;
  
  public boolean passwordlessEnabled;
  
  @FTLVariable
  public PendingIdPLink pendingIdPLink;
  
  public String pendingIdPLinkId;
  
  public String prompt;
  
  public String redirectToAuthorizeURI;
  
  public String redirectToAuthorizedNotRegisteredURI;
  
  public String redirectToChangePassword;
  
  public String redirectToConsentURI;
  
  public String redirectToEmailVerificationRequiredURI;
  
  public String redirectToExternalIdPURI;
  
  public String redirectToForgotPasswordURI;
  
  public String redirectToLogoutURI;
  
  public String redirectToPhoneVerificationRequiredURI;
  
  public String redirectToRedirectURI;
  
  public String redirectToRegisterURI;
  
  public String redirectToRegistrationVerificationRequiredURI;
  
  public String redirectToStartIdPLinkURI;
  
  public String redirectToTwoFactorEnableCompleteURI;
  
  public String redirectToTwoFactorEnableURI;
  
  public String redirectToTwoFactorMethodsURI;
  
  public String redirectToTwoFactorURI;
  
  public String redirectToTwoFactorWorkflow;
  
  public String redirectToWait;
  
  public String redirectToWebAuthnReauthEnableURI;
  
  public String redirectToWebAuthnReauthURI;
  
  public String redirectToWebAuthnURI;
  
  public String redirectToWorkflow;
  
  public URI redirect_uri;
  
  @ManagedCookie(name = "fusionauth.remember-device")
  public Cookie rememberDeviceCookie;
  
  public List<URI> resource = new ArrayList<>();
  
  public String response_mode;
  
  public String response_type;
  
  public String scope;
  
  public Set<String> scopes;
  
  public boolean showCaptcha;
  
  @FTLVariable
  public boolean skipWebAuthnReauth;
  
  public String state;
  
  public String timezone;
  
  public String twoFactorId;
  
  public String type;
  
  @UnknownParameters
  public Map<String, Object> unknownParameters = new HashMap<>();
  
  public UserState userState;
  
  public boolean userVerifyingPlatformAuthenticatorAvailable;
  
  public String user_code;
  
  public List<URI> validatedResources = new ArrayList<>();
  
  protected OAuthContext oauth_context;
  
  protected BaseOAuthAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService);
    this.oauthService = paramOAuthService;
    RequestContext.set(new RequestContext(paramFrontEndSupport.request.getPath()));
  }
  
  public String getLogin_hint() {
    return this.loginId;
  }
  
  public void setLogin_hint(String paramString) {
    this.loginId = paramString;
  }
  
  public String getOauth_context() {
    if (this.oauth_context == null || this.oauth_context.isEmpty())
      return null; 
    return this.frontEndSupport.serializeOAuthContext(this.oauth_context);
  }
  
  public void setOauth_context(String paramString) {
    if (StringTools.isTrimmedEmpty(paramString))
      return; 
    this.oauth_context = this.frontEndSupport.deserializeOAuthContext(paramString);
  }
  
  public boolean isBootStrapWebauthnEnabled() {
    return this.bootstrapWebauthnEnabled;
  }
  
  public void setUsername(String paramString) {
    this.loginId = paramString;
  }
  
  protected QueryStringBuilder addBaseParameters(QueryStringBuilder paramQueryStringBuilder) {
    String str = (!"UNKNOWN".equals(this.metaData.device.type) || this.frontEndSupport.request.getParameter("metaData.device.type") != null) ? this.metaData.device.type : null;
    paramQueryStringBuilder.with("client_id", this.client_id)
      .with("code_challenge", this.code_challenge)
      .with("code_challenge_method", this.code_challenge_method)
      .with("dpop_jkt", this.dpop_jkt)
      .with("metaData.device.name", this.metaData.device.name)
      
      .with("metaData.device.type", str)
      .with("nonce", this.nonce)
      .with("oauth_context", getOauth_context())
      .with("prompt", this.prompt)
      .with("pendingIdPLinkId", this.pendingIdPLinkId)
      .with("redirect_uri", this.redirect_uri)
      .with("response_mode", this.response_mode)
      .with("response_type", this.response_type)
      .with("scope", this.scope)
      .with("state", this.state)
      .with("tenantId", this.codeTenant.id)
      .with("timezone", this.timezone)
      .with("user_code", this.user_code);
    this.resource.forEach(paramURI -> paramQueryStringBuilder.with("resource", paramURI));
    return paramQueryStringBuilder;
  }
  
  protected QueryStringBuilder baseQueryBuilder(String paramString) {
    QueryStringBuilder queryStringBuilder = QueryStringBuilder.builder(paramString);
    return addBaseParameters(queryStringBuilder);
  }
  
  protected void buildAuthorizedRedirectForAuthorizationGrant(String paramString) {
    QueryStringBuilder queryStringBuilder = QueryStringBuilder.builder(this.redirect_uri.toString()).with("code", paramString).with("iss", this.codeTenant.issuer).with("locale", this.locale).with("state", this.state).with("userState", this.userState).ifTrue((this.oauth_context != null && this.oauth_context.passwordChanged), paramQueryStringBuilder -> paramQueryStringBuilder.with("passwordChanged", Boolean.valueOf(true)));
    if (this.redirect_uri.toString().equals("/app/callback"))
      queryStringBuilder.with("client_id", this.client_id)
        .with("tenantId", this.tenantId); 
    this.authorizedRedirectURI = queryStringBuilder.build();
  }
  
  protected void buildAuthorizedRedirectForImplicitGrant(AccessToken paramAccessToken, String paramString) {
    this













      
      .authorizedRedirectURI = QueryStringBuilder.builder(this.redirect_uri.toString()).beginFragment().with("access_token", paramAccessToken.token).with("expires_in", (paramAccessToken.token != null) ? paramAccessToken.expiresIn : null).with("iss", this.codeTenant.issuer).with("locale", this.locale).with("scope", paramString).with("state", this.state).with("token_type", (paramAccessToken.token != null) ? TokenType.Bearer : null).with("userState", this.userState).with("id_token", paramAccessToken.idToken).ifTrue((this.oauth_context != null && this.oauth_context.passwordChanged), paramQueryStringBuilder -> paramQueryStringBuilder.with("passwordChanged", Boolean.valueOf(true))).build();
  }
  
  protected void buildAuthorizedRedirectWithError(OAuthService.OAuthValidationResult paramOAuthValidationResult) {
    QueryStringBuilder queryStringBuilder = QueryStringBuilder.builder(this.redirect_uri.toString());
    boolean bool = (paramOAuthValidationResult.grantType == GrantType.implicit) ? true : false;
    this




      
      .authorizedRedirectURIWithError = (bool ? queryStringBuilder.beginFragment() : queryStringBuilder.beginQuery()).with("error", paramOAuthValidationResult.error.error).with("error_reason", paramOAuthValidationResult.error.reason).with("error_description", paramOAuthValidationResult.error.description).with("iss", this.codeTenant.issuer).with("state", this.state).build();
  }
  
  protected void buildCompleteRegistrationRedirectURI() {
    this


      
      .redirectToRegisterURI = baseQueryBuilder("/oauth2/complete-registration").with("identityProviderId", this.identityProviderId).with("userVerifyingPlatformAuthenticatorAvailable", this.userVerifyingPlatformAuthenticatorAvailable ? Boolean.valueOf(true) : null).build();
  }
  
  protected void buildDevicePendingIdPLink(String paramString) {
    ExternalIdentifier externalIdentifier = this.frontEndSupport.getExternalId(this.codeTenant.id, paramString, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.DeviceUserCode });
    if (externalIdentifier != null && externalIdentifier.getAttribute("identityProviderId") != null) {
      this.devicePendingIdPLink = externalIdentifier.buildPendingIdpLink();
      if (this.ssoSession.user != null)
        updateWithLinkLimits(this.devicePendingIdPLink, this.ssoSession.user); 
    } 
  }
  
  protected void buildForgotPasswordRedirectURI() {
    this


      
      .redirectToForgotPasswordURI = baseQueryBuilder("/password/forgot").with("locale", this.frontEndSupport.request.getParameter("locale")).build();
  }
  
  protected void buildRedirectToAuthorizeURI() {
    this
      .redirectToAuthorizeURI = baseQueryBuilder("/oauth2/authorize").build();
  }
  
  protected void buildRedirectToAuthorizedNotRegisteredURI() {
    this
      .redirectToAuthorizedNotRegisteredURI = baseQueryBuilder("/oauth2/authorized-not-registered").build();
  }
  
  protected void buildRedirectToChangePasswordURI(String paramString, ChangePasswordReason paramChangePasswordReason) {
    if (paramChangePasswordReason != null)
      switch (paramChangePasswordReason) {
        case NotRegistered:
          addGeneralInfo("[PasswordChangeRequired]", new Object[0]);
          break;
        case CompleteRegistration:
          addGeneralInfo("[PasswordChangeReasonBreached]", new Object[0]);
          break;
        case RegistrationVerification:
          addGeneralInfo("[PasswordChangeReasonExpired]", new Object[0]);
          break;
        case EmailVerification:
          addGeneralInfo("[PasswordChangeReasonValidation]", new Object[0]);
          break;
      }  
    this
      
      .redirectToChangePassword = baseQueryBuilder("/password/change").withSegment(paramString).build();
  }
  
  protected void buildRedirectToEmailVerificationRequired(String paramString) {
    this

      
      .redirectToEmailVerificationRequiredURI = baseQueryBuilder("/email/verification-required").with("userState", this.userState).withSegment(paramString).build();
  }
  
  protected void buildRedirectToLogoutURI() {
    this
      .redirectToLogoutURI = baseQueryBuilder("/oauth2/logout").build();
  }
  
  protected void buildRedirectToLogoutURI(String paramString) {
    this
      
      .redirectToLogoutURI = baseQueryBuilder("/oauth2/logout").with("redirect_uri", paramString).build();
  }
  
  protected void buildRedirectToOAuthScopeConsentURI() {
    this
      
      .redirectToConsentURI = baseQueryBuilder("/oauth2/consent").with("identityProviderId", this.identityProviderId).build();
  }
  
  protected void buildRedirectToPhoneVerificationRequired(String paramString) {
    this

      
      .redirectToPhoneVerificationRequiredURI = baseQueryBuilder("/phone/verification-required").with("userState", this.userState).withSegment(paramString).build();
  }
  
  protected void buildRedirectToRegistrationVerificationRequired(String paramString) {
    this

      
      .redirectToRegistrationVerificationRequiredURI = baseQueryBuilder("/registration/verification-required").with("userState", this.userState).withSegment(paramString).build();
  }
  
  protected void buildRedirectToStartIdPLinkLink() {
    this
      .redirectToStartIdPLinkURI = baseQueryBuilder("/oauth2/start-idp-link").build();
  }
  
  protected void buildRedirectToTwoFactorEnableURI() {
    this
      
      .redirectToTwoFactorEnableURI = baseQueryBuilder("/oauth2/two-factor-enable").with("twoFactorId", this.twoFactorId).build();
  }
  
  protected void buildRedirectToTwoFactorMethodsURI() {
    this
      
      .redirectToTwoFactorMethodsURI = baseQueryBuilder("/oauth2/two-factor-methods").with("twoFactorId", this.twoFactorId).build();
  }
  
  protected void buildRedirectToTwoFactorURI(String paramString) {
    this

      
      .redirectToTwoFactorURI = baseQueryBuilder("/oauth2/two-factor").with("methodId", paramString).with("twoFactorId", this.twoFactorId).build();
  }
  
  protected void buildRedirectToWebAuthnReauthEnableURI() {
    this
      
      .redirectToWebAuthnReauthEnableURI = baseQueryBuilder("/oauth2/webauthn-reauth-enable").with("identityProviderId", this.identityProviderId).build();
  }
  
  protected void buildRedirectToWebAuthnURI() {
    this
      .redirectToWebAuthnURI = baseQueryBuilder("/oauth2/webauthn").build();
  }
  
  protected String callLogin(LoginRequest paramLoginRequest, SSOService.RememberDeviceState paramRememberDeviceState, AuthenticationType paramAuthenticationType, boolean paramBoolean) {
    paramLoginRequest.noJWT = true;
    paramLoginRequest.twoFactorTrustId = this.twoFactorTrustCookie.value;
    UUID uUID = (this.codeUserId != null) ? this.codeUserId : resolveUserId(paramLoginRequest);
    SSOService.NewDeviceResult newDeviceResult = handleNewDevice(paramLoginRequest, uUID);
    ClientResponse<LoginResponse, Errors> clientResponse = this.client.setTenantId(this.codeApplication.tenantId).login(paramLoginRequest);
    if (clientResponse.wasSuccessful() && ((LoginResponse)clientResponse.successResponse).user != null && newDeviceResult == null)
      newDeviceResult = this.ssoService.handleNewDevice(this.frontEndSupport.request.getCookies(), this.codeTenant, ((LoginResponse)clientResponse.successResponse).user.id); 
    BaseOAuthAction baseOAuthAction = this;
    return handleInteractiveLoginResponse(clientResponse, newDeviceResult, paramErrors -> paramBaseOAuthAction.transferErrors(paramErrors), paramRememberDeviceState, paramAuthenticationType, paramBoolean);
  }
  
  protected Map<String, Object> captureState(String paramString1, String paramString2) {
    if (this.client_id == null)
      return null; 
    Map<String, String> map = CollectionTools.mapNV(new Object[] { 
          "client_id", this.client_id, "code_challenge", this.code_challenge, "code_challenge_method", this.code_challenge_method, "metaData.device.name", this.metaData.device.name, "metaData.device.type", this.metaData.device.type, 
          "nonce", this.nonce, "pendingIdPLinkId", this.pendingIdPLinkId, "redirect_uri", this.redirect_uri, "resource", 






          
          this.resource.isEmpty() ? null : this.resource.stream().map(URI::toString).collect(Collectors.joining(" ")), "response_mode", this.response_mode, 
          "response_type", this.response_type, "scope", this.scope, "state", this.state, "tenantId", this.codeTenant.id, "timezone", this.timezone, 
          "user_code", this.user_code });
    if (paramString1 != null && paramString2 != null)
      map.put(paramString1, paramString2); 
    return (Map)map;
  }
  
  protected Map<String, Object> captureState() {
    return captureState((String)null, (String)null);
  }
  
  protected AccessToken createAccessTokenForImplicitGrant(User paramUser, AuthenticationType paramAuthenticationType) {
    UUID uUID = this.ssoSession.id;
    EventInfo eventInfo = this.frontEndSupport.buildEventInfo(null);
    return this.oauthService.createAccessToken(eventInfo, this.codeTenant, this.codeApplication, paramUser, this.client_id, this.redirect_uri, GrantType.implicit, this.response_type, this.scopes, this.nonce, this.frontEndSupport.getTrustedClientIPAddress(), this.identityProviderId, this.user_code, null, uUID, null, paramAuthenticationType, this.pendingIdPLinkId, null);
  }
  
  protected String createAuthorizationCode(User paramUser, UUID paramUUID, AuthenticationType paramAuthenticationType, ZonedDateTime paramZonedDateTime) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    if (this.dpop_jkt != null)
      hashMap.put("dpop_jkt", this.dpop_jkt); 
    if (this.identityProviderId != null)
      hashMap.put("identityProviderId", this.identityProviderId.toString()); 
    if (this.nonce != null)
      hashMap.put("nonce", this.nonce); 
    if (this.ssoSession.id != null)
      hashMap.put("sid", this.ssoSession.id.toString()); 
    if (this.user_code != null)
      hashMap.put("userCode", this.user_code); 
    if (paramAuthenticationType != null)
      hashMap.put("authenticationType", paramAuthenticationType.toString()); 
    if (this.pendingIdPLinkId != null)
      hashMap.put("pendingIdPLinkId", this.pendingIdPLinkId); 
    hashMap.put("authTime", "" + paramZonedDateTime.toEpochSecond());
    if (!this.validatedResources.isEmpty())
      hashMap.put("resource", this.validatedResources.stream().map(URI::toString).collect(Collectors.joining(" "))); 
    String str = this.scopes.isEmpty() ? null : String.join(" ", (Iterable)this.scopes);
    return this.oauthService.createAuthorizationCode(this.codeTenant, paramUUID, this.client_id, this.redirect_uri, paramUser.id, str, this.code_challenge, this.metaData
        .with(paramMetaData -> paramMetaData.device.lastAccessedAddress = this.frontEndSupport.getTrustedClientIPAddress()), (Map)hashMap);
  }
  
  protected void deleteLoginIntent() {
    if (this.codeUserId != null)
      this.loginIntentService.deleteByUserId(this.codeUserId); 
    this.loginIntentCookie.value = null;
  }
  
  @JsonIgnore
  protected LoginIntentService.LoginIntent getNextIntent(User paramUser, UserRegistration paramUserRegistration, PostAuthenticationStep paramPostAuthenticationStep) {
    LoginIntentService.LoginIntent loginIntent;
    this.userState = IdentityHelper.getUserStateFromUserAndRegistration(paramUser, paramUserRegistration);
    boolean bool1 = this.prompts.contains("none");
    boolean bool2 = OAuthTools.isCompleteRegistrationAllowed(this.codeApplication, paramUserRegistration);
    if (paramPostAuthenticationStep.isBefore(PostAuthenticationStep.CompleteRegistration) && bool2) {
      loginIntent = new LoginIntentService.LoginIntent(this.codeTenant.id, this.codeApplication.id, this.codeUser, PostAuthenticationStep.CompleteRegistration, null, null);
    } else {
      if (this.codeApplication.oauthConfiguration.requireRegistration && paramUserRegistration == null) {
        if (bool1) {
          buildAuthorizedRedirectWithError((new OAuthService.OAuthValidationResult()).withError(new OAuthError(OAuthError.OAuthErrorType.interaction_required, OAuthError.OAuthErrorReason.registration_required, "The user is required to be registered for this application in order to complete authentication.")));
          throw new ErrorException("authorized-redirect-with-error");
        } 
        addGeneralError("[UserAuthorizedNotRegisteredException]", new Object[0]);
        buildRedirectToAuthorizedNotRegisteredURI();
        return new LoginIntentService.LoginIntent(this.codeTenant.id, this.codeApplication.id, this.codeUser, PostAuthenticationStep.NotRegistered, null, null);
      } 
      if (this.codeApplication.verifyRegistration && this.codeApplication.unverified.behavior == UnverifiedBehavior.Gated && paramUserRegistration != null && !paramUserRegistration.verified) {
        if (bool1) {
          buildAuthorizedRedirectWithError((new OAuthService.OAuthValidationResult()).withError(new OAuthError(OAuthError.OAuthErrorType.interaction_required, OAuthError.OAuthErrorReason.registration_verification_required, "The user is required to verify their registration in order to complete authentication.")));
          throw new ErrorException("authorized-redirect-with-error");
        } 
        String str = null;
        if (this.codeApplication.verificationStrategy == VerificationStrategy.FormField) {
          ClientResponse<RegistrationResponse, Errors> clientResponse = this.client.setTenantId(paramUser.tenantId).retrieveRegistration(paramUser.id, this.codeApplication.id);
          str = ((RegistrationResponse)clientResponse.successResponse).registrationVerificationId;
        } 
        loginIntent = new LoginIntentService.LoginIntent(this.codeTenant.id, this.codeApplication.id, this.codeUser, PostAuthenticationStep.RegistrationVerification, null, str);
      } else if (this.codeTenant.emailConfiguration.verifyEmail && this.codeTenant.emailConfiguration.unverified.behavior == UnverifiedBehavior.Gated && (
        (Boolean)Optional.<UserIdentity>ofNullable(paramUser.resolvePrimaryIdentity(IdentityType.email))
        .map(UserIdentity::verificationRequired).orElse(Boolean.valueOf(false))).booleanValue()) {
        if (bool1) {
          buildAuthorizedRedirectWithError((new OAuthService.OAuthValidationResult()).withError(new OAuthError(OAuthError.OAuthErrorType.interaction_required, OAuthError.OAuthErrorReason.email_verification_required, "The user is required to verify their email address in order to complete authentication.")));
          throw new ErrorException("authorized-redirect-with-error");
        } 
        String str = null;
        if (this.codeTenant.emailConfiguration.verificationStrategy == VerificationStrategy.FormField) {
          ClientResponse<UserResponse, Errors> clientResponse = this.client.setTenantId(paramUser.tenantId).retrieveUser(paramUser.id);
          str = ((UserResponse)clientResponse.successResponse).emailVerificationId;
        } 
        loginIntent = new LoginIntentService.LoginIntent(this.codeTenant.id, this.codeApplication.id, this.codeUser, PostAuthenticationStep.EmailVerification, null, str);
      } else if (this.codeTenant.phoneConfiguration.verifyPhoneNumber && this.codeTenant.phoneConfiguration.unverified.behavior == UnverifiedBehavior.Gated && (
        (Boolean)Optional.<UserIdentity>ofNullable(paramUser.resolvePrimaryIdentity(IdentityType.phoneNumber))
        .map(UserIdentity::verificationRequired).orElse(Boolean.valueOf(false))).booleanValue()) {
        if (bool1) {
          buildAuthorizedRedirectWithError((new OAuthService.OAuthValidationResult()).withError(new OAuthError(OAuthError.OAuthErrorType.interaction_required, OAuthError.OAuthErrorReason.phone_verification_required, "The user is required to verify their phone number in order to complete authentication.")));
          throw new ErrorException("authorized-redirect-with-error");
        } 
        String str = null;
        if (this.codeTenant.phoneConfiguration.verificationStrategy == VerificationStrategy.FormField) {
          ClientResponse<UserResponse, Errors> clientResponse = this.client.setTenantId(paramUser.tenantId).retrieveUser(paramUser.id);
          List<UserResponse.VerificationId> list = ((UserResponse)clientResponse.successResponse).verificationIds;
          if (list != null)
            str = list.stream().filter(paramVerificationId -> paramVerificationId.type.is(IdentityType.phoneNumber)).findFirst().map(paramVerificationId -> paramVerificationId.id).orElse(null); 
        } 
        loginIntent = new LoginIntentService.LoginIntent(this.codeTenant.id, this.codeApplication.id, this.codeUser, PostAuthenticationStep.PhoneVerification, null, str);
      } else if (paramPostAuthenticationStep.isBefore(PostAuthenticationStep.WebAuthnReauthEnable) && redirectToWebAuthnReauthEnable(paramUser.id) && !bool1) {
        loginIntent = new LoginIntentService.LoginIntent(this.codeTenant.id, this.codeApplication.id, this.codeUser, PostAuthenticationStep.WebAuthnReauthEnable, null, null);
      } else {
        loginIntent = new LoginIntentService.LoginIntent(this.codeTenant.id, this.codeApplication.id, this.codeUser, PostAuthenticationStep.Consent, null, null);
      } 
    } 
    buildRedirectForIntent(loginIntent);
    return loginIntent;
  }
  
  @JsonIgnore
  protected boolean getRememberDeviceCookieValue(boolean paramBoolean1, boolean paramBoolean2) {
    return (paramBoolean2 && this.rememberDeviceCookie.getValue() != null) ? 
      Boolean.parseBoolean(this.rememberDeviceCookie.getValue()) : 
      paramBoolean1;
  }
  
  @JsonIgnore
  protected SSOService.RememberDeviceState getRememberDeviceState() {
    if (this.rememberDeviceCookie.getValue() == null)
      return SSOService.RememberDeviceState.NoChange; 
    return Boolean.parseBoolean(this.rememberDeviceCookie.getValue()) ? SSOService.RememberDeviceState.Remember : SSOService.RememberDeviceState.Forget;
  }
  
  protected void handleDisableSend(String paramString, UUID paramUUID) {
    handleSendResult(this.client.sendTwoFactorCodeForEnableDisable((new TwoFactorSendRequest())
          .with(paramTwoFactorSendRequest -> paramTwoFactorSendRequest.applicationId = (this.codeApplication != null) ? this.codeApplication.id : null)
          .with(paramTwoFactorSendRequest -> paramTwoFactorSendRequest.methodId = paramString)
          .with(paramTwoFactorSendRequest -> paramTwoFactorSendRequest.userId = paramUUID)));
  }
  
  protected void handleEnableSend(String paramString1, String paramString2, String paramString3, MessageType paramMessageType, UUID paramUUID) {
    handleSendResult(this.client.sendTwoFactorCodeForEnableDisable((new TwoFactorSendRequest())
          .with(paramTwoFactorSendRequest -> paramTwoFactorSendRequest.applicationId = (this.codeApplication != null) ? this.codeApplication.id : null)
          .with(paramTwoFactorSendRequest -> paramTwoFactorSendRequest.email = paramString)
          .with(paramTwoFactorSendRequest -> paramTwoFactorSendRequest.messageType = paramMessageType)
          .with(paramTwoFactorSendRequest -> paramTwoFactorSendRequest.method = paramString)
          .with(paramTwoFactorSendRequest -> paramTwoFactorSendRequest.mobilePhone = paramString)
          .with(paramTwoFactorSendRequest -> paramTwoFactorSendRequest.userId = paramUUID)));
  }
  
  protected String handleInteractiveLoginResponse(ClientResponse<LoginResponse, Errors> paramClientResponse, SSOService.NewDeviceResult paramNewDeviceResult, Consumer<Errors> paramConsumer, SSOService.RememberDeviceState paramRememberDeviceState, AuthenticationType paramAuthenticationType, boolean paramBoolean) {
    if (paramClientResponse.wasSuccessful() && paramNewDeviceResult != null && paramNewDeviceResult.isNewDevice())
      this.frontEndSupport.addHttpOnlyPersistentCookie(paramNewDeviceResult.cookieName, paramNewDeviceResult.cookieValue); 
    if (paramClientResponse.wasSuccessful() && ((LoginResponse)paramClientResponse.successResponse).threatsDetected != null && !((LoginResponse)paramClientResponse.successResponse).threatsDetected.isEmpty())
      this.frontEndSupport.deleteCookiesWithPrefix("fusionauth.trusted-device"); 
    if (this.oauth_context == null)
      this.oauth_context = new OAuthContext(); 
    if (paramAuthenticationType == null)
      paramAuthenticationType = AuthenticationType.PASSWORD; 
    this.oauth_context.addAuthenticationType(paramAuthenticationType);
    this.oauth_context.passwordChanged = paramBoolean;
    if (paramClientResponse.status == 200 || paramClientResponse.status == 202 || paramClientResponse.status == 212 || paramClientResponse.status == 213) {
      User user = ((LoginResponse)paramClientResponse.successResponse).user;
      String str = validatePendingLinkMaxCount(user.id);
      if (str != null)
        return str; 
      LoginIntentService.LoginIntent loginIntent = getNextIntent(user, user.getRegistrationForApplication(this.codeApplication.id), PostAuthenticationStep.Authentication);
      if (loginIntent.step != PostAuthenticationStep.NotRegistered)
        this.loginIntentCookie.value = this.loginIntentService.buildLoginIntent(this.codeTenant, this.codeApplication.id, user.id, loginIntent.step, paramRememberDeviceState, loginIntent.verificationId); 
      return loginIntent.step.getResultCode();
    } 
    if (paramClientResponse.status == 232) {
      this.pendingIdPLinkId = ((LoginResponse)paramClientResponse.successResponse).pendingIdPLinkId;
      buildRedirectToStartIdPLinkLink();
      return "redirect-to-start-idp-link";
    } 
    if (paramClientResponse.status == 242) {
      this.twoFactorId = ((LoginResponse)paramClientResponse.successResponse).twoFactorId;
      if (((LoginResponse)paramClientResponse.successResponse).methods == null || ((LoginResponse)paramClientResponse.successResponse).methods.isEmpty()) {
        buildRedirectToTwoFactorEnableURI();
        addGeneralInfo("[TwoFactorRequired]", new Object[0]);
        return "redirect-to-two-factor-enable";
      } 
      buildRedirectToTwoFactorMethodsURI();
      return "redirect-to-two-factor-methods";
    } 
    if (paramClientResponse.status == 203) {
      buildRedirectToChangePasswordURI(((LoginResponse)paramClientResponse.successResponse).changePasswordId, ((LoginResponse)paramClientResponse.successResponse).changePasswordReason);
      return "redirect-to-change-password";
    } 
    if (paramClientResponse.status == 401) {
      if (paramClientResponse.errorResponse == null) {
        addGeneralError("[InvalidLogin]", new Object[0]);
      } else {
        ((Errors)paramClientResponse.errorResponse).generalErrors.forEach(paramError -> addGeneralError(paramError.code, new Object[] { paramError.message }));
      } 
    } else if (paramClientResponse.status == 404) {
      addGeneralError("[InvalidLogin]", new Object[0]);
    } else if (paramClientResponse.status == 409) {
      addGeneralError("[LoginPreventedException]", new Object[0]);
    } else if (paramClientResponse.status == 410) {
      addGeneralError("[UserExpiredException]", new Object[0]);
    } else if (paramClientResponse.status == 423) {
      addGeneralError("[UserLockedException]", new Object[0]);
    } else if (paramClientResponse.exception != null) {
      addGeneralError("[APIError]", new Object[0]);
    } else {
      paramConsumer.accept((Errors)paramClientResponse.errorResponse);
    } 
    this.oauth_context.clearAuthenticationTypes();
    return "input";
  }
  
  protected String handleLoginForWebAuthn(WebAuthnLoginRequest paramWebAuthnLoginRequest, UUID paramUUID, boolean paramBoolean) {
    paramWebAuthnLoginRequest.noJWT = true;
    paramWebAuthnLoginRequest.eventInfo = this.frontEndSupport.buildEventInfo(this.metaData);
    paramWebAuthnLoginRequest.twoFactorTrustId = this.twoFactorTrustCookie.value;
    SSOService.NewDeviceResult newDeviceResult = handleNewDevice(paramWebAuthnLoginRequest, paramUUID);
    ClientResponse<LoginResponse, Errors> clientResponse = this.client.completeWebAuthnLogin(paramWebAuthnLoginRequest);
    BaseOAuthAction baseOAuthAction = this;
    return handleInteractiveLoginResponse(clientResponse, newDeviceResult, paramErrors -> paramBaseOAuthAction.transferErrors(paramErrors), paramBoolean ? SSOService.RememberDeviceState.Remember : SSOService.RememberDeviceState.Forget, AuthenticationType.WebAuthn, false);
  }
  
  protected LoginIntentService.LoginIntent handlePostAuthenticationRedirect(User paramUser, SSOService.RememberDeviceState paramRememberDeviceState) {
    UserRegistration userRegistration = paramUser.getRegistrationForApplication(this.codeApplication.id);
    LoginIntentService.LoginIntent loginIntent = getNextIntent(paramUser, userRegistration, PostAuthenticationStep.Authentication);
    if (loginIntent.step != PostAuthenticationStep.NotRegistered)
      this.loginIntentCookie.value = this.loginIntentService.buildLoginIntent(this.codeTenant, this.codeApplication.id, paramUser.id, loginIntent.step, paramRememberDeviceState, loginIntent.verificationId); 
    return loginIntent;
  }
  
  protected String handleRedirectToExpectedStep(LoginIntentService.LoginIntent paramLoginIntent) {
    this.userState = IdentityHelper.getUserStateFromUserAndRegistration(paramLoginIntent.user, paramLoginIntent.user.getRegistrationForApplication(this.codeApplication.id));
    if (paramLoginIntent.step == null) {
      deleteLoginIntent();
      buildRedirectToAuthorizeURI();
      return "redirect-to-authorize";
    } 
    buildRedirectForIntent(paramLoginIntent);
    return paramLoginIntent.step.getResultCode();
  }
  
  protected String handleTwoFactorDuringSSO(User paramUser, AuthenticationType paramAuthenticationType) {
    boolean bool = ((Boolean)Optional.<AuthenticationType>ofNullable(this.ssoSession.getAuthenticationType()).map(AuthenticationType::isFederated).orElse(Boolean.valueOf(false))).booleanValue();
    if (bool)
      return null; 
    TwoFactorStatusRequest twoFactorStatusRequest = new TwoFactorStatusRequest(paramUser.id);
    twoFactorStatusRequest.applicationId = this.codeApplication.id;
    twoFactorStatusRequest.twoFactorTrustId = this.twoFactorTrustCookie.value;
    twoFactorStatusRequest.eventInfo = this.frontEndSupport.buildEventInfo(this.metaData);
    ClientResponse<TwoFactorStatusResponse, Errors> clientResponse = this.client.setTenantId(paramUser.tenantId).retrieveTwoFactorStatusWithRequest(twoFactorStatusRequest);
    if (clientResponse.status == 242) {
      if (this.prompts.contains("none")) {
        buildAuthorizedRedirectWithError((new OAuthService.OAuthValidationResult()).withError(new OAuthError(OAuthError.OAuthErrorType.interaction_required, OAuthError.OAuthErrorReason.multi_factor_challenge_required, "The user is required to complete a multi-factor challenge to complete authentication.")));
        return "authorized-redirect-with-error";
      } 
      TwoFactorStartResponse twoFactorStartResponse = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.startTwoFactorLogin((new TwoFactorStartRequest()).with(()).with(())));
      this.twoFactorId = twoFactorStartResponse.twoFactorId;
      if (this.oauth_context == null)
        this.oauth_context = new OAuthContext(); 
      this.oauth_context.addAuthenticationType(paramAuthenticationType);
      if (paramUser.twoFactorEnabled()) {
        buildRedirectToTwoFactorMethodsURI();
        return "redirect-to-two-factor-methods";
      } 
      buildRedirectToTwoFactorEnableURI();
      addGeneralInfo("[TwoFactorRequired]", new Object[0]);
      return "redirect-to-two-factor-enable";
    } 
    return null;
  }
  
  protected String handleWebAuthnReauth() {
    if (this.skipWebAuthnReauth)
      return null; 
    if (this.codeTenant.webAuthnConfiguration.enabled) {
      boolean bool = this.codeApplication.webAuthnConfiguration.enabled ? this.codeApplication.webAuthnConfiguration.reauthenticationWorkflow.enabled : this.codeTenant.webAuthnConfiguration.reauthenticationWorkflow.enabled;
      if (bool && 
        this.ssoService.getWebAuthnReAuthenticationCredentials(this.frontEndSupport.request.getCookies(), this.codeTenant)
        .stream()
        .anyMatch(paramWebAuthnReAuthenticationCredential -> (paramWebAuthnReAuthenticationCredential.credentialId != null))) {
        this
          
          .redirectToWebAuthnReauthURI = baseQueryBuilder("/oauth2/webauthn-reauth").with("workflow", WebAuthnWorkflow.reauthentication).build();
        return "redirect-to-webauthn-reauth";
      } 
    } 
    return null;
  }
  
  protected boolean isDomainBasedOrPasswordless(BaseIdentityProvider<?> paramBaseIdentityProvider) {
    return ((paramBaseIdentityProvider instanceof DomainBasedIdentityProvider && ((DomainBasedIdentityProvider)paramBaseIdentityProvider).getDomains().size() > 0) || paramBaseIdentityProvider instanceof io.fusionauth.domain.provider.PasswordlessIdentityProvider);
  }
  
  protected void persistRememberDeviceChoice(boolean paramBoolean1, boolean paramBoolean2) {
    if (paramBoolean2)
      this.rememberDeviceCookie.setValue(String.valueOf(paramBoolean1)); 
  }
  
  protected boolean redirectToWebAuthnReauthEnable(UUID paramUUID) {
    if (this.userVerifyingPlatformAuthenticatorAvailable && this.codeTenant.webAuthnConfiguration.enabled) {
      boolean bool = this.codeApplication.webAuthnConfiguration.enabled ? this.codeApplication.webAuthnConfiguration.reauthenticationWorkflow.enabled : this.codeTenant.webAuthnConfiguration.reauthenticationWorkflow.enabled;
      if (bool) {
        SSOService.WebAuthnReAuthenticationCredential webAuthnReAuthenticationCredential = this.ssoService.getWebAuthnReAuthenticationCredential(this.frontEndSupport.request.getCookies(), this.codeTenant, paramUUID);
        return (webAuthnReAuthenticationCredential == null);
      } 
    } 
    return false;
  }
  
  protected String safeGet(Map<String, Object> paramMap, String paramString) {
    Object object = paramMap.get(paramString);
    return (object == null) ? null : object.toString();
  }
  
  protected void setAntiClickJackingHeader(Application paramApplication) {
    if (paramApplication == null || paramApplication.oauthConfiguration.authorizedOriginURLs == null || paramApplication.oauthConfiguration.authorizedOriginURLs.isEmpty()) {
      this.frontEndSupport.response.addHeader("X-Frame-Options", "DENY");
    } else {
      String str = HTTPTools.getOriginHeader(this.frontEndSupport.request);
      if (str == null) {
        this.frontEndSupport.response.addHeader("X-Frame-Options", "DENY");
      } else {
        URI uRI1 = URITools.sanitizeOrigin(str);
        URI uRI2 = URI.create(this.frontEndSupport.request.getBaseURL());
        if (!URITools.isSameBaseURL(uRI1, uRI2) && 
          !this.oauthService.validateOrigin(paramApplication.oauthConfiguration, uRI1))
          this.frontEndSupport.response.addHeader("X-Frame-Options", "DENY"); 
      } 
    } 
  }
  
  protected void setResultValues(OAuthService.OAuthValidationResult paramOAuthValidationResult) {
    this.bootstrapWebauthnEnabled = (this.codeTenant.webAuthnConfiguration.enabled && (this.codeApplication.webAuthnConfiguration.enabled ? this.codeApplication.webAuthnConfiguration.bootstrapWorkflow.enabled : this.codeTenant.webAuthnConfiguration.bootstrapWorkflow.enabled));
    this.codeApplication = paramOAuthValidationResult.application;
    this.passwordlessEnabled = this.frontEndSupport.passwordlessService.isEnabled(this.codeTenant, this.codeApplication);
    this.prompts.addAll(paramOAuthValidationResult.prompts);
    this.scopes = paramOAuthValidationResult.scopes;
    this.validatedResources = new ArrayList<>(paramOAuthValidationResult.validatedResources);
  }
  
  protected boolean shouldPromptForOAuthScopeConsent(UUID paramUUID) {
    if (this.codeApplication.oauthConfiguration.relationship.equals(OAuthApplicationRelationship.ThirdParty) && 
      !this.codeApplication.oauthConfiguration.consentMode.equals(OAuthScopeConsentMode.NeverPrompt) && 
      ReactorStatusValidator.isLicensedFor(this.frontEndSupport.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.advancedOAuthScopesThirdPartyApplications)) {
      Set<String> set = this.oauthService.getOAuthScopeNamesForPromptFromApplication(this.codeApplication);
      Objects.requireNonNull(set);
      if (this.scopes.stream().anyMatch(set::contains)) {
        if (this.prompts.contains("consent"))
          return true; 
        if (this.codeApplication.oauthConfiguration.consentMode.equals(OAuthScopeConsentMode.RememberDecision))
          return this.oauthService.checkPersistedUserConsentChoices(this.codeTenant, this.codeApplication, paramUUID, this.scopes); 
        return true;
      } 
    } 
    return false;
  }
  
  protected void unpackSavedState(Map<String, Object> paramMap) {
    if (paramMap == null)
      return; 
    this.client_id = safeGet(paramMap, "client_id");
    this.code_challenge = safeGet(paramMap, "code_challenge");
    this.code_challenge_method = safeGet(paramMap, "code_challenge_method");
    this.metaData.device.name = safeGet(paramMap, "metaData.device.name");
    this.metaData.device.type = safeGet(paramMap, "metaData.device.type");
    this.nonce = safeGet(paramMap, "nonce");
    String str1 = safeGet(paramMap, "redirect_uri");
    if (str1 != null)
      this.redirect_uri = URI.create(str1); 
    this.response_mode = safeGet(paramMap, "response_mode");
    this.response_type = safeGet(paramMap, "response_type");
    this.scope = safeGet(paramMap, "scope");
    this.state = safeGet(paramMap, "state");
    String str2 = safeGet(paramMap, "tenantId");
    if (str2 != null)
      this.tenantId = StringTools.parseUUID(str2); 
    this.timezone = safeGet(paramMap, "timezone");
    this.user_code = safeGet(paramMap, "user_code");
    String str3 = safeGet(paramMap, "resource");
    if (str3 != null && !str3.isBlank())
      this
        
        .resource = (List<URI>)Arrays.<String>stream(str3.split(" ")).map(URI::create).collect(Collectors.toList()); 
  }
  
  protected String validateAndHandleErrors(boolean paramBoolean) {
    return validateAndHandleErrors(paramBoolean, (Predicate<OAuthError>)null);
  }
  
  protected String validateAndHandleErrors(boolean paramBoolean, Predicate<OAuthError> paramPredicate) {
    return validateAndHandleErrors(paramBoolean, paramPredicate, (Set<String>)null);
  }
  
  protected String validateAndHandleErrors(boolean paramBoolean, Predicate<OAuthError> paramPredicate, Set<String> paramSet) {
    OAuthService.OAuthValidationResult oAuthValidationResult = this.oauthService.validateAuthorizeRequest(this.tenantId, this.client_id, this.prompt, this.redirect_uri, this.response_mode, this.response_type, this.state, this.scope, this.code_challenge, this.code_challenge_method, paramBoolean, this.resource);
    setAntiClickJackingHeader(oAuthValidationResult.application);
    if (oAuthValidationResult.error != null && (
      paramPredicate == null || paramPredicate.test(oAuthValidationResult.error))) {
      if (oAuthValidationResult.doNotRedirect) {
        this.oauthJSONError = this.frontEndSupport.writeToPrettyString(oAuthValidationResult.error);
        return "render-error";
      } 
      buildAuthorizedRedirectWithError(oAuthValidationResult);
      return "authorized-redirect-with-error";
    } 
    if (this.pendingIdPLinkId != null)
      buildPendingIdPLink(); 
    if (this.user_code != null)
      buildDevicePendingIdPLink(this.user_code); 
    if (paramSet != null) {
      OAuthService.OAuthValidationResult oAuthValidationResult1 = this.oauthService.validateConsentedScopes(oAuthValidationResult.application, oAuthValidationResult.scopes, paramSet);
      if (oAuthValidationResult1.error != null) {
        oAuthValidationResult.withError(oAuthValidationResult1.error);
        buildAuthorizedRedirectWithError(oAuthValidationResult);
        return "authorized-redirect-with-error";
      } 
      oAuthValidationResult.scopes = oAuthValidationResult1.scopes;
    } 
    setResultValues(oAuthValidationResult);
    return null;
  }
  
  protected String validatePendingLinkMaxCount(UUID paramUUID) {
    if (this.pendingIdPLinkId == null)
      return null; 
    ClientResponse<IdentityProviderPendingLinkResponse, Errors> clientResponse = this.client.retrievePendingLink(this.pendingIdPLinkId, paramUUID);
    if (clientResponse.wasSuccessful()) {
      IdentityProviderTenantConfiguration identityProviderTenantConfiguration = ((IdentityProviderPendingLinkResponse)clientResponse.successResponse).identityProviderTenantConfiguration;
      if (identityProviderTenantConfiguration != null && identityProviderTenantConfiguration.limitUserLinkCount.enabled && 
        ((IdentityProviderPendingLinkResponse)clientResponse.successResponse).linkCount.intValue() >= identityProviderTenantConfiguration.limitUserLinkCount.maximumLinks) {
        Integer integer = Integer.valueOf(identityProviderTenantConfiguration.limitUserLinkCount.maximumLinks);
        addGeneralError("[LinkCountExceeded]", new Object[] { integer });
        buildRedirectToStartIdPLinkLink();
        return "redirect-to-start-idp-link";
      } 
    } 
    return null;
  }
  
  private void buildPendingIdPLink() {
    if (this.cancelPendingIdpLink) {
      this.pendingIdPLinkId = null;
      return;
    } 
    if (this.pendingIdPLinkId != null) {
      ExternalIdentifier externalIdentifier = this.frontEndSupport.getExternalId(this.codeTenant.id, this.pendingIdPLinkId, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.PendingIdPLinkId });
      if (externalIdentifier == null) {
        addGeneralError("[InvalidPendingIdPLinkId]", new Object[0]);
        this.pendingIdPLinkId = null;
        buildRedirectToAuthorizeURI();
        throw new ErrorException("redirect-to-authorize");
      } 
      this.pendingIdPLink = externalIdentifier.buildPendingIdpLink();
      if (this.ssoSession.user != null)
        updateWithLinkLimits(this.pendingIdPLink, this.ssoSession.user); 
    } 
  }
  
  private void buildRedirectForIntent(LoginIntentService.LoginIntent paramLoginIntent) {
    switch (paramLoginIntent.step) {
      case NotRegistered:
        buildRedirectToAuthorizedNotRegisteredURI();
        return;
      case CompleteRegistration:
        buildCompleteRegistrationRedirectURI();
        return;
      case RegistrationVerification:
        buildRedirectToRegistrationVerificationRequired(paramLoginIntent.verificationId);
        return;
      case EmailVerification:
        buildRedirectToEmailVerificationRequired(paramLoginIntent.verificationId);
        return;
      case PhoneVerification:
        buildRedirectToPhoneVerificationRequired(paramLoginIntent.verificationId);
        return;
      case WebAuthnReauthEnable:
        buildRedirectToWebAuthnReauthEnableURI();
        return;
      case Consent:
        buildRedirectToOAuthScopeConsentURI();
        return;
    } 
    buildRedirectToAuthorizeURI();
  }
  
  private void handleSendResult(ClientResponse<Void, Errors> paramClientResponse) {
    if (paramClientResponse.wasSuccessful()) {
      addGeneralInfo("[TwoFactorMessageSent]", new Object[0]);
    } else {
      transferErrors((Errors)paramClientResponse.errorResponse);
      if (paramClientResponse.exception != null)
        addGeneralError("[TwoFactorSendFailed]", new Object[0]); 
    } 
  }
  
  private void updateWithLinkLimits(PendingIdPLink paramPendingIdPLink, User paramUser) {
    ClientResponse<IdentityProviderLinkResponse, Errors> clientResponse = this.client.retrieveUserLinksByUserId(paramPendingIdPLink.identityProviderId, paramUser.id);
    if (!clientResponse.wasSuccessful())
      return; 
    paramPendingIdPLink.identityProviderLinks = ((IdentityProviderLinkResponse)clientResponse.successResponse).identityProviderLinks;
    FusionAuthClient fusionAuthClient = this.frontEndSupport.fusionAuthClientProvider.get();
    ClientResponse<IdentityProviderResponse, Errors> clientResponse1 = fusionAuthClient.retrieveIdentityProvider(paramPendingIdPLink.identityProviderId);
    if (clientResponse1.wasSuccessful()) {
      paramPendingIdPLink.identityProviderTenantConfiguration = ((IdentityProviderResponse)clientResponse1.getSuccessResponse()).identityProvider.tenantConfiguration.get(paramUser.tenantId);
    } else {
      this.frontEndSupport.frontEndErrorHandling(clientResponse1);
    } 
  }
  
  public static class FormPostResponse {
    public AccessToken accessToken;
    
    public String code;
    
    public String iss;
    
    public String scope;
    
    public FormPostResponse(AccessToken param1AccessToken, String param1String1, String param1String2) {
      this.accessToken = param1AccessToken;
      this.scope = param1String1;
      this.iss = param1String2;
    }
    
    public FormPostResponse(String param1String1, String param1String2) {
      this.code = param1String1;
      this.iss = param1String2;
    }
  }
  
  public static class OAuthContext implements Buildable<OAuthContext> {
    @JsonProperty("ats")
    public List<AuthenticationType> authenticationTypes;
    
    @JsonProperty("cpid")
    public String changePasswordId;
    
    @JsonProperty("cpr")
    public ChangePasswordReason changePasswordReason;
    
    @JsonProperty("evid")
    public String emailVerificationId;
    
    @JsonProperty("pc")
    public boolean passwordChanged;
    
    @JsonProperty("rvid")
    public String registrationVerificationId;
    
    @JsonProperty("tfrcs")
    public List<String> twoFactorRecoveryCodes;
    
    @JsonIgnore
    public void addAuthenticationType(AuthenticationType param1AuthenticationType) {
      if (this.authenticationTypes == null)
        this.authenticationTypes = new ArrayList<>(1); 
      this.authenticationTypes.add(param1AuthenticationType);
    }
    
    @JsonIgnore
    public void clearAuthenticationTypes() {
      if (this.authenticationTypes != null)
        this.authenticationTypes.clear(); 
    }
    
    @JsonIgnore
    public AuthenticationType getFirstAuthenticationType() {
      return (this.authenticationTypes == null || this.authenticationTypes.isEmpty()) ? null : this.authenticationTypes.get(0);
    }
    
    @JsonIgnore
    public boolean isEmpty() {
      return ((this.authenticationTypes == null || this.authenticationTypes.isEmpty()) && this.changePasswordId == null && this.changePasswordReason == null && this.emailVerificationId == null && !this.passwordChanged && (this.twoFactorRecoveryCodes == null || this.twoFactorRecoveryCodes



        
        .isEmpty()) && this.registrationVerificationId == null);
    }
  }
}
