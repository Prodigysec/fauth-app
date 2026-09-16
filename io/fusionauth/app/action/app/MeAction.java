package io.fusionauth.app.action.app;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.domain.oauth2.UserinfoResponse;
import io.fusionauth.http.Cookie;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class MeAction extends BaseAppAction {
  @JSONResponse
  public UserinfoResponse response;
  
  @Inject
  public MeAction(FrontEndSupport paramFrontEndSupport, OAuthService paramOAuthService) {
    super(paramFrontEndSupport, paramOAuthService);
  }
  
  public String get() {
    Cookie cookie = this.frontEndSupport.getCookie("app.at");
    if (cookie == null || cookie.value == null)
      return "unauthorized"; 
    ClientResponse<UserinfoResponse, OAuthError> clientResponse = this.client.retrieveUserInfoFromAccessToken(cookie.value);
    if (clientResponse.wasSuccessful()) {
      this.response = (UserinfoResponse)clientResponse.successResponse;
      return "render";
    } 
    return "unauthorized";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    validateOrigin();
  }
}
