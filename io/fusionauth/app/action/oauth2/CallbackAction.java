package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.json.ToString;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.StringTools;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.authentication.IdentityProviderAuthenticationService;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.identity.IdentityProviderHelper;
import io.fusionauth.api.service.identity.IdentityProviderReaderService;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.system.CipherService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.util.IdentityProviderTools;
import io.fusionauth.app.Cookies;
import io.fusionauth.app.action.oauth1.RequestTokenAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.api.LoginResponse;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLoginRequest;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.scope.annotation.BrowserActionSession;
import org.primeframework.mvc.scope.annotation.ManagedSessionCookie;
import org.primeframework.mvc.util.QueryStringBuilder;

@Action
@Forward(code = "post-back", page = "/oauth2/callback/post.ftl")
@Redirect(code = "redirect-to-connect-test-complete", uri = "${redirectToConnectionTestCompleteURI}")
public class CallbackAction extends BaseOAuthAuthenticationAction {
  private final CipherService cipherService;
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final IdentityProviderCache identityProviderCache;
  
  private final IdentityProviderReaderService identityProviderReader;
  
  public String RelayState;
  
  public String SAMLResponse;
  
  public boolean bypassedAuthorize;
  
  public String code;
  
  public String csrf;
  
  public String error;
  
  public String error_description;
  
  @ManagedSessionCookie(name = "federated.csrf", encrypt = false)
  public Cookie federatedCSRF;
  
  public String id_token;
  
  @BrowserActionSession(action = RequestTokenAction.class, name = "state")
  public RequestTokenAction.OAuth1 oauth1;
  
  public String oauth_token;
  
  public String oauth_token_secret;
  
  public String oauth_verifier;
  
  public boolean postBack;
  
  public String redirectToConnectionTestCompleteURI;
  
  public Boolean rememberDevice;
  
  public String token;
  
  private String appleUser;
  
  private boolean idpInitiated;
  
  private BaseIdentityProvider<?> initiatedIdentityProvider;
  
  @Inject
  public CallbackAction(ExternalIdentifierReaderService paramExternalIdentifierReaderService, FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, IdentityProviderCache paramIdentityProviderCache, IdentityProviderReaderService paramIdentityProviderReaderService, LoginIntentService paramLoginIntentService, SSOService paramSSOService, OAuthService paramOAuthService, ThreatDetectionService paramThreatDetectionService, CipherService paramCipherService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.cipherService = paramCipherService;
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.identityProviderCache = paramIdentityProviderCache;
    this.identityProviderReader = paramIdentityProviderReaderService;
  }
  
