package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.identity.IdentityProviderReaderService;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.util.PKCETools;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLinkResponse;
import io.fusionauth.domain.oauth2.AccessToken;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.domain.oauth2.OAuthResponse;
import io.fusionauth.domain.provider.IdentityProviderTenantConfiguration;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.scope.annotation.ManagedCookie;
import org.primeframework.mvc.util.QueryStringBuilder;
import org.primeframework.mvc.validation.ValidationMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Action
@List({@Redirect(code = "activation-complete", uri = "${activateCompleteURI}"), @Redirect(code = "success", uri = "${redirectURI}")})
@ThemedForward(code = "invalid-csrf-token", page = "/oauth2/error.ftl")
public class DeviceAction extends BaseOAuthAuthenticationAction {
  private static final Logger logger = LoggerFactory.getLogger(DeviceAction.class);
  
  private final HTTPRequest httpRequest;
  
  private final IdentityProviderReaderService identityProviderReaderService;
  
  public String activateCompleteURI;
  
  public String code;
  
  @ManagedCookie(name = "fusionauth.app.pkce-verifier")
  public Cookie codeVerifier;
  
  @FTLVariable
  public String interactive_user_code;
  
  public String redirectURI;
  
  @JSONResponse
  public OAuthResponse response;
  
  public OAuthService.OAuthValidationResult result;
  
  @FTLVariable
  public int userCodeLength;
  
  private User linkedUser;
  
