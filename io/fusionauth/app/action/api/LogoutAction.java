package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.LogoutRequest;
import io.fusionauth.http.Cookie;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class LogoutAction extends BaseTenantAPIAction {
  @JSONRequest
  public final LogoutRequest request = new LogoutRequest();
  
  private final RefreshTokenService refreshTokenService;
  
  private JWTService.ValidationResult result;
  
  @Inject
  public LogoutAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, RefreshTokenService paramRefreshTokenService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.refreshTokenService = paramRefreshTokenService;
  }
  
  public String post() {
    this.frontEndSupport.deleteCookies(new String[] { "access_token" });
    if (this.result.refreshToken != null) {
      if (this.request.global) {
        this.refreshTokenService.revokeRefreshTokensByUser(getTenant(), this.result.user, this.request.eventInfo);
      } else {
        this.refreshTokenService.revokeRefreshToken(getTenant(), this.result.application, this.result.refreshToken, this.result.user, this.request.eventInfo);
      } 
      this.frontEndSupport.deleteCookies(new String[] { "refresh_token" });
    } 
    return "success";
  }
  
  public void setGlobal(boolean paramBoolean) {
    this.request.global = paramBoolean;
  }
  
  public void setRefreshToken(String paramString) {
    this.request.refreshToken = paramString;
  }
  
  @PostParameterMethod
  public void setupRefreshToken() {
    if (this.request.refreshToken == null) {
      Cookie cookie = this.frontEndSupport.getCookie("refresh_token");
      if (cookie != null)
        this.request.refreshToken = cookie.value; 
    } 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    this.result = this.refreshTokenService.validateLogout(getOptionalTenant(), this.request.refreshToken);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
