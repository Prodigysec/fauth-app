package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.jwt.RefreshToken;
import java.util.List;

public class RefreshTokenImportRequest {
  public List<RefreshToken> refreshTokens;
  
  public boolean validateDbConstraints;
  
  @JacksonConstructor
  public RefreshTokenImportRequest() {}
  
  public RefreshTokenImportRequest(List<RefreshToken> paramList) {
    this.refreshTokens = paramList;
  }
}