  public String get() {
    SSOService.RememberDeviceState rememberDeviceState;
    if (this.oauth1 != null) {
      if (this.oauth_token == null || !this.oauth_token.equals(this.oauth1.oauth_token))
        return addGeneralErrorAndRedirect("[OAuthv1TokenMismatch]"); 
      this.oauth_token_secret = this.oauth1.oauth_token_secret;
      this.state = this.oauth1.state;
    } 
    if (!this.idpInitiated) {
      if (this.csrf == null)
        return addGeneralErrorAndRedirect("[ExternalAuthenticationException]MissingFederatedCSRFToken"); 
      if (!this.csrf.equals(this.federatedCSRF.value))
        return addGeneralErrorAndRedirect("[ExternalAuthenticationException]InvalidFederatedCSRFToken"); 
    } 
    this.federatedCSRF.value = null;
    if (this.error != null) {
      if (this.error_description != null)
        this.error_description = this.error_description.replace("&quot;", "\""); 
      addGeneralError("[Oauthv2Error]", new Object[] { "\n\nerror: " + this.error + "\n\nerror_description: " + this.error_description });
      buildRedirectToAuthorizeURI();
      return "redirect-to-authorize";
    } 
    String str1 = validateAndHandleErrors(false);
    if (str1 != null)
      return str1; 
    IdentityProviderHelper.ResolvedIdentityProviderResult resolvedIdentityProviderResult = IdentityProviderHelper.resolveIdentityProvider(this.identityProviderId, this.connectionTestId, this.codeTenant, this.externalIdentifierReader, this.identityProviderCache, this.identityProviderReader);
    BaseIdentityProvider<?> baseIdentityProvider = resolvedIdentityProviderResult.identityProvider;
    if (baseIdentityProvider == null || (baseIdentityProvider.tenantId != null && !baseIdentityProvider.tenantId.equals(this.codeTenant.id))) {
      addGeneralError("[InvalidIdentityProviderId]", new Object[0]);
      buildRedirectToAuthorizeURI();
      return "redirect-to-authorize";
    } 
    String[] arrayOfString = new String[1];
    if (baseIdentityProvider instanceof io.fusionauth.domain.provider.OpenIdConnectIdentityProvider) {
      arrayOfString[0] = Cookies.getValue(this.frontEndSupport.request, "fusionauth.pkce-verifier");
      if (arrayOfString[0] == null) {
        addGeneralError("[MissingPKCECodeVerifier]", new Object[0]);
        buildRedirectToAuthorizeURI();
        return "redirect-to-authorize";
      } 
    } 
    this.oauth1 = null;
    UUID uUID = StringTools.parseUUID(this.client_id);
    ClientResponse<LoginResponse, Errors> clientResponse = this.client.identityProviderLogin((new IdentityProviderLoginRequest())
        .with(paramIdentityProviderLoginRequest -> paramIdentityProviderLoginRequest.applicationId = paramUUID)
        .with(paramIdentityProviderLoginRequest -> paramIdentityProviderLoginRequest.eventInfo = this.frontEndSupport.buildEventInfo(this.metaData))
        .with(paramIdentityProviderLoginRequest -> paramIdentityProviderLoginRequest.identityProviderId = this.identityProviderId)
        .with(paramIdentityProviderLoginRequest -> paramIdentityProviderLoginRequest.connectionTestId = this.connectionTestId)
        
        .with(paramIdentityProviderLoginRequest -> paramIdentityProviderLoginRequest.noJWT = true)
        
        .with(paramIdentityProviderLoginRequest -> paramIdentityProviderLoginRequest.addData("token", this.token).addData("id_token", this.id_token).addData("appleUser", this.appleUser).addData("samlResponse", this.SAMLResponse).addData("code", this.code).addData("code_verifier", paramArrayOfString[0]).addData("redirect_uri", this.frontEndSupport.getFusionAuthBaseURL() + "/oauth2/callback").addData("oauth_token", this.oauth_token).addData("oauth_token_secret", this.oauth_token_secret).addData("oauth_verifier", this.oauth_verifier)));
    if (this.connectionTestId != null) {
      buildRedirectToConnectionTestCompleteURI();
      return "redirect-to-connect-test-complete";
    } 
    if (IdentityProviderTools.isIdpInitAble(baseIdentityProvider))
      if (Application.FUSIONAUTH_APP_ID.equals(uUID))
        this.state = this.frontEndSupport.storeCSRFToken();  
    if (this.idpInitiated) {
      rememberDeviceState = SSOService.RememberDeviceState.Remember;
    } else if (this.bypassedAuthorize) {
      rememberDeviceState = getRememberDeviceState();
      if (rememberDeviceState == SSOService.RememberDeviceState.NoChange)
        rememberDeviceState = SSOService.RememberDeviceState.fromBoolean(this.rememberDevice); 
      if (rememberDeviceState == SSOService.RememberDeviceState.NoChange)
        rememberDeviceState = SSOService.RememberDeviceState.Remember; 
    } else {
      rememberDeviceState = SSOService.RememberDeviceState.fromBoolean(this.rememberDevice);
      if (rememberDeviceState == SSOService.RememberDeviceState.NoChange) {
        rememberDeviceState = getRememberDeviceState();
      } else {
        persistRememberDeviceChoice(this.rememberDevice.booleanValue(), true);
      } 
    } 
    AuthenticationType authenticationType = IdentityProviderAuthenticationService.authenticationType(baseIdentityProvider.getType());
    String str2 = handleInteractiveLoginResponse(clientResponse, (SSOService.NewDeviceResult)null, paramErrors -> {
          String str1 = paramClientResponse.getHeader("X-FusionAuth-ValidationId");
          String str2 = "[IdentityProviderValidationException]";
          boolean bool = (paramBaseIdentityProvider.debug || str1 == null) ? true : false;
          if (bool) {
            String str = (str1 != null) ? ("These errors were returned by the configured Login Validation Lambda with Id " + str1 + ".") : ("These errors are unexpected. The end user will be shown a more generic error:\n" + str2 + "=" + this.theme.message(str2, new Object[0]));
            EventLogType eventLogType = (str1 != null) ? EventLogType.Debug : EventLogType.Error;
            EventLogHelper.create(new EventLog(eventLogType, "Failed to complete a login for Identity Provider [{{identityProviderName}}].\n\nThe following errors occurred:\n\n{{errors}}\n\n{{explanation}}\n".replace("{{identityProviderName}}", paramBaseIdentityProvider.name).replace("{{errors}}", ToString.toString(paramErrors)).replace("{{explanation}}", str)));
          } 
          if (str1 != null) {
            transferErrors(paramErrors);
          } else {
            addGeneralError("[IdentityProviderValidationException]", new Object[0]);
          } 
        }rememberDeviceState, authenticationType, false);
    if (str2.equals("input")) {
      buildRedirectToAuthorizeURI();
      return "redirect-to-authorize";
    } 
    return str2;
  }
  
