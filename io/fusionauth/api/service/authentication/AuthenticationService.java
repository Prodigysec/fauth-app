package io.fusionauth.api.service.authentication;

import com.inversoft.error.Errors;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.webauthn.AuthenticatorAssertionResponse;
import io.fusionauth.api.domain.webauthn.PublicKeyCredential;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.api.service.connector.Connector;
import io.fusionauth.api.service.risk.CompositeRisk;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.WebAuthnCredential;
import io.fusionauth.domain.api.LoginPreventedResponse;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorPolicy;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.util.List;
import java.util.UUID;

public interface AuthenticationService {
  AuthenticationResult authenticate(Tenant paramTenant, Application paramApplication, String paramString1, List<IdentityType> paramList, String paramString2, ExternalIdentifier paramExternalIdentifier, EventInfo paramEventInfo, Double paramDouble, boolean paramBoolean);
  
  AuthenticationResult authenticateOneTimePassword(Tenant paramTenant, Application paramApplication, ExternalIdentifier paramExternalIdentifier1, ExternalIdentifier paramExternalIdentifier2, EventInfo paramEventInfo, Double paramDouble, boolean paramBoolean);
  
  AuthenticationResult authenticatePasswordless(Tenant paramTenant, Application paramApplication, ExternalIdentifier paramExternalIdentifier1, ExternalIdentifier paramExternalIdentifier2, EventInfo paramEventInfo, Double paramDouble, boolean paramBoolean);
  
  AuthenticationResult authenticateTwoFactor(Tenant paramTenant, Application paramApplication, String paramString, ExternalIdentifier paramExternalIdentifier, boolean paramBoolean, EventInfo paramEventInfo);
  
  AuthenticationResult authenticateWebauthn(Tenant paramTenant, Application paramApplication, User paramUser, WebAuthnCredential paramWebAuthnCredential, ExternalIdentifier paramExternalIdentifier1, ExternalIdentifier paramExternalIdentifier2, byte[] paramArrayOfbyte, PublicKeyCredential<AuthenticatorAssertionResponse> paramPublicKeyCredential, EventInfo paramEventInfo, Double paramDouble, boolean paramBoolean);
  
  AuthenticationResult ping(Tenant paramTenant, User paramUser, Application paramApplication, EventInfo paramEventInfo);
  
  List<LoginPreventedResponse> retrieveLoginPreventedActions(User paramUser, UUID paramUUID);
  
  ValidationResult validateAuthenticate(Tenant paramTenant, UUID paramUUID, String paramString1, String paramString2, List<String> paramList, Double paramDouble);
  
  ValidationResult validateOneTimePassword(Tenant paramTenant, UUID paramUUID, ExternalIdentifier paramExternalIdentifier);
  
  Errors validatePasswordless(String paramString1, String paramString2, ExternalIdentifier paramExternalIdentifier);
  
  ValidationResult validatePing(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2);
  
  ValidationResult validateTwoFactor(Tenant paramTenant, UUID paramUUID, String paramString1, String paramString2);
  
  TwoFactorValidationResult validateTwoFactorCode(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, EventInfo paramEventInfo);
  
  public static interface TwoFactorValidationResult {
    default boolean success() {
      return true;
    }
    
    public static final class ExternalIdSuccess extends Record implements TwoFactorValidationResult {
      private final ExternalIdentifier id;
      
      public ExternalIdSuccess(ExternalIdentifier param2ExternalIdentifier) {
        this.id = param2ExternalIdentifier;
      }
      
      public final String toString() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$ExternalIdSuccess;)Ljava/lang/String;
        //   6: areturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #236	-> 0
      }
      
