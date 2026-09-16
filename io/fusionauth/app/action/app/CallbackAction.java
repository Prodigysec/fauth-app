package io.fusionauth.app.action.app;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.oauth2.AccessToken;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.http.Cookie;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.scope.annotation.ManagedCookie;
import org.primeframework.mvc.util.QueryStringBuilder;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
@Redirect(code = "authorized-redirect", uri = "${authorizedRedirectURI}")
public class CallbackAction extends BaseAppAction {
  public String authorizedRedirectURI;
  
  public String code;
  
  @ManagedCookie(name = "app.pkce_v")
  public Cookie codeVerifier;
  
  public String error;
  
  public String error_description;
  
  public String error_reason;
  
  public String iss;
  
  public String locale;
  
  @JSONResponse
  public OAuthError response;
  
  public String state;
  
  public String userState;
  
  @Inject
  public CallbackAction(FrontEndSupport paramFrontEndSupport, OAuthService paramOAuthService) {
    super(paramFrontEndSupport, paramOAuthService);
  }
  
  public String get() {
    if (this.error != null) {
      buildErrorResponseToAuthorizedRedirect(this.error, this.error_description, this.error_reason);
      return "authorized-redirect";
    } 
    ClientResponse<AccessToken, OAuthError> clientResponse = exchangeCodeForToken();
    if (clientResponse.errorResponse != null) {
      buildErrorResponseToAuthorizedRedirect(((OAuthError)clientResponse.errorResponse).error.name(), ((OAuthError)clientResponse.errorResponse).description, ((OAuthError)clientResponse.errorResponse).reason.name());
      return "authorized-redirect";
    } 
    handleAccessTokenResponse((AccessToken)clientResponse.successResponse);
    this


      
      .authorizedRedirectURI = QueryStringBuilder.builder(this.redirect_uri.toString()).with("locale", this.locale).with("state", this.state).with("userState", this.userState).build();
    return "authorized-redirect";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    OAuthService.AppOAuthState appOAuthState = this.frontEndSupport.<OAuthService.AppOAuthState>deserializeBase64EncodedObjectOrDefault(this.state, OAuthService.AppOAuthState.class, () -> null);
    this.client_id = (appOAuthState != null) ? appOAuthState.c : null;
    this.redirect_uri = (appOAuthState != null) ? appOAuthState.r : null;
    this.state = (appOAuthState != null) ? appOAuthState.s : null;
    OAuthService.OAuthValidationResult oAuthValidationResult = this.oauthService.validateAppCallbackHandle(this.client_id, this.code, this.redirect_uri);
    if (oAuthValidationResult.error != null) {
      this.response = oAuthValidationResult.error;
      throw new ErrorException("input", false);
    } 
    this.codeApplication = oAuthValidationResult.application;
    this.codeTenant = oAuthValidationResult.tenant;
    if (this.codeTenant != null && this.tenantId == null)
      this.tenantId = this.codeTenant.id; 
  }
  
  protected ClientResponse<AccessToken, OAuthError> exchangeCodeForToken() {
    ClientResponse<AccessToken, OAuthError> clientResponse = this.client.exchangeOAuthCodeForAccessTokenUsingPKCE(this.code, this.codeApplication.oauthConfiguration.clientId, this.codeApplication.oauthConfiguration.clientSecret, "/app/callback", this.codeVerifier.value);
    this.codeVerifier.value = null;
    if (!clientResponse.wasSuccessful() && 
      this.codeApplication.oauthConfiguration.debug)
      EventLogHelper.create(new EventLog(EventLogType.Debug, Debugger.buildMessageFromResponse("Unable to complete app login. The auth code exchange has failed.\nApplication Id: " + String.valueOf(this.codeApplication.id), clientResponse))); 
    return clientResponse;
  }
  
  private void buildErrorResponseToAuthorizedRedirect(String paramString1, String paramString2, String paramString3) {
    this




      
      .authorizedRedirectURI = QueryStringBuilder.builder(this.redirect_uri.toString()).with("error", paramString1).with("error_description", paramString2).with("error_reason", paramString3).with("locale", this.locale).with("state", this.state).build();
  }
}
