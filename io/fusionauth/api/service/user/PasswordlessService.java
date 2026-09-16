package io.fusionauth.api.service.user;

import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.PasswordlessStrategy;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public interface PasswordlessService {
  PasswordlessCode createCode(Tenant paramTenant, Application paramApplication, User paramUser, String paramString, List<IdentityType> paramList, Map<String, Object> paramMap, PasswordlessStrategy paramPasswordlessStrategy);
  
  boolean isEnabled(Tenant paramTenant, Application paramApplication);
  
  void sendCode(Tenant paramTenant, Application paramApplication, User paramUser, String paramString, ExternalIdentifier paramExternalIdentifier, Map<String, Object> paramMap);
  
  ValidationResult validateSend(Tenant paramTenant, UUID paramUUID, String paramString1, String paramString2);
  
  ValidationResult validateStart(Tenant paramTenant, UUID paramUUID, String paramString, List<String> paramList, PasswordlessStrategy paramPasswordlessStrategy);
  
  public static final class PasswordlessCode extends Record {
    private final String code;
    
    private final String otp;
    
    public String otp() {
      return this.otp;
    }
    
    public String code() {
      return this.code;
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/user/PasswordlessService$PasswordlessCode;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #87	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/user/PasswordlessService$PasswordlessCode;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #87	-> 0
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/user/PasswordlessService$PasswordlessCode;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #87	-> 0
    }
    
    public PasswordlessCode(String param1String1, String param1String2) {
      Objects.requireNonNull(param1String1);
      this.code = param1String1;
      this.otp = param1String2;
    }
  }
  
  public static class ValidationResult extends BaseValidationResult {
    public Application application;
    
    public ExternalIdentifier code;
    
    public List<IdentityType> identityTypes = new ArrayList<>();
    
    public PasswordlessStrategy strategy;
    
    public Tenant tenant;
    
    public User user;
  }
}
