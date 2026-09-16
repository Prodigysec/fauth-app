package io.fusionauth.app.action.api.jwt;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.util.StringTools;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.jwt.JWTRefreshResponse;
import io.fusionauth.domain.api.jwt.RefreshRequest;
import io.fusionauth.domain.api.jwt.RefreshResponse;
import io.fusionauth.domain.api.jwt.RefreshTokenResponse;
import io.fusionauth.domain.api.jwt.RefreshTokenRevokeRequest;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.oauth2.OAuthApplicationRelationship;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.jwt.domain.JWT;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.security.annotation.AnonymousAccess;
import org.primeframework.mvc.security.annotation.JWTAuthorizeMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{tokenId}", requiresAuthentication = true, scheme = {"api", "scoped-jwt"})
public class RefreshAction extends BaseTenantAPIAction {
  @JSONRequest(httpMethods = {"DELETE"})
  public final RefreshTokenRevokeRequest deleteRequest = new RefreshTokenRevokeRequest();
  
  @JSONRequest(httpMethods = {"POST"})
  public final RefreshRequest request = new RefreshRequest();
  
  private final ApplicationCache applicationCache;
  
  private final HTTPRequest httpRequest;
  
  private final JWTService jwtService;
  
  private final RefreshTokenService refreshTokenService;
  
  @JSONResponse
  public RefreshResponse response;
  
  public UUID tokenId;
  
  public UUID userId;
  
  private JWT jwt;
  
  private JWTService.ValidationResult result;
  