  @Inject
  public DeviceAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, IdentityProviderReaderService paramIdentityProviderReaderService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService, HTTPRequest paramHTTPRequest) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.identityProviderReaderService = paramIdentityProviderReaderService;
    this.httpRequest = paramHTTPRequest;
  }
  
  public String get() {
    if (this.code != null && this.codeApplication != null) {
      if (this.ssoSession.user != null && isLinkCountExceeded()) {
        buildRedirectToStartIdPLinkLink();
        return "redirect-to-start-idp-link";
      } 
      this.frontEndSupport.validateCSRFToken(this.state);
      this.state = null;
      if (exchangeCodeForToken()) {
        this
          .activateCompleteURI = baseQueryBuilder("/oauth2/device-complete").build();
        return "activation-complete";
      } 
      return "input";
    } 
    this.interactive_user_code = this.user_code;
    return "input";
  }
  
  public String post() {
    this.client_id = this.result.application.oauthConfiguration.clientId;
    this.tenantId = this.result.tenant.id;
    this.scope = String.join(" ", (Iterable)this.result.scopes);
    boolean bool = (this.redirect_uri == null) ? true : false;
    if (bool) {
      this.codeVerifier.setValue(PKCETools.generateCodeVerifier());
      this.code_challenge = PKCETools.generateCodeChallenge(this.codeVerifier.getValue());
      this.code_challenge_method = "S256";
      this.redirect_uri = URI.create("/oauth2/device");
      this.response_type = "code";
    } 
    if (this.linkedUser != null) {
      String str = handleTwoFactorDuringSSO(this.linkedUser, AuthenticationType.PING);
      if (str != null) {
        persistRememberDeviceChoice(true, true);
        return str;
      } 
      this.oauth_context = new BaseOAuthAction.OAuthContext();
      this.oauth_context.addAuthenticationType(AuthenticationType.PING);
      return (handlePostAuthenticationRedirect(this.linkedUser, SSOService.RememberDeviceState.Remember)).step.getResultCode();
    } 
    if (this.devicePendingIdPLink != null && (this.ssoSession.user == null || this.devicePendingIdPLink.isLinkLimitExceeded())) {
      buildRedirectToStartIdPLinkLink();
      return "redirect-to-start-idp-link";
    } 
    this
      .redirectURI = baseQueryBuilder("/oauth2/authorize").build();
    return "success";
  }
  
  @PostParameterMethod
  public void setup() {
    this.userCodeLength = this.codeTenant.externalIdentifierConfiguration.deviceUserCodeIdGenerator.length;
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    this.result = this.oauthService.validateDeviceRequest(this.tenantId, this.client_id, null, this.httpRequest);
    setAntiClickJackingHeader(this.result.application);
    if (this.result.error != null) {
      this.oauthJSONError = this.frontEndSupport.writeToPrettyString(this.result.error);
      throw new ErrorException("render-error");
    } 
    if (this.user_code != null) {
      buildDevicePendingIdPLink(this.user_code);
      if (this.devicePendingIdPLink != null && this.devicePendingIdPLink.isLinkLimitExceeded())
        addGeneralError("[LinkCountExceeded]", new Object[] { Integer.valueOf(this.devicePendingIdPLink.identityProviderTenantConfiguration.limitUserLinkCount.maximumLinks) }); 
    } 
    setResultValues(this.result);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.interactive_user_code == null) {
      addFieldError("user_code", "[blank]user_code", new Object[0]);
      this.user_code = null;
      return;
    } 
    this.result = this.oauthService.validateUserCodeRequest(this.tenantId, this.client_id, this.interactive_user_code);
    if (this.result.error != null) {
      addFieldError("user_code", "[invalid]user_code", new Object[0]);
      this.user_code = this.interactive_user_code;
      return;
    } 
    buildDevicePendingIdPLink(this.interactive_user_code);
    if (this.devicePendingIdPLink != null) {
      IdentityProviderLink identityProviderLink = this.identityProviderReaderService.retrieveIdProviderUser(this.codeTenant.id, this.devicePendingIdPLink.identityProviderId, this.devicePendingIdPLink.identityProviderUserId);
      if (identityProviderLink != null) {
        if (this.ssoSession.user != null && !this.ssoSession.user.id.equals(identityProviderLink.userId))
          this.ssoService.logout(this.ssoSession, this.ssoCookie, this.codeTenant, this.frontEndSupport.buildEventInfo(this.metaData)); 
        this.linkedUser = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(paramIdentityProviderLink.userId))).user;
      } 
    } 
    this.user_code = this.result.user_code;
    this.state = this.oauthService.encodeStateForRedirect(paramQueryStringBuilder -> paramQueryStringBuilder.with("client_id", this.result.application.oauthConfiguration.clientId).with("state", (this.redirect_uri == null) ? this.frontEndSupport.storeCSRFToken() : null).with("tenantId", this.codeTenant.id));
  }
  
  protected void preResolveApplicationTenantAndTheme() {
    if (this.frontEndSupport.isGET())
      this.oauthService.decodeAndRestoreStateFromCallback(this, this.state); 
  }
  
  private boolean exchangeCodeForToken() {
    ClientResponse<AccessToken, OAuthError> clientResponse = this.client.exchangeOAuthCodeForAccessTokenUsingPKCE(this.code, this.codeApplication.oauthConfiguration.clientId, this.codeApplication.oauthConfiguration.clientSecret, getRedirectURI().toString(), this.codeVerifier.getValue());
    if (clientResponse.exception != null) {
      logger.error("Unable to call FusionAuth Token endpoint using code [{}] in device code exchange.\n", this.code);
      logger.error("Returned Exception", clientResponse.exception);
      addGeneralError("[TokenExchangeException]", new Object[0]);
      return false;
    } 
    if (!clientResponse.wasSuccessful()) {
      logger.error("Unable to call FusionAuth Token endpoint using code [{}] in device code exchange. HTTP Status Code [{}]. Error message: \n[{}]", new Object[] { this.code, Integer.valueOf(clientResponse.status), clientResponse.errorResponse });
      addGeneralError("[TokenExchangeFailed]", new Object[0]);
      return false;
    } 
    this.codeVerifier = null;
    return true;
  }
  
  private URI getRedirectURI() {
    return (this.redirect_uri == null) ? URI.create("/oauth2/device") : this.redirect_uri;
  }
  
  private boolean isLinkCountExceeded() {
    try {
      ExternalIdentifier externalIdentifier1 = this.frontEndSupport.getExternalId(this.result.tenant.id, this.code, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.AuthorizationCode });
      String str = externalIdentifier1.getAttribute("userCode");
      ExternalIdentifier externalIdentifier2 = this.frontEndSupport.getExternalId(this.result.tenant.id, str, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.DeviceUserCode });
      UUID uUID = externalIdentifier2.getAttributeAsUUID("identityProviderId");
      FusionAuthClient fusionAuthClient = this.frontEndSupport.fusionAuthClientProvider.get();
      ClientResponse<IdentityProviderResponse, Errors> clientResponse = fusionAuthClient.retrieveIdentityProvider(uUID);
      IdentityProviderTenantConfiguration identityProviderTenantConfiguration = null;
      if (clientResponse.wasSuccessful()) {
        identityProviderTenantConfiguration = ((IdentityProviderResponse)clientResponse.getSuccessResponse()).identityProvider.tenantConfiguration.get(this.tenant.id);
      } else {
        this.frontEndSupport.frontEndErrorHandling(clientResponse);
      } 
      if (identityProviderTenantConfiguration != null && identityProviderTenantConfiguration.limitUserLinkCount.enabled) {
        String str1 = externalIdentifier2.getAttribute("identityProviderUserId");
        List<IdentityProviderLink> list = ((IdentityProviderLinkResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserLinksByUserId(paramUUID, this.ssoSession.user.id))).identityProviderLinks;
        if (list.stream().anyMatch(paramIdentityProviderLink -> paramIdentityProviderLink.identityProviderUserId.equals(paramString)))
          return false; 
        if (list.size() >= identityProviderTenantConfiguration.limitUserLinkCount.maximumLinks) {
          addGeneralError("[LinkCountExceeded]", new Object[] { Integer.valueOf(identityProviderTenantConfiguration.limitUserLinkCount.maximumLinks) });
          this.client_id = this.codeApplication.oauthConfiguration.clientId;
          this.redirect_uri = getRedirectURI();
          this.response_type = "code";
          this.tenantId = this.codeApplication.tenantId;
          this.user_code = str;
          this.state = this.oauthService.encodeStateForRedirect(paramQueryStringBuilder -> paramQueryStringBuilder.with("client_id", this.result.application.oauthConfiguration.clientId).with("state", this.frontEndSupport.storeCSRFToken()));
          return true;
        } 
      } 
    } catch (NullPointerException nullPointerException) {}
    return false;
  }
}
