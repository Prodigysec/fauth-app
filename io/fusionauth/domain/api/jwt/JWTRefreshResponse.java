package io.fusionauth.domain.api.jwt;

import com.inversoft.json.JacksonConstructor;
import java.util.UUID;

public class JWTRefreshResponse implements RefreshResponse {
  public String refreshToken;
  
  public UUID refreshTokenId;
  
  public String token;
  
  @JacksonConstructor
  public JWTRefreshResponse() {}
  
  public JWTRefreshResponse(UUID paramUUID, String paramString1, String paramString2) {
    this.refreshTokenId = paramUUID;
    this.refreshToken = paramString1;
    this.token = paramString2;
  }
}
