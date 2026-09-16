package io.fusionauth.app.action.api.jwt;

import com.google.inject.Inject;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.jwt.ValidateResponse;
import io.fusionauth.jwt.domain.JWT;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.security.annotation.JWTAuthorizeMethod;

@Action(requiresAuthentication = true, scheme = {"unsafe-jwt"})
public class ValidateAction extends BaseAPIAction {
  @JSONResponse
  public ValidateResponse response;
  
  @Inject
  public ValidateAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @JWTAuthorizeMethod
  public boolean authorizeJWT(JWT paramJWT) {
    this.response = new ValidateResponse(paramJWT);
    return true;
  }
  
  public String get() {
    return "render";
  }
}