  public String post() {
    if (this.idpInitiated) {
      this.code_challenge = buildSurrogateCodeChallenge(this.initiatedIdentityProvider.id);
      OAuthService.OAuthValidationResult oAuthValidationResult = this.oauthService.rehydrateIdPInitiatedLoginToOAuth(this.tenantId, this.client_id, this.redirect_uri, this.RelayState);
      if (this.response_type == null)
        this.response_type = "code"; 
      this.redirect_uri = oAuthValidationResult.redirect_uri;
      if (oAuthValidationResult.application != null && 
        Application.FUSIONAUTH_APP_ID.equals(oAuthValidationResult.application.id))
        if (this.scope == null) {
          this.scope = "offline_access";
        } else if (!this.scope.contains("offline_access")) {
          this.scope += " offline_access";
        }  
    } 
    return get();
  }
  
  public void setAccess_token(String paramString) {
    this.token = paramString;
  }
  
  public void setCredential(String paramString) {
    this.token = paramString;
  }
  
  public void setUser(String paramString) {
    this.appleUser = paramString;
  }
  
  protected void preResolveApplicationTenantAndTheme() {
    if (this.frontEndSupport.isPOST()) {
      if (this.client_id != null && this.identityProviderId != null && this.SAMLResponse != null) {
        this.initiatedIdentityProvider = (BaseIdentityProvider)this.identityProviderCache.get(this.identityProviderId);
        this.idpInitiated = IdentityProviderTools.isIdpInitAble(this.initiatedIdentityProvider);
      } 
      if (!this.idpInitiated && !this.postBack)
        throw new ErrorException("post-back"); 
      if (!this.idpInitiated && this.RelayState != null)
        this.state = this.RelayState; 
    } 
    if (!this.idpInitiated) {
      this.csrf = null;
      this.oauthService.decodeAndRestoreStateFromCallback(this, this.state);
    } 
  }
  
  private String addGeneralErrorAndRedirect(String paramString) {
    addGeneralError(paramString, new Object[0]);
    buildRedirectToAuthorizeURI();
    return "redirect-to-authorize";
  }
  
  private void buildRedirectToConnectionTestCompleteURI() {
    this



      
      .redirectToConnectionTestCompleteURI = QueryStringBuilder.builder("/tenant-manager/sso/test/complete").with("connectionTestId", this.connectionTestId).with("identityProviderId", this.identityProviderId).with("tenantId", this.codeTenant.id).build();
  }
  
  private String buildSurrogateCodeChallenge(Object paramObject) {
    try {
      byte[] arrayOfByte = this.cipherService.encrypt(paramObject.toString().getBytes(StandardCharsets.UTF_8));
      return "fa-surrogate-challenge" + Base64.getUrlEncoder().withoutPadding().encodeToString(arrayOfByte);
    } catch (GeneralSecurityException generalSecurityException) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to encrypt [" + String.valueOf(paramObject) + "] during an IdP initiated login request."));
      return null;
    } 
  }
}
