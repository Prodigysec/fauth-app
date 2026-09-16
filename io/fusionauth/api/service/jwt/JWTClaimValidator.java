package io.fusionauth.api.service.jwt;

import io.fusionauth.domain.Application;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.Tenant;
import io.fusionauth.jwt.domain.JWT;
import javax.annotation.Nonnull;

public interface JWTClaimValidator {
  default ClaimValidationResult validateForApplication(@Nonnull JWT paramJWT, @Nonnull Tenant paramTenant, @Nonnull Application paramApplication) {
    return validateForApplication(paramJWT, paramTenant, paramApplication, false);
  }
  
  ClaimValidationResult validateForApplication(@Nonnull JWT paramJWT, @Nonnull Tenant paramTenant, @Nonnull Application paramApplication, boolean paramBoolean);
  
  ClaimValidationResult validateForEntity(@Nonnull JWT paramJWT, @Nonnull Tenant paramTenant, @Nonnull Entity paramEntity);
  
  ClaimValidationResult validateForTenant(@Nonnull JWT paramJWT, @Nonnull Tenant paramTenant);
  
  public static final class InvalidAudience extends Record implements ClaimValidationResult {
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$InvalidAudience;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #87	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$InvalidAudience;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #87	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$InvalidAudience;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #87	-> 0
    }
  }
  
  public static final class Mismatch extends Record implements ClaimValidationResult {
    private final String reason;
    
    public Mismatch(String param1String) {
      this.reason = param1String;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$Mismatch;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #90	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$Mismatch;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #90	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$Mismatch;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #90	-> 0
    }
    
    public String reason() {
      return this.reason;
    }
  }
  
  public static final class Valid extends Record implements ClaimValidationResult {
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$Valid;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #93	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$Valid;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #93	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$Valid;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #93	-> 0
    }
  }
  
  public static interface ClaimValidationResult {
    public static final class InvalidAudience extends Record implements ClaimValidationResult {
      public final String toString() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$InvalidAudience;)Ljava/lang/String;
        //   6: areturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #87	-> 0
      }
      
      public final int hashCode() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$InvalidAudience;)I
        //   6: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #87	-> 0
      }
      
      public final boolean equals(Object param2Object) {
        // Byte code:
        //   0: aload_0
        //   1: aload_1
        //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$InvalidAudience;Ljava/lang/Object;)Z
        //   7: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #87	-> 0
      }
    }
    
    public static final class Mismatch extends Record implements ClaimValidationResult {
      private final String reason;
      
      public Mismatch(String param2String) {
        this.reason = param2String;
      }
      
      public final String toString() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$Mismatch;)Ljava/lang/String;
        //   6: areturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #90	-> 0
      }
      
      public final int hashCode() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$Mismatch;)I
        //   6: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #90	-> 0
      }
      
      public final boolean equals(Object param2Object) {
        // Byte code:
        //   0: aload_0
        //   1: aload_1
        //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$Mismatch;Ljava/lang/Object;)Z
        //   7: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #90	-> 0
      }
      
      public String reason() {
        return this.reason;
      }
    }
    
    public static final class Valid extends Record implements ClaimValidationResult {
      public final String toString() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$Valid;)Ljava/lang/String;
        //   6: areturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #93	-> 0
      }
      
      public final int hashCode() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$Valid;)I
        //   6: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #93	-> 0
      }
      
      public final boolean equals(Object param2Object) {
        // Byte code:
        //   0: aload_0
        //   1: aload_1
        //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$Valid;Ljava/lang/Object;)Z
        //   7: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #93	-> 0
      }
    }
  }
}