  @Inject
  public RefreshAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ApplicationCache paramApplicationCache, HTTPRequest paramHTTPRequest, JWTService paramJWTService, RefreshTokenService paramRefreshTokenService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.applicationCache = paramApplicationCache;
    this.httpRequest = paramHTTPRequest;
    this.jwtService = paramJWTService;
    this.refreshTokenService = paramRefreshTokenService;
  }
  
  @JWTAuthorizeMethod
  public boolean authorizeJWT(JWT paramJWT) {
    if (this.frontEndSupport.isDELETE()) {
      this.jwt = paramJWT;
      this.deleteRequest.userId = StringTools.parseUUID(paramJWT.subject);
      return (this.deleteRequest.userId != null);
    } 
    return false;
  }
  
  public String delete() {
    if (this.deleteRequest.token != null || this.tokenId != null) {
      if (this.result.refreshToken == null)
        return "missing"; 
      if (this.jwt != null) {
        if (!this.result.refreshToken.userId.equals(this.deleteRequest.userId))
          return "missing"; 
        if (this.result.jwtApplicationId != null) {
          if (this.result.jwtApplication == null)
            return "missing"; 
          if (this.result.jwtApplication.oauthConfiguration.relationship == OAuthApplicationRelationship.ThirdParty && !Objects.equals(this.result.refreshToken.applicationId, this.result.jwtApplicationId))
            return "unauthorized"; 
        } 
      } 
      this.refreshTokenService.revokeRefreshToken(getTenant(), this.result.refreshTokenApplication, this.result.refreshToken, this.result.user, this.deleteRequest.eventInfo);
      return "success";
    } 
    if (this.jwt != null)
      return "unauthorized"; 
    if (this.deleteRequest.userId != null && this.deleteRequest.applicationId != null) {
      this.refreshTokenService.revokeRefreshTokensByUserAndApplicationId(getTenant(), this.result.user, this.result.application, this.deleteRequest.eventInfo);
      return "success";
    } 
    if (this.deleteRequest.userId != null) {
      this.refreshTokenService.revokeRefreshTokensByUser(getTenant(), this.result.user, this.deleteRequest.eventInfo);
      return "success";
    } 
    if (this.deleteRequest.applicationId != null) {
      this.refreshTokenService.revokeRefreshTokensByApplicationId(getTenant(), this.result.application, this.deleteRequest.eventInfo);
      return "success";
    } 
    return "missing";
  }
  
  @PostParameterMethod
  public void extractRefreshToken() {
    if (this.frontEndSupport.isPOST()) {
      Cookie cookie = this.frontEndSupport.getCookie("refresh_token");
      if (cookie != null)
        this.request.refreshToken = cookie.value; 
    } 
  }
  
  public String get() {
    if (this.userId != null) {
      List<RefreshToken> list = this.refreshTokenService.retrieveRefreshTokensByUserId(this.userId);
      list.removeIf(paramRefreshToken -> paramRefreshToken.isExpired(this.result.tenant, (Application)this.applicationCache.get(paramRefreshToken.applicationId)));
      list.forEach(RefreshToken::secure);
      this.response = new RefreshTokenResponse(list);
    } else {
      if (this.result.refreshToken == null || this.result.refreshToken.isExpired(this.result.tenant, this.result.application))
        return "missing"; 
      this.result.refreshToken.secure();
      this.response = new RefreshTokenResponse(this.result.refreshToken);
    } 
    return "render";
  }
  
  @AnonymousAccess
  public String post() {
    if (this.result.user == null)
      return "missing"; 
    List list = (List)Objects.requireNonNullElse(this.result.resolvedResources, List.of());
    JWTService.RefreshResult refreshResult = this.jwtService.refreshAccessToken(
        getTenant(), this.result.user, this.result.application, this.result.refreshToken, this.result.accessToken, 
        
        (Set<String>)Objects.requireNonNullElse(this.result.refreshToken.metaData.scopes, Collections.emptySet()), this.request.timeToLiveInSeconds, () -> {
          UserRegistration userRegistration = this.result.user.getRegistrationForApplication(this.result.refreshToken.applicationId);
          HashMap<Object, Object> hashMap = new HashMap<>(3);
          if (userRegistration != null) {
            hashMap.put("applicationId", this.result.refreshToken.applicationId);
            hashMap.put("roles", userRegistration.roles);
          } 
          if (paramList.isEmpty()) {
            hashMap.put("aud", this.result.refreshToken.applicationId);
          } else {
            hashMap.put("aud", Stream.concat(Stream.of(this.result.refreshToken.applicationId.toString()), paramList.stream().map(URI::toString)).collect(Collectors.toList()));
          } 
          return hashMap;
        }this.request.eventInfo, this.result.dPoPThumbprint);
    if (refreshResult == null) {
      this.frontEndSupport.addFieldError("refreshToken", "[invalid]refreshToken", new Object[0]);
      return "input";
    } 
    this.response = new JWTRefreshResponse(refreshResult.refreshToken.id, refreshResult.refreshToken.token, refreshResult.accessToken.token);
    JWTRefreshResponse jWTRefreshResponse = (JWTRefreshResponse)this.response;
    this.frontEndSupport.addHttpOnlySessionCookie("access_token", jWTRefreshResponse.token);
    int i = (getTenant().lookupJWTConfiguration(this.result.application)).refreshTokenTimeToLiveInMinutes;
    this.frontEndSupport.addHttpOnlyPersistentCookie("refresh_token", jWTRefreshResponse.refreshToken, TimeUnit.MINUTES.toSeconds(i));
    return "render";
  }
  
  public void setApplicationId(UUID paramUUID) {
    this.deleteRequest.applicationId = paramUUID;
  }
  
  public void setToken(String paramString) {
    this.deleteRequest.token = paramString;
  }
  
  public void setUserId(UUID paramUUID) {
    this.userId = paramUUID;
    this.deleteRequest.userId = paramUUID;
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validate() {
    this.result = this.jwtService.validateRetrieve(getOptionalTenant(), this.tokenId, this.userId);
    if (tenantScopedKeyInvalidForUser(this.result.application, this.result.user))
      this.result.refreshToken = null; 
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.deleteRequest.userId == null && this.deleteRequest.applicationId == null && this.deleteRequest.token == null && this.tokenId == null) {
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]);
      return;
    } 
    this.result = this.refreshTokenService.validateDeleteRefreshToken(getOptionalTenant(), this.deleteRequest.userId, this.deleteRequest.applicationId, this.deleteRequest.token, this.tokenId, this.jwt, this.httpRequest);
    if (tenantScopedKeyInvalidForUser((this.result.application != null) ? this.result.application : this.result.refreshTokenApplication, this.result.user))
      this.result.refreshToken = null; 
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePOST() {
    Cookie cookie = this.frontEndSupport.getCookie("access_token");
    if (cookie != null)
      this.request.token = cookie.value; 
    this.result = this.jwtService.validateRefresh(getOptionalTenant(), this.request.refreshToken, this.httpRequest, this.request.token, this.request.timeToLiveInSeconds);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
