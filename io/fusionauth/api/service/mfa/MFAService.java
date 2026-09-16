package io.fusionauth.api.service.mfa;

import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.risk.CompositeRisk;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.MultiFactorAction;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.lambda.parameters.mfa.RequiredLambdaResult;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface MFAService {
  public static final Set<String> LicensedMethods = new HashSet<>(Arrays.asList(new String[] { "email", "sms" }));
  
  public static final Set<String> SupportedMethods = new LinkedHashSet<>(Arrays.asList(new String[] { "authenticator", "email", "sms" }));
  
  public static final Set<String> SupportedMethodsForSend = new LinkedHashSet<>(Arrays.asList(new String[] { "email", "sms" }));
  
  public static final String defaultRiskLevel = "NOT_COMPUTED";
  
  ChallengeResult determineChallengeRequired(MultiFactorAction paramMultiFactorAction, Tenant paramTenant, User paramUser, Application paramApplication, ExternalIdentifier paramExternalIdentifier, EventInfo paramEventInfo, CompositeRisk paramCompositeRisk, String paramString, AuthenticationType paramAuthenticationType, boolean paramBoolean);
  
  boolean isMethodRequired(Tenant paramTenant, Application paramApplication);
  
  ProcessLoginResult processLogin(Tenant paramTenant, Application paramApplication, ExternalIdentifier paramExternalIdentifier1, User paramUser, AuthenticationService.AuthenticationResult paramAuthenticationResult, ExternalIdentifier paramExternalIdentifier2, EventInfo paramEventInfo);
  
  void sendTwoFactorCode(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2, MessageType paramMessageType);
  
  void sendTwoFactorCodeForEnableDisable(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2, String paramString3, String paramString4, MessageType paramMessageType);
  
  StartTwoFactorResult startTwoFactorRequest(Tenant paramTenant, Application paramApplication, User paramUser, UserIdentity paramUserIdentity, String paramString1, String paramString2, Map<String, Object> paramMap, EventInfo paramEventInfo);
  
  ValidationResult validateSend(Tenant paramTenant, String paramString1, String paramString2, MessageType paramMessageType);
  
  ValidationResult validateSendForEnableDisable(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString1, String paramString2, String paramString3, String paramString4, MessageType paramMessageType);
  
  ValidationResult validateStart(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString, List<String> paramList);
  
  ValidationResult validateTwoFactorStatus(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString);
  
  public static final class ChallengeResult extends Record {
    private final List<String> configurableMethods;
    
    private final boolean required;
    
    private final boolean sendSuspiciousLoginEvent;
    
    private final boolean advancedMFALicensed;
    
    private final boolean advancedLambdaMFALicense;
    
    private final boolean intelligentMFALicensed;
    
    private final Optional<Application> licensedApplication;
    
    public ChallengeResult(List<String> param1List, boolean param1Boolean1, boolean param1Boolean2, boolean param1Boolean3, boolean param1Boolean4, boolean param1Boolean5, Optional<Application> param1Optional) {
      this.configurableMethods = param1List;
      this.required = param1Boolean1;
      this.sendSuspiciousLoginEvent = param1Boolean2;
      this.advancedMFALicensed = param1Boolean3;
      this.advancedLambdaMFALicense = param1Boolean4;
      this.intelligentMFALicensed = param1Boolean5;
      this.licensedApplication = param1Optional;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/mfa/MFAService$ChallengeResult;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #208	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/mfa/MFAService$ChallengeResult;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #208	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/mfa/MFAService$ChallengeResult;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #208	-> 0
    }
    
    public List<String> configurableMethods() {
      return this.configurableMethods;
    }
    
    public boolean required() {
      return this.required;
    }
    
    public boolean sendSuspiciousLoginEvent() {
      return this.sendSuspiciousLoginEvent;
    }
    
    public boolean advancedMFALicensed() {
      return this.advancedMFALicensed;
    }
    
    public boolean advancedLambdaMFALicense() {
      return this.advancedLambdaMFALicense;
    }
    
    public boolean intelligentMFALicensed() {
      return this.intelligentMFALicensed;
    }
    
    public Optional<Application> licensedApplication() {
      return this.licensedApplication;
    }
    
    ChallengeResult updateWithLambdaAnswer(RequiredLambdaResult param1RequiredLambdaResult) {
      return new ChallengeResult(configurableMethods(), param1RequiredLambdaResult.required, param1RequiredLambdaResult.sendSuspiciousLoginEvent, 

          
          advancedMFALicensed(), 
          advancedLambdaMFALicense(), 
          intelligentMFALicensed(), 
          licensedApplication());
    }
  }
  
  public static final class ProcessLoginResult extends Record {
    private final boolean required;
    
    private final boolean sendSuspiciousLoginEvent;
    
    public ProcessLoginResult(boolean param1Boolean1, boolean param1Boolean2) {
      this.required = param1Boolean1;
      this.sendSuspiciousLoginEvent = param1Boolean2;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/mfa/MFAService$ProcessLoginResult;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #227	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/mfa/MFAService$ProcessLoginResult;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #227	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/mfa/MFAService$ProcessLoginResult;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #227	-> 0
    }
    
    public boolean required() {
      return this.required;
    }
    
    public boolean sendSuspiciousLoginEvent() {
      return this.sendSuspiciousLoginEvent;
    }
  }
  
  public static class StartTwoFactorResult {
    public String code;
    
    public String twoFactorId;
    
    public StartTwoFactorResult(String param1String1, String param1String2) {
      this.code = param1String1;
      this.twoFactorId = param1String2;
    }
  }
  
  public static class ValidationResult extends BaseValidationResult {
    public Application application;
    
    public ExternalIdentifier code;
    
    public ExternalIdentifier id;
    
    public MessageType messageType;
    
    public String method;
    
    public String methodId;
    
    public ReactorStatus reactorStatus;
    
    public Tenant tenant;
    
    public User user;
    
    public UserIdentity userIdentity;
  }
}
