package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import io.fusionauth.api.domain.TokenResult;
import io.fusionauth.api.service.oauth2.DPoPService;
import io.fusionauth.api.service.oauth2.UserInfoService;
import io.fusionauth.app.action.NoStoreJSONBaseAction;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class UserinfoAction extends NoStoreJSONBaseAction {
  private final DPoPService dPoPService;
  
  private final HTTPRequest httpRequest;
  
  private final UserInfoService userInfoService;
  
  @JSONResponse
  public Object response;
  
  private UserInfoService.UserInfoValidationResult result;
  
  @Inject
  public UserinfoAction(DPoPService paramDPoPService, HTTPRequest paramHTTPRequest, UserInfoService paramUserInfoService) {
    this.dPoPService = paramDPoPService;
    this.httpRequest = paramHTTPRequest;
    this.userInfoService = paramUserInfoService;
  }
  
  public String get() {
    this.response = this.userInfoService.retrieveUserInfo(this.result.jwt, this.result.user, this.result.application);
    return "render";
  }
  
  public String post() {
    return get();
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validateGetAndPost() {
    TokenResult tokenResult = this.dPoPService.extractAccessTokenFromAuthorizationHeader(this.httpRequest);
    if (tokenResult.error() != null)
      throw new ErrorException("unauthorized", false); 
    if (tokenResult.accessToken() == null)
      throw new ErrorException("unauthorized", false); 
    this.result = this.userInfoService.validateRetrieveUserInfo(tokenResult.accessToken());
    if (this.result.error != null) {
      this.response = this.result.error;
      throw new ErrorException("input", false);
    } 
  }
}
