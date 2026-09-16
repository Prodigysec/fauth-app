package io.fusionauth.api.service.oauth2;

import io.fusionauth.api.domain.TokenResult;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.http.server.HTTPRequest;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface DPoPService {
  @Nullable
  OAuthError checkRefreshToken(@Nonnull RefreshToken paramRefreshToken, @Nullable String paramString);
  
  @Nonnull
  TokenResult extractAccessTokenFromAuthorizationHeader(HTTPRequest paramHTTPRequest);
  
  boolean isJWTDPoPBound(String paramString);
  
  DPoPResult parseDPoP(HTTPRequest paramHTTPRequest);
  
  DPoPResult parseDPoP(HTTPRequest paramHTTPRequest, @Nullable String paramString);
  
  DPoPResult parseDPoPRefreshToken(HTTPRequest paramHTTPRequest, @Nonnull RefreshToken paramRefreshToken);
  
  DPoPResult parseDPoPRequired(HTTPRequest paramHTTPRequest, @Nullable String paramString);
  
  public static final class DPoPResult extends Record {
    @Nullable
    private final String dPoPThumbprint;
    
    @Nullable
    private final OAuthError error;
    
    public DPoPResult(@Nullable String param1String, @Nullable OAuthError param1OAuthError) {
      this.dPoPThumbprint = param1String;
      this.error = param1OAuthError;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #91	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #91	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #91	-> 0
    }
    
    @Nullable
    public String dPoPThumbprint() {
      return this.dPoPThumbprint;
    }
    
    @Nullable
    public OAuthError error() {
      return this.error;
    }
    
    public static DPoPResult None = new DPoPResult(null, null);
    
    public static DPoPResult error(String param1String) {
      return new DPoPResult(null, new OAuthError(OAuthError.OAuthErrorType.invalid_dpop_proof, param1String));
    }
    
    public static DPoPResult thumbprint(String param1String) {
      return new DPoPResult(param1String, null);
    }
    
    public boolean isError() {
      return (this.error != null);
    }
  }
}
