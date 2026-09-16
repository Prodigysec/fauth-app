package io.fusionauth.api.service.user;

import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ChangePasswordResult;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.api.service.authentication.LoginQueue;
import io.fusionauth.api.service.moderation.cleanspeak.ModerationAction;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.PasswordType;
import io.fusionauth.domain.SendSetPasswordIdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.VerificationStrategy;
import io.fusionauth.domain.api.identity.verify.ExistingUserStrategy;
import io.fusionauth.domain.email.EmailAddress;
import io.fusionauth.domain.email.EmailTemplate;
import io.fusionauth.domain.event.BaseEvent;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.search.SortField;
import io.fusionauth.jwt.domain.JWT;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public interface UserService {
  void completeVerify(Tenant paramTenant, Application paramApplication, User paramUser, ExternalIdentifier paramExternalIdentifier, EventInfo paramEventInfo);
  
  UserResult create(Tenant paramTenant, Application paramApplication, User paramUser, SendSetPasswordIdentityType paramSendSetPasswordIdentityType, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4, EventInfo paramEventInfo, List<ExternalIdentifier> paramList);
  
  void createBulk(Tenant paramTenant, List<User> paramList, String paramString, Integer paramInteger, EventInfo paramEventInfo);
  
  VerificationId createNewEmailVerificationId(Tenant paramTenant, Application paramApplication, String paramString, boolean paramBoolean);
  
  VerificationId createNewRegistrationVerificationId(Tenant paramTenant, Application paramApplication, String paramString, boolean paramBoolean);
  
  RegistrationResult createRegistration(Tenant paramTenant, Application paramApplication, User paramUser, UserRegistration paramUserRegistration, Collection<ApplicationRole> paramCollection, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, EventInfo paramEventInfo);
  
  UserResult createUserWithoutIdentity(Tenant paramTenant, IdentityProviderLink paramIdentityProviderLink, User paramUser, EventInfo paramEventInfo);
  
  boolean deactivate(Tenant paramTenant, User paramUser, EventInfo paramEventInfo);
  
  List<UUID> deactivateAllByIds(UUID paramUUID, List<UUID> paramList, EventInfo paramEventInfo, boolean paramBoolean);
  
  void deactivateUsers(Map<UUID, Tenant> paramMap, List<User> paramList, EventInfo paramEventInfo);
  
  boolean delete(Tenant paramTenant, User paramUser, EventInfo paramEventInfo);
  
  List<UUID> deleteAllByIds(UUID paramUUID, List<UUID> paramList, EventInfo paramEventInfo, boolean paramBoolean);
  
  List<UUID> deleteAllBySearchQuery(UUID paramUUID, String paramString, boolean paramBoolean1, boolean paramBoolean2, int paramInt, EventInfo paramEventInfo);
  
  List<UUID> deleteAllBySearchQueryString(UUID paramUUID, String paramString, boolean paramBoolean1, boolean paramBoolean2, int paramInt, EventInfo paramEventInfo);
  
  void deleteAllByTenantId(Tenant paramTenant, EventInfo paramEventInfo);
  
  void deleteRegistration(Tenant paramTenant, UserRegistration paramUserRegistration, User paramUser, Application paramApplication, EventInfo paramEventInfo);
  
  void deleteUnverifiedChildren(Tenant paramTenant, ZonedDateTime paramZonedDateTime);
  
  void deleteUnverifiedRegistrations(Tenant paramTenant, Application paramApplication, ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2);
  
  void deleteUnverifiedUsers(Tenant paramTenant, ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2);
  
  void deleteUsers(Map<UUID, Tenant> paramMap, List<User> paramList, EventInfo paramEventInfo);
  
  boolean disableTwoFactor(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2, String paramString3, EventInfo paramEventInfo);
  
  TwoFactorEnableResult enableTwoFactor(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, EventInfo paramEventInfo, String paramString8);
  
  void forceVerifyIdentity(Tenant paramTenant, User paramUser, UserIdentity paramUserIdentity, EventInfo paramEventInfo);
  
  ForgotPasswordResult forgotPassword(Tenant paramTenant, Application paramApplication, User paramUser, UserIdentity paramUserIdentity, String paramString1, String paramString2, boolean paramBoolean, Map<String, Object> paramMap, EventInfo paramEventInfo);
  
  List<String> generateTwoFactorRecoveryCodes(Tenant paramTenant, User paramUser);
  
  boolean handleImplicitVerification(Tenant paramTenant, Application paramApplication, User paramUser, UserIdentity paramUserIdentity, EventInfo paramEventInfo);
  
  void moderateUsername(User paramUser, Validator paramValidator);
  
  void moderateUsername(UserRegistration paramUserRegistration, Application paramApplication, UUID paramUUID, Validator paramValidator);
  
  void preVerify(Tenant paramTenant, Application paramApplication, ExternalIdentifier paramExternalIdentifier, EventInfo paramEventInfo);
  
  void reactivate(Tenant paramTenant, User paramUser, EventInfo paramEventInfo);
  
  void refreshSearchIndex();
  
  void reindexUser(User paramUser, UUID paramUUID);
  
  void removeParentEmail(User paramUser);
  
  DefaultUserService.EmailSendResult sendEmail(Tenant paramTenant, Application paramApplication, EmailTemplate paramEmailTemplate, List<User> paramList, List<EmailAddress> paramList1, List<String> paramList2, List<String> paramList3, Map<String, Object> paramMap, List<Locale> paramList4);
  
  void sendVerify(Tenant paramTenant, ExternalIdentifier paramExternalIdentifier, Application paramApplication, User paramUser);
  
  ExternalIdentifier startVerify(Tenant paramTenant, String paramString, IdentityType paramIdentityType, VerificationStrategy paramVerificationStrategy, User paramUser, Application paramApplication, Map<String, Object> paramMap);
  
  UserResult update(Tenant paramTenant, Application paramApplication, User paramUser1, User paramUser2, boolean paramBoolean, PasswordType paramPasswordType, EventInfo paramEventInfo);
  
  UserResult updateAllowConnectorIdChange(Tenant paramTenant, Application paramApplication, User paramUser1, User paramUser2, UpdateUserOptions paramUpdateUserOptions, EventInfo paramEventInfo, PasswordType paramPasswordType);
  
  void updateFromModeration(Map<UUID, ModerationAction> paramMap);
  
  void updateIdentityVerifiedReason(User paramUser, UserIdentity paramUserIdentity);
  
  ChangePasswordResult updatePassword(Tenant paramTenant, Application paramApplication, String paramString1, String paramString2, JWT paramJWT, RefreshToken paramRefreshToken, String paramString3, EventInfo paramEventInfo, User paramUser);
  
  ChangePasswordResult updatePasswordByChangePasswordId(Tenant paramTenant, Application paramApplication, User paramUser, ExternalIdentifier paramExternalIdentifier, String paramString1, String paramString2, String paramString3, EventInfo paramEventInfo);
  
  ChangePasswordResult updatePasswordByLoginId(Tenant paramTenant, Application paramApplication, String paramString1, String paramString2, String paramString3, String paramString4, EventInfo paramEventInfo, User paramUser);
  
  void updatePasswordChangeRequired(User paramUser);
  
  RegistrationResult updateRegistration(Tenant paramTenant, Application paramApplication, User paramUser, UserRegistration paramUserRegistration1, UserRegistration paramUserRegistration2, Collection<ApplicationRole> paramCollection, boolean paramBoolean, EventInfo paramEventInfo);
  
  void updateTwoFactor(User paramUser, String paramString1, String paramString2);
  
  IdentityValidationResult validateAdministrativeVerify(Tenant paramTenant, String paramString1, String paramString2);
  
  Errors validateBulk(Tenant paramTenant, List<User> paramList, boolean paramBoolean, String paramString, Integer paramInteger, Map<String, String> paramMap);
  
  ValidationResult validateBulkDelete(Tenant paramTenant, List<UUID> paramList, int paramInt, String paramString1, String paramString2);
  
  Errors validateChangePassword(UUID paramUUID, String paramString1, String paramString2);
  
  IdentityValidationResult validateCompleteVerify(Tenant paramTenant, String paramString1, String paramString2);
  
  ValidationResult validateConnectorCreate(Tenant paramTenant, User paramUser, Application paramApplication, EventInfo paramEventInfo, boolean paramBoolean);
  
  ValidationResult validateConnectorUpdate(Tenant paramTenant, Application paramApplication, EventInfo paramEventInfo, User paramUser);
  
  ValidationResult validateCreate(Tenant paramTenant, User paramUser, UUID paramUUID, boolean paramBoolean1, SendSetPasswordIdentityType paramSendSetPasswordIdentityType, @Deprecated boolean paramBoolean2, EventInfo paramEventInfo, boolean paramBoolean3);
  
  TwoFactorEnableValidationResult validateDisableTwoFactor(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString1, String paramString2);
  
  ValidationResult validateEmailSend(Tenant paramTenant, UUID paramUUID1, List<UUID> paramList, List<EmailAddress> paramList1, UUID paramUUID2);
  
  TwoFactorEnableValidationResult validateEnableTwoFactor(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7);
  
  ValidationResult validateForgotPassword(Tenant paramTenant, UUID paramUUID, String paramString, List<String> paramList);
  
  ValidationResult validateId(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2);
  
  ValidationResult validateLoginId(Tenant paramTenant, UUID paramUUID, String paramString, List<String> paramList);
  
  ValidationResult validatePreVerifiedCreate(Tenant paramTenant, User paramUser, UUID paramUUID, List<String> paramList, boolean paramBoolean1, SendSetPasswordIdentityType paramSendSetPasswordIdentityType, @Deprecated boolean paramBoolean2, EventInfo paramEventInfo);
  
  ValidationResult validateRecoveryCodeRequest(Tenant paramTenant, UUID paramUUID);
  
  ValidationResult validateRegistrationCreate(Tenant paramTenant, UserRegistration paramUserRegistration, UUID paramUUID, boolean paramBoolean1, boolean paramBoolean2);
  
  ValidationResult validateRegistrationDelete(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2);
  
  ValidationResult validateRegistrationRetrieve(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2);
  
  ValidationResult validateRegistrationUpdate(Tenant paramTenant, UserRegistration paramUserRegistration, UUID paramUUID, boolean paramBoolean);
  
  ValidationResult validateRetrieveById(Tenant paramTenant, UUID paramUUID);
  
  ValidationResult validateRetrieveByLoginId(Tenant paramTenant, String paramString, List<IdentityType> paramList);
  
  Errors validateSearchQuery(UUID paramUUID, String paramString1, String paramString2, List<SortField> paramList, int paramInt1, int paramInt2);
  
  IdentityValidationResult validateSendVerify(Tenant paramTenant, String paramString);
  
  IdentityValidationResult validateStartVerify(Tenant paramTenant, UUID paramUUID, String paramString1, String paramString2, VerificationStrategy paramVerificationStrategy, ExistingUserStrategy paramExistingUserStrategy);
  
  TwoFactorEnableValidationResult validateTwoFactorUpdate(Tenant paramTenant, UUID paramUUID, String paramString1, String paramString2);
  
  ValidationResult validateUpdate(Tenant paramTenant, User paramUser, boolean paramBoolean1, UUID paramUUID, String paramString, boolean paramBoolean2, EventInfo paramEventInfo, PasswordType paramPasswordType);
  
  ValidationResult validateUserId(Tenant paramTenant, UUID paramUUID);
  
  RegistrationValidationResult validateWithRegistrationConfiguration(Tenant paramTenant, Application paramApplication, Form paramForm, User paramUser, String paramString, boolean paramBoolean1, boolean paramBoolean2, EventInfo paramEventInfo);
  
  void verifyRegistration(Tenant paramTenant, Application paramApplication, User paramUser, EventInfo paramEventInfo, ExternalIdentifier paramExternalIdentifier);
  
  public static class ForgotPasswordResult {
    public boolean askedToSendButNoDeliveryAvailable;
    
    public String changePasswordId;
  }
  
  public static class IdentityValidationResult extends BaseValidationResult {
    public Application application;
    
    public ExternalIdentifier externalId;
    
    public UserIdentity identity;
    
    public IdentityType identityType;
    
    public Tenant tenant;
    
    public User user;
    
    public VerificationStrategy verificationStrategy;
  }
  
  public static class RegistrationResult {
    public LoginQueue.LoginQueueRawLogin rawLogin;
    
    public UserRegistration registration;
    
    public String registrationVerificationId;
    
    public String registrationVerificationOneTimeCode;
    
    public User user;
  }
  
  public static class RegistrationValidationResult {
    public List<Errors> errors = new ArrayList<>();
    
    public Map<String, String> fieldMapping = new HashMap<>();
  }
  
  public static class TwoFactorEnableResult {
    public List<String> recoveryCodes;
    
    public boolean success;
    
    public String twoFactorCode;
  }
  
  public static class TwoFactorEnableValidationResult extends BaseValidationResult {
    public Application application;
    
    public ExternalIdentifier externalId;
    
    public String method;
    
    public Tenant tenant;
    
    public String twoFactorMethodId;
    
    public User user;
  }
  
  public static class UpdateUserOptions implements Buildable<UpdateUserOptions> {
    public boolean deleteRefreshTokensOnPasswordChange;
    
    public boolean sendPasswordUpdatedEventOnPasswordChange;
    
    public boolean skipVerification;
  }
  
  public static class UserResult {
    public List<BaseEvent> events = new ArrayList<>(1);
    
    public LoginQueue.LoginQueueRawLogin rawLogin;
    
    public UserRegistration registration;
    
    public String registrationVerificationId;
    
    public String registrationVerificationOneTimeCode;
    
    public String twoFactorId;
    
    public User user;
    
    public Map<UserIdentity, ExternalIdentifier> verificationIds = new HashMap<>();
  }
  
  public static class ValidationResult extends BaseValidationResult {
    public final List<ApplicationRole> roles = new ArrayList<>();
    
    public Application application;
    
    public String email;
    
    public EmailTemplate emailTemplate;
    
    public User existing;
    
    public Map<String, String> fieldMapping = new HashMap<>();
    
    public List<IdentityType> identityTypes;
    
    public Application jwtApplication;
    
    public UUID jwtApplicationId;
    
    public String mobilePhone;
    
    public List<ExternalIdentifier> preVerifiedExternalIds = new ArrayList<>();
    
    public RefreshToken refreshToken;
    
    public UserRegistration registration;
    
    public String registrationVerificationId;
    
    public Map<UUID, String> registrationVerificationIds = new HashMap<>();
    
    public String registrationVerificationOneTimeCode;
    
    public Map<UUID, String> registrationVerificationOneTimeCodes = new HashMap<>();
    
    public SendSetPasswordIdentityType sendSetPasswordIdentityType;
    
    public Tenant tenant;
    
    public Map<UUID, Tenant> tenants = new HashMap<>();
    
    public User user;
    
    public List<User> users;
    
    public Map<UserIdentity, ExternalIdentifier> verificationIds = new HashMap<>();
  }
  
  public static class VerificationId {
    public String id;
    
    public String otp;
  }
}
