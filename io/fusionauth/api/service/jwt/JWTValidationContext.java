package io.fusionauth.api.service.jwt;

import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import javax.annotation.Nonnull;

public interface JWTValidationContext {
  public static final class AudienceApplicationRequired extends Record implements JWTValidationContext {
    @Nonnull
    private final TenantSource tenantSource;
    
    public AudienceApplicationRequired(@Nonnull TenantSource param1TenantSource) {
      this.tenantSource = param1TenantSource;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/jwt/JWTValidationContext$AudienceApplicationRequired;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #22	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/jwt/JWTValidationContext$AudienceApplicationRequired;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #22	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/jwt/JWTValidationContext$AudienceApplicationRequired;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #22	-> 0
    }
    
    @Nonnull
    public TenantSource tenantSource() {
      return this.tenantSource;
    }
  }
  
  public static final class SuppliedTenantAndApplication extends Record implements JWTValidationContext {
    @Nonnull
    private final Tenant tenant;
    
    @Nonnull
    private final Application application;
    
    public SuppliedTenantAndApplication(@Nonnull Tenant param1Tenant, @Nonnull Application param1Application) {
      this.tenant = param1Tenant;
      this.application = param1Application;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/jwt/JWTValidationContext$SuppliedTenantAndApplication;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #29	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/jwt/JWTValidationContext$SuppliedTenantAndApplication;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #29	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/jwt/JWTValidationContext$SuppliedTenantAndApplication;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #29	-> 0
    }
    
    @Nonnull
    public Tenant tenant() {
      return this.tenant;
    }
    
    @Nonnull
    public Application application() {
      return this.application;
    }
  }
  
  public static final class SuppliedTenantAudienceApplicationIfResolvable extends Record implements JWTValidationContext {
    @Nonnull
    private final Tenant tenant;
    
    public SuppliedTenantAudienceApplicationIfResolvable(@Nonnull Tenant param1Tenant) {
      this.tenant = param1Tenant;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/jwt/JWTValidationContext$SuppliedTenantAudienceApplicationIfResolvable;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #37	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/jwt/JWTValidationContext$SuppliedTenantAudienceApplicationIfResolvable;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #37	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/jwt/JWTValidationContext$SuppliedTenantAudienceApplicationIfResolvable;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #37	-> 0
    }
    
    @Nonnull
    public Tenant tenant() {
      return this.tenant;
    }
  }
}