      public final int hashCode() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$ExternalIdSuccess;)I
        //   6: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #236	-> 0
      }
      
      public final boolean equals(Object param2Object) {
        // Byte code:
        //   0: aload_0
        //   1: aload_1
        //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$ExternalIdSuccess;Ljava/lang/Object;)Z
        //   7: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #236	-> 0
      }
      
      public ExternalIdentifier id() {
        return this.id;
      }
    }
    
    public static final class Failure extends Record implements TwoFactorValidationResult {
      public final boolean equals(Object param2Object) {
        // Byte code:
        //   0: aload_0
        //   1: aload_1
        //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$Failure;Ljava/lang/Object;)Z
        //   7: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #239	-> 0
      }
      
      public final int hashCode() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$Failure;)I
        //   6: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #239	-> 0
      }
      
      public final String toString() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$Failure;)Ljava/lang/String;
        //   6: areturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #239	-> 0
      }
      
      public boolean success() {
        return false;
      }
    }
    
    public static final class RecoveryCodeSuccess extends Record implements TwoFactorValidationResult {
      public final String toString() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$RecoveryCodeSuccess;)Ljava/lang/String;
        //   6: areturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #245	-> 0
      }
      
      public final int hashCode() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$RecoveryCodeSuccess;)I
        //   6: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #245	-> 0
      }
      
      public final boolean equals(Object param2Object) {
        // Byte code:
        //   0: aload_0
        //   1: aload_1
        //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$RecoveryCodeSuccess;Ljava/lang/Object;)Z
        //   7: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #245	-> 0
      }
    }
    
    public static final class TOTPSuccess extends Record implements TwoFactorValidationResult {
      public final String toString() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$TOTPSuccess;)Ljava/lang/String;
        //   6: areturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #248	-> 0
      }
      
      public final int hashCode() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$TOTPSuccess;)I
        //   6: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #248	-> 0
      }
      
      public final boolean equals(Object param2Object) {
        // Byte code:
        //   0: aload_0
        //   1: aload_1
        //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$TOTPSuccess;Ljava/lang/Object;)Z
        //   7: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #248	-> 0
      }
    }
  }
  
  public static final class ExternalIdSuccess extends Record implements TwoFactorValidationResult {
    private final ExternalIdentifier id;
    
    public ExternalIdSuccess(ExternalIdentifier param1ExternalIdentifier) {
      this.id = param1ExternalIdentifier;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$ExternalIdSuccess;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #236	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$ExternalIdSuccess;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #236	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$ExternalIdSuccess;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #236	-> 0
    }
    
    public ExternalIdentifier id() {
      return this.id;
    }
  }
  
  public static final class Failure extends Record implements TwoFactorValidationResult {
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$Failure;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #239	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$Failure;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #239	-> 0
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$Failure;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #239	-> 0
    }
    
    public boolean success() {
      return false;
    }
  }
  
  public static final class RecoveryCodeSuccess extends Record implements TwoFactorValidationResult {
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$RecoveryCodeSuccess;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #245	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$RecoveryCodeSuccess;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #245	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$RecoveryCodeSuccess;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #245	-> 0
    }
  }
  
  public static final class TOTPSuccess extends Record implements TwoFactorValidationResult {
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$TOTPSuccess;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #248	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$TOTPSuccess;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #248	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$TOTPSuccess;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #248	-> 0
    }
  }
  
  public static class AuthenticationResult implements Buildable<AuthenticationResult> {
    public static final String X_FusionAuth_ValidationId = "X-FusionAuth-ValidationId";
    
    public final UUID connectorId;
    
    public final AuthenticationType type;
    
    public final User user;
    
    public boolean authenticatedNotRegistered;
    
    public String changePasswordId;
    
    public CompositeRisk clientRisk;
    
    public List<String> configurableTwoFactorMethods;
    
    public Connector connector;
    
    public BaseConnectorConfiguration connectorConfiguration;
    
    public ConnectorPolicy connectorPolicy;
    
    public Debugger debugger;
    
    public String emailVerificationId;
    
    public RuntimeException exception;
    
    public ExternalIdentifier externalIdentifier;
    
    public String identityVerificationId;
    
    public AuthenticationService.LoginLambdaValidationResult loginLambdaValidationResult;
    
    public UUID loginValidationId;
    
    public String pendingIdPLinkId;
    
    public LoginQueue.LoginQueueRawLogin rawLogin;
    
    public String registrationVerificationId;
    
    public String registrationVerificationOneTimeCode;
    
    public boolean trustOnly;
    
    public String trustToken;
    
    public String twoFactorId;
    
    public String twoFactorTrustId;
    
    public UserIdentity userIdentity;
    
    public AuthenticationResult(AuthenticationType param1AuthenticationType, UUID param1UUID, User param1User) {
      this.connectorId = param1UUID;
      this.type = param1AuthenticationType;
      this.user = param1User;
    }
  }
  
  public static class LoginLambdaValidationContext implements Buildable<LoginLambdaValidationContext> {
    public AuthenticationType authenticationType;
    
    public IdentityProviderContext identityProvider;
    
    public LoginLambdaValidationContext(AuthenticationType param1AuthenticationType) {
      this.authenticationType = param1AuthenticationType;
    }
    
    public static class IdentityProviderContext {
      public UUID id;
      
      public String name;
      
      public IdentityProviderType type;
      
      public IdentityProviderContext(BaseIdentityProvider<?> param2BaseIdentityProvider) {
        this.id = param2BaseIdentityProvider.id;
        this.name = param2BaseIdentityProvider.name;
        this.type = param2BaseIdentityProvider.getType();
      }
    }
  }
  
  public static class IdentityProviderContext {
    public UUID id;
    
    public String name;
    
    public IdentityProviderType type;
    
    public IdentityProviderContext(BaseIdentityProvider<?> param1BaseIdentityProvider) {
      this.id = param1BaseIdentityProvider.id;
      this.name = param1BaseIdentityProvider.name;
      this.type = param1BaseIdentityProvider.getType();
    }
  }
  
  public static class LoginLambdaValidationResult {
    public Errors errors = new Errors();
  }
  
  public static class ValidationResult extends BaseValidationResult {
    public Application application;
    
    public List<IdentityType> identityTypes;
    
    public Tenant tenant;
    
    public User user;
  }
}
