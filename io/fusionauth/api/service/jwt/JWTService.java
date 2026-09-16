package io.fusionauth.api.service.jwt;

import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.jwt.claims.JWTType;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.oauth2.AccessToken;
import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.jwt.domain.Algorithm;
import io.fusionauth.jwt.domain.JWT;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface JWTService {
  JWTResult createJWT(Tenant paramTenant, User paramUser, AuthenticationType paramAuthenticationType, Application paramApplication, Map<String, Object> paramMap, UUID paramUUID, GrantType paramGrantType, JWTType paramJWTType, Set<String> paramSet, @Nullable String paramString);
  
  JWTResult createJWT(Tenant paramTenant, Entity paramEntity, Map<String, Entity> paramMap, Map<String, Set<String>> paramMap1, GrantType paramGrantType, Map<String, Object> paramMap2, @Nullable String paramString);
  
  JWTResult createJWTFromAnotherJWT(Tenant paramTenant, User paramUser, Application paramApplication, Map<String, Object> paramMap, JWT paramJWT, UUID paramUUID, GrantType paramGrantType, JWTType paramJWTType, @Nullable String paramString);
  
  JWTResult createJWTWithExpiration(Tenant paramTenant, User paramUser, AuthenticationType paramAuthenticationType, Application paramApplication, ZonedDateTime paramZonedDateTime, Map<String, Object> paramMap, UUID paramUUID, GrantType paramGrantType, JWTType paramJWTType, Set<String> paramSet);
  
  JWTResult createVendedJWT(Key paramKey, int paramInt, Map<String, Object> paramMap);
  
  RefreshResult refreshAccessToken(Tenant paramTenant, User paramUser, Application paramApplication, RefreshToken paramRefreshToken, String paramString1, Set<String> paramSet, Integer paramInteger, Supplier<Map<String, Object>> paramSupplier, EventInfo paramEventInfo, @Nullable String paramString2);
  
  boolean validateAccessTokenOwnershipForRefresh(RefreshToken paramRefreshToken, String paramString, Tenant paramTenant, Application paramApplication);
  
  ValidatedJWTResult validateEntityJWT(String paramString, FusionAuthJWTDecoder.JWTConstraints paramJWTConstraints, Tenant paramTenant, @Nonnull Entity paramEntity);
  
  ValidatedJWTResult validateIdTokenHint(String paramString);
  
  ValidationResult validateIssue(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString, JWT paramJWT, HTTPRequest paramHTTPRequest);
  
  ValidatedJWTResult validateJWT(String paramString, FusionAuthJWTDecoder.JWTConstraints paramJWTConstraints, JWTValidationContext paramJWTValidationContext);
  
  ValidationResult validateRefresh(Tenant paramTenant, String paramString1, HTTPRequest paramHTTPRequest, String paramString2, Integer paramInteger);
  
  ValidationResult validateRetrieve(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2);
  
  VendValidationResult validateVend(Tenant paramTenant, UUID paramUUID, int paramInt);
  
  public static class JWTResult {
    public Algorithm algorithm;
    
    public String encodedJWT;
    
    public JWT jwt;
    
    public JWTResult(JWT param1JWT, String param1String, Algorithm param1Algorithm) {
      this.algorithm = param1Algorithm;
      this.jwt = param1JWT;
      this.encodedJWT = param1String;
    }
  }
  
  public static class RefreshResult {
    public AccessToken accessToken;
    
    public RefreshToken refreshToken;
    
    public RefreshResult(AccessToken param1AccessToken, RefreshToken param1RefreshToken) {
      this.accessToken = param1AccessToken;
      this.refreshToken = param1RefreshToken;
    }
  }
  
  public static class SSORefreshTokenResult {
    public RefreshToken refreshToken;
    
    public User user;
  }
  
  public static class ValidationResult extends BaseValidationResult {
    public String accessToken;
    
    public Application application;
    
    @Nullable
    public String dPoPThumbprint;
    
    public Application jwtApplication;
    
    public UUID jwtApplicationId;
    
    public RefreshToken refreshToken;
    
    public Application refreshTokenApplication;
    
    public List<URI> resolvedResources;
    
    public Tenant tenant;
    
    public User user;
  }
  
  public static class VendValidationResult extends BaseValidationResult {
    public Key key;
    
    public int timeToLiveInSeconds;
  }
}
