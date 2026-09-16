package io.fusionauth.api.domain;

import io.fusionauth.api.service.oauth2.DPoPService;
import io.fusionauth.domain.oauth2.OAuthError;
import javax.annotation.Nullable;

public final class TokenResult extends Record {
  @Nullable
  private final String accessToken;
  
  private final boolean dpopToken;
  
  @Nullable
  private final OAuthError error;
  
  public TokenResult(@Nullable String paramString, boolean paramBoolean, @Nullable OAuthError paramOAuthError) {
    this.accessToken = paramString;
    this.dpopToken = paramBoolean;
    this.error = paramOAuthError;
  }
  
  public final String toString() {
    // Byte code:
    //   0: aload_0
    //   1: <illegal opcode> toString : (Lio/fusionauth/api/domain/TokenResult;)Ljava/lang/String;
    //   6: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #16	-> 0
  }
  
  public final int hashCode() {
    // Byte code:
    //   0: aload_0
    //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/domain/TokenResult;)I
    //   6: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #16	-> 0
  }
  
  public final boolean equals(Object paramObject) {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: <illegal opcode> equals : (Lio/fusionauth/api/domain/TokenResult;Ljava/lang/Object;)Z
    //   7: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #16	-> 0
  }
  
  @Nullable
  public String accessToken() {
    return this.accessToken;
  }
  
  public boolean dpopToken() {
    return this.dpopToken;
  }
  
  @Nullable
  public OAuthError error() {
    return this.error;
  }
  
  public static final TokenResult None = new TokenResult(null, false, null);
  
  public static TokenResult bearer(String paramString) {
    return new TokenResult(paramString, false, null);
  }
  
  public static TokenResult dpop(DPoPService.DPoPResult paramDPoPResult, String paramString) {
    if (paramDPoPResult.isError())
      return new TokenResult(null, true, paramDPoPResult.error()); 
    return new TokenResult(paramString, true, null);
  }
  
  public static TokenResult error(OAuthError paramOAuthError) {
    return new TokenResult(null, false, paramOAuthError);
  }
}
