package io.fusionauth.app.action.api.jwt;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.util.StringTools;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.jwt.claims.JWTType;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.jwt.IssueResponse;
import io.fusionauth.domain.oauth2.OAuthApplicationRelationship;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.jwt.domain.JWT;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.security.annotation.JWTAuthorizeMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"scoped-jwt"})
@JSON(code = "not-registered", status = 202)
public class IssueAction extends BaseTenantAPIAction {
  private final HTTPRequest httpRequest;
  
  private final JWTService jwtService;
  
  private final RefreshTokenService refreshTokenService;
  
  public UUID applicationId;
  
  public String refreshToken;
  
  @JSONResponse
  public IssueResponse response;
  
  private JWT jwt;
  
  private JWTService.ValidationResult result;
  
  private UUID userId;
  
  @Inject
  public IssueAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, HTTPRequest paramHTTPRequest, JWTService paramJWTService, RefreshTokenService paramRefreshTokenService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.httpRequest = paramHTTPRequest;
    this.jwtService = paramJWTService;
    this.refreshTokenService = paramRefreshTokenService;
  }
  
  @JWTAuthorizeMethod
  public boolean authorizeJWT(JWT paramJWT) {
    this.jwt = paramJWT;
    this.userId = StringTools.parseUUID(paramJWT.subject);
    return (this.userId != null);
  }
  
  public String get() {
    if (this.result.user == null)
      return "unauthorized"; 
    if (this.result.jwtApplicationId != null && (this.result.jwtApplication == null || this.result.jwtApplication.oauthConfiguration.relationship == OAuthApplicationRelationship.ThirdParty))
      return "unauthorized"; 
    HashMap<Object, Object> hashMap = new HashMap<>(3);
    hashMap.put("aud", this.applicationId);
    UserRegistration userRegistration = this.result.user.getRegistrationForApplication(this.applicationId);
    if (userRegistration == null) {
      JWTService.JWTResult jWTResult1 = this.jwtService.createJWTFromAnotherJWT(getTenant(), this.result.user, this.result.application, (Map)hashMap, this.jwt, this.result.application.lambdaConfiguration.accessTokenPopulateId, null, JWTType.AccessToken, this.result.dPoPThumbprint);
      this.response = new IssueResponse(jWTResult1.encodedJWT);
      return "not-registered";
    } 
    hashMap.put("applicationId", this.applicationId);
    hashMap.put("roles", userRegistration.roles);
    UUID uUID = (this.result.refreshToken != null && this.result.application.loginConfiguration.generateRefreshTokens) ? UUID.randomUUID() : null;
    hashMap.put("sid", uUID);
    JWTService.JWTResult jWTResult = this.jwtService.createJWTFromAnotherJWT(getTenant(), this.result.user, this.result.application, (Map)hashMap, this.jwt, this.result.application.lambdaConfiguration.accessTokenPopulateId, null, JWTType.AccessToken, this.result.dPoPThumbprint);
    this.response = new IssueResponse(jWTResult.encodedJWT);
    if (this.result.refreshToken != null && 
      this.result.application.loginConfiguration.generateRefreshTokens)
      this.response.refreshToken = this.refreshTokenService.createRefreshTokenFromAnotherRefreshToken(uUID, getTenant(), this.result.user, this.result.application, this.result.refreshToken); 
    return "render";
  }
  
  @PostParameterMethod
  public void setupRefreshToken() {
    Cookie cookie = this.frontEndSupport.getCookie("refresh_token");
    if (cookie != null)
      this.refreshToken = cookie.value; 
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    this.result = this.jwtService.validateIssue(getOptionalTenant(), this.applicationId, this.userId, this.refreshToken, this.jwt, this.httpRequest);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
