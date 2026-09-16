package io.fusionauth.domain.api.jwt;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.jwt.RefreshToken;
import java.util.List;

public class RefreshTokenResponse implements RefreshResponse {
  public RefreshToken refreshToken;
  
  public List<RefreshToken> refreshTokens;
  
  @JacksonConstructor
  public RefreshTokenResponse() {}
  
  public RefreshTokenResponse(RefreshToken paramRefreshToken) {
    this.refreshToken = paramRefreshToken;
  }
  
  public RefreshTokenResponse(List<RefreshToken> paramList) {
    this.refreshTokens = paramList;
  }
}
