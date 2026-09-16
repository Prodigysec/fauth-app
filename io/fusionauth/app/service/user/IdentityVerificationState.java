package io.fusionauth.app.service.user;

import io.fusionauth.api.service.user.IdentityHelper;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.VerificationStrategy;

public final class IdentityVerificationState extends Record {
  private final String externalId;
  
  private final String identityValue;
  
  private final VerificationStrategy verificationStrategy;
  
  private final boolean completed;
  
  public IdentityVerificationState(String paramString1, String paramString2, VerificationStrategy paramVerificationStrategy, boolean paramBoolean) {
    this.externalId = paramString1;
    this.identityValue = paramString2;
    this.verificationStrategy = paramVerificationStrategy;
    this.completed = paramBoolean;
  }
  
  public final String toString() {
    // Byte code:
    //   0: aload_0
    //   1: <illegal opcode> toString : (Lio/fusionauth/app/service/user/IdentityVerificationState;)Ljava/lang/String;
    //   6: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #20	-> 0
  }
  
  public final int hashCode() {
    // Byte code:
    //   0: aload_0
    //   1: <illegal opcode> hashCode : (Lio/fusionauth/app/service/user/IdentityVerificationState;)I
    //   6: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #20	-> 0
  }
  
  public final boolean equals(Object paramObject) {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: <illegal opcode> equals : (Lio/fusionauth/app/service/user/IdentityVerificationState;Ljava/lang/Object;)Z
    //   7: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #20	-> 0
  }
  
  public String externalId() {
    return this.externalId;
  }
  
  public String identityValue() {
    return this.identityValue;
  }
  
  public VerificationStrategy verificationStrategy() {
    return this.verificationStrategy;
  }
  
  public boolean completed() {
    return this.completed;
  }
  
  public IdentityVerificationState(String paramString, UserIdentity paramUserIdentity, VerificationStrategy paramVerificationStrategy, boolean paramBoolean) {
    this(paramString, IdentityHelper.canonicalizeValue(paramUserIdentity), paramVerificationStrategy, paramBoolean);
  }
  
  public IdentityVerificationState complete() {
    return new IdentityVerificationState(this.externalId, this.identityValue, this.verificationStrategy, true);
  }
}
