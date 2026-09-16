package io.fusionauth.app.action.app;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.oauth2.AccessToken;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.http.Cookie;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action("{client_id}")
public class RefreshAction extends BaseAppAction {
  @JSONResponse
  public OAuthError response;
  
  @Inject
  public RefreshAction(FrontEndSupport paramFrontEndSupport, OAuthService paramOAuthService) {
    super(paramFrontEndSupport, paramOAuthService);
  }
  
  public String post() {
    Cookie cookie = this.frontEndSupport.getCookie("app.rt");
    String str1 = (cookie != null) ? cookie.value : null;
    String str2 = (this.codeApplication != null) ? this.codeApplication.oauthConfiguration.clientSecret : null;
    ClientResponse<AccessToken, OAuthError> clientResponse = this.client.exchangeRefreshTokenForAccessToken(str1, this.client_id, str2, null, null);
    if (!clientResponse.wasSuccessful()) {
      this
        
        .response = (clientResponse.errorResponse != null) ? (OAuthError)clientResponse.errorResponse : new OAuthError(OAuthError.OAuthErrorType.server_error, OAuthError.OAuthErrorReason.unknown, "An unexpected error occurred. The request may not be completed.");
      return "input";
    } 
    handleAccessTokenResponse((AccessToken)clientResponse.successResponse);
    return "success";
  }
  
  @ValidationMethod
  public void validatePost() {
    validateOrigin();
    OAuthService.OAuthValidationResult oAuthValidationResult = this.oauthService.validateAppRefreshRequest(this.client_id);
    this.codeApplication = oAuthValidationResult.application;
    this.codeTenant = oAuthValidationResult.tenant;
  }
}
