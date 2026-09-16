package io.fusionauth.api.service.jwt;

import com.inversoft.error.Errors;
import com.inversoft.util.SecurityTools;
import io.fusionauth.api.domain.mybatis._RefreshToken;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.jwt.domain.JWT;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RefreshTokenService {
  public static final String DPoPThumbprint = "DPoPThumbprint";
  
  void createBulk(Tenant paramTenant, List<RefreshToken> paramList);
  
  RefreshToken createRefreshToken(UUID paramUUID, Tenant paramTenant, User paramUser, Application paramApplication, Map<String, Object> paramMap, RefreshToken.MetaData paramMetaData);
  
  String createRefreshTokenFromAnotherRefreshToken(UUID paramUUID, Tenant paramTenant, User paramUser, Application paramApplication, RefreshToken paramRefreshToken);
  
  RefreshToken createRefreshTokenWithStartInstant(UUID paramUUID, Tenant paramTenant, User paramUser, Application paramApplication, Map<String, Object> paramMap, RefreshToken.MetaData paramMetaData, ZonedDateTime paramZonedDateTime);
  
  RefreshToken createSSOSessionToken(User paramUser, RefreshToken.MetaData paramMetaData, String paramString);
  
  void refreshSSOSessionToken(RefreshToken paramRefreshToken, String paramString);
  
  RefreshToken retrieveForUpdateUsingSeed(String paramString);
  
  RefreshToken retrieveRefreshToken(String paramString);
  
  RefreshToken retrieveRefreshTokenById(UUID paramUUID);
  
  List<RefreshToken> retrieveRefreshTokenByUserIdAndApplicationId(UUID paramUUID1, UUID paramUUID2);
  
  RefreshTokenResult retrieveRefreshTokenForUser(Tenant paramTenant, User paramUser, String paramString);
  
  RefreshToken retrieveRefreshTokenUsingSeed(String paramString);
  
  List<RefreshToken> retrieveRefreshTokensByApplicationId(UUID paramUUID);
  
  List<RefreshToken> retrieveRefreshTokensByUserId(UUID paramUUID);
  
  int revokeExpiredRefreshTokens();
  
  void revokeFusionAuthSessionToken(UUID paramUUID, EventInfo paramEventInfo);
  
  boolean revokeRefreshToken(Tenant paramTenant, Application paramApplication, RefreshToken paramRefreshToken, User paramUser, EventInfo paramEventInfo);
  
  int revokeRefreshTokensByApplicationId(Tenant paramTenant, Application paramApplication, EventInfo paramEventInfo);
  
  void revokeRefreshTokensByUser(Tenant paramTenant, User paramUser, EventInfo paramEventInfo);
  
  int revokeRefreshTokensByUserAndApplicationId(Tenant paramTenant, User paramUser, Application paramApplication, EventInfo paramEventInfo);
  
  void revokeSSOSessionToken(String paramString, EventInfo paramEventInfo);
  
  Errors validateBulkCreate(Tenant paramTenant, List<RefreshToken> paramList, boolean paramBoolean);
  
  JWTService.ValidationResult validateDeleteRefreshToken(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString, UUID paramUUID3, JWT paramJWT, HTTPRequest paramHTTPRequest);
  
  JWTService.ValidationResult validateLogout(Tenant paramTenant, String paramString);
  
  JWTService.ValidationResult validateRefreshToken(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString, UUID paramUUID3, HTTPRequest paramHTTPRequest);
  
  public static class HashedRefreshToken extends _RefreshToken {
    public String hash;
    
    public String seed;
    
    public HashedRefreshToken(RefreshToken param1RefreshToken, String param1String) {
      super(param1RefreshToken);
      this.hash = param1String;
      this
        
        .seed = isVersion(2) ? this.token.substring(0, 20) : null;
    }
    
    static String v2Rotate(String param1String, boolean param1Boolean) {
      return param1Boolean ? (
        param1String.substring(0, 20) + param1String.substring(0, 20)) : 
        SecurityTools.secureRandom(48);
    }
  }
  
  public static class RefreshTokenResult {
    public Application application;
    
    public RefreshToken refreshToken;
  }
}
