package io.fusionauth.api.service.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.error.Errors;
import com.inversoft.search.SearchEngineException;
import com.inversoft.util.Pair;
import com.inversoft.util.SecurityTools;
import com.inversoft.util.StringTools;
import com.inversoft.validator.EmailValidator;
import com.inversoft.validator.Validator;
import freemarker.core.ParseException;
import freemarker.template.TemplateException;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.ChangePasswordResult;
import io.fusionauth.api.domain.ConsentMapper;
import io.fusionauth.api.domain.EmailTemplateMapper;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.FamilyMapper;
import io.fusionauth.api.domain.GroupMapper;
import io.fusionauth.api.domain.IdentityExternalIdHelper;
import io.fusionauth.api.domain.IdentityProviderLinkMapper;
import io.fusionauth.api.domain.LoginMapper;
import io.fusionauth.api.domain.PreviousPassword;
import io.fusionauth.api.domain.RequestFrequencyMapper;
import io.fusionauth.api.domain.SCIMMapper;
import io.fusionauth.api.domain.SearchContinuationToken;
import io.fusionauth.api.domain.SearchEngineType;
import io.fusionauth.api.domain.UserActionLogMapper;
import io.fusionauth.api.domain.UserCommentMapper;
import io.fusionauth.api.domain.UserIdentityStatus;
import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.api.domain.WebAuthnCredentialMapper;
import io.fusionauth.api.domain.ZonedDateTimeWrapper;
import io.fusionauth.api.domain.guice.FusionAuthTenantId;
import io.fusionauth.api.domain.mybatis._UserRegistration;
import io.fusionauth.api.security.PasswordEncryptorLibrary;
import io.fusionauth.api.security.PhoneNumberValidator;
import io.fusionauth.api.service.TemplateHelper;
import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.api.service.authentication.LoginQueue;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.count.RegistrationCountService;
import io.fusionauth.api.service.email.EmailProxy;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.message.MessageTemplateHelper;
import io.fusionauth.api.service.messenger.MessageTemplateException;
import io.fusionauth.api.service.messenger.MessengerException;
import io.fusionauth.api.service.messenger.MessengerService;
import io.fusionauth.api.service.mfa.MFAService;
import io.fusionauth.api.service.moderation.ModerationService;
import io.fusionauth.api.service.moderation.cleanspeak.FilterAction;
import io.fusionauth.api.service.moderation.cleanspeak.ModerateResponse;
import io.fusionauth.api.service.moderation.cleanspeak.ModerationAction;
import io.fusionauth.api.service.moderation.cleanspeak.ModerationType;
import io.fusionauth.api.service.search.ElasticsearchUserSearchEngine;
import io.fusionauth.api.service.search.UserSearchEngine;
import io.fusionauth.api.service.security.RateLimitService;
import io.fusionauth.api.service.security.RecoveryCodeHasher;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.AuditService;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.service.system.TenantService;
import io.fusionauth.api.service.useraction.ActionService;
import io.fusionauth.api.service.useraction.UserActionLogService;
import io.fusionauth.api.service.useraction.UserActionService;
import io.fusionauth.api.time.TimeUtils;
import io.fusionauth.api.util.EmailTools;
import io.fusionauth.api.util.EncoderTools;
import io.fusionauth.api.util.LocaleTools;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.api.util.NumberTools;
import io.fusionauth.api.util.PhoneNumberTools;
import io.fusionauth.api.util.TwoFactorTools;
import io.fusionauth.api.util.UserTools;
import io.fusionauth.app.primeframework.error.MessageTemplateExceptionHandler;
import io.fusionauth.app.primeframework.error.MessengerExceptionHandler;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.AuthenticatorConfiguration;
import io.fusionauth.domain.ChangePasswordReason;
import io.fusionauth.domain.ContentStatus;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.IdentityVerifiedReason;
import io.fusionauth.domain.PasswordType;
import io.fusionauth.domain.RateLimitedRequestType;
import io.fusionauth.domain.Requirable;
import io.fusionauth.domain.SendSetPasswordIdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TenantUsernameConfiguration;
import io.fusionauth.domain.Tenantable;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.UserActionLog;
import io.fusionauth.domain.UserConsent;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.UserTwoFactorConfiguration;
import io.fusionauth.domain.VerificationStrategy;
import io.fusionauth.domain.api.email.SendResponse;
import io.fusionauth.domain.api.identity.verify.ExistingUserStrategy;
import io.fusionauth.domain.api.user.ActionRequest;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.email.EmailAddress;
import io.fusionauth.domain.email.EmailTemplate;
import io.fusionauth.domain.event.BaseEvent;
import io.fusionauth.domain.event.UserBulkCreateEvent;
import io.fusionauth.domain.event.UserCreateCompleteEvent;
import io.fusionauth.domain.event.UserCreateEvent;
import io.fusionauth.domain.event.UserDeactivateEvent;
import io.fusionauth.domain.event.UserDeleteCompleteEvent;
import io.fusionauth.domain.event.UserDeleteEvent;
import io.fusionauth.domain.event.UserEmailUpdateEvent;
import io.fusionauth.domain.event.UserEmailVerifiedEvent;
import io.fusionauth.domain.event.UserIdentityUpdateEvent;
import io.fusionauth.domain.event.UserIdentityVerifiedEvent;
import io.fusionauth.domain.event.UserLoginIdDuplicateOnCreateEvent;
import io.fusionauth.domain.event.UserLoginIdDuplicateOnUpdateEvent;
import io.fusionauth.domain.event.UserLoginSuspiciousEvent;
import io.fusionauth.domain.event.UserPasswordResetSendEvent;
import io.fusionauth.domain.event.UserPasswordResetStartEvent;
import io.fusionauth.domain.event.UserPasswordResetSuccessEvent;
import io.fusionauth.domain.event.UserPasswordUpdateEvent;
import io.fusionauth.domain.event.UserReactivateEvent;
import io.fusionauth.domain.event.UserRegistrationCreateCompleteEvent;
import io.fusionauth.domain.event.UserRegistrationCreateEvent;
import io.fusionauth.domain.event.UserRegistrationDeleteCompleteEvent;
import io.fusionauth.domain.event.UserRegistrationDeleteEvent;
import io.fusionauth.domain.event.UserRegistrationUpdateCompleteEvent;
import io.fusionauth.domain.event.UserRegistrationUpdateEvent;
import io.fusionauth.domain.event.UserRegistrationVerifiedEvent;
import io.fusionauth.domain.event.UserTwoFactorMethodAddEvent;
import io.fusionauth.domain.event.UserTwoFactorMethodRemoveEvent;
import io.fusionauth.domain.event.UserUpdateCompleteEvent;
import io.fusionauth.domain.event.UserUpdateEvent;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.FormStep;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.SortField;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.plugin.spi.security.PasswordEncryptor;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.guice.transactional.Transactional;
import org.primeframework.email.domain.EmailAddress;
import org.primeframework.email.domain.SendResult;
import org.primeframework.email.service.EmailService;
import org.primeframework.email.service.SendEmailBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultUserService implements UserService {
  private static final String emailTemplatePlaceholderValue = "{{For testing purposes only. This template should be configured to be sent by FusionAuth with a generated value.}}";
  
  private static final Logger logger = LoggerFactory.getLogger(DefaultUserService.class);
  
  private final ActionService actionService;
  
  private final ApplicationCache applicationCache;
  
  private final ApplicationReaderService applicationReader;
  
  private final AuditService auditService;
  
  private final AuthenticationService authenticationService;
  
  private final GroupMapper backgroundGroupMapper;
  
  private final SqlSessionFactory backgroundSQLSessionFactory;
  
  private final UserMapper backgroundUserMapper;
  
  private final FusionAuthConfiguration configuration;
  
  private final ConsentMapper consentMapper;
  
  private final EmailProxy emailProxy;
  
  private final EmailTemplateMapper emailTemplateMapper;
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final ExternalIdentifierService externalIdentifierService;
  
  private final FamilyMapper familyMapper;
  
  private final UUID fusionauthTenantId;
  
  private final GroupMapper groupMapper;
  
  private final IdentityProviderLinkMapper identityProviderLinkMapper;
  
  private final LoginMapper loginMapper;
  
  private final MessengerService messengerService;
  
  private final MFAService mfaService;
  
  private final ModerationService moderationService;
  
  private final ObjectMapper objectMapper;
  
  private final PasswordEncryptorLibrary passwordEncryptorLibrary;
  
  private final PasswordService passwordService;
  
  private final RequestFrequencyMapper rateLimitMapper;
  
  private final RateLimitService rateLimitService;
  
  private final RecoveryCodeHasher recoveryCodeHasher;
  
  private final RefreshTokenService refreshTokenService;
  
  private final RegistrationCountService registrationCountService;
  
  private final SCIMMapper scimMapper;
  
  private final UserSearchEngine searchEngine;
  
  private final TenantCache tenantCache;
  
  private final TenantReaderService tenantReader;
  
  private final UserActionLogMapper userActionLogMapper;
  
  private final UserActionLogService userActionLogService;
  
  private final UserActionService userActionService;
  
  private final UserCommentMapper userCommentMapper;
  
  private final UserMapper userMapper;
  
  private final UserMetricsService userMetricsService;
  
  private final UserReaderService userReader;
  
  private final WebAuthnCredentialMapper webAuthnCredentialMapper;
  
  @Inject
  public DefaultUserService(ActionService paramActionService, ApplicationCache paramApplicationCache, ApplicationReaderService paramApplicationReaderService, AuditService paramAuditService, AuthenticationService paramAuthenticationService, @Named("background") GroupMapper paramGroupMapper1, @Named("background") UserMapper paramUserMapper1, @Named("background") SqlSessionFactory paramSqlSessionFactory, FusionAuthConfiguration paramFusionAuthConfiguration, ConsentMapper paramConsentMapper, EmailProxy paramEmailProxy, EmailTemplateMapper paramEmailTemplateMapper, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, RateLimitService paramRateLimitService, FamilyMapper paramFamilyMapper, GroupMapper paramGroupMapper2, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, LoginMapper paramLoginMapper, ModerationService paramModerationService, @FusionAuthTenantId UUID paramUUID, ObjectMapper paramObjectMapper, PasswordEncryptorLibrary paramPasswordEncryptorLibrary, PasswordService paramPasswordService, RequestFrequencyMapper paramRequestFrequencyMapper, RecoveryCodeHasher paramRecoveryCodeHasher, RefreshTokenService paramRefreshTokenService, RegistrationCountService paramRegistrationCountService, SCIMMapper paramSCIMMapper, TenantReaderService paramTenantReaderService, UserSearchEngine paramUserSearchEngine, TenantCache paramTenantCache, UserActionLogMapper paramUserActionLogMapper, UserActionLogService paramUserActionLogService, UserActionService paramUserActionService, UserCommentMapper paramUserCommentMapper, UserMapper paramUserMapper2, UserReaderService paramUserReaderService, UserMetricsService paramUserMetricsService, WebAuthnCredentialMapper paramWebAuthnCredentialMapper, MessengerService paramMessengerService, MFAService paramMFAService) {
    this.actionService = paramActionService;
    this.applicationCache = paramApplicationCache;
    this.applicationReader = paramApplicationReaderService;
    this.auditService = paramAuditService;
    this.authenticationService = paramAuthenticationService;
    this.backgroundGroupMapper = paramGroupMapper1;
    this.backgroundUserMapper = paramUserMapper1;
    this.backgroundSQLSessionFactory = paramSqlSessionFactory;
    this.configuration = paramFusionAuthConfiguration;
    this.consentMapper = paramConsentMapper;
    this.emailProxy = paramEmailProxy;
    this.emailTemplateMapper = paramEmailTemplateMapper;
    this.passwordEncryptorLibrary = paramPasswordEncryptorLibrary;
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.externalIdentifierService = paramExternalIdentifierService;
    this.familyMapper = paramFamilyMapper;
    this.groupMapper = paramGroupMapper2;
    this.identityProviderLinkMapper = paramIdentityProviderLinkMapper;
    this.rateLimitMapper = paramRequestFrequencyMapper;
    this.rateLimitService = paramRateLimitService;
    this.recoveryCodeHasher = paramRecoveryCodeHasher;
    this.refreshTokenService = paramRefreshTokenService;
    this.loginMapper = paramLoginMapper;
    this.moderationService = paramModerationService;
    this.fusionauthTenantId = paramUUID;
    this.objectMapper = paramObjectMapper;
    this.passwordService = paramPasswordService;
    this.registrationCountService = paramRegistrationCountService;
    this.scimMapper = paramSCIMMapper;
    this.searchEngine = paramUserSearchEngine;
    this.tenantReader = paramTenantReaderService;
    this.tenantCache = paramTenantCache;
    this.userActionLogMapper = paramUserActionLogMapper;
    this.userActionLogService = paramUserActionLogService;
    this.userActionService = paramUserActionService;
    this.userCommentMapper = paramUserCommentMapper;
    this.userMapper = paramUserMapper2;
    this.userReader = paramUserReaderService;
    this.userMetricsService = paramUserMetricsService;
    this.webAuthnCredentialMapper = paramWebAuthnCredentialMapper;
    this.messengerService = paramMessengerService;
    this.mfaService = paramMFAService;
  }
  
  public static void handleUserPrimaryIdentities(User paramUser) {
    handleUserPrimaryIdentities(paramUser, new HashMap<>());
  }
  
  public static void handleUserPrimaryIdentities(User paramUser, Map<String, String> paramMap) {
    String str = "user.identities[%d].value";
    List<String> list = (List)IntStream.range(0, paramUser.identities.size()).mapToObj(paramObject -> "user.identities[%d].value".formatted(new Object[] { paramObject })).collect(Collectors.toCollection(ArrayList::new));
    handleUserPrimaryIdentity(paramUser, IdentityType.email, paramUser.email, () -> buildPrimaryEmailIdentity(paramUser), list);
    handleUserPrimaryIdentity(paramUser, IdentityType.phoneNumber, paramUser.phoneNumber, () -> buildPrimaryPhoneNumberIdentity(paramUser), list);
    handleUserPrimaryIdentity(paramUser, IdentityType.username, paramUser.username, () -> buildPrimaryUsernameIdentity(paramUser), list);
    for (byte b = 0; b < Math.min(paramUser.identities.size(), list.size()); b++) {
      paramMap.put("user.identities[%d].value".formatted(new Object[] { Integer.valueOf(b) }, ), list.get(b));
    } 
  }
  
  private static UserIdentity buildPrimaryEmailIdentity(User paramUser) {
    return (new UserIdentity())
      .with(paramUserIdentity -> paramUserIdentity.primary = true)
      .with(paramUserIdentity -> paramUserIdentity.tenantId = paramUser.tenantId)
      .with(paramUserIdentity -> paramUserIdentity.type = IdentityType.email)
      .with(paramUserIdentity -> paramUserIdentity.userId = paramUser.id)
      .with(paramUserIdentity -> paramUserIdentity.value = paramUser.email);
  }
  
  private static UserIdentity buildPrimaryPhoneNumberIdentity(User paramUser) {
    return (new UserIdentity())
      .with(paramUserIdentity -> paramUserIdentity.primary = true)
      .with(paramUserIdentity -> paramUserIdentity.tenantId = paramUser.tenantId)
      .with(paramUserIdentity -> paramUserIdentity.type = IdentityType.phoneNumber)
      .with(paramUserIdentity -> paramUserIdentity.userId = paramUser.id)
      .with(paramUserIdentity -> paramUserIdentity.value = paramUser.phoneNumber);
  }
  
  private static UserIdentity buildPrimaryUsernameIdentity(User paramUser) {
    return (new UserIdentity())
      .with(paramUserIdentity -> paramUserIdentity.primary = true)
      .with(paramUserIdentity -> paramUserIdentity.tenantId = paramUser.tenantId)
      .with(paramUserIdentity -> paramUserIdentity.type = IdentityType.username)
      .with(paramUserIdentity -> paramUserIdentity.userId = paramUser.id)

      
      .with(paramUserIdentity -> paramUserIdentity.displayValue = paramUser.username)
      .with(paramUserIdentity -> paramUserIdentity.value = paramUser.username)
      .with(paramUserIdentity -> paramUserIdentity.moderationStatus = paramUser.usernameStatus);
  }
  
  private static void clearRecoveryCodes(UserTwoFactorConfiguration paramUserTwoFactorConfiguration) {
    paramUserTwoFactorConfiguration.recoveryCodes.clear();
    paramUserTwoFactorConfiguration.recoveryCodeEncryptionScheme = null;
    paramUserTwoFactorConfiguration.recoveryCodeWorkFactor = null;
  }
  
  private static ZonedDateTime getVerifiedInstant(UserIdentity paramUserIdentity, ZonedDateTime paramZonedDateTime) {
    if (paramUserIdentity.verifiedInstant != null)
      return paramUserIdentity.verifiedInstant; 
    return paramUserIdentity.verified ? paramZonedDateTime : null;
  }
  
  private static IdentityVerifiedReason getVerifiedReason(UserIdentity paramUserIdentity, Tenant paramTenant, boolean paramBoolean) {
    if (paramUserIdentity.type.is(IdentityType.username))
      return IdentityVerifiedReason.Unverifiable; 
    if (paramUserIdentity.verifiedReason != null)
      return paramUserIdentity.verifiedReason; 
    if (paramBoolean)
      return IdentityVerifiedReason.Skipped; 
    if (paramUserIdentity.type.is(IdentityType.email))
      return paramTenant.emailConfiguration.verifyEmail ? IdentityVerifiedReason.Pending : IdentityVerifiedReason.Disabled; 
    if (paramUserIdentity.type.is(IdentityType.phoneNumber))
      return paramTenant.phoneConfiguration.verifyPhoneNumber ? IdentityVerifiedReason.Pending : IdentityVerifiedReason.Disabled; 
    return IdentityVerifiedReason.Unverifiable;
  }
  
  private static String handleIdentityDisplayValue(UserIdentity paramUserIdentity) {
    return paramUserIdentity.type.is(IdentityType.username) ? paramUserIdentity.displayValue : null;
  }
  
  private static void handleUserPrimaryIdentity(User paramUser, IdentityType paramIdentityType, String paramString, Supplier<UserIdentity> paramSupplier, List<String> paramList) {
    List<UserIdentity> list = paramUser.identities;
    String str = IdentityHelper.canonicalizeValue(paramString, paramIdentityType);
    if (str == null) {
      for (int j = list.size() - 1; j >= 0; j--) {
        UserIdentity userIdentity = list.get(j);
        if (userIdentity.type.is(paramIdentityType) && userIdentity.primary) {
          list.remove(j);
          paramList.remove(j);
        } 
      } 
      return;
    } 
    for (int i = list.size() - 1; i >= 0; i--) {
      UserIdentity userIdentity = list.get(i);
      String str1 = IdentityHelper.canonicalizeValue(userIdentity);
      if (userIdentity.type.is(paramIdentityType) && userIdentity.primary)
        if (!str1.equals(str)) {
          list.remove(i);
          paramList.remove(i);
        } else {
          paramList.set(i, "user." + paramIdentityType.name);
        }  
    } 
    Optional optional = list.stream().filter(paramUserIdentity -> (paramUserIdentity.type.is(paramIdentityType) && IdentityHelper.canonicalizeValue(paramUserIdentity).equals(paramString))).findFirst();
    if (optional.isEmpty()) {
      list.add(paramSupplier.get());
      paramList.add("user." + paramIdentityType.name);
    } else {
      ((UserIdentity)optional.get()).primary = true;
      if (((UserIdentity)optional.get()).type.is(IdentityType.username) && paramUser.usernameStatus != null)
        ((UserIdentity)optional.get()).moderationStatus = paramUser.usernameStatus; 
    } 
  }
  
  @Transactional
  public UserService.UserResult _create(Tenant paramTenant, Application paramApplication, User paramUser, SendSetPasswordIdentityType paramSendSetPasswordIdentityType, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4, EventInfo paramEventInfo, List<ExternalIdentifier> paramList) {
    ZonedDateTime zonedDateTime = ZonedDateTimeWrapper.now(ZoneOffset.UTC);
    UserService.UserResult userResult = new UserService.UserResult();
    paramUser.active = true;
    paramUser.tenantId = paramTenant.id;
    if (paramUser.id == null)
      paramUser.id = UUID.randomUUID(); 
    setDefaults(paramTenant, paramUser, true);
    handleTwoFactorOnCreate(paramTenant, paramUser);
    List<UserIdentity> list = paramUser.resolveIdentitiesOfType(IdentityType.username);
    list.forEach(paramUserIdentity -> handleUniqueUsernames(paramTenant, paramUserIdentity, null));
    if (paramSendSetPasswordIdentityType != SendSetPasswordIdentityType.doNotSend)
      paramUser.password = null; 
    this.passwordService.hashPassword(paramTenant, paramUser, paramUser.password);
    paramUser.insertInstant = zonedDateTime;
    paramUser.lastUpdateInstant = paramUser.insertInstant;
    handleIdentitiesDuringCreate(paramUser, paramTenant, paramBoolean1, zonedDateTime, paramBoolean2);
    populateUserVerifiedInstant(paramUser, null, zonedDateTime);
    this.userMapper.create(paramUser);
    DefaultUserReaderService.normalizeFromIdentities(paramUser);
    this.userMapper.createIdentityBulk(paramUser.identities, UserIdentityStatus.Active);
    if (paramList != null) {
      List<String> list1 = paramList.stream().map(paramExternalIdentifier -> paramExternalIdentifier.id).toList();
      this.externalIdentifierService.deleteByIds(list1);
    } 
    if (paramTenant.passwordValidationRules.rememberPreviousPasswords.enabled && paramUser.password != null)
      this.userMapper.createPreviousPassword(new PreviousPassword(paramUser.passwordLastUpdateInstant, paramUser.encryptionScheme, paramUser.factor, paramUser.password, paramUser.salt, paramUser.id)); 
    this.registrationCountService.insertGlobal(1);
    createMemberships(paramUser, zonedDateTime);
    LoginQueue.LoginQueueRawLogin loginQueueRawLogin = paramBoolean2 ? this.userMetricsService.buildRawLogin(paramUser, null, zonedDateTime, null, paramEventInfo) : new LoginQueue.LoginQueueRawLogin(new User(paramUser));
    EventHelper.send(paramTenant, paramApplication, new UserCreateEvent(paramEventInfo, paramUser));
    UserIdentity userIdentity1 = paramUser.resolvePrimaryIdentity(IdentityType.email);
    if (userIdentity1 != null) {
      if (paramSendSetPasswordIdentityType == SendSetPasswordIdentityType.email) {
        ExternalIdentifier.ExternalIdData externalIdData = (new ExternalIdentifier.ExternalIdData()).setAttribute("loginId", userIdentity1.value).setAttribute("loginIdType", userIdentity1.type);
        this.emailProxy.sendSetupPasswordEmail(paramTenant, paramApplication, paramUser, this.externalIdentifierService.createSetupPassword(paramTenant, paramUser.id, externalIdData));
      } 
      if ((paramSendSetPasswordIdentityType != SendSetPasswordIdentityType.email || !paramTenant.emailConfiguration.implicitEmailVerificationAllowed) && 
        userIdentity1.verificationRequired()) {
        ExternalIdentifier externalIdentifier = startVerify(paramTenant, userIdentity1.value, userIdentity1.type, paramTenant.emailConfiguration.verificationStrategy, paramUser, paramApplication, null);
        if (paramBoolean3)
          sendVerifyWithoutRateLimit(paramTenant, externalIdentifier, paramApplication, paramUser); 
        userResult.verificationIds.put(userIdentity1, externalIdentifier);
      } 
    } 
    UserIdentity userIdentity2 = paramUser.resolvePrimaryIdentity(IdentityType.phoneNumber);
    if (userIdentity2 != null) {
      if (paramSendSetPasswordIdentityType == SendSetPasswordIdentityType.phone) {
        ExternalIdentifier.ExternalIdData externalIdData = (new ExternalIdentifier.ExternalIdData()).setAttribute("loginId", userIdentity2.value).setAttribute("loginIdType", userIdentity2.type);
        String str = this.externalIdentifierService.createSetupPassword(paramTenant, paramUser.id, externalIdData);
        UUID uUID1 = paramTenant.phoneConfiguration.messengerId;
        UUID uUID2 = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> paramApplication.phoneConfiguration.setPasswordTemplateId).orElse(paramTenant.phoneConfiguration.setPasswordTemplateId);
        Map<String, Object> map = MessageTemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser, userIdentity2.value);
        map.put("changePasswordId", str);
        List<Locale> list1 = TemplateHelper.getPreferredLanguages(paramUser, paramApplication);
        try {
          this.messengerService.send(uUID2, uUID1, list1.isEmpty() ? null : (Locale)list1.getFirst(), map);
        } catch (MessageTemplateException messageTemplateException) {
          MessageTemplateExceptionHandler.logEvent(messageTemplateException);
        } catch (MessengerException messengerException) {
          MessengerExceptionHandler.logEvent(messengerException);
        } 
      } 
      if ((paramSendSetPasswordIdentityType != SendSetPasswordIdentityType.phone || !paramTenant.phoneConfiguration.implicitPhoneVerificationAllowed) && 
        userIdentity2.verificationRequired()) {
        ExternalIdentifier externalIdentifier = startVerify(paramTenant, userIdentity2.value, userIdentity2.type, paramTenant.phoneConfiguration.verificationStrategy, paramUser, paramApplication, null);
        if (paramBoolean4)
          try {
            sendVerifyWithoutRateLimit(paramTenant, externalIdentifier, paramApplication, paramUser);
          } catch (MessageTemplateException messageTemplateException) {
            MessageTemplateExceptionHandler.logEvent(messageTemplateException);
          } catch (MessengerException messengerException) {
            MessengerExceptionHandler.logEvent(messengerException);
          }  
        userResult.verificationIds.put(userIdentity2, externalIdentifier);
      } 
    } 
    for (TwoFactorMethod twoFactorMethod : paramUser.twoFactor.methods)
      userResult.events.add(new UserTwoFactorMethodAddEvent(paramEventInfo, (new TwoFactorMethod(twoFactorMethod)).secure(), paramUser)); 
    sendFamilyEmails(paramTenant, paramUser);
    userResult.user = paramUser;
    userResult.rawLogin = loginQueueRawLogin;
    return userResult;
  }
  
  @Transactional
  public UserService.RegistrationResult _createRegistration(Tenant paramTenant, Application paramApplication, User paramUser, UserRegistration paramUserRegistration, Collection<ApplicationRole> paramCollection, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, EventInfo paramEventInfo) {
    UserService.RegistrationResult registrationResult = new UserService.RegistrationResult();
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    if (paramUserRegistration.id == null)
      paramUserRegistration.id = UUID.randomUUID(); 
    if (paramUserRegistration.usernameStatus == null)
      paramUserRegistration.usernameStatus = ContentStatus.ACTIVE; 
    if (paramBoolean1 && paramApplication.authenticationTokenConfiguration.enabled)
      paramUserRegistration.authenticationToken = generateAuthenticationToken(); 
    paramUserRegistration.verified = (paramBoolean2 || !paramApplication.verifyRegistration);
    if (paramUserRegistration.verified)
      paramUserRegistration.verifiedInstant = zonedDateTime; 
    if (paramUserRegistration.insertInstant == null)
      paramUserRegistration.insertInstant = zonedDateTime; 
    paramUserRegistration.lastUpdateInstant = paramUserRegistration.insertInstant;
    this.userMapper.createRegistration(paramUserRegistration, paramApplication, paramUser);
    if (paramCollection.isEmpty()) {
      paramCollection = (Collection<ApplicationRole>)paramApplication.roles.stream().filter(paramApplicationRole -> paramApplicationRole.isDefault).collect(Collectors.toList());
      paramCollection.forEach(paramApplicationRole -> paramUserRegistration.roles.add(paramApplicationRole.name));
    } 
    MapperTools.safeCreateUpdate(1000, paramCollection, paramList -> this.userMapper.addRolesToUserRegistration(paramUserRegistration.id, paramList));
    this.registrationCountService.insertApplication(paramApplication.id, 1);
    paramUser.getRegistrations().add(paramUserRegistration);
    LoginQueue.LoginQueueRawLogin loginQueueRawLogin = paramBoolean3 ? this.userMetricsService.buildRawLogin(paramUser, null, paramUserRegistration.lastUpdateInstant, paramApplication.id, paramEventInfo) : new LoginQueue.LoginQueueRawLogin(new User(paramUser));
    paramUser.getRegistrations().remove(paramUserRegistration);
    if (!paramUserRegistration.verified) {
      ExternalIdentifier.ExternalIdData externalIdData = (paramApplication.verificationStrategy == VerificationStrategy.FormField) ? new ExternalIdentifier.ExternalIdData("otp", this.externalIdentifierService.generateRegistrationVerificationOneTimeCode(paramTenant)) : null;
      boolean bool = (paramUser.lookupEmail() != null) ? true : false;
      String str = this.externalIdentifierService.createRegistrationVerification(paramTenant, paramUser.id, paramApplication.id, bool, externalIdData);
      registrationResult.registrationVerificationId = str;
      registrationResult.registrationVerificationOneTimeCode = (externalIdData != null) ? externalIdData.getAttribute("otp") : null;
      if (bool)
        this.emailProxy.sendVerifyRegistrationEmail(paramTenant, paramApplication, paramUserRegistration, paramUser, str, (externalIdData != null) ? externalIdData.getAttribute("otp") : null); 
    } 
    EventHelper.send(paramTenant, paramApplication, new UserRegistrationCreateEvent(paramEventInfo, paramApplication.id, paramUserRegistration, paramUser));
    registrationResult.rawLogin = loginQueueRawLogin;
    registrationResult.registration = paramUserRegistration;
    return registrationResult;
  }
  
  @Transactional
  public boolean _delete(Tenant paramTenant, User paramUser, boolean paramBoolean, EventInfo paramEventInfo) {
    this.userCommentMapper.deleteForUser(paramUser.id);
    this.userCommentMapper.deleteForCommenter(paramUser.id);
    this.userActionLogMapper.deleteApplicationAssociationsForActionee(paramUser.id);
    this.userActionLogMapper.deleteForActionee(paramUser.id);
    this.userActionLogService.disassociateActioner(paramUser.id);
    this.userMapper.deleteRolesForUser(paramUser.id);
    this.userMapper.deleteRegistrations(paramUser.id);
    this.userMapper.deletePreviousPasswordsByUserId(paramUser.id);
    this.externalIdentifierService.deleteAllByUserId(paramUser.id);
    this.webAuthnCredentialMapper.deleteByUserId(paramUser.id);
    this.loginMapper.deleteRawLoginsForUser(paramUser.id);
    this.rateLimitMapper.deleteRequestFrequencyRecordsByUserId(paramTenant.id, RateLimitedRequestType.FailedLogin, paramUser.id.toString());
    this.groupMapper.deleteAllMembershipsByUserId(paramUser.id);
    this.familyMapper.removeFromAllFamilies(paramUser.id);
    this.consentMapper.retrieveAllUserConsentsOrGivenByUserId(paramUser.id).forEach(paramUserConsent -> this.consentMapper.deleteEmailPlusConsentByUserConsentId(paramUserConsent.id));
    this.consentMapper.deleteConsentsByUserId(paramUser.id);
    this.identityProviderLinkMapper.deleteIdentityProviderLinksByUserId(paramUser.id);
    this.refreshTokenService.revokeRefreshTokensByUser(paramTenant, paramUser, paramEventInfo);
    this.scimMapper.deleteSCIMUserExternalIdsByUserId(paramUser.id);
    this.userMapper.deleteIdentityByUserId(paramUser.id);
    boolean bool = (this.userMapper.delete(paramUser.id) == 1) ? true : false;
    if (bool) {
      paramUser.getRegistrations().forEach(paramUserRegistration -> this.registrationCountService.insertApplication(paramUserRegistration.applicationId, -1));
      this.registrationCountService.insertGlobal(-1);
      EventHelper.send(paramTenant, null, new UserDeleteEvent(paramEventInfo, paramUser));
      this.searchEngine.delete(paramUser.id);
      if (paramBoolean)
        this.searchEngine.refresh(); 
    } 
    return bool;
  }
  
  @Transactional
  public boolean _deleteRegistration(Tenant paramTenant, UserRegistration paramUserRegistration, User paramUser, Application paramApplication, EventInfo paramEventInfo) {
    this.userMapper.deleteRolesFromRegistration(paramUserRegistration.id);
    boolean bool = (this.userMapper.deleteRegistration(paramUserRegistration.id) >= 1) ? true : false;
    if (bool) {
      this.externalIdentifierService.deleteByUserIdApplicationIdAndType(paramUser.id, paramApplication.id, ExternalIdentifier.ExternalIdType.RegistrationVerification);
      this.registrationCountService.insertApplication(paramApplication.id, -1);
      EventHelper.send(paramTenant, paramApplication, new UserRegistrationDeleteEvent(paramEventInfo, paramApplication.id, paramUserRegistration, paramUser));
      this.searchEngine.index(Collections.singletonList((new User(paramUser))
            .with(paramUser -> paramUser.getRegistrations().removeIf(()))));
    } 
    return bool;
  }
  
  @Transactional
  public UserService.UserResult _updateAllowConnectorIdChange(Tenant paramTenant, Application paramApplication, User paramUser1, User paramUser2, UserService.UpdateUserOptions paramUpdateUserOptions, EventInfo paramEventInfo, PasswordType paramPasswordType) {
    List<UserIdentity> list2;
    UserService.UserResult userResult = new UserService.UserResult();
    paramUser2.active = paramUser1.active;
    paramUser2.insertInstant = paramUser1.insertInstant;
    paramUser2.tenantId = paramUser1.tenantId;
    paramUser2.legacyIdentifier = paramUser1.legacyIdentifier;
    paramUser2.getRegistrations().clear();
    paramUser2.getRegistrations().addAll(paramUser1.getRegistrations());
    paramUser2.getMemberships().clear();
    paramUser2.getMemberships().addAll(paramUser1.getMemberships());
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    paramUser2.lastUpdateInstant = zonedDateTime;
    boolean bool1 = paramUser1.identities.isEmpty();
    boolean bool2 = false;
    switch (paramPasswordType) {
      default:
        throw new MatchException(null, null);
      case PLAINTEXT:
      
      case HASHED:
        if (!Objects.equals(paramUser2.password, paramUser1.password));
        break;
    } 
    boolean bool3 = false;
    if (bool3) {
      switch (paramPasswordType) {
        case PLAINTEXT:
          this.passwordService.rehashPasswordOnUserUpdate(paramTenant, paramUser1, paramUser2, paramUser2.password);
          break;
        case HASHED:
          if (paramUser2.passwordLastUpdateInstant == null || paramUser2.passwordLastUpdateInstant


            
            .equals(paramUser1.passwordLastUpdateInstant))
            paramUser2.passwordLastUpdateInstant = ZonedDateTimeWrapper.now(ZoneOffset.UTC); 
          break;
        default:
          throw new IllegalStateException("Unexpected passwordFieldType value of " + String.valueOf(paramPasswordType));
      } 
      if (paramTenant.passwordValidationRules.rememberPreviousPasswords.enabled)
        this.userMapper.createPreviousPassword(new PreviousPassword(paramUser2.passwordLastUpdateInstant, paramUser2.encryptionScheme, paramUser2.factor, paramUser2.password, paramUser2.salt, paramUser2.id)); 
      if (!paramTenant.passwordValidationRules.breachDetection.enabled) {
        paramUser2.breachedPasswordStatus = null;
        paramUser2.breachedPasswordLastCheckedInstant = null;
      } 
      deleteTokensAndIdsOnPasswordChange(paramTenant, paramUser2, paramUpdateUserOptions.deleteRefreshTokensOnPasswordChange, paramEventInfo, null);
      bool2 = paramUpdateUserOptions.sendPasswordUpdatedEventOnPasswordChange;
    } else {
      paramUser2.encryptionScheme = paramUser1.encryptionScheme;
      paramUser2.factor = paramUser1.factor;
      paramUser2.salt = paramUser1.salt;
      paramUser2.password = paramUser1.password;
      paramUser2.passwordLastUpdateInstant = paramUser1.passwordLastUpdateInstant;
    } 
    setDefaults(paramTenant, paramUser2, false);
    handleTwoFactorOnUpdate(paramTenant, paramUser1, paramUser2, paramEventInfo);
    List<UserIdentity> list1 = paramUser2.resolveIdentitiesOfType(IdentityType.username);
    list1.forEach(paramUserIdentity -> handleUniqueUsernames(paramTenant, paramUserIdentity, IdentityHelper.resolveIdentity(paramUser, paramUserIdentity.value, IdentityType.username)));
    if (bool1) {
      list2 = List.of();
      handleIdentitiesDuringUpdateForAnonymousUser(paramUser2, paramTenant, zonedDateTime, paramUpdateUserOptions.skipVerification);
    } else {
      normalizeIdentitiesDuringUpdate(paramTenant, paramUser2, paramUser1, zonedDateTime, paramUpdateUserOptions.skipVerification);
      list2 = getIdentitiesRequiringVerification(paramTenant, paramUser2, paramUser1, paramUpdateUserOptions.skipVerification);
      handleIdentitiesDuringUpdate(paramUser2, paramUser1);
    } 
    populateUserVerifiedInstant(paramUser2, paramUser1, zonedDateTime);
    DefaultUserReaderService.normalizeFromIdentities(paramUser2);
    boolean bool4 = (paramUser1.email != null && (paramUser2.email == null || !paramUser2.email.equalsIgnoreCase(paramUser1.email))) ? true : false;
    boolean bool5 = (paramUser1.phoneNumber != null && (paramUser2.phoneNumber == null || !paramUser2.phoneNumber.equals(paramUser1.phoneNumber))) ? true : false;
    if (bool4 || bool5)
      this.externalIdentifierService.deleteByUserId(paramUser2.id, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.ChangePassword, ExternalIdentifier.ExternalIdType.SetupPassword }); 
    buildNonTransactionalEventsForUserUpdate(userResult, paramUser2, paramUser1, bool2, paramEventInfo);
    this.userMapper.update(paramUser2);
    for (UserIdentity userIdentity : list2) {
      VerificationStrategy verificationStrategy;
      if (userIdentity.type.is(IdentityType.email)) {
        verificationStrategy = paramTenant.emailConfiguration.verificationStrategy;
      } else if (userIdentity.type.is(IdentityType.phoneNumber)) {
        verificationStrategy = paramTenant.phoneConfiguration.verificationStrategy;
        if (verificationStrategy == VerificationStrategy.FormField)
          continue; 
      } else {
        throw new IllegalArgumentException("Unsupported identity type: " + String.valueOf(userIdentity.type));
      } 
      ExternalIdentifier externalIdentifier = startVerify(paramTenant, userIdentity.value, userIdentity.type, verificationStrategy, paramUser2, paramApplication, null);
      userResult.verificationIds.put(userIdentity, externalIdentifier);
      try {
        sendVerifyWithoutRateLimit(paramTenant, externalIdentifier, paramApplication, paramUser2);
      } catch (MessageTemplateException messageTemplateException) {
        MessageTemplateExceptionHandler.logEvent(messageTemplateException);
      } catch (MessengerException messengerException) {
        MessengerExceptionHandler.logEvent(messengerException);
      } 
    } 
    List<ExternalIdentifier> list = this.externalIdentifierReader.retrieveAllByUserId(paramUser2.id, IdentityExternalIdHelper.identityExternalIdTypes);
    Map<UserIdentity, ExternalIdentifier> map = IdentityExternalIdHelper.groupActiveIdentifiersByIdentity(list, paramUser2, paramTenant);
    userResult.verificationIds.putAll(map);
    sendFamilyEmails(paramTenant, paramUser2);
    EventHelper.send(paramTenant, paramApplication, new UserUpdateEvent(paramEventInfo, paramUser1, paramUser2));
    reindexUser(paramUser2, null);
    return userResult;
  }
  
  @Transactional
  public UserService.RegistrationResult _updateRegistration(Tenant paramTenant, Application paramApplication, User paramUser, UserRegistration paramUserRegistration1, UserRegistration paramUserRegistration2, Collection<ApplicationRole> paramCollection, boolean paramBoolean, EventInfo paramEventInfo) {
    paramUserRegistration2.id = paramUserRegistration1.id;
    if (paramUserRegistration2.usernameStatus == null)
      paramUserRegistration2.usernameStatus = ContentStatus.ACTIVE; 
    paramUserRegistration2.verified = paramUserRegistration1.verified;
    paramUserRegistration2.verifiedInstant = paramUserRegistration1.verifiedInstant;
    if (paramBoolean && paramApplication.authenticationTokenConfiguration.enabled)
      paramUserRegistration2.authenticationToken = generateAuthenticationToken(); 
    paramUserRegistration2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.userMapper.updateRegistration(paramUserRegistration2);
    this.userMapper.deleteRolesFromRegistration(paramUserRegistration2.id);
    MapperTools.safeCreateUpdate(1000, paramCollection, paramList -> this.userMapper.addRolesToUserRegistration(paramUserRegistration.id, paramList));
    paramUser = this.userReader.retrieveById(paramTenant.id, paramUser.id);
    UserRegistration userRegistration = paramUser.getRegistrationForApplication(paramApplication.id);
    EventHelper.send(paramTenant, paramApplication, new UserRegistrationUpdateEvent(paramEventInfo, paramUserRegistration2.applicationId, paramUserRegistration1, userRegistration, paramUser));
    reindexUser(paramUser, paramApplication.id);
    UserService.RegistrationResult registrationResult = new UserService.RegistrationResult();
    if (!paramUserRegistration2.verified) {
      List<ExternalIdentifier> list = this.externalIdentifierReader.retrieveAllByUserId(paramUser.id, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.RegistrationVerification });
      ExternalIdentifier externalIdentifier = list.stream().filter(paramExternalIdentifier -> (paramExternalIdentifier.applicationId != null && paramExternalIdentifier.applicationId.equals(paramUserRegistration.applicationId))).findFirst().orElse((ExternalIdentifier)null);
      registrationResult.registrationVerificationId = (externalIdentifier != null) ? externalIdentifier.id : null;
      registrationResult.registrationVerificationOneTimeCode = (externalIdentifier != null) ? externalIdentifier.getAttribute("otp") : null;
    } 
    registrationResult.registration = userRegistration;
    registrationResult.user = paramUser;
    return registrationResult;
  }
  
  public void completeVerify(Tenant paramTenant, Application paramApplication, User paramUser, ExternalIdentifier paramExternalIdentifier, EventInfo paramEventInfo) {
    verifyIdentity(paramTenant, paramApplication, paramUser, paramExternalIdentifier, null, paramEventInfo);
  }
  
  public UserService.UserResult create(Tenant paramTenant, Application paramApplication, User paramUser, SendSetPasswordIdentityType paramSendSetPasswordIdentityType, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4, EventInfo paramEventInfo, List<ExternalIdentifier> paramList) {
    Objects.requireNonNull(paramTenant);
    UserService.UserResult userResult = _create(paramTenant, paramApplication, paramUser, paramSendSetPasswordIdentityType, paramBoolean1, paramBoolean2, paramBoolean3, paramBoolean4, paramEventInfo, paramList);
    EventHelper.send(paramTenant, paramApplication, new UserCreateCompleteEvent(paramEventInfo, userResult.user));
    userResult.events.forEach(paramBaseEvent -> EventHelper.send(paramTenant, paramApplication, paramBaseEvent));
    this.userMetricsService.addToLoginQueue(userResult.rawLogin);
    return userResult;
  }
  
  @Transactional
  public void createBulk(Tenant paramTenant, List<User> paramList, String paramString, Integer paramInteger, EventInfo paramEventInfo) {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    Set set = (Set)paramList.stream().flatMap(paramUser -> paramUser.getRegistrations().stream()).map(paramUserRegistration -> paramUserRegistration.applicationId).filter(Objects::nonNull).collect(Collectors.toSet());
    List<Application> list = this.applicationReader.retrieveByIds(paramTenant.id, set.stream().toList(), Set.of(ApplicationReaderService.ApplicationExpansion.roles));
    Map<UUID, Application> map = (Map)list.stream().filter(Objects::nonNull).collect(Collectors.toMap(paramApplication -> paramApplication.id, paramApplication -> paramApplication));
    HashMap<Object, Object> hashMap1 = new HashMap<>();
    HashMap<Object, Object> hashMap2 = new HashMap<>();
    this.searchEngine.setIndexRefreshInterval("60s");
    try {
      for (byte b = 0; b < paramList.size(); b += 1000) {
        int i = Math.min(b + 1000, paramList.size());
        List<User> list1 = paramList.subList(b, i);
        List list2 = list1.stream().filter(paramUser -> (paramUser.password != null)).toList();
        list1.stream()
          .filter(paramUser -> (paramUser.password == null))
          .forEach(paramUser -> this.passwordService.hashPassword(paramTenant, paramUser, null));
        list1.forEach(paramUser -> paramUser.with(()).with(()));
        list1.stream()
          .filter(paramUser -> (paramUser.insertInstant == null))
          .forEach(paramUser -> paramUser.insertInstant = paramZonedDateTime);
        list1.stream()
          .filter(paramUser -> (paramUser.lastUpdateInstant == null))
          .forEach(paramUser -> paramUser.lastUpdateInstant = paramZonedDateTime);
        list1.stream()
          .map(paramUser -> Integer.valueOf(TimeUtils.toHour(paramUser.insertInstant.truncatedTo(ChronoUnit.HOURS))))
          .forEach(paramInteger -> ((AtomicInteger)paramMap.computeIfAbsent(paramInteger, ())).incrementAndGet());
        list2.stream()
          .filter(paramUser -> (paramUser.encryptionScheme == null))
          .forEach(paramUser -> updatePasswordWithOverrides(paramTenant, paramUser, paramString, paramInteger));
        list2.stream()
          .filter(paramUser -> (paramUser.passwordLastUpdateInstant == null))
          .forEach(paramUser -> paramUser.passwordLastUpdateInstant = paramZonedDateTime);
        list2.forEach(paramUser -> paramUser.passwordChangeReason = paramUser.passwordChangeRequired ? ((paramUser.passwordChangeReason != null) ? paramUser.passwordChangeReason : ChangePasswordReason.Administrative) : null);
        list1.stream()
          .filter(paramUser -> (paramUser.id == null))
          .forEach(paramUser -> paramUser.id = UUID.randomUUID());
        list1.stream()
          .filter(paramUser -> (paramUser.tenantId == null))
          .forEach(paramUser -> paramUser.tenantId = this.fusionauthTenantId);
        list1.forEach(paramUser -> handleTwoFactorOnBulkCreate(paramTenant, paramUser));
        ArrayList<?> arrayList = new ArrayList(list1.size());
        list1.forEach(paramUser -> {
              paramList.addAll(paramUser.identities);
              for (IdentityType identityType : IdentityTypeValidator.VerifiableIdentityTypes) {
                UserIdentity userIdentity = paramUser.resolvePrimaryIdentity(identityType);
                if (userIdentity != null) {
                  userIdentity.verified = paramUser.verified;
                  userIdentity.verifiedReason = paramUser.verified ? IdentityVerifiedReason.Import : IdentityVerifiedReason.Pending;
                  userIdentity.verifiedInstant = paramUser.verified ? paramUser.verifiedInstant : null;
                } 
              } 
              paramUser.verifiedInstant = null;
              handleIdentitiesDuringCreate(paramUser, paramTenant, false, paramZonedDateTime, false);
              populateUserVerifiedInstant(paramUser, null, paramZonedDateTime);
              DefaultUserReaderService.normalizeFromIdentities(paramUser);
            });
        this.userMapper.createBulk(list1);
        MapperTools.safeCreateUpdate(1000, arrayList, paramList -> this.userMapper.createIdentityBulk(paramList, UserIdentityStatus.Active));
        if (list2.size() > 0)
          this.userMapper.createPreviousPasswordBulk((Collection<PreviousPassword>)list2.stream()
              .map(PreviousPassword::new)
              .collect(Collectors.toList())); 
        createRegistrations(list1, map, (Map)hashMap2, zonedDateTime);
        createMemberships(list1, zonedDateTime);
        this.searchEngine.index(list1);
      } 
      this.registrationCountService.incrementGlobal((Map)hashMap1);
      Objects.requireNonNull(this.registrationCountService);
      hashMap2.forEach(this.registrationCountService::incrementApplication);
    } finally {
      this.searchEngine.setIndexRefreshInterval(this.configuration.searchEngineDefaultRefreshInterval());
    } 
    EventHelper.send(paramTenant, null, new UserBulkCreateEvent(paramEventInfo, (List<User>)paramList.stream().map(User::new).map(User::secure).map(User::sort).collect(Collectors.toList())));
  }
  
  public UserService.VerificationId createNewEmailVerificationId(Tenant paramTenant, Application paramApplication, String paramString, boolean paramBoolean) {
    User user = this.userReader.retrieveByLoginId(paramTenant.id, paramString, List.of(IdentityType.email));
    if (user == null)
      return null; 
    UserService.VerificationId verificationId = new UserService.VerificationId();
    ExternalIdentifier externalIdentifier = startVerify(paramTenant, paramString, IdentityType.email, paramTenant.emailConfiguration.verificationStrategy, user, paramApplication, null);
    verificationId.otp = externalIdentifier.getAttribute("otp");
    verificationId.id = externalIdentifier.id;
    if (paramBoolean)
      sendVerify(paramTenant, externalIdentifier, paramApplication, user); 
    return verificationId;
  }
  
  public UserService.VerificationId createNewRegistrationVerificationId(Tenant paramTenant, Application paramApplication, String paramString, boolean paramBoolean) {
    User user = (paramString != null) ? this.userReader.retrieveByLoginId(paramTenant.id, paramString, List.of(IdentityType.email)) : null;
    if (user == null)
      return null; 
    UserRegistration userRegistration = user.getRegistrationForApplication(paramApplication.id);
    if (userRegistration == null)
      return null; 
    if (userRegistration.verified)
      return null; 
    if (paramBoolean)
      this.rateLimitService.handleAndThrow(paramTenant, RateLimitedRequestType.SendRegistrationVerification, user.id.toString()); 
    UserService.VerificationId verificationId = new UserService.VerificationId();
    ExternalIdentifier.ExternalIdData externalIdData = null;
    if (paramApplication.verificationStrategy == VerificationStrategy.FormField) {
      verificationId.otp = this.externalIdentifierService.generateRegistrationVerificationOneTimeCode(paramTenant);
      externalIdData = new ExternalIdentifier.ExternalIdData("otp", verificationId.otp);
    } 
    verificationId.id = this.externalIdentifierService.createRegistrationVerification(paramTenant, user.id, paramApplication.id, paramBoolean, externalIdData);
    if (paramBoolean)
      this.emailProxy.sendVerifyRegistrationEmail(paramTenant, paramApplication, userRegistration, user, verificationId.id, verificationId.otp); 
    return verificationId;
  }
  
  public UserService.RegistrationResult createRegistration(Tenant paramTenant, Application paramApplication, User paramUser, UserRegistration paramUserRegistration, Collection<ApplicationRole> paramCollection, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, EventInfo paramEventInfo) {
    UserService.RegistrationResult registrationResult = _createRegistration(paramTenant, paramApplication, paramUser, paramUserRegistration, paramCollection, paramBoolean1, paramBoolean2, paramBoolean3, paramEventInfo);
    this.userMetricsService.addToLoginQueue(registrationResult.rawLogin);
    EventHelper.send(paramTenant, paramApplication, new UserRegistrationCreateCompleteEvent(paramEventInfo, paramApplication.id, new UserRegistration(registrationResult.registration), paramUser));
    return registrationResult;
  }
  
  @Transactional
  public UserService.UserResult createUserWithoutIdentity(Tenant paramTenant, IdentityProviderLink paramIdentityProviderLink, User paramUser, EventInfo paramEventInfo) {
    Objects.requireNonNull(paramTenant);
    ZonedDateTime zonedDateTime = ZonedDateTimeWrapper.now(ZoneOffset.UTC);
    UserService.UserResult userResult = new UserService.UserResult();
    paramUser.verifiedInstant = zonedDateTime;
    paramUser.active = true;
    paramUser.tenantId = paramTenant.id;
    if (paramUser.id == null)
      paramUser.id = UUID.randomUUID(); 
    paramUser.insertInstant = zonedDateTime;
    paramUser.lastUpdateInstant = paramUser.insertInstant;
    paramIdentityProviderLink.userId = paramUser.id;
    paramIdentityProviderLink.tenantId = paramUser.tenantId;
    paramIdentityProviderLink.insertInstant = zonedDateTime;
    paramIdentityProviderLink.lastLoginInstant = zonedDateTime;
    this.userMapper.create(paramUser);
    this.identityProviderLinkMapper.upsertIdentityProviderLink(paramIdentityProviderLink);
    this.registrationCountService.insertGlobal(1);
    LoginQueue.LoginQueueRawLogin loginQueueRawLogin = this.userMetricsService.buildRawLogin(paramUser, null, zonedDateTime, null, null);
    EventHelper.send(paramTenant, null, new UserCreateEvent(paramEventInfo, paramUser));
    userResult.rawLogin = loginQueueRawLogin;
    return userResult;
  }
  
  @Transactional
  public boolean deactivate(Tenant paramTenant, User paramUser, EventInfo paramEventInfo) {
    return _deactivate(paramTenant, paramUser, paramEventInfo);
  }
  
  public List<UUID> deactivateAllByIds(UUID paramUUID, List<UUID> paramList, EventInfo paramEventInfo, boolean paramBoolean) {
    List<User> list = this.userReader.retrieveByIds(paramUUID, paramList, UserReaderService.UserExpansion.all());
    if (!paramBoolean) {
      Map<UUID, Tenant> map = usersToTenantMap(list);
      deactivateUsers(map, list, paramEventInfo);
    } 
    return list.stream().map(paramUser -> paramUser.id).toList();
  }
  
  @Transactional
  public void deactivateUsers(Map<UUID, Tenant> paramMap, List<User> paramList, EventInfo paramEventInfo) {
    paramList.forEach(paramUser -> _deactivate((Tenant)paramMap.get(paramUser.tenantId), paramUser, paramEventInfo));
  }
  
  public boolean delete(Tenant paramTenant, User paramUser, EventInfo paramEventInfo) {
    Objects.requireNonNull(paramTenant);
    if (_delete(paramTenant, paramUser, true, paramEventInfo)) {
      EventHelper.send(paramTenant, null, new UserDeleteCompleteEvent(paramEventInfo, paramUser));
      return true;
    } 
    return false;
  }
  
  public List<UUID> deleteAllByIds(UUID paramUUID, List<UUID> paramList, EventInfo paramEventInfo, boolean paramBoolean) {
    List<User> list = this.userReader.retrieveByIds(paramUUID, paramList, UserReaderService.UserExpansion.all());
    if (!paramBoolean) {
      Map<UUID, Tenant> map = usersToTenantMap(list);
      deleteUsers(map, list, paramEventInfo);
    } 
    return list.stream().map(paramUser -> paramUser.id).toList();
  }
  
  public List<UUID> deleteAllBySearchQuery(UUID paramUUID, String paramString, boolean paramBoolean1, boolean paramBoolean2, int paramInt, EventInfo paramEventInfo) {
    if (this.configuration.searchEngineType() == SearchEngineType.elasticsearch)
      return deleteAllByElasticSearch(paramUUID, paramBoolean1, paramBoolean2, paramInt, paramEventInfo, (paramInteger, paramString2) -> this.userReader.searchByQuery(paramUUID, paramString1, 0, null, paramInteger.intValue(), true, UserReaderService.UserExpansion.all(), null, paramString2)); 
    throw new SearchByQueryUnsupportedException();
  }
  
  public List<UUID> deleteAllBySearchQueryString(UUID paramUUID, String paramString, boolean paramBoolean1, boolean paramBoolean2, int paramInt, EventInfo paramEventInfo) {
    if (this.configuration.searchEngineType() == SearchEngineType.elasticsearch)
      return deleteAllByElasticSearch(paramUUID, paramBoolean1, paramBoolean2, paramInt, paramEventInfo, (paramInteger, paramString2) -> this.userReader.searchByQueryString(paramUUID, paramString1, paramInteger.intValue(), 0, null, true, UserReaderService.UserExpansion.all(), null, paramString2)); 
    List<User> list = this.userReader.retrieveAllBySearchQueryString(paramUUID, paramString, null, paramInt, UserReaderService.UserExpansion.all());
    return bulkDeleteAllUsers(paramUUID, list, paramBoolean2, paramBoolean1, paramEventInfo);
  }
  
  public void deleteAllByTenantId(Tenant paramTenant, EventInfo paramEventInfo) {
    char c = '썐';
    AtomicInteger atomicInteger = new AtomicInteger();
    Instant instant = Instant.now();
    int i = this.backgroundUserMapper.retrieveCountByTenantId(paramTenant.id);
    logger.debug("0% complete. Deleted [0] of [{}] users for Tenant Id [{}].", NumberTools.format(i), paramTenant.id);
    try {
      SqlSession sqlSession = this.backgroundSQLSessionFactory.openSession();
      try {
        sqlSession.select("retrieveAllByTenantId", paramTenant.id, paramResultContext -> {
              User user = (User)paramResultContext.getResultObject();
              DefaultUserReaderService.fixLegacyIdentity(user);
              user = DefaultUserReaderService.expand(user, UserReaderService.UserExpansion.all(), this.configuration, this.backgroundUserMapper, this.backgroundGroupMapper);
              _delete(paramTenant, user, false, paramEventInfo);
              paramAtomicInteger.getAndIncrement();
              if (paramAtomicInteger.get() % paramInt1 == 0) {
                Thread.yield();
                float f = paramAtomicInteger.get() * 100.0F / paramInt2;
                logger.info("{}% complete. Deleted [{}] of [{}] users for Tenant Id [{}].", new Object[] { NumberTools.format(f), NumberTools.format(paramAtomicInteger.get()), NumberTools.format(paramInt2), paramTenant.id });
              } 
            });
        logger.debug("100% complete. Deleted [{}] users in [{}] seconds for Tenant Id [{}].", new Object[] { NumberTools.format(atomicInteger.get()), NumberTools.format(Duration.between(instant, Instant.now()).toSeconds()), paramTenant.id });
        if (sqlSession != null)
          sqlSession.close(); 
      } catch (Throwable throwable) {
        if (sqlSession != null)
          try {
            sqlSession.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (Throwable throwable) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed delete users for Tenant Id [" + String.valueOf(paramTenant.id) + "].", throwable));
      logger.error("Failed delete users for Tenant Id [{}].", paramTenant.id, throwable);
    } finally {
      this.searchEngine.refresh();
    } 
  }
  
  @Transactional
  public void deleteRegistration(Tenant paramTenant, UserRegistration paramUserRegistration, User paramUser, Application paramApplication, EventInfo paramEventInfo) {
    if (_deleteRegistration(paramTenant, paramUserRegistration, paramUser, paramApplication, paramEventInfo))
      EventHelper.send(paramTenant, paramApplication, new UserRegistrationDeleteCompleteEvent(paramEventInfo, paramApplication.id, paramUserRegistration, paramUser)); 
  }
  
  public void deleteUnverifiedChildren(Tenant paramTenant, ZonedDateTime paramZonedDateTime) {
    List<User> list = this.userReader.retrieveUnverifiedChildrenForReaping(paramTenant.id, paramZonedDateTime);
    if (list.isEmpty())
      return; 
    this.auditService.create((new AuditLog("[FusionAuth] UserReaper", "Delete [" + list
          .size() + "] children that have not yet been verified by a parent.\nTenant Id: " + String.valueOf(paramTenant.id) + "\nUser Ids: " + 
          
          String.join(", ", list.stream().map(paramUser -> paramUser.id.toString()).toList()) + "\n"))
        .with(paramAuditLog -> paramAuditLog.reason = "Tenant configured to delete unverified children after [" + paramTenant.familyConfiguration.deleteOrphanedAccountsDays + "] days."), new EventInfo());
    deleteUsers(Collections.singletonMap(paramTenant.id, paramTenant), list, null);
  }
  
  public void deleteUnverifiedRegistrations(Tenant paramTenant, Application paramApplication, ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2) {
    List<_UserRegistration> list = this.userMapper.retrieveUnverifiedRegistrationsForReaping(paramApplication.id, paramZonedDateTime1, paramZonedDateTime2);
    if (list.isEmpty())
      return; 
    this.auditService.create((new AuditLog("[FusionAuth] UserReaper", "Delete [" + list
          .size() + "] user registrations that have ont yet been verified.\nTenant Id: " + String.valueOf(paramTenant.id) + "\nApplication Id: " + String.valueOf(paramApplication.id) + "\nUser Ids: " + 

          
          String.join(", ", list.stream().map(param_UserRegistration -> param_UserRegistration.userId.toString()).toList()) + "\n"))
        .with(paramAuditLog -> paramAuditLog.reason = "Delete unverified User Registrations after [" + paramApplication.registrationDeletePolicy.unverified.numberOfDaysToRetain + "] days."), new EventInfo());
    HashMap<Object, Object> hashMap = new HashMap<>();
    list.forEach(param_UserRegistration -> paramMap.computeIfAbsent(param_UserRegistration.id, ()));
    list.forEach(param_UserRegistration -> deleteRegistration(paramTenant, param_UserRegistration, (User)paramMap.get(param_UserRegistration.id), paramApplication, null));
  }
  
  public void deleteUnverifiedUsers(Tenant paramTenant, ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2) {
    List<User> list = this.userReader.retrieveUnverifiedUsersForReaping(paramTenant.id, paramZonedDateTime1, paramZonedDateTime2);
    if (list.isEmpty())
      return; 
    this.auditService.create((new AuditLog("[FusionAuth] UserReaper", "Delete [" + list
          .size() + "] users without verified identities.\nTenant Id: " + String.valueOf(paramTenant.id) + "\nUser Ids: " + 
          
          String.join(", ", list.stream().map(paramUser -> paramUser.id.toString()).toList()) + "\n"))
        .with(paramAuditLog -> paramAuditLog.reason = "Delete unverified Users after [" + paramTenant.userDeletePolicy.unverified.numberOfDaysToRetain + "] days."), new EventInfo());
    deleteUsers(Collections.singletonMap(paramTenant.id, paramTenant), list, null);
  }
  
  public void deleteUsers(Map<UUID, Tenant> paramMap, List<User> paramList, EventInfo paramEventInfo) {
    paramList.forEach(paramUser -> _delete((Tenant)paramMap.get(paramUser.tenantId), paramUser, false, paramEventInfo));
    this.searchEngine.deleteByIds((List<UUID>)paramList.stream().map(paramUser -> paramUser.id).collect(Collectors.toList()));
    this.searchEngine.refresh();
  }
  
  public boolean disableTwoFactor(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2, String paramString3, EventInfo paramEventInfo) {
    // Byte code:
    //   0: aload_0
    //   1: getfield authenticationService : Lio/fusionauth/api/service/authentication/AuthenticationService;
    //   4: aload_1
    //   5: aconst_null
    //   6: aload_3
    //   7: aconst_null
    //   8: aload #5
    //   10: aload #4
    //   12: aload #6
    //   14: aconst_null
    //   15: aload #7
    //   17: invokeinterface validateTwoFactorCode : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lio/fusionauth/domain/EventInfo;)Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult;
    //   22: astore #8
    //   24: aload #8
    //   26: instanceof io/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$Failure
    //   29: ifeq -> 34
    //   32: iconst_0
    //   33: ireturn
    //   34: aload #8
    //   36: instanceof io/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$ExternalIdSuccess
    //   39: ifeq -> 75
    //   42: aload #8
    //   44: checkcast io/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$ExternalIdSuccess
    //   47: astore #9
    //   49: aload #9
    //   51: invokevirtual id : ()Lio/fusionauth/api/domain/ExternalIdentifier;
    //   54: astore #11
    //   56: aload #11
    //   58: astore #10
    //   60: aload_0
    //   61: getfield externalIdentifierService : Lio/fusionauth/api/service/user/ExternalIdentifierService;
    //   64: aload #10
    //   66: getfield id : Ljava/lang/String;
    //   69: invokeinterface deleteById : (Ljava/lang/String;)I
    //   74: pop
    //   75: aload_3
    //   76: getfield twoFactor : Lio/fusionauth/domain/UserTwoFactorConfiguration;
    //   79: getfield methods : Ljava/util/List;
    //   82: invokeinterface stream : ()Ljava/util/stream/Stream;
    //   87: aload #6
    //   89: <illegal opcode> test : (Ljava/lang/String;)Ljava/util/function/Predicate;
    //   94: invokeinterface filter : (Ljava/util/function/Predicate;)Ljava/util/stream/Stream;
    //   99: invokeinterface findFirst : ()Ljava/util/Optional;
    //   104: aconst_null
    //   105: invokevirtual orElse : (Ljava/lang/Object;)Ljava/lang/Object;
    //   108: checkcast io/fusionauth/domain/TwoFactorMethod
    //   111: astore #9
    //   113: aload #8
    //   115: instanceof io/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$RecoveryCodeSuccess
    //   118: ifeq -> 136
    //   121: aload_3
    //   122: getfield twoFactor : Lio/fusionauth/domain/UserTwoFactorConfiguration;
    //   125: getfield methods : Ljava/util/List;
    //   128: invokeinterface clear : ()V
    //   133: goto -> 156
    //   136: aload_3
    //   137: getfield twoFactor : Lio/fusionauth/domain/UserTwoFactorConfiguration;
    //   140: getfield methods : Ljava/util/List;
    //   143: aload #6
    //   145: <illegal opcode> test : (Ljava/lang/String;)Ljava/util/function/Predicate;
    //   150: invokeinterface removeIf : (Ljava/util/function/Predicate;)Z
    //   155: pop
    //   156: aload_3
    //   157: getfield twoFactor : Lio/fusionauth/domain/UserTwoFactorConfiguration;
    //   160: getfield methods : Ljava/util/List;
    //   163: invokeinterface isEmpty : ()Z
    //   168: ifeq -> 178
    //   171: aload_3
    //   172: getfield twoFactor : Lio/fusionauth/domain/UserTwoFactorConfiguration;
    //   175: invokestatic clearRecoveryCodes : (Lio/fusionauth/domain/UserTwoFactorConfiguration;)V
    //   178: aload_0
    //   179: getfield userMapper : Lio/fusionauth/api/domain/UserMapper;
    //   182: aload_3
    //   183: invokeinterface update : (Lio/fusionauth/domain/User;)I
    //   188: pop
    //   189: aload_1
    //   190: aload_2
    //   191: new io/fusionauth/domain/event/UserTwoFactorMethodRemoveEvent
    //   194: dup
    //   195: aload #7
    //   197: new io/fusionauth/domain/TwoFactorMethod
    //   200: dup
    //   201: aload #9
    //   203: invokespecial <init> : (Lio/fusionauth/domain/TwoFactorMethod;)V
    //   206: invokevirtual secure : ()Lio/fusionauth/domain/TwoFactorMethod;
    //   209: aload_3
    //   210: invokespecial <init> : (Lio/fusionauth/domain/EventInfo;Lio/fusionauth/domain/TwoFactorMethod;Lio/fusionauth/domain/User;)V
    //   213: invokestatic send : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/event/BaseEvent;)V
    //   216: aload_0
    //   217: aload_3
    //   218: aconst_null
    //   219: invokevirtual reindexUser : (Lio/fusionauth/domain/User;Ljava/util/UUID;)V
    //   222: iconst_1
    //   223: ireturn
    //   224: astore #10
    //   226: new java/lang/MatchException
    //   229: dup
    //   230: aload #10
    //   232: invokevirtual toString : ()Ljava/lang/String;
    //   235: aload #10
    //   237: invokespecial <init> : (Ljava/lang/String;Ljava/lang/Throwable;)V
    //   240: athrow
    // Line number table:
    //   Java source line number -> byte code offset
    //   #1621	-> 0
    //   #1622	-> 24
    //   #1623	-> 32
    //   #1626	-> 34
    //   #1627	-> 60
    //   #1630	-> 75
    //   #1633	-> 113
    //   #1634	-> 121
    //   #1636	-> 136
    //   #1640	-> 156
    //   #1641	-> 171
    //   #1644	-> 178
    //   #1646	-> 189
    //   #1648	-> 216
    //   #1649	-> 222
    //   #1626	-> 224
    // Exception table:
    //   from	to	target	type
    //   51	54	224	java/lang/Throwable
  }
  
  public UserService.TwoFactorEnableResult enableTwoFactor(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, EventInfo paramEventInfo, String paramString8) {
    // Byte code:
    //   0: aload #7
    //   2: ldc_w 'authenticator'
    //   5: invokevirtual equals : (Ljava/lang/Object;)Z
    //   8: ifeq -> 29
    //   11: aload #9
    //   13: ifnull -> 21
    //   16: aload #9
    //   18: goto -> 31
    //   21: aload #5
    //   23: invokestatic toBase64 : (Ljava/lang/String;)Ljava/lang/String;
    //   26: goto -> 31
    //   29: aload #9
    //   31: astore #13
    //   33: new io/fusionauth/api/service/user/UserService$TwoFactorEnableResult
    //   36: dup
    //   37: invokespecial <init> : ()V
    //   40: astore #14
    //   42: aload_0
    //   43: getfield authenticationService : Lio/fusionauth/api/service/authentication/AuthenticationService;
    //   46: aload_1
    //   47: aconst_null
    //   48: aload_3
    //   49: aconst_null
    //   50: aload #4
    //   52: aload #7
    //   54: aconst_null
    //   55: aload #13
    //   57: aload #11
    //   59: invokeinterface validateTwoFactorCode : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lio/fusionauth/domain/EventInfo;)Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult;
    //   64: astore #15
    //   66: aload #15
    //   68: instanceof io/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$Failure
    //   71: ifeq -> 77
    //   74: aload #14
    //   76: areturn
    //   77: aload #15
    //   79: instanceof io/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$ExternalIdSuccess
    //   82: ifeq -> 118
    //   85: aload #15
    //   87: checkcast io/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$ExternalIdSuccess
    //   90: astore #16
    //   92: aload #16
    //   94: invokevirtual id : ()Lio/fusionauth/api/domain/ExternalIdentifier;
    //   97: astore #18
    //   99: aload #18
    //   101: astore #17
    //   103: aload_0
    //   104: getfield externalIdentifierService : Lio/fusionauth/api/service/user/ExternalIdentifierService;
    //   107: aload #17
    //   109: getfield id : Ljava/lang/String;
    //   112: invokeinterface deleteById : (Ljava/lang/String;)I
    //   117: pop
    //   118: new io/fusionauth/domain/TwoFactorMethod
    //   121: dup
    //   122: invokespecial <init> : ()V
    //   125: astore #16
    //   127: aload #16
    //   129: aload_3
    //   130: invokestatic generateUniqueTwoFactorMethodId : (Lio/fusionauth/domain/User;)Ljava/lang/String;
    //   133: putfield id : Ljava/lang/String;
    //   136: aload #16
    //   138: aload #7
    //   140: putfield method : Ljava/lang/String;
    //   143: aload #16
    //   145: aload #12
    //   147: putfield name : Ljava/lang/String;
    //   150: aload #7
    //   152: astore #17
    //   154: iconst_m1
    //   155: istore #18
    //   157: aload #17
    //   159: invokevirtual hashCode : ()I
    //   162: lookupswitch default -> 244, 114009 -> 230, 96619420 -> 213, 1815000435 -> 196
    //   196: aload #17
    //   198: ldc_w 'authenticator'
    //   201: invokevirtual equals : (Ljava/lang/Object;)Z
    //   204: ifeq -> 244
    //   207: iconst_0
    //   208: istore #18
    //   210: goto -> 244
    //   213: aload #17
    //   215: ldc_w 'email'
    //   218: invokevirtual equals : (Ljava/lang/Object;)Z
    //   221: ifeq -> 244
    //   224: iconst_1
    //   225: istore #18
    //   227: goto -> 244
    //   230: aload #17
    //   232: ldc_w 'sms'
    //   235: invokevirtual equals : (Ljava/lang/Object;)Z
    //   238: ifeq -> 244
    //   241: iconst_2
    //   242: istore #18
    //   244: iload #18
    //   246: tableswitch default -> 365, 0 -> 272, 1 -> 348, 2 -> 358
    //   272: aload #16
    //   274: new io/fusionauth/domain/AuthenticatorConfiguration
    //   277: dup
    //   278: invokespecial <init> : ()V
    //   281: putfield authenticator : Lio/fusionauth/domain/AuthenticatorConfiguration;
    //   284: aload #16
    //   286: getfield authenticator : Lio/fusionauth/domain/AuthenticatorConfiguration;
    //   289: aload_1
    //   290: getfield multiFactorConfiguration : Lio/fusionauth/domain/TenantMultiFactorConfiguration;
    //   293: getfield authenticator : Lio/fusionauth/domain/TenantMultiFactorConfiguration$MultiFactorAuthenticatorMethod;
    //   296: getfield algorithm : Lio/fusionauth/domain/AuthenticatorConfiguration$TOTPAlgorithm;
    //   299: putfield algorithm : Lio/fusionauth/domain/AuthenticatorConfiguration$TOTPAlgorithm;
    //   302: aload #16
    //   304: getfield authenticator : Lio/fusionauth/domain/AuthenticatorConfiguration;
    //   307: aload_1
    //   308: getfield multiFactorConfiguration : Lio/fusionauth/domain/TenantMultiFactorConfiguration;
    //   311: getfield authenticator : Lio/fusionauth/domain/TenantMultiFactorConfiguration$MultiFactorAuthenticatorMethod;
    //   314: getfield codeLength : I
    //   317: putfield codeLength : I
    //   320: aload #16
    //   322: getfield authenticator : Lio/fusionauth/domain/AuthenticatorConfiguration;
    //   325: aload_1
    //   326: getfield multiFactorConfiguration : Lio/fusionauth/domain/TenantMultiFactorConfiguration;
    //   329: getfield authenticator : Lio/fusionauth/domain/TenantMultiFactorConfiguration$MultiFactorAuthenticatorMethod;
    //   332: getfield timeStep : I
    //   335: putfield timeStep : I
    //   338: aload #16
    //   340: aload #13
    //   342: putfield secret : Ljava/lang/String;
    //   345: goto -> 365
    //   348: aload #16
    //   350: aload #6
    //   352: putfield email : Ljava/lang/String;
    //   355: goto -> 365
    //   358: aload #16
    //   360: aload #8
    //   362: putfield mobilePhone : Ljava/lang/String;
    //   365: aload_3
    //   366: invokevirtual twoFactorEnabled : ()Z
    //   369: ifne -> 376
    //   372: iconst_1
    //   373: goto -> 377
    //   376: iconst_0
    //   377: istore #17
    //   379: iload #17
    //   381: ifeq -> 467
    //   384: aload_0
    //   385: getfield recoveryCodeHasher : Lio/fusionauth/api/service/security/RecoveryCodeHasher;
    //   388: invokeinterface generateCodes : ()Ljava/util/List;
    //   393: astore #18
    //   395: aload_0
    //   396: getfield recoveryCodeHasher : Lio/fusionauth/api/service/security/RecoveryCodeHasher;
    //   399: aload #18
    //   401: aload_3
    //   402: getfield twoFactor : Lio/fusionauth/domain/UserTwoFactorConfiguration;
    //   405: invokeinterface hashCodes : (Ljava/util/List;Lio/fusionauth/domain/UserTwoFactorConfiguration;)V
    //   410: aload #14
    //   412: aload #18
    //   414: putfield recoveryCodes : Ljava/util/List;
    //   417: aload_1
    //   418: getfield jwtConfiguration : Lio/fusionauth/domain/JWTConfiguration;
    //   421: getfield refreshTokenRevocationPolicy : Lio/fusionauth/domain/RefreshTokenRevocationPolicy;
    //   424: getfield onMultiFactorEnable : Z
    //   427: ifeq -> 467
    //   430: aload_0
    //   431: getfield refreshTokenService : Lio/fusionauth/api/service/jwt/RefreshTokenService;
    //   434: aload_1
    //   435: aload_3
    //   436: aload #11
    //   438: invokeinterface revokeRefreshTokensByUser : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/User;Lio/fusionauth/domain/EventInfo;)V
    //   443: aload_0
    //   444: getfield externalIdentifierService : Lio/fusionauth/api/service/user/ExternalIdentifierService;
    //   447: aload_3
    //   448: getfield id : Ljava/util/UUID;
    //   451: iconst_1
    //   452: anewarray io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType
    //   455: dup
    //   456: iconst_0
    //   457: getstatic io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType.LoginIntent : Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;
    //   460: aastore
    //   461: invokeinterface deleteByUserId : (Ljava/util/UUID;[Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;)I
    //   466: pop
    //   467: aload_3
    //   468: getfield twoFactor : Lio/fusionauth/domain/UserTwoFactorConfiguration;
    //   471: getfield methods : Ljava/util/List;
    //   474: aload #16
    //   476: invokeinterface add : (Ljava/lang/Object;)Z
    //   481: pop
    //   482: aload_0
    //   483: getfield userMapper : Lio/fusionauth/api/domain/UserMapper;
    //   486: aload_3
    //   487: invokeinterface update : (Lio/fusionauth/domain/User;)I
    //   492: pop
    //   493: aload_1
    //   494: aload_2
    //   495: new io/fusionauth/domain/event/UserTwoFactorMethodAddEvent
    //   498: dup
    //   499: aload #11
    //   501: new io/fusionauth/domain/TwoFactorMethod
    //   504: dup
    //   505: aload #16
    //   507: invokespecial <init> : (Lio/fusionauth/domain/TwoFactorMethod;)V
    //   510: invokevirtual secure : ()Lio/fusionauth/domain/TwoFactorMethod;
    //   513: aload_3
    //   514: invokespecial <init> : (Lio/fusionauth/domain/EventInfo;Lio/fusionauth/domain/TwoFactorMethod;Lio/fusionauth/domain/User;)V
    //   517: invokestatic send : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/event/BaseEvent;)V
    //   520: aload_0
    //   521: aload_3
    //   522: aconst_null
    //   523: invokevirtual reindexUser : (Lio/fusionauth/domain/User;Ljava/util/UUID;)V
    //   526: iload #17
    //   528: ifeq -> 665
    //   531: aload #10
    //   533: ifnull -> 665
    //   536: aload_0
    //   537: getfield externalIdentifierReader : Lio/fusionauth/api/service/user/ExternalIdentifierReaderService;
    //   540: aload_1
    //   541: aload #10
    //   543: iconst_1
    //   544: anewarray io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType
    //   547: dup
    //   548: iconst_0
    //   549: getstatic io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType.TwoFactor : Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;
    //   552: aastore
    //   553: invokeinterface validate : (Lio/fusionauth/domain/Tenant;Ljava/lang/String;[Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;)Lio/fusionauth/api/service/user/ExternalIdentifierReaderService$ValidationResult;
    //   558: astore #18
    //   560: aload #18
    //   562: getfield id : Lio/fusionauth/api/domain/ExternalIdentifier;
    //   565: ifnull -> 665
    //   568: aload_0
    //   569: getfield externalIdentifierService : Lio/fusionauth/api/service/user/ExternalIdentifierService;
    //   572: aload_1
    //   573: invokeinterface generateTwoFactorOneTimeCode : (Lio/fusionauth/domain/Tenant;)Ljava/lang/String;
    //   578: astore #19
    //   580: aload #14
    //   582: aload #19
    //   584: putfield twoFactorCode : Ljava/lang/String;
    //   587: aload #18
    //   589: getfield id : Lio/fusionauth/api/domain/ExternalIdentifier;
    //   592: getfield data : Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdData;
    //   595: ifnonnull -> 613
    //   598: aload #18
    //   600: getfield id : Lio/fusionauth/api/domain/ExternalIdentifier;
    //   603: new io/fusionauth/api/domain/ExternalIdentifier$ExternalIdData
    //   606: dup
    //   607: invokespecial <init> : ()V
    //   610: putfield data : Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdData;
    //   613: aload #18
    //   615: getfield id : Lio/fusionauth/api/domain/ExternalIdentifier;
    //   618: getfield data : Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdData;
    //   621: ldc_w 'otp'
    //   624: aload #19
    //   626: invokevirtual setAttribute : (Ljava/lang/String;Ljava/lang/Object;)Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdData;
    //   629: pop
    //   630: aload #18
    //   632: getfield id : Lio/fusionauth/api/domain/ExternalIdentifier;
    //   635: getfield data : Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdData;
    //   638: ldc_w 'methodId'
    //   641: aload #16
    //   643: getfield id : Ljava/lang/String;
    //   646: invokevirtual setAttribute : (Ljava/lang/String;Ljava/lang/Object;)Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdData;
    //   649: pop
    //   650: aload_0
    //   651: getfield externalIdentifierService : Lio/fusionauth/api/service/user/ExternalIdentifierService;
    //   654: aload #18
    //   656: getfield id : Lio/fusionauth/api/domain/ExternalIdentifier;
    //   659: invokeinterface update : (Lio/fusionauth/api/domain/ExternalIdentifier;)I
    //   664: pop
    //   665: aload #14
    //   667: iconst_1
    //   668: putfield success : Z
    //   671: aload #14
    //   673: areturn
    //   674: astore #18
    //   676: new java/lang/MatchException
    //   679: dup
    //   680: aload #18
    //   682: invokevirtual toString : ()Ljava/lang/String;
    //   685: aload #18
    //   687: invokespecial <init> : (Ljava/lang/String;Ljava/lang/Throwable;)V
    //   690: athrow
    // Line number table:
    //   Java source line number -> byte code offset
    //   #1659	-> 0
    //   #1660	-> 11
    //   #1661	-> 29
    //   #1663	-> 33
    //   #1665	-> 42
    //   #1666	-> 66
    //   #1667	-> 74
    //   #1670	-> 77
    //   #1671	-> 103
    //   #1674	-> 118
    //   #1675	-> 127
    //   #1676	-> 136
    //   #1677	-> 143
    //   #1681	-> 150
    //   #1683	-> 272
    //   #1684	-> 284
    //   #1685	-> 302
    //   #1686	-> 320
    //   #1687	-> 338
    //   #1688	-> 345
    //   #1689	-> 348
    //   #1690	-> 358
    //   #1693	-> 365
    //   #1694	-> 379
    //   #1696	-> 384
    //   #1697	-> 395
    //   #1698	-> 410
    //   #1701	-> 417
    //   #1702	-> 430
    //   #1704	-> 443
    //   #1708	-> 467
    //   #1709	-> 482
    //   #1710	-> 493
    //   #1712	-> 520
    //   #1715	-> 526
    //   #1716	-> 536
    //   #1717	-> 560
    //   #1718	-> 568
    //   #1719	-> 580
    //   #1720	-> 587
    //   #1721	-> 598
    //   #1723	-> 613
    //   #1724	-> 630
    //   #1725	-> 650
    //   #1729	-> 665
    //   #1730	-> 671
    //   #1670	-> 674
    // Exception table:
    //   from	to	target	type
    //   94	97	674	java/lang/Throwable
  }
  
  public void forceVerifyIdentity(Tenant paramTenant, User paramUser, UserIdentity paramUserIdentity, EventInfo paramEventInfo) {
    verifyIdentity(paramTenant, null, paramUser, null, paramUserIdentity, paramEventInfo);
  }
  
  @Transactional
  public UserService.ForgotPasswordResult forgotPassword(Tenant paramTenant, Application paramApplication, User paramUser, UserIdentity paramUserIdentity, String paramString1, String paramString2, boolean paramBoolean, Map<String, Object> paramMap, EventInfo paramEventInfo) {
    String str1;
    UserService.ForgotPasswordResult forgotPasswordResult = new UserService.ForgotPasswordResult();
    if (paramUser == null) {
      this.rateLimitService.handleAndThrow(paramTenant, RateLimitedRequestType.ForgotPassword, paramString1);
      return forgotPasswordResult;
    } 
    if (paramUserIdentity.type.is(IdentityType.phoneNumber)) {
      str1 = paramUserIdentity.value;
    } else {
      str1 = paramUser.lookupEmail();
    } 
    forgotPasswordResult.askedToSendButNoDeliveryAvailable = (paramBoolean && str1 == null);
    if (forgotPasswordResult.askedToSendButNoDeliveryAvailable)
      return forgotPasswordResult; 
    this.rateLimitService.handleAndThrow(paramTenant, RateLimitedRequestType.ForgotPassword, paramUser.id.toString());
    ExternalIdentifier.ExternalIdData externalIdData = (paramMap == null) ? new ExternalIdentifier.ExternalIdData() : new ExternalIdentifier.ExternalIdData(paramMap);
    externalIdData.setAttribute("loginId", paramUserIdentity.value)
      .setAttribute("loginIdType", paramUserIdentity.type.name);
    String str2 = this.externalIdentifierService.createChangePassword(paramTenant, paramUser.id, paramString2, paramBoolean, externalIdData);
    EventHelper.send(paramTenant, paramApplication, new UserPasswordResetStartEvent(paramEventInfo, paramUser));
    if (paramBoolean) {
      if (paramUserIdentity.type.is(IdentityType.phoneNumber)) {
        UUID uUID1 = paramTenant.phoneConfiguration.messengerId;
        UUID uUID2 = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> paramApplication.phoneConfiguration.forgotPasswordTemplateId).orElse(paramTenant.phoneConfiguration.forgotPasswordTemplateId);
        Map<String, Object> map = MessageTemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser, str1);
        map.put("changePasswordId", str2);
        map.put("state", externalIdData.state);
        List<Locale> list = TemplateHelper.getPreferredLanguages(paramUser, paramApplication);
        this.messengerService.send(uUID2, uUID1, list.isEmpty() ? null : (Locale)list.getFirst(), map);
      } else {
        this.emailProxy.sendForgotPasswordEmail(paramTenant, paramApplication, paramUser, str1, str2, paramMap);
      } 
      EventHelper.send(paramTenant, paramApplication, new UserPasswordResetSendEvent(paramEventInfo, paramUser));
    } 
    forgotPasswordResult.changePasswordId = str2;
    return forgotPasswordResult;
  }
  
  public List<String> generateTwoFactorRecoveryCodes(Tenant paramTenant, User paramUser) {
    List<String> list = this.recoveryCodeHasher.generateCodes();
    this.recoveryCodeHasher.hashCodes(list, paramUser.twoFactor);
    this.userMapper.update(paramUser);
    return list;
  }
  
  public boolean handleImplicitVerification(Tenant paramTenant, Application paramApplication, User paramUser, UserIdentity paramUserIdentity, EventInfo paramEventInfo) {
    boolean bool;
    if (!paramUserIdentity.verificationRequired())
      return false; 
    if (paramUserIdentity.type.is(IdentityType.email)) {
      bool = paramTenant.emailConfiguration.implicitEmailVerificationAllowed;
    } else if (paramUserIdentity.type.is(IdentityType.phoneNumber)) {
      bool = paramTenant.phoneConfiguration.implicitPhoneVerificationAllowed;
    } else {
      throw new IllegalArgumentException("Unsupported identity type " + String.valueOf(paramUserIdentity.type));
    } 
    if (!bool)
      return false; 
    paramUserIdentity.verified = true;
    paramUserIdentity.verifiedReason = IdentityVerifiedReason.Implicit;
    paramUserIdentity
      
      .verifiedInstant = (paramUserIdentity.verifiedInstant != null) ? paramUserIdentity.verifiedInstant : ZonedDateTime.now(ZoneOffset.UTC);
    this.userMapper.setIdentityVerified(paramUser.id, paramUserIdentity.value, paramUserIdentity.type, paramUserIdentity.verifiedInstant, IdentityVerifiedReason.Implicit);
    if (paramUser.verifiedInstant == null) {
      paramUser.verifiedInstant = paramUserIdentity.verifiedInstant;
      this.userMapper.updateUserVerifiedInstant(paramUser.id, paramUser.verifiedInstant);
    } 
    DefaultUserReaderService.normalizeFromIdentities(paramUser);
    if (paramUserIdentity.type.is(IdentityType.email))
      EventHelper.send(paramTenant, paramApplication, new UserEmailVerifiedEvent(paramEventInfo, paramUser)); 
    EventHelper.send(paramTenant, paramApplication, new UserIdentityVerifiedEvent(paramEventInfo, paramUserIdentity.value, paramUserIdentity.type.name, paramUser));
    this.externalIdentifierService.deleteIdentityVerificationsByUserAndIdentityType(paramUser.id, paramUserIdentity.value, paramUserIdentity.type);
    return true;
  }
  
  public void moderateUsername(User paramUser, Validator paramValidator) {
    UserIdentity userIdentity = paramUser.resolvePrimaryIdentity(IdentityType.username);
    if (userIdentity == null)
      return; 
    ModerateResponse moderateResponse = this.moderationService.moderate(paramUser);
    if (moderateResponse != null && moderateResponse.contentAction == FilterAction.reject) {
      paramValidator.ensure(false, "user.username", "[moderationRejected]", new Object[] { paramUser.username });
    } else if (moderateResponse != null && moderateResponse.moderationAction == ModerationType.requiresApproval) {
      userIdentity.moderationStatus = ContentStatus.PENDING;
    } 
  }
  
  public void moderateUsername(UserRegistration paramUserRegistration, Application paramApplication, UUID paramUUID, Validator paramValidator) {
    ModerateResponse moderateResponse = this.moderationService.moderate(paramUserRegistration, paramApplication, paramUUID);
    if (moderateResponse != null && moderateResponse.contentAction == FilterAction.reject) {
      paramValidator.ensure(false, "registration.username", "[moderationRejected]", new Object[] { paramUserRegistration.username });
    } else if (moderateResponse != null && moderateResponse.moderationAction == ModerationType.requiresApproval) {
      paramUserRegistration.usernameStatus = ContentStatus.PENDING;
    } 
  }
  
  public void preVerify(Tenant paramTenant, Application paramApplication, ExternalIdentifier paramExternalIdentifier, EventInfo paramEventInfo) {
    verifyIdentity(paramTenant, paramApplication, null, paramExternalIdentifier, null, paramEventInfo);
  }
  
  @Transactional
  public void reactivate(Tenant paramTenant, User paramUser, EventInfo paramEventInfo) {
    if (paramUser.active)
      return; 
    paramUser.active = true;
    this.userMapper.setActive(paramUser.id, true);
    EventHelper.send(paramTenant, null, new UserReactivateEvent(paramEventInfo, paramUser));
    this.searchEngine.index(Collections.singletonList(paramUser));
  }
  
  public void refreshSearchIndex() {
    this.searchEngine.refresh();
  }
  
  public void reindexUser(User paramUser, UUID paramUUID) {
    logger.atTrace().addArgument(() -> UserTools.userAndRegistrationApplications(paramUser)).log("Reindexing {}");
    try {
      this.searchEngine.index(Collections.singletonList(paramUser));
    } catch (SearchEngineException searchEngineException) {
      if (Application.FUSIONAUTH_APP_ID.equals(paramUUID))
        EventLogHelper.create(new EventLog(EventLogType.Error, "Search engine is unavailable. Unable to index User's last login instant during the last login attempt.\nEnsure the search engine is running and accessible by FusionAuth.", (Throwable)searchEngineException)); 
    } 
  }
  
  public void removeParentEmail(User paramUser) {
    this.userMapper.removeParentEmail(paramUser.id);
    paramUser.parentEmail = null;
    this.searchEngine.index(Collections.singletonList(paramUser));
  }
  
  public EmailSendResult sendEmail(Tenant paramTenant, Application paramApplication, EmailTemplate paramEmailTemplate, List<User> paramList, List<EmailAddress> paramList1, List<String> paramList2, List<String> paramList3, Map<String, Object> paramMap, List<Locale> paramList4) {
    EmailSendResult emailSendResult = new EmailSendResult();
    emailSendResult
      
      .userIdErrors = (Map<UUID, SendResponse.EmailTemplateErrors>)paramList.stream().collect(Collectors.toMap(paramUser -> paramUser.id, paramUser -> toEmailTemplateErrors(sendEmailToUser(paramTenant, paramApplication, paramUser, paramEmailTemplate, paramUser.preferredLanguages, paramList1, paramList2, paramMap))));
    emailSendResult
      
      .emailErrors = (Map<String, SendResponse.EmailTemplateErrors>)paramList1.stream().collect(
        Collectors.toMap(paramEmailAddress -> paramEmailAddress.address, paramEmailAddress -> {
            User user = (new User()).with(()).with(()).with(());
            return toEmailTemplateErrors(sendEmailToUser(paramTenant, paramApplication, user, paramEmailTemplate, paramList1, paramList2, paramList3, paramMap));
          }));
    return emailSendResult;
  }
  
  public void sendVerify(Tenant paramTenant, ExternalIdentifier paramExternalIdentifier, Application paramApplication, User paramUser) {
    RateLimitedRequestType rateLimitedRequestType;
    IdentityType identityType = IdentityExternalIdHelper.getLoginIdentityType(paramExternalIdentifier);
    if (identityType.is(IdentityType.email)) {
      rateLimitedRequestType = RateLimitedRequestType.SendEmailVerification;
    } else if (identityType.is(IdentityType.phoneNumber)) {
      rateLimitedRequestType = RateLimitedRequestType.SendPhoneVerification;
    } else {
      throw new IllegalArgumentException("Unsupported identity type: " + String.valueOf(identityType));
    } 
    String str = (paramUser == null) ? IdentityExternalIdHelper.getLoginId(paramExternalIdentifier) : paramUser.id.toString();
    this.rateLimitService.handleAndThrow(paramTenant, rateLimitedRequestType, str);
    sendVerifyWithoutRateLimit(paramTenant, paramExternalIdentifier, paramApplication, paramUser);
  }
  
  public ExternalIdentifier startVerify(Tenant paramTenant, String paramString, IdentityType paramIdentityType, VerificationStrategy paramVerificationStrategy, User paramUser, Application paramApplication, Map<String, Object> paramMap) {
    ExternalIdentifier.ExternalIdType externalIdType1, externalIdType2;
    String str = IdentityHelper.canonicalizeValue(paramString, paramIdentityType);
    ExternalIdentifier.ExternalIdData externalIdData = (new ExternalIdentifier.ExternalIdData(paramMap)).setAttribute("loginId", str);
    if (paramIdentityType.is(IdentityType.email)) {
      externalIdType1 = ExternalIdentifier.ExternalIdType.EmailVerification;
      externalIdType2 = ExternalIdentifier.ExternalIdType.EmailVerificationOneTimeCode;
    } else if (paramIdentityType.is(IdentityType.phoneNumber)) {
      externalIdType1 = ExternalIdentifier.ExternalIdType.PhoneVerification;
      externalIdType2 = ExternalIdentifier.ExternalIdType.PhoneVerificationOneTimeCode;
    } else {
      throw new IllegalArgumentException("Unsupported identity type: " + String.valueOf(paramIdentityType));
    } 
    if (paramVerificationStrategy == VerificationStrategy.FormField) {
      String str1 = this.externalIdentifierService.generateExternalId(paramTenant, externalIdType2);
      externalIdData.setAttribute("otp", str1);
    } 
    return this.externalIdentifierService.createIdentityVerification(paramTenant, externalIdType1, externalIdData, 

        
        Optional.<Application>ofNullable(paramApplication)
        .map(paramApplication -> paramApplication.id)
        .orElse((UUID)null), 
        Optional.<User>ofNullable(paramUser).map(paramUser -> paramUser.id).orElse((UUID)null));
  }
  
  public UserService.UserResult update(Tenant paramTenant, Application paramApplication, User paramUser1, User paramUser2, boolean paramBoolean, PasswordType paramPasswordType, EventInfo paramEventInfo) {
    paramUser2.connectorId = paramUser1.connectorId;
    UserService.UpdateUserOptions updateUserOptions = (new UserService.UpdateUserOptions()).with(paramUpdateUserOptions -> paramUpdateUserOptions.deleteRefreshTokensOnPasswordChange = paramTenant.jwtConfiguration.refreshTokenRevocationPolicy.onPasswordChanged).with(paramUpdateUserOptions -> paramUpdateUserOptions.sendPasswordUpdatedEventOnPasswordChange = true).with(paramUpdateUserOptions -> paramUpdateUserOptions.skipVerification = paramBoolean);
    return updateAllowConnectorIdChange(paramTenant, paramApplication, paramUser1, paramUser2, updateUserOptions, paramEventInfo, paramPasswordType);
  }
  
  public UserService.UserResult updateAllowConnectorIdChange(Tenant paramTenant, Application paramApplication, User paramUser1, User paramUser2, UserService.UpdateUserOptions paramUpdateUserOptions, EventInfo paramEventInfo, PasswordType paramPasswordType) {
    UserService.UserResult userResult = _updateAllowConnectorIdChange(paramTenant, paramApplication, paramUser1, paramUser2, paramUpdateUserOptions, paramEventInfo, paramPasswordType);
    EventHelper.send(paramTenant, paramApplication, new UserUpdateCompleteEvent(paramEventInfo, paramUser1, paramUser2));
    userResult.events.forEach(paramBaseEvent -> EventHelper.send(paramTenant, paramApplication, paramBaseEvent));
    return userResult;
  }
  
  @Transactional
  public void updateFromModeration(Map<UUID, ModerationAction> paramMap) {
    Set<UUID> set1 = (Set)paramMap.entrySet().stream().filter(paramEntry -> (paramEntry.getValue() == ModerationAction.approved)).map(Map.Entry::getKey).collect(Collectors.toSet());
    if (set1.size() > 0) {
      this.userMapper.updateUsernameStatuses(set1, ContentStatus.ACTIVE);
      this.userMapper.updateRegistrationUsernameStatuses(set1, ContentStatus.ACTIVE);
    } 
    Set<UUID> set2 = (Set)paramMap.entrySet().stream().filter(paramEntry -> (paramEntry.getValue() == ModerationAction.rejected)).map(Map.Entry::getKey).collect(Collectors.toSet());
    if (set2.size() > 0) {
      this.userMapper.updateUsernameStatuses(set2, ContentStatus.REJECTED);
      this.userMapper.updateRegistrationUsernameStatuses(set2, ContentStatus.REJECTED);
    } 
  }
  
  public void updateIdentityVerifiedReason(User paramUser, UserIdentity paramUserIdentity) {
    String str = paramUserIdentity.value;
    IdentityType identityType = paramUserIdentity.type;
    this.userMapper.setIdentityVerifiedReason(paramUser.id, str, identityType, paramUserIdentity.verifiedReason);
  }
  
  @Transactional
  public ChangePasswordResult updatePassword(Tenant paramTenant, Application paramApplication, String paramString1, String paramString2, JWT paramJWT, RefreshToken paramRefreshToken, String paramString3, EventInfo paramEventInfo, User paramUser) {
    ChangePasswordResult changePasswordResult = new ChangePasswordResult();
    changePasswordResult.success = _updatePassword(paramTenant, paramUser, paramString1, paramString2, paramString3, paramEventInfo, null);
    if (changePasswordResult.success) {
      ExternalIdentifier.ExternalIdData externalIdData = new ExternalIdentifier.ExternalIdData();
      externalIdData.setAttribute("bypass2FA", true);
      if (paramJWT != null) {
        externalIdData.setAttribute("issueJWT", true);
        externalIdData.setAttribute("jwtExpirationInstant", paramJWT.expiration);
      } 
      if (paramRefreshToken != null) {
        externalIdData.setAttribute("issueRefreshToken", true);
        externalIdData.setAttribute("refreshTokenStartInstant", paramRefreshToken.startInstant);
      } 
      changePasswordResult.oneTimePassword = this.externalIdentifierService.createOneTimePassword(paramTenant, paramUser.id, externalIdData);
      EventHelper.send(paramTenant, paramApplication, new UserPasswordUpdateEvent(paramEventInfo, paramUser));
    } 
    return changePasswordResult;
  }
  
  @Transactional
  public ChangePasswordResult updatePasswordByChangePasswordId(Tenant paramTenant, Application paramApplication, User paramUser, ExternalIdentifier paramExternalIdentifier, String paramString1, String paramString2, String paramString3, EventInfo paramEventInfo) {
    ChangePasswordResult changePasswordResult = new ChangePasswordResult();
    changePasswordResult.state = paramExternalIdentifier.getStateHelper();
    changePasswordResult.success = _updatePassword(paramTenant, paramUser, paramString1, paramString2, paramString3, paramEventInfo, paramExternalIdentifier);
    if (changePasswordResult.success) {
      verifyIdentityAfterPasswordChange(paramTenant, paramApplication, paramExternalIdentifier, paramEventInfo, paramUser);
      boolean bool = (paramUser.twoFactorEnabled() || !this.mfaService.isMethodRequired(paramTenant, paramApplication)) ? true : false;
      ExternalIdentifier.ExternalIdData externalIdData = (new ExternalIdentifier.ExternalIdData()).setAttribute("bypass2FA", bool).setAttribute("issueJWT", true).setAttribute("issueRefreshToken", true).setAttribute("loginId", paramExternalIdentifier.getAttribute("loginId")).setAttribute("loginIdType", paramExternalIdentifier.getAttribute("loginIdType"));
      changePasswordResult.oneTimePassword = this.externalIdentifierService.createOneTimePassword(paramTenant, paramUser.id, externalIdData);
      EventHelper.send(paramTenant, paramApplication, new UserPasswordResetSuccessEvent(paramEventInfo, paramUser));
    } 
    if (paramTenant.failedAuthenticationConfiguration.userActionId != null && paramTenant.failedAuthenticationConfiguration.actionCancelPolicy.onPasswordReset) {
      UserActionLog userActionLog = this.userActionService.retrieveAllCurrentPreventLoginActionLogsForUser(paramUser.id).stream().filter(paramUserActionLog -> paramUserActionLog.userActionId.equals(paramTenant.failedAuthenticationConfiguration.userActionId)).filter(UserActionLog::isActive).findFirst().orElse((UserActionLog)null);
      if (userActionLog != null) {
        UserAction userAction = this.userActionService.retrieveById(userActionLog.userActionId);
        ActionRequest.ActionData actionData = new ActionRequest.ActionData();
        actionData.comment = "User has reset their password.";
        actionData.notifyUser = userAction.userNotificationsEnabled;
        actionData.emailUser = (paramTenant.failedAuthenticationConfiguration.emailUser && userAction.userEmailingEnabled);
        actionData.userActionId = paramTenant.failedAuthenticationConfiguration.userActionId;
        this.actionService.cancelAction(paramTenant, paramUser, userAction, userActionLog, actionData, true, paramEventInfo);
      } 
      this.rateLimitMapper.deleteRequestFrequencyRecordsByUserId(paramTenant.id, RateLimitedRequestType.FailedLogin, paramUser.id.toString());
    } 
    return changePasswordResult;
  }
  
  @Transactional
  public ChangePasswordResult updatePasswordByLoginId(Tenant paramTenant, Application paramApplication, String paramString1, String paramString2, String paramString3, String paramString4, EventInfo paramEventInfo, User paramUser) {
    ChangePasswordResult changePasswordResult = new ChangePasswordResult();
    changePasswordResult.success = _updatePassword(paramTenant, paramUser, paramString2, paramString3, paramString4, paramEventInfo, null);
    if (changePasswordResult.success)
      EventHelper.send(paramTenant, paramApplication, new UserPasswordUpdateEvent(paramEventInfo, paramUser)); 
    return changePasswordResult;
  }
  
  public void updatePasswordChangeRequired(User paramUser) {
    this.userMapper.updatePasswordChangeRequired(paramUser.id, paramUser.passwordChangeReason, paramUser.passwordChangeRequired);
    this.searchEngine.index(Collections.singletonList(paramUser));
  }
  
  public UserService.RegistrationResult updateRegistration(Tenant paramTenant, Application paramApplication, User paramUser, UserRegistration paramUserRegistration1, UserRegistration paramUserRegistration2, Collection<ApplicationRole> paramCollection, boolean paramBoolean, EventInfo paramEventInfo) {
    UserService.RegistrationResult registrationResult = _updateRegistration(paramTenant, paramApplication, paramUser, paramUserRegistration1, paramUserRegistration2, paramCollection, paramBoolean, paramEventInfo);
    EventHelper.send(paramTenant, paramApplication, new UserRegistrationUpdateCompleteEvent(paramEventInfo, paramUserRegistration2.applicationId, paramUserRegistration1, registrationResult.registration, registrationResult.user));
    return registrationResult;
  }
  
  public void updateTwoFactor(User paramUser, String paramString1, String paramString2) {
    TwoFactorMethod twoFactorMethod = paramUser.twoFactor.getMethodById(paramString1);
    if (twoFactorMethod == null)
      return; 
    twoFactorMethod.name = paramString2;
    this.userMapper.update(paramUser);
    reindexUser(paramUser, null);
  }
  
  public UserService.IdentityValidationResult validateAdministrativeVerify(Tenant paramTenant, String paramString1, String paramString2) {
    UserService.IdentityValidationResult identityValidationResult = new UserService.IdentityValidationResult();
    identityValidationResult.identityType = IdentityType.of(paramString2);
    identityValidationResult.tenant = paramTenant;
    identityValidationResult


      
      .errors = (new Validator()).notBlank(paramString1, "loginId", new Object[0]).notBlank(paramString2, "loginIdType", new Object[0]).ifLastCheckHadNoError(paramValidator -> IdentityTypeValidator.validate(paramValidator, paramString, "loginIdType", IdentityTypeValidator.VerifiableIdentityTypes)).done();
    if (identityValidationResult.errors.empty()) {
      identityValidationResult.user = this.userReader.retrieveByLoginId(paramTenant.id, paramString1, List.of(identityValidationResult.identityType));
      if (identityValidationResult.user != null)
        identityValidationResult.identity = IdentityHelper.resolveIdentity(identityValidationResult.user, paramString1, identityValidationResult.identityType); 
    } 
    return identityValidationResult;
  }
  
  public Errors validateBulk(Tenant paramTenant, List<User> paramList, boolean paramBoolean, String paramString, Integer paramInteger, Map<String, String> paramMap) {
    Set set = (Set)paramList.stream().flatMap(paramUser -> paramUser.getRegistrations().stream()).map(paramUserRegistration -> paramUserRegistration.applicationId).filter(Objects::nonNull).collect(Collectors.toSet());
    List<Application> list = this.applicationReader.retrieveByIds(paramTenant.id, set.stream().toList(), Set.of(ApplicationReaderService.ApplicationExpansion.roles));
    Map map = (Map)list.stream().filter(Objects::nonNull).collect(Collectors.toMap(paramApplication -> paramApplication.id, paramApplication -> paramApplication));
    TreeSet<String> treeSet1 = new TreeSet();
    TreeSet<? extends CharSequence> treeSet = new TreeSet();
    TreeSet<String> treeSet2 = new TreeSet();
    TreeSet<String> treeSet3 = new TreeSet();
    TreeSet<Integer> treeSet4 = new TreeSet();
    TreeSet<UUID> treeSet5 = new TreeSet();
    TreeSet<String> treeSet6 = new TreeSet();
    TreeSet<String> treeSet7 = new TreeSet();
    TreeSet<String> treeSet8 = new TreeSet();
    TreeSet<String> treeSet9 = new TreeSet();
    TreeSet<String> treeSet10 = new TreeSet();
    TreeSet<String> treeSet11 = new TreeSet();
    TreeSet<String> treeSet12 = new TreeSet();
    TreeSet<String> treeSet13 = new TreeSet();
    TreeSet<String> treeSet14 = new TreeSet();
    TreeSet<String> treeSet15 = new TreeSet();
    TreeSet<String> treeSet16 = new TreeSet();
    TreeSet<String> treeSet17 = new TreeSet();
    TreeSet<String> treeSet18 = new TreeSet();
    TreeSet<String> treeSet19 = new TreeSet();
    TreeSet<String> treeSet20 = new TreeSet();
    TreeSet<String> treeSet21 = new TreeSet();
    TreeSet<String> treeSet22 = new TreeSet();
    TreeSet<String> treeSet23 = new TreeSet();
    TreeSet<String> treeSet24 = new TreeSet();
    TreeSet<String> treeSet25 = new TreeSet();
    TreeSet<String> treeSet26 = new TreeSet();
    Validator validator = new Validator();
    int i = 1;
    int j = 1;
    int k = 1;
    int m = 1;
    int n = 1;
    int i1 = 1;
    int i2 = 1;
    int i3 = 1;
    boolean bool = false;
    for (User user : paramList) {
      handleUserPrimaryIdentities(user, paramMap);
      if (user.id != null)
        if (treeSet5.contains(user.id)) {
          treeSet9.add(user.id.toString());
        } else {
          treeSet5.add(user.id);
        }  
      String str1 = user.email;
      if (!StringTools.isTrimmedEmpty(str1))
        if (treeSet6.contains(str1)) {
          treeSet10.add(str1);
        } else {
          treeSet6.add(str1);
          if (str1.length() > MapperTools.MaximumIndexedColumnLength)
            treeSet13.add(str1); 
        }  
      String str2 = user.phoneNumber;
      if (!StringTools.isTrimmedEmpty(str2))
        if (treeSet7.contains(str2)) {
          treeSet11.add(str2);
        } else {
          treeSet7.add(str2);
          if (!PhoneNumberValidator.validateE164format(str2))
            treeSet19.add(str2); 
        }  
      String str3 = user.username;
      if (!StringTools.isTrimmedEmpty(str3))
        if (treeSet8.contains(str3)) {
          treeSet12.add(str3);
        } else {
          treeSet8.add(str3);
          if (str3.length() > MapperTools.MaximumIndexedColumnLength)
            treeSet14.add(str3); 
        }  
      if (user.legacyIdentifier != null)
        if (treeSet17.contains(user.legacyIdentifier)) {
          treeSet18.add(user.legacyIdentifier);
        } else {
          treeSet17.add(user.legacyIdentifier);
          if (user.legacyIdentifier.length() > MapperTools.MaximumIndexedColumnLength)
            treeSet16.add(user.legacyIdentifier); 
        }  
      i &= (!StringTools.isTrimmedEmpty(str1) || !StringTools.isTrimmedEmpty(str2) || !StringTools.isTrimmedEmpty(str3)) ? 1 : 0;
      if (user.encryptionScheme != null) {
        j &= (user.salt != null) ? 1 : 0;
        k &= (user.factor != null) ? 1 : 0;
      } 
      validateSalt(treeSet3, user);
      validateEncryptionScheme(treeSet2, user);
      validateFactor(treeSet4, user, (paramString != null) ? paramString : paramTenant.passwordEncryptionConfiguration.encryptionScheme);
      validateTwoFactorOnBulkCreate(treeSet20, treeSet22, treeSet23, treeSet21, treeSet25, treeSet24, user);
      if (user.twoFactor.recoveryCodeEncryptionScheme != null) {
        if (!this.passwordEncryptorLibrary.validateScheme(user.twoFactor.recoveryCodeEncryptionScheme))
          treeSet26.add(user.twoFactor.recoveryCodeEncryptionScheme); 
        if (user.twoFactor.recoveryCodeWorkFactor == null)
          bool = true; 
      } 
      for (TwoFactorMethod twoFactorMethod : user.twoFactor.methods) {
        n &= (twoFactorMethod.method != null) ? 1 : 0;
        if (twoFactorMethod.method != null) {
          i1 &= (!twoFactorMethod.method.equals("authenticator") || twoFactorMethod.secret != null) ? 1 : 0;
          i3 &= (!twoFactorMethod.method.equals("email") || twoFactorMethod.email != null) ? 1 : 0;
          i2 &= (!twoFactorMethod.method.equals("sms") || twoFactorMethod.mobilePhone != null) ? 1 : 0;
        } 
      } 
      for (UserRegistration userRegistration : user.getRegistrations()) {
        Application application = (Application)map.get(userRegistration.applicationId);
        if (application == null) {
          m &= (userRegistration.applicationId != null) ? 1 : 0;
          if (userRegistration.applicationId != null)
            treeSet1.add(userRegistration.applicationId.toString()); 
        } else {
          treeSet.addAll(userRegistration.roles.stream()
              .filter(paramString -> (paramApplication.getRole(paramString) == null))
              .map(paramString -> paramString + " (app: " + paramString + ")")
              .toList());
        } 
        if (!StringTools.isTrimmedEmpty(userRegistration.username) && userRegistration.username.length() > MapperTools.MaximumIndexedColumnLength)
          treeSet15.add(userRegistration.username); 
      } 
    } 
    return validator.valid(treeSet1.isEmpty(), "user.registrations.applicationId", new Object[] { String.join(", ", (Iterable)treeSet1) }).valid(treeSet.isEmpty(), "user.registrations.roles", new Object[] { String.join(", ", treeSet) }).ensure(treeSet15.isEmpty(), "user.registrations.username", "[tooLong]", new Object[] { String.join(", ", (Iterable)treeSet15), Integer.valueOf(MapperTools.MaximumIndexedColumnLength) }).valid(treeSet2.isEmpty(), "user.encryptionScheme", new Object[] { String.join(", ", (Iterable)treeSet2) }).valid(treeSet4.isEmpty(), "user.factor", new Object[] { String.join(", ", (Iterable<? extends CharSequence>)treeSet4.stream().map(Objects::toString).collect(Collectors.toSet())) }).valid(treeSet3.isEmpty(), "user.salt", new Object[] { String.join(", ", (Iterable<? extends CharSequence>)treeSet3.stream().map(Objects::toString).collect(Collectors.toSet())) }).valid(treeSet9.isEmpty(), "user.id", new Object[] { String.join(", ", (Iterable)treeSet9) }).valid(treeSet10.isEmpty(), "user.email", new Object[] { String.join(", ", (Iterable)treeSet10) }).valid(treeSet11.isEmpty(), "user.phoneNumber", new Object[] { String.join(", ", (Iterable)treeSet11) }).valid(treeSet12.isEmpty(), "user.username", new Object[] { String.join(", ", (Iterable)treeSet12) }).ensure(treeSet13.isEmpty(), "user.email", "[tooLong]", new Object[] { String.join(", ", (Iterable)treeSet13), Integer.valueOf(MapperTools.MaximumIndexedColumnLength) }).ensure(treeSet14.isEmpty(), "user.username", "[tooLong]", new Object[] { String.join(", ", (Iterable)treeSet14), Integer.valueOf(MapperTools.MaximumIndexedColumnLength) }).ensure(treeSet16.isEmpty(), "user.legacyIdentifier", "[tooLong]", new Object[] { String.join(", ", (Iterable)treeSet16), Integer.valueOf(MapperTools.MaximumIndexedColumnLength) }).valid(treeSet18.isEmpty(), "user.legacyIdentifier", new Object[] { String.join(", ", (Iterable)treeSet18) }).ensure(treeSet19.isEmpty(), "user.phoneNumber", "[invalidPhone]", new Object[] { String.join(", ", (Iterable)treeSet19) }).valid(treeSet20.isEmpty(), "user.twoFactor.methods.method", new Object[] { String.join(", ", (Iterable<? extends CharSequence>)treeSet20.stream().map(Objects::toString).collect(Collectors.toSet())) }).ensure(treeSet24.isEmpty(), "user.twoFactor.methods.name", "[tooLong]", new Object[] { String.join(", ", (Iterable)treeSet24), Integer.valueOf(256) }).valid(treeSet21.isEmpty(), "user.twoFactor.methods.secret", new Object[] { String.join(", ", (Iterable<? extends CharSequence>)treeSet21.stream().map(Objects::toString).collect(Collectors.toSet())) }).valid(treeSet22.isEmpty(), "user.twoFactor.methods.email", new Object[] { String.join(", ", (Iterable<? extends CharSequence>)treeSet22.stream().map(Objects::toString).collect(Collectors.toSet())) }).valid(treeSet23.isEmpty(), "user.twoFactor.methods.mobilePhone", new Object[] { String.join(", ", (Iterable<? extends CharSequence>)treeSet23.stream().map(Objects::toString).collect(Collectors.toSet())) }).ensure(treeSet25.isEmpty(), "user.twoFactor.methods", "[duplicate]", new Object[] { String.join(", ", (Iterable<? extends CharSequence>)treeSet25.stream().map(Objects::toString).collect(Collectors.toSet())) }).ensure(j, "user.salt", "[blank]", new Object[0])
      .ensure(k, "user.factor", "[blank]", new Object[0])
      .ensure(i, "user.email", "[blank]", new Object[0])
      .ensure(i, "user.phoneNumber", "[blank]", new Object[0])
      .ensure(i, "user.username", "[blank]", new Object[0])
      .ensure(m, "user.registration.applicationId", "[missing]", new Object[0])
      
      .ensure(n, "user.twoFactor.methods.method", "[blank]", new Object[0])
      .ensure(i3, "user.twoFactor.methods.email", "[blank]", new Object[0])
      .ensure(i2, "user.twoFactor.methods.mobilePhone", "[blank]", new Object[0])
      .ensure(i1, "user.twoFactor.methods.secret", "[blank]", new Object[0])
      .ensure(treeSet26.isEmpty(), "user.twoFactor.recoveryCodeEncryptionScheme", "[unsupported]", new Object[] { String.join(", ", (Iterable)treeSet26) }).ensure(!bool, "user.twoFactor.recoveryCodeWorkFactor", "[missing]", new Object[0])
      .ifTrue((paramString != null), () -> paramValidator.ensure(this.passwordEncryptorLibrary.validateScheme(paramString), "encryptionScheme", "[invalid]", new Object[] { paramString })).ifTrue((paramInteger != null), () -> paramValidator.ensure(this.passwordEncryptorLibrary.validateFactor(paramString, paramTenant.passwordEncryptionConfiguration.encryptionScheme, paramInteger), "factor", "[invalid]", new Object[] { paramInteger })).ifTrue(paramBoolean, () -> paramValidator.ifNoErrors(()))
      
      .done();
  }
  
  public UserService.ValidationResult validateBulkDelete(Tenant paramTenant, List<UUID> paramList, int paramInt, String paramString1, String paramString2) {
    UserService.ValidationResult validationResult = new UserService.ValidationResult();
    validationResult


      
      .errors = (new Validator()).ifTrue((paramString2 != null || paramString1 != null), paramValidator -> paramValidator.withErrors(validateSearchQuery((paramTenant != null) ? paramTenant.id : null, paramString1, paramString2, null, 0, 1))).ifTrue((this.configuration.searchEngineType() == SearchEngineType.database), paramValidator -> paramValidator.ensure((paramString == null), "query", "[unsupported]", new Object[0])).ensure((paramInt > 0), "limit", "[invalid]", new Object[0]).done();
    if (!validationResult.errors.empty())
      return validationResult; 
    return validationResult;
  }
  
  public Errors validateChangePassword(UUID paramUUID, String paramString1, String paramString2) {
    return (new Validator()).notBlank(paramString1, "password", new Object[0])
      .ifTrue((paramUUID != null), paramValidator -> paramValidator.notBlank(paramString, "currentPassword", new Object[0]))
      .done();
  }
  
  public UserService.IdentityValidationResult validateCompleteVerify(Tenant paramTenant, String paramString1, String paramString2) {
    UserService.IdentityValidationResult identityValidationResult = commonVerifyValidation(paramTenant, paramString1);
    if (identityValidationResult.errors.empty() && identityValidationResult.externalId != null) {
      String str = identityValidationResult.externalId.getAttribute("otp");
      identityValidationResult.errors.add((new Validator())
          
          .ifTrue((str != null), paramValidator -> paramValidator.notBlank(paramString1, "oneTimeCode", new Object[0]).ifLastCheckHadNoError(()))



          
          .done());
    } 
    return identityValidationResult;
  }
  
  public UserService.ValidationResult validateConnectorCreate(Tenant paramTenant, User paramUser, Application paramApplication, EventInfo paramEventInfo, boolean paramBoolean) {
    return validateUserCreate(paramTenant, paramUser, paramApplication.id, true, SendSetPasswordIdentityType.doNotSend, false, false, paramEventInfo, paramBoolean);
  }
  
  public UserService.ValidationResult validateConnectorUpdate(Tenant paramTenant, Application paramApplication, EventInfo paramEventInfo, User paramUser) {
    return validateUserUpdate(paramTenant, paramUser, false, (paramApplication != null) ? paramApplication.id : null, true, false, null, paramEventInfo, PasswordType.PLAINTEXT);
  }
  
  public UserService.ValidationResult validateCreate(Tenant paramTenant, User paramUser, UUID paramUUID, boolean paramBoolean1, SendSetPasswordIdentityType paramSendSetPasswordIdentityType, @Deprecated boolean paramBoolean2, EventInfo paramEventInfo, boolean paramBoolean3) {
    return validateUserCreate(paramTenant, paramUser, paramUUID, paramBoolean1, paramSendSetPasswordIdentityType, paramBoolean2, true, paramEventInfo, paramBoolean3);
  }
  
  public UserService.TwoFactorEnableValidationResult validateDisableTwoFactor(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString1, String paramString2) {
    UserService.TwoFactorEnableValidationResult twoFactorEnableValidationResult = new UserService.TwoFactorEnableValidationResult();
    twoFactorEnableValidationResult.user = (paramUUID2 != null) ? this.userReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID2) : null;
    Objects.requireNonNull(this.tenantReader);
    twoFactorEnableValidationResult.tenant = this.tenantCache.resolve(paramTenant, this.tenantReader::retrieveById, new Tenantable[] { twoFactorEnableValidationResult.user });
    twoFactorEnableValidationResult


      
      .errors = (new Validator()).notBlank(paramString1, "code", new Object[0]).notBlank(paramString2, "methodId", new Object[0]).notBlank(paramUUID2, "userId", new Object[0]).done();
    if (twoFactorEnableValidationResult.tenant == null || twoFactorEnableValidationResult.user == null || !twoFactorEnableValidationResult.errors.empty())
      return twoFactorEnableValidationResult; 
    Objects.requireNonNull(this.applicationReader);
    twoFactorEnableValidationResult.application = (paramUUID1 != null) ? this.applicationCache.get(twoFactorEnableValidationResult.tenant.id, paramUUID1, this.applicationReader::retrieveById) : null;
    TwoFactorMethod twoFactorMethod = twoFactorEnableValidationResult.user.twoFactor.getMethodById(paramString2);
    if (twoFactorMethod != null) {
      if (twoFactorMethod.method.equals("authenticator")) {
        twoFactorEnableValidationResult.twoFactorMethodId = twoFactorMethod.id;
      } else if (paramString1 != null) {
        twoFactorEnableValidationResult.externalId = (this.externalIdentifierReader.validate(paramTenant, paramString1, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.TwoFactorOneTimeCode })).id;
        if (twoFactorEnableValidationResult.externalId != null) {
          twoFactorEnableValidationResult.twoFactorMethodId = twoFactorEnableValidationResult.externalId.getAttribute("methodId");
        } else {
          twoFactorEnableValidationResult.twoFactorMethodId = paramString2;
        } 
      } 
      twoFactorEnableValidationResult.method = twoFactorMethod.method;
    } 
    twoFactorEnableValidationResult











      
      .errors = (new Validator()).notMissingWithCode(twoFactorMethod, "methodId", "[invalid]methodId", new Object[] { paramString2 }).ifLastCheckHadNoError(paramValidator -> paramValidator.ifTrue((paramTwoFactorMethod.method.equals("email") || paramTwoFactorMethod.method.equals("sms")), ()).ifNoFieldErrors("method", ())).done();
    return twoFactorEnableValidationResult;
  }
  
  public UserService.ValidationResult validateEmailSend(Tenant paramTenant, UUID paramUUID1, List<UUID> paramList, List<EmailAddress> paramList1, UUID paramUUID2) {
    UserService.ValidationResult validationResult = new UserService.ValidationResult();
    validationResult.emailTemplate = this.emailTemplateMapper.retrieveById(paramUUID2);
    validationResult.users = this.userReader.retrieveByIds((paramTenant != null) ? paramTenant.id : null, paramList, UserReaderService.UserExpansion.all());
    validationResult.users.removeIf(paramUser -> !paramUser.hasIdentityType(IdentityType.email));
    Objects.requireNonNull(this.applicationReader);
    validationResult.application = (paramUUID1 != null) ? this.applicationCache.get((paramTenant != null) ? paramTenant.id : null, paramUUID1, this.applicationReader::retrieveById) : null;
    Objects.requireNonNull(this.tenantReader);
    validationResult.tenant = this.tenantCache.resolve(paramTenant, this.tenantReader::retrieveById, new Tenantable[] { (validationResult.users.size() > 0) ? validationResult.users.get(0) : null, validationResult.application });
    validationResult


      
      .errors = (new Validator()).ifTrue(paramList1.isEmpty(), paramValidator -> paramValidator.notEmpty(paramList, "userIds", new Object[0])).notMissing(paramUUID2, "emailTemplateId", new Object[0]).ifTrue((paramUUID1 != null), paramValidator -> paramValidator.ensure((paramValidationResult.application != null), "applicationId", "[invalid]", new Object[] { paramUUID })).done();
    return validationResult;
  }
  
  public UserService.TwoFactorEnableValidationResult validateEnableTwoFactor(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7) {
    UserService.TwoFactorEnableValidationResult twoFactorEnableValidationResult = new UserService.TwoFactorEnableValidationResult();
    twoFactorEnableValidationResult.user = (paramUUID2 != null) ? this.userReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID2) : null;
    Objects.requireNonNull(this.tenantReader);
    twoFactorEnableValidationResult.tenant = this.tenantCache.resolve(paramTenant, this.tenantReader::retrieveById, new Tenantable[] { twoFactorEnableValidationResult.user });
    if (twoFactorEnableValidationResult.tenant == null)
      return twoFactorEnableValidationResult; 
    Objects.requireNonNull(this.applicationReader);
    twoFactorEnableValidationResult.application = (paramUUID1 != null) ? this.applicationCache.get(twoFactorEnableValidationResult.tenant.id, paramUUID1, this.applicationReader::retrieveById) : null;
    twoFactorEnableValidationResult



      
      .errors = (new Validator()).notBlank(paramUUID2, "userId", new Object[0]).notBlank(paramString1, "code", new Object[0]).notBlank(paramString3, "method", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(MFAService.SupportedMethods.contains(paramString), "method", "[invalid]", new Object[] { paramString, String.join(", ", (Iterable)MFAService.SupportedMethods) })).done();
    if (!twoFactorEnableValidationResult.errors.empty())
      return twoFactorEnableValidationResult; 
    if (MFAService.SupportedMethodsForSend.contains(paramString3)) {
      ExternalIdentifierReaderService.ValidationResult validationResult = this.externalIdentifierReader.validate(twoFactorEnableValidationResult.tenant, paramString1, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.TwoFactorOneTimeCode });
      twoFactorEnableValidationResult.externalId = validationResult.id;
    } 
    twoFactorEnableValidationResult.method = paramString3;
    twoFactorEnableValidationResult





























      
      .errors = (new Validator()).ifNoFieldErrors("method", paramValidator -> paramValidator.ifTrue(paramTwoFactorEnableValidationResult.method.equals("authenticator"), ()).ifTrue(paramTwoFactorEnableValidationResult.method.equals("email"), ()).ifTrue(paramTwoFactorEnableValidationResult.method.equals("sms"), ()).ifTrue((paramString5 != null), ())).validate(paramValidator -> commonTwoFactorValidation(paramValidator, paramTwoFactorEnableValidationResult.tenant, paramTwoFactorEnableValidationResult.method)).ifTrue(MFAService.SupportedMethodsForSend.contains(paramString3), paramValidator -> paramValidator.ensure((paramTwoFactorEnableValidationResult.externalId != null), "code", "[invalid]", new Object[0]).ifLastCheckHadNoError(())).done();
    return twoFactorEnableValidationResult;
  }
  
  public UserService.ValidationResult validateForgotPassword(Tenant paramTenant, UUID paramUUID, String paramString, List<String> paramList) {
    UserService.ValidationResult validationResult = new UserService.ValidationResult();
    if (paramUUID != null) {
      Objects.requireNonNull(this.applicationReader);
      validationResult.application = this.applicationCache.get((paramTenant != null) ? paramTenant.id : null, paramUUID, this.applicationReader::retrieveById);
    } 
    Objects.requireNonNull(this.tenantReader);
    validationResult.tenant = this.tenantCache.resolve(paramTenant, this.tenantReader::retrieveById, new Tenantable[] { validationResult.application });
    validationResult.identityTypes = IdentityTypeHelper.convert(paramList);
    validationResult



      
      .errors = (new Validator()).ifTrue((paramUUID != null), paramValidator -> paramValidator.validObject(paramValidationResult.application, "applicationId", new Object[] { paramUUID })).validate(paramValidator -> IdentityTypeValidator.validate(paramValidator, paramList, "loginIdTypes")).done();
    return validationResult;
  }
  
  public UserService.ValidationResult validateId(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2) {
    UserService.ValidationResult validationResult = new UserService.ValidationResult();
    Objects.requireNonNull(this.applicationReader);
    validationResult.application = (paramUUID1 != null) ? this.applicationCache.get((paramTenant != null) ? paramTenant.id : null, paramUUID1, this.applicationReader::retrieveById) : null;
    validationResult.user = this.userReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID2);
    Objects.requireNonNull(this.tenantReader);
    validationResult.tenant = this.tenantCache.resolve(paramTenant, this.tenantReader::retrieveById, new Tenantable[] { validationResult.user });
    return validationResult;
  }
  
  public UserService.ValidationResult validateLoginId(Tenant paramTenant, UUID paramUUID, String paramString, List<String> paramList) {
    UserService.ValidationResult validationResult = new UserService.ValidationResult();
    Objects.requireNonNull(this.applicationReader);
    validationResult.application = (paramUUID != null) ? this.applicationCache.get((paramTenant != null) ? paramTenant.id : null, paramUUID, this.applicationReader::retrieveById) : null;
    validationResult.identityTypes = IdentityTypeHelper.convert(paramList);
    Objects.requireNonNull(this.tenantReader);
    validationResult.tenant = this.tenantCache.resolve(paramTenant, this.tenantReader::retrieveById, new Tenantable[] { validationResult.application });
    validationResult

      
      .errors = (new Validator()).validate(paramValidator -> IdentityTypeValidator.validate(paramValidator, paramList, "loginIdTypes")).done();
    if (validationResult.errors.empty())
      validationResult.user = this.userReader.retrieveByLoginId((validationResult.tenant != null) ? validationResult.tenant.id : null, paramString, validationResult.identityTypes); 
    return validationResult;
  }
  
  public UserService.ValidationResult validatePreVerifiedCreate(Tenant paramTenant, User paramUser, UUID paramUUID, List<String> paramList, boolean paramBoolean1, SendSetPasswordIdentityType paramSendSetPasswordIdentityType, @Deprecated boolean paramBoolean2, EventInfo paramEventInfo) {
    User user = Optional.<User>ofNullable(paramUser).orElse(new User());
    handleUserPrimaryIdentities(user);
    UserService.ValidationResult validationResult1 = new UserService.ValidationResult();
    ExternalIdentifier externalIdentifier = null;
    HashMap<Object, Object> hashMap = new HashMap<>();
    ArrayList<ExternalIdentifier> arrayList = new ArrayList();
    Validator validator = new Validator();
    for (String str1 : paramList) {
      ExternalIdentifier externalIdentifier1 = (this.externalIdentifierReader.validate(paramTenant, str1, IdentityExternalIdHelper.identityExternalIdTypes)).id;
      Consumer<String> consumer = paramString2 -> paramValidator.ensure(false, "verificationIds", paramString2, new Object[] { paramString1 });
      if (externalIdentifier1 == null) {
        consumer.accept("[invalid]");
        continue;
      } 
      if (externalIdentifier == null)
        externalIdentifier = externalIdentifier1; 
      arrayList.add(externalIdentifier1);
      IdentityType identityType = IdentityExternalIdHelper.getLoginIdentityType(externalIdentifier1);
      Set<String> set = (Set)hashMap.getOrDefault(identityType, new HashSet());
      if (!set.add(externalIdentifier1.id))
        consumer.accept("[duplicate]"); 
      validator.ensure((set.size() == 1), "verificationIds", "[multiplePerType]", new Object[] { str1, identityType });
      hashMap.put(identityType, set);
      if (!externalIdentifier1.tenantId.equals(externalIdentifier.tenantId))
        consumer.accept("[invalid]"); 
      if (!externalIdentifier1.getAttributeAsBoolean("loginIdVerified"))
        consumer.accept("[invalid]"); 
      UserIdentity userIdentity = user.resolvePrimaryIdentity(identityType);
      String str2 = IdentityExternalIdHelper.getLoginId(externalIdentifier1);
      if (userIdentity != null && !IdentityHelper.canonicalizeValue(userIdentity).equals(str2))
        validator.ensure(false, "verificationIds", "[mismatch]", new Object[] { str1, str2, identityType, userIdentity.value }); 
    } 
    validator.ifTrue((paramSendSetPasswordIdentityType == SendSetPasswordIdentityType.email), paramValidator -> paramValidator.ensure(paramMap.containsKey(IdentityType.email), "verificationIds", "[notEmail]", new Object[0]))


      
      .ifTrue((paramSendSetPasswordIdentityType == SendSetPasswordIdentityType.phone), paramValidator -> paramValidator.ensure(paramMap.containsKey(IdentityType.phoneNumber), "verificationIds", "[notPhoneNumber]", new Object[0]));
    validationResult1.errors = validator.done();
    if (!validationResult1.errors.empty())
      return validationResult1; 
    for (ExternalIdentifier externalIdentifier1 : arrayList) {
      UserIdentity userIdentity = (new UserIdentity()).with(paramUserIdentity -> paramUserIdentity.type = IdentityExternalIdHelper.getLoginIdentityType(paramExternalIdentifier)).with(paramUserIdentity -> paramUserIdentity.value = IdentityExternalIdHelper.getLoginId(paramExternalIdentifier)).with(paramUserIdentity -> paramUserIdentity.verified = true).with(paramUserIdentity -> paramUserIdentity.verifiedInstant = paramExternalIdentifier.getAttributeAsZonedDateTime("loginIdVerifiedInstant")).with(paramUserIdentity -> paramUserIdentity.verifiedReason = IdentityVerifiedReason.Completed);
      user.identities.removeIf(paramUserIdentity2 -> (paramUserIdentity2.type.is(paramUserIdentity1.type) && IdentityHelper.canonicalizeValue(paramUserIdentity2).equals(paramUserIdentity1.value)));
      user.identities.add(userIdentity);
    } 
    validationResult1.preVerifiedExternalIds = arrayList;
    DefaultUserReaderService.normalizeFromIdentities(user);
    Objects.requireNonNull(this.tenantReader);
    validationResult1.tenant = this.tenantCache.resolve(paramTenant, this.tenantReader::retrieveById, new Tenantable[] { externalIdentifier });
    UserService.ValidationResult validationResult2 = validateCreate(validationResult1.tenant, user, paramUUID, paramBoolean1, paramSendSetPasswordIdentityType, paramBoolean2, paramEventInfo, true);
    validationResult1.sendSetPasswordIdentityType = validationResult2.sendSetPasswordIdentityType;
    validationResult1.errors.generalErrors.addAll(validationResult2.errors.generalErrors);
    ArrayList arrayList1 = new ArrayList();
    Objects.requireNonNull(arrayList1);
    validationResult2.errors.fieldErrors.values().forEach(arrayList1::addAll);
    validationResult1.errors.fieldErrors.put("verificationIds", arrayList1);
    validationResult1.user = user;
    return validationResult1;
  }
  
  public UserService.ValidationResult validateRecoveryCodeRequest(Tenant paramTenant, UUID paramUUID) {
    UserService.ValidationResult validationResult = validateId(paramTenant, null, paramUUID);
    if (validationResult.user != null && validationResult.user.twoFactor.methods.isEmpty())
      validationResult.errors.addGeneralError("[TwoFactorDisabled]", null, new Object[0]); 
    return validationResult;
  }
  
  public UserService.ValidationResult validateRegistrationCreate(Tenant paramTenant, UserRegistration paramUserRegistration, UUID paramUUID, boolean paramBoolean1, boolean paramBoolean2) {
    UserService.ValidationResult validationResult = resolveUserAppTenantAndReg(paramUUID, paramUserRegistration.applicationId, TenantService.optionalTenantId(paramTenant));
    Map map = (validationResult.application != null) ? (Map)this.applicationReader.retrieveRolesByNames(TenantService.optionalTenantId(validationResult.tenant), paramUserRegistration.applicationId, paramUserRegistration.roles).stream().collect(Collectors.toMap(paramApplicationRole -> paramApplicationRole.name, paramApplicationRole -> paramApplicationRole)) : Map.of();
    Validator validator = (new Validator()).check(paramValidator -> noDuplicateRegistrations(paramValidator, paramUUID, paramUserRegistration)).notMissing(paramUserRegistration.applicationId, "registration.applicationId", new Object[0]).ifFalse(paramBoolean1, paramValidator -> paramValidator.notMissing(paramUUID, "userId", new Object[0])).ifTrue((paramUserRegistration.applicationId != null), paramValidator -> paramValidator.validObject(paramValidationResult.application, "registration.applicationId", new Object[] { paramUserRegistration.applicationId })).ifTrue((validationResult.user != null && validationResult.application != null && !validationResult.application.universalConfiguration.universal), paramValidator -> paramValidator.ensureWithCode(paramValidationResult.user.tenantId.equals(paramValidationResult.application.tenantId), "registration.applicationId", "[invalid]registration.applicationId", new Object[] { paramUserRegistration.applicationId })).ifNoFieldErrors("userId", paramValidator -> paramValidator.ifFalse(paramBoolean, ())).ifTrue((validationResult.application != null), paramValidator -> paramValidator.forEach(paramUserRegistration.roles, ())).ifTrue((paramBoolean2 && validationResult.application != null), paramValidator -> paramValidator.valid(paramValidationResult.application.authenticationTokenConfiguration.enabled, "generateAuthenticationToken", new Object[0])).ifTrue((paramUserRegistration.authenticationToken != null), paramValidator -> paramValidator.valid(paramValidationResult.application.authenticationTokenConfiguration.enabled, "registration.authenticationToken", new Object[0])).validate(paramValidator -> commonRegistrationValidation(paramValidator, paramUserRegistration));
    boolean bool = (paramUserRegistration.username != null && validationResult.application != null) ? true : false;
    if (bool)
      moderateUsername(paramUserRegistration, validationResult.application, paramUUID, validator); 
    validationResult.errors = validator.done();
    return validationResult;
  }
  
  public UserService.ValidationResult validateRegistrationDelete(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2) {
    UserService.ValidationResult validationResult = resolveUserAppTenantAndReg(paramUUID1, paramUUID2, TenantService.optionalTenantId(paramTenant), true);
    validationResult

      
      .errors = (new Validator()).notMissing(paramUUID1, "userId", new Object[0]).notMissing(paramUUID2, "applicationId", new Object[0]).done();
    return validationResult;
  }
  
  public UserService.ValidationResult validateRegistrationRetrieve(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2) {
    UUID uUID = (paramTenant != null) ? paramTenant.id : null;
    UserService.ValidationResult validationResult = new UserService.ValidationResult();
    validationResult.registration = (paramUUID1 != null && paramUUID2 != null) ? this.userReader.retrieveRegistration(uUID, paramUUID1, paramUUID2) : null;
    validationResult
      
      .errors = (new Validator()).notMissing(paramUUID1, "userId", new Object[0]).notMissing(paramUUID2, "applicationId", new Object[0]).done();
    retrieveOptionalRegistrationVerificationId(paramUUID1, validationResult);
    return validationResult;
  }
  
  public UserService.ValidationResult validateRegistrationUpdate(Tenant paramTenant, UserRegistration paramUserRegistration, UUID paramUUID, boolean paramBoolean) {
    UserService.ValidationResult validationResult = resolveUserAppTenantAndReg(paramUUID, paramUserRegistration.applicationId, TenantService.optionalTenantId(paramTenant));
    Map map = (validationResult.application != null) ? (Map)validationResult.application.roles.stream().collect(Collectors.toMap(paramApplicationRole -> paramApplicationRole.name, paramApplicationRole -> paramApplicationRole)) : Collections.emptyMap();
    Validator validator = (new Validator()).notMissing(paramUserRegistration.applicationId, "registration.applicationId", new Object[0]).ifTrue((paramUserRegistration.applicationId != null), paramValidator -> paramValidator.validObject(paramValidationResult.application, "registration.applicationId", new Object[] { paramUserRegistration.applicationId })).notMissing(paramUUID, "userId", new Object[0]).ifTrue((validationResult.application != null), paramValidator -> paramValidator.forEach(paramUserRegistration.roles, ())).ifTrue((validationResult.application != null), paramValidator -> paramValidator.ifTrue(paramBoolean, ()).ifTrue((paramUserRegistration.authenticationToken != null), ())).validate(paramValidator -> commonRegistrationValidation(paramValidator, paramUserRegistration));
    boolean bool = (paramUserRegistration.username != null && validationResult.application != null && validationResult.registration != null && (validationResult.registration.username == null || !validationResult.registration.username.equals(paramUserRegistration.username))) ? true : false;
    if (bool)
      moderateUsername(paramUserRegistration, validationResult.application, paramUUID, validator); 
    validationResult.errors = validator.done();
    return validationResult;
  }
  
  public UserService.ValidationResult validateRetrieveById(Tenant paramTenant, UUID paramUUID) {
    UserService.ValidationResult validationResult = new UserService.ValidationResult();
    validationResult.user = this.userReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID);
    Objects.requireNonNull(this.tenantReader);
    validationResult.tenant = this.tenantCache.resolve(paramTenant, this.tenantReader::retrieveById, new Tenantable[] { validationResult.user });
    retrieveOptionalVerificationId(validationResult);
    return validationResult;
  }
  
  public UserService.ValidationResult validateRetrieveByLoginId(Tenant paramTenant, String paramString, List<IdentityType> paramList) {
    UserService.ValidationResult validationResult = new UserService.ValidationResult();
    validationResult.user = this.userReader.retrieveByLoginId(paramTenant.id, paramString, paramList);
    validationResult.tenant = paramTenant;
    retrieveOptionalVerificationId(validationResult);
    return validationResult;
  }
  
  public Errors validateSearchQuery(UUID paramUUID, String paramString1, String paramString2, List<SortField> paramList, int paramInt1, int paramInt2) {
    return this.searchEngine.validate(paramUUID, paramString1, paramString2, paramList, paramInt1, paramInt2);
  }
  
  public UserService.IdentityValidationResult validateSendVerify(Tenant paramTenant, String paramString) {
    UserService.IdentityValidationResult identityValidationResult = commonVerifyValidation(paramTenant, paramString);
    Optional.<Tenant>ofNullable(identityValidationResult.tenant).ifPresent(paramTenant -> {
          Optional<Application> optional = Optional.ofNullable(paramIdentityValidationResult.application);
          IdentityType identityType = IdentityExternalIdHelper.getLoginIdentityType(paramIdentityValidationResult.externalId);
          if (identityType.is(IdentityType.email)) {
            UUID uUID = optional.<UUID>map(()).orElse(paramTenant.emailConfiguration.verificationEmailTemplateId);
            if (uUID == null)
              paramIdentityValidationResult.errors.addGeneralError("[EmailVerificationDisabled]", null, new Object[] { paramTenant.id }); 
          } 
          if (identityType.is(IdentityType.phoneNumber)) {
            UUID uUID = optional.<UUID>map(()).orElse(paramTenant.phoneConfiguration.verificationTemplateId);
            if (paramTenant.phoneConfiguration.messengerId == null || uUID == null)
              paramIdentityValidationResult.errors.addGeneralError("[PhoneVerificationDisabled]", null, new Object[] { paramTenant.id }); 
          } 
        });
    return identityValidationResult;
  }
  
  public UserService.IdentityValidationResult validateStartVerify(Tenant paramTenant, UUID paramUUID, String paramString1, String paramString2, VerificationStrategy paramVerificationStrategy, ExistingUserStrategy paramExistingUserStrategy) {
    UserService.IdentityValidationResult identityValidationResult = new UserService.IdentityValidationResult();
    Objects.requireNonNull(this.applicationReader);
    identityValidationResult.application = (paramUUID != null) ? this.applicationCache.get((paramTenant != null) ? paramTenant.id : null, paramUUID, this.applicationReader::retrieveById) : null;
    identityValidationResult.identityType = IdentityType.of(paramString2);
    identityValidationResult.verificationStrategy = paramVerificationStrategy;
    Objects.requireNonNull(this.tenantReader);
    identityValidationResult.tenant = this.tenantCache.resolve(paramTenant, this.tenantReader::retrieveById, new Tenantable[] { identityValidationResult.application });
    Validator validator = (new Validator()).notBlank(paramString1, "loginId", new Object[0]).notBlank(paramString2, "loginIdType", new Object[0]).ifTrue((identityValidationResult.tenant != null), paramValidator -> paramValidator.ifNoFieldErrors("loginIdType", ()).ifNoFieldErrors("loginIdType", ()));
    if (validator.hasErrors()) {
      identityValidationResult.errors.add(validator.done());
      return identityValidationResult;
    } 
    if (identityValidationResult.tenant == null)
      return identityValidationResult; 
    identityValidationResult.user = this.userReader.retrieveByLoginId(identityValidationResult.tenant.id, paramString1, List.of(identityValidationResult.identityType));
    validator.ifTrue((paramExistingUserStrategy == ExistingUserStrategy.mustNotExist), paramValidator -> paramValidator.ensure((paramIdentityValidationResult.user == null), "loginId", "[duplicate]", new Object[] { paramIdentityValidationResult.identityType, paramString }));
    if (identityValidationResult.verificationStrategy == null)
      if (identityValidationResult.identityType.is(IdentityType.email)) {
        identityValidationResult.verificationStrategy = identityValidationResult.tenant.emailConfiguration.verificationStrategy;
      } else if (identityValidationResult.identityType.is(IdentityType.phoneNumber)) {
        identityValidationResult.verificationStrategy = identityValidationResult.tenant.phoneConfiguration.verificationStrategy;
      }  
    identityValidationResult.errors.add(validator.done());
    return identityValidationResult;
  }
  
  public UserService.TwoFactorEnableValidationResult validateTwoFactorUpdate(Tenant paramTenant, UUID paramUUID, String paramString1, String paramString2) {
    UserService.TwoFactorEnableValidationResult twoFactorEnableValidationResult = new UserService.TwoFactorEnableValidationResult();
    twoFactorEnableValidationResult.user = (paramUUID != null) ? this.userReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID) : null;
    Objects.requireNonNull(this.tenantReader);
    twoFactorEnableValidationResult.tenant = this.tenantCache.resolve(paramTenant, this.tenantReader::retrieveById, new Tenantable[] { twoFactorEnableValidationResult.user });
    twoFactorEnableValidationResult

      
      .errors = (new Validator()).notMissing(paramUUID, "userId", new Object[0]).notBlank(paramString1, "methodId", new Object[0]).done();
    if (twoFactorEnableValidationResult.tenant == null || twoFactorEnableValidationResult.user == null || !twoFactorEnableValidationResult.errors.empty())
      return twoFactorEnableValidationResult; 
    TwoFactorMethod twoFactorMethod = twoFactorEnableValidationResult.user.twoFactor.getMethodById(paramString1);
    twoFactorEnableValidationResult.twoFactorMethodId = (twoFactorMethod != null) ? twoFactorMethod.id : null;
    twoFactorEnableValidationResult

      
      .errors = (new Validator()).validObject(twoFactorMethod, "methodId", new Object[] { paramString1 }).ifTrue((paramString2 != null), paramValidator -> paramValidator.maxLength(paramString, 256, "name", new Object[] { Integer.valueOf(256) })).done();
    return twoFactorEnableValidationResult;
  }
  
  public UserService.ValidationResult validateUpdate(Tenant paramTenant, User paramUser, boolean paramBoolean1, UUID paramUUID, String paramString, boolean paramBoolean2, EventInfo paramEventInfo, PasswordType paramPasswordType) {
    return validateUserUpdate(paramTenant, paramUser, paramBoolean1, paramUUID, paramBoolean2, true, paramString, paramEventInfo, paramPasswordType);
  }
  
  public UserService.ValidationResult validateUserId(Tenant paramTenant, UUID paramUUID) {
    UserService.ValidationResult validationResult = new UserService.ValidationResult();
    validationResult.existing = (paramUUID != null) ? this.userReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID) : null;
    validationResult.errors = (new Validator()).notMissing(paramUUID, "userId", new Object[0]).done();
    Objects.requireNonNull(this.tenantReader);
    validationResult.tenant = this.tenantCache.resolve(paramTenant, this.tenantReader::retrieveById, new Tenantable[] { validationResult.existing });
    return validationResult;
  }
  
  public UserService.RegistrationValidationResult validateWithRegistrationConfiguration(Tenant paramTenant, Application paramApplication, Form paramForm, User paramUser, String paramString, boolean paramBoolean1, boolean paramBoolean2, EventInfo paramEventInfo) {
    UserService.RegistrationValidationResult registrationValidationResult = new UserService.RegistrationValidationResult();
    Validator validator = new Validator();
    if (paramBoolean1) {
      UserService.ValidationResult validationResult = validateCreate(paramTenant, paramUser, paramApplication.id, false, SendSetPasswordIdentityType.doNotSend, false, paramEventInfo, false);
      registrationValidationResult.fieldMapping.putAll(validationResult.fieldMapping);
      validator.withErrors(validationResult.errors)
        .ifTrue((paramApplication.registrationConfiguration.type == Application.RegistrationConfiguration.RegistrationType.basic), paramValidator -> paramValidator.notBlank(paramUser.password, "user.password", new Object[0]).ifTrue(paramApplication.registrationConfiguration.confirmPassword, ()))



        
        .ifTrue((paramApplication.registrationConfiguration.loginIdType == Application.RegistrationConfiguration.LoginIdType.email && !paramTenant.registrationConfiguration.blockedDomains.isEmpty()), paramValidator -> paramValidator.ifNoFieldErrors("user.email", ()));
    } 
    if (paramApplication.registrationConfiguration.type == Application.RegistrationConfiguration.RegistrationType.basic) {
      if (paramApplication.registrationConfiguration.loginIdType != Application.RegistrationConfiguration.LoginIdType.email)
        (validator.getErrors()).fieldErrors.remove("user.email"); 
      if (paramApplication.registrationConfiguration.loginIdType != Application.RegistrationConfiguration.LoginIdType.username)
        (validator.getErrors()).fieldErrors.remove("user.username"); 
      if (paramApplication.registrationConfiguration.loginIdType != Application.RegistrationConfiguration.LoginIdType.phoneNumber)
        (validator.getErrors()).fieldErrors.remove("user.phoneNumber"); 
    } 
    Function function = paramRequirable -> Boolean.valueOf((paramRequirable.enabled && paramRequirable.required));
    Errors errors = validator.ifTrue((paramApplication.registrationConfiguration.type == Application.RegistrationConfiguration.RegistrationType.basic), paramValidator -> paramValidator.ifTrue(((Boolean)paramFunction.apply(paramApplication.registrationConfiguration.birthDate)).booleanValue(), ()).ifTrue(((Boolean)paramFunction.apply(paramApplication.registrationConfiguration.firstName)).booleanValue(), ()).ifTrue(((Boolean)paramFunction.apply(paramApplication.registrationConfiguration.fullName)).booleanValue(), ()).ifTrue(((Boolean)paramFunction.apply(paramApplication.registrationConfiguration.lastName)).booleanValue(), ()).ifTrue(((Boolean)paramFunction.apply(paramApplication.registrationConfiguration.middleName)).booleanValue(), ()).ifTrue(((Boolean)paramFunction.apply(paramApplication.registrationConfiguration.mobilePhone)).booleanValue(), ()).ifTrue(((Boolean)paramFunction.apply(paramApplication.registrationConfiguration.preferredLanguages)).booleanValue(), ())).ifTrue(paramBoolean2, paramValidator -> paramValidator.notBlank(paramUser.parentEmail, "user.parentEmail", new Object[0])).ifFalse(paramBoolean1, () -> paramValidator.ifLastCheckHadNoError(())).done();
    if (errors.size() > 0 && paramApplication.registrationConfiguration.type == Application.RegistrationConfiguration.RegistrationType.advanced) {
      Map map = (Map)registrationValidationResult.fieldMapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
      for (FormStep formStep : paramForm.steps) {
        Errors errors1 = new Errors();
        for (FormField formField : formStep.fieldObjects) {
          String str = (String)map.get(formField.key);
          if (str != null && errors.fieldErrors.containsKey(str))
            errors1.fieldErrors.put(str, (List)errors.fieldErrors.get(str)); 
          if (errors.fieldErrors.containsKey(formField.key))
            errors1.fieldErrors.put(formField.key, (List)errors.fieldErrors.get(formField.key)); 
        } 
        registrationValidationResult.errors.add(errors1);
      } 
    } else {
      registrationValidationResult.errors.add(errors);
    } 
    return registrationValidationResult;
  }
  
  @Transactional
  public void verifyIdentity(Tenant paramTenant, Application paramApplication, User paramUser, ExternalIdentifier paramExternalIdentifier, UserIdentity paramUserIdentity, EventInfo paramEventInfo) {
    String str;
    IdentityType identityType;
    IdentityVerifiedReason identityVerifiedReason;
    User user = null;
    if (paramUserIdentity != null) {
      str = paramUserIdentity.value;
      identityType = paramUserIdentity.type;
      identityVerifiedReason = IdentityVerifiedReason.Administrative;
    } else if (paramExternalIdentifier != null) {
      str = IdentityExternalIdHelper.getLoginId(paramExternalIdentifier);
      identityType = IdentityExternalIdHelper.getLoginIdentityType(paramExternalIdentifier);
      identityVerifiedReason = IdentityVerifiedReason.Completed;
    } else {
      str = paramUser.email;
      identityType = IdentityType.email;
      identityVerifiedReason = IdentityVerifiedReason.Administrative;
    } 
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    if (paramUser != null) {
      this.userMapper.setIdentityVerified(paramUser.id, str, identityType, zonedDateTime, identityVerifiedReason);
      UserIdentity userIdentity = IdentityHelper.resolveIdentity(paramUser, str, identityType);
      userIdentity.verified = true;
      userIdentity.verifiedReason = identityVerifiedReason;
      userIdentity.verifiedInstant = zonedDateTime;
      DefaultUserReaderService.normalizeFromIdentities(paramUser);
      this.externalIdentifierService.deleteIdentityVerificationsByUserAndIdentityType(paramUser.id, userIdentity.value, userIdentity.type);
      if (paramUser.verifiedInstant == null) {
        paramUser.verifiedInstant = zonedDateTime;
        this.userMapper.updateUserVerifiedInstant(paramUser.id, zonedDateTime);
      } 
      user = (new User(paramUser)).secure().sort();
    } else {
      paramExternalIdentifier.data.setAttribute("loginIdVerified", true);
      paramExternalIdentifier.data.setAttribute("loginIdVerifiedInstant", zonedDateTime);
      this.externalIdentifierService.update(paramExternalIdentifier);
    } 
    ArrayList<UserIdentityVerifiedEvent> arrayList = new ArrayList();
    arrayList.add(new UserIdentityVerifiedEvent(paramEventInfo, str, identityType.name, user));
    boolean bool = (paramExternalIdentifier == null || paramExternalIdentifier.type == ExternalIdentifier.ExternalIdType.EmailVerification) ? true : false;
    if (paramUser != null && bool)
      arrayList.add(new UserEmailVerifiedEvent(paramEventInfo, user)); 
    arrayList.forEach(paramBaseEvent -> EventHelper.send(paramTenant, paramApplication, paramBaseEvent));
    if (paramUser != null)
      this.searchEngine.index(Collections.singletonList(paramUser)); 
  }
  
  public void verifyIdentityAfterPasswordChange(Tenant paramTenant, Application paramApplication, ExternalIdentifier paramExternalIdentifier, EventInfo paramEventInfo, User paramUser) {
    String str = IdentityExternalIdHelper.getLoginId(paramExternalIdentifier);
    IdentityType identityType = Optional.<IdentityType>ofNullable(IdentityExternalIdHelper.getLoginIdentityType(paramExternalIdentifier)).orElse(IdentityType.email);
    UserIdentity userIdentity = (str == null) ? paramUser.resolvePrimaryIdentity(identityType) : IdentityHelper.resolveIdentity(paramUser, str, identityType);
    if (userIdentity != null && paramExternalIdentifier.wasSentToUser() && 
      handleImplicitVerification(paramTenant, paramApplication, paramUser, userIdentity, paramEventInfo))
      this.searchEngine.index(Collections.singletonList(paramUser)); 
  }
  
  @Transactional
  public void verifyRegistration(Tenant paramTenant, Application paramApplication, User paramUser, EventInfo paramEventInfo, ExternalIdentifier paramExternalIdentifier) {
    UUID uUID = paramExternalIdentifier.applicationId;
    UserRegistration userRegistration1 = paramUser.getRegistrationForApplication(uUID);
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    userRegistration1.verified = true;
    userRegistration1
      
      .verifiedInstant = (userRegistration1.verifiedInstant != null) ? userRegistration1.verifiedInstant : zonedDateTime;
    this.userMapper.verifyRegistration(userRegistration1.id, userRegistration1.verifiedInstant);
    this.externalIdentifierService.deleteByUserIdApplicationIdAndType(paramUser.id, uUID, ExternalIdentifier.ExternalIdType.RegistrationVerification);
    UserIdentity userIdentity = paramUser.resolvePrimaryIdentity(IdentityType.email);
    if (userIdentity != null && paramExternalIdentifier.wasSentToUser())
      handleImplicitVerification(paramTenant, paramApplication, paramUser, userIdentity, paramEventInfo); 
    UserRegistration userRegistration2 = paramUser.getRegistrationForApplication(uUID);
    EventHelper.send(paramTenant, paramApplication, new UserRegistrationVerifiedEvent(paramEventInfo, uUID, userRegistration2, paramUser));
    this.searchEngine.index(Collections.singletonList(paramUser));
  }
  
  private boolean _deactivate(Tenant paramTenant, User paramUser, EventInfo paramEventInfo) {
    this.refreshTokenService.revokeRefreshTokensByUser(paramTenant, paramUser, paramEventInfo);
    this.externalIdentifierService.deleteAllByUserId(paramUser.id);
    paramUser.active = false;
    boolean bool = (this.userMapper.setActive(paramUser.id, false) == 1) ? true : false;
    EventHelper.send(paramTenant, null, new UserDeactivateEvent(paramEventInfo, paramUser));
    if (bool)
      this.searchEngine.index(Collections.singletonList(paramUser)); 
    return bool;
  }
  
  private boolean _updatePassword(Tenant paramTenant, User paramUser, String paramString1, String paramString2, String paramString3, EventInfo paramEventInfo, ExternalIdentifier paramExternalIdentifier) {
    if (paramString2 != null && 
      !this.passwordService.passwordsEqual(paramString2, paramUser.password, paramUser.salt, paramUser.encryptionScheme, paramUser.factor))
      return false; 
    String str = null;
    if (paramString3 != null) {
      ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveById(paramString3);
      str = externalIdentifier.getAttribute("twoFactorTrustId");
    } else if (paramExternalIdentifier != null) {
      str = paramExternalIdentifier.getAttribute("twoFactorTrustId");
    } 
    deleteTokensAndIdsOnPasswordChange(paramTenant, paramUser, paramTenant.jwtConfiguration.refreshTokenRevocationPolicy.onPasswordChanged, paramEventInfo, str);
    if (paramString3 != null)
      this.externalIdentifierService.deleteById(paramString3); 
    paramUser.passwordChangeRequired = false;
    paramUser.passwordChangeReason = null;
    this.passwordService.rehashPasswordOnChange(paramTenant, paramUser, paramString1);
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    boolean bool = (paramUser.identities.size() == 0) ? true : false;
    if (bool && 
      paramUser.lastUpdateInstant == null)
      paramUser.lastUpdateInstant = zonedDateTime; 
    this.userMapper.updatePasswordFields(paramUser);
    if (paramTenant.passwordValidationRules.breachDetection.enabled) {
      this.userMapper.updateBreachStatus(paramUser.id, paramUser.breachedPasswordStatus, paramUser.breachedPasswordLastCheckedInstant);
      this.searchEngine.index(Collections.singletonList(paramUser));
    } else if (paramUser.breachedPasswordStatus != null || paramUser.breachedPasswordLastCheckedInstant != null) {
      paramUser.breachedPasswordStatus = null;
      paramUser.breachedPasswordLastCheckedInstant = null;
      this.userMapper.deleteBreachStatus(paramUser.id);
      this.searchEngine.index(Collections.singletonList(paramUser));
    } 
    if (paramTenant.passwordValidationRules.rememberPreviousPasswords.enabled)
      this.userMapper.createPreviousPassword(new PreviousPassword(paramUser.passwordLastUpdateInstant, paramUser.encryptionScheme, paramUser.factor, paramUser.password, paramUser.salt, paramUser.id)); 
    return true;
  }
  
  private void buildNonTransactionalEventsForUserUpdate(UserService.UserResult paramUserResult, User paramUser1, User paramUser2, boolean paramBoolean, EventInfo paramEventInfo) {
    for (TwoFactorMethod twoFactorMethod : paramUser1.twoFactor.methods) {
      if (paramUser2.twoFactor.getMethodById(twoFactorMethod.id) == null)
        paramUserResult.events.add(new UserTwoFactorMethodAddEvent(paramEventInfo, (new TwoFactorMethod(twoFactorMethod)).secure(), paramUser1)); 
    } 
    for (TwoFactorMethod twoFactorMethod : paramUser2.twoFactor.methods) {
      if (paramUser1.twoFactor.getMethodById(twoFactorMethod.id) == null)
        paramUserResult.events.add(new UserTwoFactorMethodRemoveEvent(paramEventInfo, (new TwoFactorMethod(twoFactorMethod)).secure(), paramUser1)); 
    } 
    if (paramBoolean)
      paramUserResult.events.add(new UserPasswordUpdateEvent(paramEventInfo, paramUser1)); 
    String str1 = paramUser2.email;
    if (!Objects.equals(paramUser1.email, str1))
      paramUserResult.events.add(new UserEmailUpdateEvent(paramEventInfo, str1, paramUser1)); 
    String str2 = paramUser2.phoneNumber;
    if (!Objects.equals(paramUser1.phoneNumber, str2))
      paramUserResult.events.add(new UserIdentityUpdateEvent(paramEventInfo, str2, paramUser1.phoneNumber, IdentityType.phoneNumber.name, paramUser1)); 
  }
  
  private List<UUID> bulkDeleteAllUsers(UUID paramUUID, List<User> paramList, boolean paramBoolean1, boolean paramBoolean2, EventInfo paramEventInfo) {
    if (!paramBoolean1) {
      Objects.requireNonNull(this.tenantReader);
      Map<UUID, Tenant> map = (Map<UUID, Tenant>)((paramUUID == null) ? usersToTenantMap(paramList) : Collections.<UUID, Tenant>singletonMap(paramUUID, this.tenantCache.get(paramUUID, this.tenantReader::retrieveById)));
      if (paramBoolean2) {
        deleteUsers(map, paramList, paramEventInfo);
      } else {
        deactivateUsers(map, paramList, paramEventInfo);
      } 
    } 
    return paramList.stream().map(paramUser -> paramUser.id).toList();
  }
  
  private void commonRegistrationValidation(Validator paramValidator, UserRegistration paramUserRegistration) {
    paramValidator

      
      .forEach(paramUserRegistration.preferredLanguages, (paramValidator, paramLocale, paramInteger) -> paramValidator.ensureWithCode(LocaleTools.validate(paramLocale), String.format("registration.preferredLanguages[%d]", new Object[] { paramInteger }), "[invalid]registration.preferredLanguages", new Object[0])).ensure((paramUserRegistration.preferredLanguages.size() <= 20), "registration.preferredLanguages", "[tooMany]", new Object[] { Integer.valueOf(20) }).maxLength(paramUserRegistration.username, MapperTools.MaximumIndexedColumnLength, "registration.username", new Object[] { Integer.valueOf(MapperTools.MaximumIndexedColumnLength) });
  }
  
  private void commonTwoFactorValidation(Validator paramValidator, Tenant paramTenant, String paramString) {
    paramValidator.notBlank(paramString, "method", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(MFAService.SupportedMethods.contains(paramString), "method", "[invalid]", new Object[] { paramString, String.join(", ", (Iterable)MFAService.SupportedMethods) }).ifLastCheckHadNoError(()));
  }
  
  private void commonValidation(Validator paramValidator, Tenant paramTenant, Application paramApplication, User paramUser1, boolean paramBoolean1, boolean paramBoolean2, EventInfo paramEventInfo, User paramUser2) {
    boolean bool1 = (!StringTools.isTrimmedEmpty(paramUser2.email) || !StringTools.isTrimmedEmpty(paramUser2.username) || !StringTools.isTrimmedEmpty(paramUser2.phoneNumber)) ? true : false;
    boolean bool2 = (paramTenant.familyConfiguration.enabled && paramTenant.familyConfiguration.allowChildRegistrations && paramUser2.birthDate != null) ? true : false;
    boolean bool3 = (bool2 && paramTenant.familyConfiguration.parentEmailRequired) ? true : false;
    UserExists userExists1 = userExistsByIdentity(paramTenant, paramUser2, IdentityType.username);
    UserExists userExists2 = userExistsByIdentity(paramTenant, paramUser2, IdentityType.email);
    UserExists userExists3 = userExistsByIdentity(paramTenant, paramUser2, IdentityType.phoneNumber);
    paramValidator.notInactive(userExists2, paramUserExists -> Boolean.valueOf(paramUserExists.user.active), "user.email", new Object[] { paramUser2.email }).ifLastCheckHadNoError(paramValidator -> paramValidator.ifNoFieldErrors("user.email", ()).ifNoFieldErrors("user.email", ()))
























      
      .ifFalse(paramTenant.usernameConfiguration.unique.enabled, paramValidator -> paramValidator.notInactive(paramUserExists, (), "user.username", new Object[] { paramUser1.username }).ifLastCheckHadNoError(())).notInactive(userExists3, paramUserExists -> Boolean.valueOf(paramUserExists.user.active), "user.phoneNumber", new Object[] { paramUser2.phoneNumber }).ifLastCheckHadNoError(paramValidator -> paramValidator.notDuplicate(paramUserExists, "user.phoneNumber", new Object[] { paramUser1.phoneNumber }).ifLastCheckHadError(())).ensure((!paramUser2.identities.isEmpty() || bool1), "user.email", "[blank]", new Object[0])
      .ifTrue(!StringTools.isTrimmedEmpty(paramUser2.email), paramValidator -> paramValidator.email(paramUser.email, "user.email", new Object[] { paramUser.email })).ensure((!paramUser2.identities.isEmpty() || bool1), "user.phoneNumber", "[blank]", new Object[0])
      .ensure((!paramUser2.identities.isEmpty() || bool1), "user.username", "[blank]", new Object[0])

      
      .forEach(paramUser2.identities, (paramValidator, paramUserIdentity, paramInteger) -> paramValidator.notBlankWithCode(paramUserIdentity.value, "user.identities[%d].value".formatted(new Object[] { paramInteger }, ), "[blank]identities.value", new Object[0])).forEach(userIdentitiesByType(paramUser2, IdentityType.email), (paramValidator, paramUserIdentityWithField, paramInteger) -> paramValidator.maxLength(paramUserIdentityWithField.identityValue(), MapperTools.MaximumIndexedColumnLength, paramUserIdentityWithField.field, new Object[] { Integer.valueOf(MapperTools.MaximumIndexedColumnLength) })).forEach(userIdentitiesByType(paramUser2, IdentityType.username), (paramValidator, paramUserIdentityWithField, paramInteger) -> paramValidator.maxLength(paramUserIdentityWithField.identityValue(), MapperTools.MaximumIndexedColumnLength, paramUserIdentityWithField.field, new Object[] { Integer.valueOf(MapperTools.MaximumIndexedColumnLength) })).forEach(userIdentitiesByType(paramUser2, IdentityType.phoneNumber), (paramValidator, paramUserIdentityWithField, paramInteger) -> paramValidator.ensure(PhoneNumberValidator.validateE164format(paramUserIdentityWithField.identityValue()), paramUserIdentityWithField.field, "[invalidPhone]", new Object[] { paramUserIdentityWithField.identityValue() })).withErrors(validateNoDuplicateIdentities(paramUser2))

      
      .ifTrue(bool3, paramValidator -> paramValidator.ifTrue(LocalDate.now(ZoneOffset.UTC).minusYears(paramTenant.familyConfiguration.maximumChildAge).isBefore(paramUser.birthDate), ()))





      
      .ifTrue((bool2 && paramUser2.parentEmail != null), paramValidator -> paramValidator.email(paramUser.parentEmail, "user.parentEmail", new Object[] { paramUser.parentEmail })).forEach(paramUser2.preferredLanguages, (paramValidator, paramLocale, paramInteger) -> paramValidator.ensureWithCode(LocaleTools.validate(paramLocale), String.format("user.preferredLanguages[%d]", new Object[] { paramInteger }), "[invalid]user.preferredLanguages", new Object[0])).ensure((paramUser2.preferredLanguages.size() <= 20), "user.preferredLanguages", "[tooMany]", new Object[] { Integer.valueOf(20) }).ifTrue((paramUser2.mobilePhone != null), paramValidator -> paramValidator.ensure(PhoneNumberValidator.validateE164format(paramUser.mobilePhone), "user.mobilePhone", "[invalid]", new Object[0]))


      
      .forEach(paramUser2.twoFactor.methods, (paramValidator, paramTwoFactorMethod, paramInteger) -> paramValidator.ifTrue((paramTwoFactorMethod.method.equals("authenticator") && (paramUser == null || paramTwoFactorMethod.id == null || paramUser.twoFactor.getMethodById(paramTwoFactorMethod.id) == null)), ()).ifTrue(

          
          (paramTwoFactorMethod.method.equals("email") && (paramUser == null || paramTwoFactorMethod.id == null || paramUser.twoFactor.getMethodById(paramTwoFactorMethod.id) == null)), ()).ifTrue(





          
          (paramTwoFactorMethod.method.equals("sms") && (paramUser == null || paramTwoFactorMethod.id == null || paramUser.twoFactor.getMethodById(paramTwoFactorMethod.id) == null)), ()))















      
      .ifTrue((paramUser1 == null) ? ((paramUser2.password == null)) : ((paramUser1.password == null && paramUser2.password == null)), paramValidator -> paramValidator.ensure(!paramUser.passwordChangeRequired, "user.passwordChangeRequired", "[invalid]", new Object[0]))



      
      .ifTrue((paramUser2.password != null && paramUser2.encryptionScheme != null), paramValidator -> paramValidator.ensure(this.passwordEncryptorLibrary.validateScheme(paramUser.encryptionScheme), "user.encryptionScheme", "[invalid]", new Object[] { paramUser.encryptionScheme })).ifLastCheckHadNoError(paramValidator -> paramValidator.ifTrue((paramUser.password != null && paramUser.factor != null), ()))



      
      .ifTrue((paramUser2.legacyIdentifier != null), paramValidator -> paramValidator.maxLength(paramUser.legacyIdentifier, MapperTools.MaximumIndexedColumnLength, "user.legacyIdentifier", new Object[] { Integer.valueOf(MapperTools.MaximumIndexedColumnLength) }).ifLastCheckHadNoError(()));
  }
  
  private UserService.IdentityValidationResult commonVerifyValidation(Tenant paramTenant, String paramString) {
    UserService.IdentityValidationResult identityValidationResult = new UserService.IdentityValidationResult();
    ExternalIdentifierReaderService.ValidationResult validationResult = this.externalIdentifierReader.validate(paramTenant, paramString, IdentityExternalIdHelper.identityExternalIdTypes);
    identityValidationResult.externalId = validationResult.id;
    identityValidationResult.errors.add((new Validator())
        .notBlank(paramString, "verificationId", new Object[0])
        .done());
    if (identityValidationResult.errors.empty() && identityValidationResult.externalId != null) {
      ExternalIdentifier externalIdentifier = identityValidationResult.externalId;
      Objects.requireNonNull(this.applicationReader);
      identityValidationResult.application = this.applicationCache.get((paramTenant != null) ? paramTenant.id : null, externalIdentifier.applicationId, this.applicationReader::retrieveById);
      identityValidationResult.user = this.userReader.retrieveById(identityValidationResult.externalId.tenantId, externalIdentifier.userId);
      Objects.requireNonNull(this.tenantReader);
      identityValidationResult.tenant = this.tenantCache.resolve(paramTenant, this.tenantReader::retrieveById, new Tenantable[] { externalIdentifier });
    } 
    return identityValidationResult;
  }
  
  private void createMemberships(User paramUser, ZonedDateTime paramZonedDateTime) {
    List<GroupMember> list = paramUser.getMemberships();
    if (list.isEmpty())
      return; 
    list.forEach(paramGroupMember -> {
          paramGroupMember.insertInstant = paramZonedDateTime;
          paramGroupMember.userId = paramUser.id;
          if (paramGroupMember.id == null)
            paramGroupMember.id = UUID.randomUUID(); 
        });
    Objects.requireNonNull(this.groupMapper);
    MapperTools.safeCreateUpdate(5000, list, this.groupMapper::createMembers);
  }
  
  private void createMemberships(List<User> paramList, ZonedDateTime paramZonedDateTime) {
    paramList.forEach(paramUser -> paramUser.getMemberships().forEach(()));
    List list = (List)paramList.stream().flatMap(paramUser -> paramUser.getMemberships().stream()).collect(Collectors.toList());
    for (byte b = 0; b < list.size(); b += 1000) {
      int i = Math.min(b + 1000, list.size());
      List<GroupMember> list1 = list.subList(b, i);
      list1.forEach(paramGroupMember -> {
            if (paramGroupMember.id == null)
              paramGroupMember.id = UUID.randomUUID(); 
            if (paramGroupMember.insertInstant == null)
              paramGroupMember.insertInstant = paramZonedDateTime; 
          });
      this.groupMapper.createMembers(list1);
    } 
  }
  
  private void createRegistrations(List<User> paramList, Map<UUID, Application> paramMap, Map<UUID, Map<Integer, AtomicInteger>> paramMap1, ZonedDateTime paramZonedDateTime) {
    ArrayList arrayList = new ArrayList();
    paramList.forEach(paramUser -> paramUser.getRegistrations().forEach(()));
    for (byte b = 0; b < arrayList.size(); b += 1000) {
      int i = Math.min(b + 1000, arrayList.size());
      List<Pair<UserRegistration, User>> list = arrayList.subList(b, i);
      list.forEach(paramPair -> {
            UserRegistration userRegistration = (UserRegistration)paramPair.first;
            if (userRegistration.id == null)
              userRegistration.id = UUID.randomUUID(); 
            if (userRegistration.usernameStatus == null)
              userRegistration.usernameStatus = ContentStatus.ACTIVE; 
            if (userRegistration.insertInstant == null)
              userRegistration.insertInstant = paramZonedDateTime; 
            if (userRegistration.lastUpdateInstant == null)
              userRegistration.lastUpdateInstant = paramZonedDateTime; 
            if (userRegistration.verified && userRegistration.verifiedInstant == null)
              userRegistration.verifiedInstant = paramZonedDateTime; 
            if (!userRegistration.verified)
              userRegistration.verifiedInstant = null; 
          });
      this.userMapper.createRegistrationBulk(list);
      ArrayList<?> arrayList1 = new ArrayList();
      list.forEach(paramPair -> {
            UserRegistration userRegistration = (UserRegistration)paramPair.first;
            int i = TimeUtils.toHour(userRegistration.insertInstant.truncatedTo(ChronoUnit.HOURS));
            ((AtomicInteger)((Map<Integer, AtomicInteger>)paramMap1.computeIfAbsent(userRegistration.applicationId, ())).computeIfAbsent(Integer.valueOf(i), ())).incrementAndGet();
            Application application = (Application)paramMap2.get(userRegistration.applicationId);
            userRegistration.roles.forEach(());
          });
      Objects.requireNonNull(this.userMapper);
      MapperTools.safeCreateUpdate(1000, arrayList1, this.userMapper::addRolesToUserRegistrationBulk);
    } 
  }
  
  private SearchContinuationToken decodeToken(String paramString) {
    try {
      return (SearchContinuationToken)this.objectMapper.readValue(Base64.getUrlDecoder().decode(paramString), SearchContinuationToken.class);
    } catch (IOException iOException) {
      logger.error("Error unmarshalling search continuation token " + iOException.getMessage(), iOException);
      throw new RuntimeException(iOException);
    } 
  }
  
  private List<UUID> deleteAllByElasticSearch(UUID paramUUID, boolean paramBoolean1, boolean paramBoolean2, int paramInt, EventInfo paramEventInfo, BiFunction<Integer, String, SearchResults<User>> paramBiFunction) {
    int i = ((ElasticsearchUserSearchEngine)this.searchEngine).getMaxResultWindow();
    int j = Math.min(i, paramInt);
    int k = Math.min(1000, j);
    String str = this.searchEngine.createPointInTime();
    SearchResults<User> searchResults = paramBiFunction.apply(Integer.valueOf(k), str);
    Set set = (Set)searchResults.results.stream().map(paramUser -> paramUser.id).collect(Collectors.toSet());
    if (searchResults.total > 0L) {
      handleDeleteSearchResults(searchResults, paramBoolean2, paramBoolean1, paramEventInfo);
      while (!searchResults.results.isEmpty() && set.size() < paramInt) {
        int m = Math.min(k, paramInt - set.size());
        SearchContinuationToken searchContinuationToken = decodeToken(searchResults.nextResults);
        searchResults = (searchContinuationToken.qs != null) ? this.userReader.searchByQueryString(paramUUID, searchContinuationToken.qs, m, 0, searchContinuationToken.sf, true, UserReaderService.UserExpansion.all(), searchContinuationToken.ls, searchContinuationToken.pit) : this.userReader.searchByQuery(paramUUID, searchContinuationToken.q, 0, searchContinuationToken.sf, m, true, UserReaderService.UserExpansion.all(), searchContinuationToken.ls, searchContinuationToken.pit);
        if (StringTools.isBlank(str))
          searchResults.results.removeIf(paramUser -> paramSet.contains(paramUser.id)); 
        handleDeleteSearchResults(searchResults, paramBoolean2, paramBoolean1, paramEventInfo);
        searchResults.results.forEach(paramUser -> paramSet.add(paramUser.id));
      } 
    } 
    return set.stream().toList();
  }
  
  private void deleteTokensAndIdsOnPasswordChange(Tenant paramTenant, User paramUser, boolean paramBoolean, EventInfo paramEventInfo, String paramString) {
    if (paramBoolean)
      this.refreshTokenService.revokeRefreshTokensByUser(paramTenant, paramUser, paramEventInfo); 
    this.externalIdentifierService.deleteByUserId(paramUser.id, paramString, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.AuthorizationCode, ExternalIdentifier.ExternalIdType.ChangePassword, ExternalIdentifier.ExternalIdType.LoginIntent, ExternalIdentifier.ExternalIdType.OneTimePassword, ExternalIdentifier.ExternalIdType.SetupPassword, ExternalIdentifier.ExternalIdType.TwoFactor, ExternalIdentifier.ExternalIdType.TwoFactorTrust, ExternalIdentifier.ExternalIdType.TwoFactorOneTimeCode });
  }
  
  private String generateAuthenticationToken() {
    return SecurityTools.secureRandom(32);
  }
  
  private List<UserIdentity> getIdentitiesRequiringVerification(Tenant paramTenant, User paramUser1, User paramUser2, boolean paramBoolean) {
    if (paramBoolean)
      return List.of(); 
    return paramUser1.identities.stream().filter(paramUserIdentity -> {
          boolean bool;
          if (paramUserIdentity.type.is(IdentityType.email)) {
            bool = paramTenant.emailConfiguration.verifyEmail;
          } else if (paramUserIdentity.type.is(IdentityType.phoneNumber)) {
            bool = paramTenant.phoneConfiguration.verifyPhoneNumber;
          } else {
            return false;
          } 
          if (!paramUserIdentity.primary || !paramUserIdentity.type.is(IdentityType.email))
            return (paramUserIdentity.verificationRequired() && bool); 
          UserIdentity userIdentity = paramUser.resolvePrimaryIdentity(paramUserIdentity.type);
          if (bool) {
            if (userIdentity == null)
              return true; 
            if (!userIdentity.value.equals(paramUserIdentity.value))
              if (userIdentity.verificationRequired() || paramTenant.emailConfiguration.verifyEmailWhenChanged)
                return true;  
          } 
          if (userIdentity != null) {
            paramUserIdentity.verified = userIdentity.verified;
            paramUserIdentity.verifiedReason = userIdentity.verifiedReason;
            paramUserIdentity.verifiedInstant = userIdentity.verifiedInstant;
          } 
          return false;
        }).toList();
  }
  
  private void handleDeleteSearchResults(SearchResults<User> paramSearchResults, boolean paramBoolean1, boolean paramBoolean2, EventInfo paramEventInfo) {
    if (!paramBoolean1) {
      List<User> list = paramSearchResults.results;
      Map<UUID, Tenant> map = usersToTenantMap(list);
      if (paramBoolean2) {
        deleteUsers(map, list, paramEventInfo);
      } else {
        deactivateUsers(map, list, paramEventInfo);
      } 
    } 
  }
  
  private void handleIdentitiesDuringCreate(User paramUser, Tenant paramTenant, boolean paramBoolean1, ZonedDateTime paramZonedDateTime, boolean paramBoolean2) {
    paramUser.identities.forEach(paramUserIdentity -> paramUserIdentity.with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()));
  }
  
  private void handleIdentitiesDuringUpdate(User paramUser1, User paramUser2) {
    for (UserIdentity userIdentity : paramUser2.identities) {
      if (paramUser1.identities.stream().noneMatch(paramUserIdentity2 -> (paramUserIdentity2.type.equals(paramUserIdentity1.type) && paramUserIdentity2.value.equals(paramUserIdentity1.value)))) {
        this.userMapper.deleteIdentityById(userIdentity.id.longValue());
        this.externalIdentifierService.deleteIdentityVerificationsByUserAndIdentityType(paramUser1.id, userIdentity.value, userIdentity.type);
      } 
    } 
    for (UserIdentity userIdentity : paramUser1.identities)
      this.userMapper.upsertIdentity(paramUser1.tenantId, paramUser1.id, userIdentity, UserIdentityStatus.Active); 
  }
  
  private void handleIdentitiesDuringUpdateForAnonymousUser(User paramUser, Tenant paramTenant, ZonedDateTime paramZonedDateTime, boolean paramBoolean) {
    paramUser.identities.forEach(paramUserIdentity -> paramUserIdentity.with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()));
    if (paramUser.identities.size() > 0)
      this.userMapper.createIdentityBulk(paramUser.identities, UserIdentityStatus.Active); 
  }
  
  private void handleTwoFactorOnBulkCreate(Tenant paramTenant, User paramUser) {
    paramUser.twoFactor.methods.removeIf(paramTwoFactorMethod -> (paramTwoFactorMethod.method == null));
    for (TwoFactorMethod twoFactorMethod : paramUser.twoFactor.methods) {
      twoFactorMethod.id = TwoFactorTools.generateUniqueTwoFactorMethodId(paramUser);
      twoFactorMethod.normalize();
      if (twoFactorMethod.method.equals("authenticator")) {
        twoFactorMethod.authenticator = new AuthenticatorConfiguration();
        twoFactorMethod.authenticator.algorithm = paramTenant.multiFactorConfiguration.authenticator.algorithm;
        twoFactorMethod.authenticator.codeLength = paramTenant.multiFactorConfiguration.authenticator.codeLength;
        twoFactorMethod.authenticator.timeStep = paramTenant.multiFactorConfiguration.authenticator.timeStep;
      } 
    } 
    if (!paramUser.twoFactor.recoveryCodes.isEmpty() && paramUser.twoFactor.recoveryCodeEncryptionScheme == null)
      this.recoveryCodeHasher.hashCodes(new ArrayList<>(paramUser.twoFactor.recoveryCodes), paramUser.twoFactor); 
  }
  
  private void handleTwoFactorOnCreate(Tenant paramTenant, User paramUser) {
    for (TwoFactorMethod twoFactorMethod : paramUser.twoFactor.methods) {
      twoFactorMethod.id = TwoFactorTools.generateUniqueTwoFactorMethodId(paramUser);
      twoFactorMethod.normalize();
      if ("authenticator".equals(twoFactorMethod.method)) {
        twoFactorMethod.authenticator = new AuthenticatorConfiguration();
        twoFactorMethod.authenticator.algorithm = paramTenant.multiFactorConfiguration.authenticator.algorithm;
        twoFactorMethod.authenticator.codeLength = paramTenant.multiFactorConfiguration.authenticator.codeLength;
        twoFactorMethod.authenticator.timeStep = paramTenant.multiFactorConfiguration.authenticator.timeStep;
      } 
    } 
    clearRecoveryCodes(paramUser.twoFactor);
    if (!paramUser.twoFactor.methods.isEmpty())
      this.recoveryCodeHasher.hashCodes(this.recoveryCodeHasher.generateCodes(), paramUser.twoFactor); 
  }
  
  private void handleTwoFactorOnUpdate(Tenant paramTenant, User paramUser1, User paramUser2, EventInfo paramEventInfo) {
    if (paramUser2.twoFactor.methods.size() > 0 && paramUser1.twoFactor.methods.isEmpty()) {
      clearRecoveryCodes(paramUser2.twoFactor);
      this.recoveryCodeHasher.hashCodes(this.recoveryCodeHasher.generateCodes(), paramUser2.twoFactor);
      if (paramTenant.jwtConfiguration.refreshTokenRevocationPolicy.onMultiFactorEnable) {
        this.refreshTokenService.revokeRefreshTokensByUser(paramTenant, paramUser2, paramEventInfo);
        this.externalIdentifierService.deleteByUserId(paramUser2.id, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.LoginIntent });
      } 
    } else if (paramUser2.twoFactor.methods.isEmpty() && paramUser1.twoFactor.methods.size() > 0) {
      clearRecoveryCodes(paramUser2.twoFactor);
    } else if (paramUser2.twoFactor.methods.size() > 0) {
      paramUser2.twoFactor.recoveryCodes.clear();
      paramUser2.twoFactor.recoveryCodes.addAll(paramUser1.twoFactor.recoveryCodes);
      paramUser2.twoFactor.recoveryCodeEncryptionScheme = paramUser1.twoFactor.recoveryCodeEncryptionScheme;
      paramUser2.twoFactor.recoveryCodeWorkFactor = paramUser1.twoFactor.recoveryCodeWorkFactor;
    } 
    for (TwoFactorMethod twoFactorMethod1 : paramUser2.twoFactor.methods) {
      TwoFactorMethod twoFactorMethod2 = paramUser1.twoFactor.getMethodById(twoFactorMethod1.id);
      if (twoFactorMethod2 != null) {
        twoFactorMethod1.method = twoFactorMethod2.method;
        twoFactorMethod1.authenticator = twoFactorMethod2.authenticator;
        twoFactorMethod1.email = twoFactorMethod2.email;
        twoFactorMethod1.lastUsed = twoFactorMethod2.lastUsed;
        twoFactorMethod1.mobilePhone = twoFactorMethod2.mobilePhone;
        twoFactorMethod1.secret = twoFactorMethod2.secret;
        continue;
      } 
      twoFactorMethod1.id = TwoFactorTools.generateUniqueTwoFactorMethodId(paramUser2);
      twoFactorMethod1.normalize();
      if (twoFactorMethod1.method.equals("authenticator")) {
        twoFactorMethod1.authenticator = new AuthenticatorConfiguration();
        twoFactorMethod1.authenticator.algorithm = paramTenant.multiFactorConfiguration.authenticator.algorithm;
        twoFactorMethod1.authenticator.codeLength = paramTenant.multiFactorConfiguration.authenticator.codeLength;
        twoFactorMethod1.authenticator.timeStep = paramTenant.multiFactorConfiguration.authenticator.timeStep;
      } 
    } 
  }
  
  private void handleUniqueUsernames(Tenant paramTenant, UserIdentity paramUserIdentity1, UserIdentity paramUserIdentity2) {
    if (!paramTenant.usernameConfiguration.unique.enabled) {
      paramUserIdentity1.displayValue = paramUserIdentity1.value;
      return;
    } 
    if (paramUserIdentity1.value.length() == 0)
      return; 
    if (paramUserIdentity2 != null && paramUserIdentity2.value != null && paramUserIdentity1.value.equals(paramUserIdentity2.value))
      return; 
    String str1 = paramUserIdentity1.value;
    String str2 = str1;
    paramUserIdentity1.displayValue = str2;
    byte b = 0;
    while (b < 40) {
      if (this.userMapper.existsByUsername(paramTenant.id, str1) == null) {
        paramUserIdentity1.value = str1;
        if (!paramUserIdentity1.value.equals(str2))
          return; 
        if (paramTenant.usernameConfiguration.unique.strategy == TenantUsernameConfiguration.UniqueUsernameStrategy.OnCollision)
          return; 
      } 
      str1 = str2 + str2 + paramTenant.usernameConfiguration.unique.separator;
      b++;
    } 
    EventLogHelper.create(new EventLog(EventLogType.Error, "Username unavailable.\n\nAttempted [" + b + "] times and failed to find a unique username for [" + paramUserIdentity1.value + "].\n\nYou may need to increase the number of digits in your unique username configuration, current value is [" + paramTenant.usernameConfiguration.unique.numberOfDigits + "] digits."));
    throw new UsernameUnavailableException();
  }
  
  private void noDuplicateRegistrations(Validator paramValidator, UUID paramUUID, UserRegistration paramUserRegistration) {
    paramValidator
      
      .ifTrue((paramUserRegistration.id != null), paramValidator -> paramValidator.notDuplicate(this.userMapper.retrieveRegistrationById(null, paramUserRegistration.id), "registration.id", new Object[] { paramUserRegistration.id })).ifTrue((paramUserRegistration.applicationId != null && paramUUID != null), paramValidator -> paramValidator.notDuplicate(this.userMapper.retrieveRegistration(null, paramUUID, paramUserRegistration.applicationId), "registration", new Object[] { paramUserRegistration.applicationId, paramUUID }));
  }
  
  private void normalizeIdentitiesDuringUpdate(Tenant paramTenant, User paramUser1, User paramUser2, ZonedDateTime paramZonedDateTime, boolean paramBoolean) {
    ArrayList<UserIdentity> arrayList = new ArrayList();
    for (UserIdentity userIdentity1 : paramUser1.identities) {
      userIdentity1.value = IdentityHelper.canonicalizeValue(userIdentity1);
      userIdentity1.displayValue = handleIdentityDisplayValue(userIdentity1);
      UserIdentity userIdentity2 = paramUser2.identities.stream().filter(paramUserIdentity2 -> (paramUserIdentity2.type.equals(paramUserIdentity1.type) && paramUserIdentity2.value.equals(paramUserIdentity1.value))).findFirst().orElse((UserIdentity)null);
      if (userIdentity2 != null) {
        arrayList.add(userIdentity2);
        continue;
      } 
      userIdentity1.with(paramUserIdentity -> paramUserIdentity.verifiedReason = getVerifiedReason(paramUserIdentity, paramTenant, paramBoolean))
        .with(paramUserIdentity -> paramUserIdentity.moderationStatus = (paramUserIdentity.type.is(IdentityType.username) && paramUserIdentity.moderationStatus == null) ? ContentStatus.ACTIVE : paramUserIdentity.moderationStatus)
        .with(paramUserIdentity -> paramUserIdentity.verifiedInstant = getVerifiedInstant(paramUserIdentity, paramZonedDateTime))
        .with(paramUserIdentity -> paramUserIdentity.insertInstant = (paramUserIdentity.insertInstant == null) ? paramZonedDateTime : paramUserIdentity.insertInstant)
        .with(paramUserIdentity -> paramUserIdentity.lastUpdateInstant = paramZonedDateTime);
      arrayList.add(userIdentity1);
    } 
    paramUser1.identities.clear();
    paramUser1.identities.addAll(arrayList);
  }
  
  private boolean passwordChanged(User paramUser, String paramString) {
    if (paramString == null)
      return false; 
    if (paramUser.encryptionScheme == null)
      return true; 
    return !this.passwordService.passwordsEqual(paramString, paramUser.password, paramUser.salt, paramUser.encryptionScheme, paramUser.factor);
  }
  
  private void populateUserVerifiedInstant(User paramUser1, User paramUser2, ZonedDateTime paramZonedDateTime) {
    if (paramUser2 != null)
      paramUser1.verifiedInstant = paramUser2.verifiedInstant; 
    if (paramUser1.verifiedInstant != null)
      return; 
    Optional<ZonedDateTime> optional = paramUser1.identities.stream().filter(paramUserIdentity -> IdentityTypeValidator.VerifiableIdentityTypes.contains(paramUserIdentity.type)).filter(paramUserIdentity -> ((paramUserIdentity.primary && paramUserIdentity.type.is(IdentityType.email) && !paramUserIdentity.verificationRequired()) || paramUserIdentity.verified)).map(paramUserIdentity -> (ZonedDateTime)Optional.<ZonedDateTime>ofNullable(paramUserIdentity.verifiedInstant).orElse(paramUserIdentity.insertInstant)).sorted().findFirst();
    if (optional.isPresent()) {
      paramUser1.verifiedInstant = optional.get();
    } else if (paramUser1.identities.isEmpty() || (paramUser1.resolvePrimaryIdentity(IdentityType.username) != null && paramUser1
      .resolvePrimaryIdentity(IdentityType.email) == null)) {
      paramUser1.verifiedInstant = paramZonedDateTime;
    } 
  }
  
  private UserService.ValidationResult resolveUserAppTenantAndReg(UUID paramUUID1, UUID paramUUID2, UUID paramUUID3) {
    return resolveUserAppTenantAndReg(paramUUID1, paramUUID2, paramUUID3, false);
  }
  
  private UserService.ValidationResult resolveUserAppTenantAndReg(UUID paramUUID1, UUID paramUUID2, UUID paramUUID3, boolean paramBoolean) {
    UserService.ValidationResult validationResult = new UserService.ValidationResult();
    validationResult.registration = (paramUUID2 != null && paramUUID1 != null) ? this.userMapper.retrieveRegistration(paramUUID3, paramUUID1, paramUUID2) : null;
    validationResult.application = paramBoolean ? this.applicationReader.retrieveByIdIgnoreActive(paramUUID3, paramUUID2) : this.applicationReader.retrieveById(paramUUID3, paramUUID2);
    validationResult.user = (paramUUID1 != null) ? this.userReader.retrieveById(paramUUID3, paramUUID1) : null;
    Objects.requireNonNull(this.tenantReader);
    validationResult.tenant = this.tenantCache.resolve(null, this.tenantReader::retrieveById, new Tenantable[] { () -> paramUUID, validationResult.application, validationResult.user });
    return validationResult;
  }
  
  private void retrieveOptionalRegistrationVerificationId(UUID paramUUID, UserService.ValidationResult paramValidationResult) {
    if (paramValidationResult.registration == null || paramUUID == null)
      return; 
    if (!paramValidationResult.registration.verified) {
      List<ExternalIdentifier> list = this.externalIdentifierReader.retrieveAllByUserId(paramUUID, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.RegistrationVerification });
      ExternalIdentifier externalIdentifier = list.stream().filter(paramExternalIdentifier -> (paramExternalIdentifier.applicationId != null && paramExternalIdentifier.applicationId.equals(paramValidationResult.registration.applicationId))).findFirst().orElse((ExternalIdentifier)null);
      paramValidationResult.registrationVerificationId = (externalIdentifier != null) ? externalIdentifier.id : null;
      paramValidationResult.registrationVerificationOneTimeCode = (externalIdentifier != null) ? externalIdentifier.getAttribute("otp") : null;
    } 
  }
  
  private void retrieveOptionalVerificationId(UserService.ValidationResult paramValidationResult) {
    if (paramValidationResult.tenant == null || paramValidationResult.user == null)
      return; 
    List<ExternalIdentifier> list1 = this.externalIdentifierReader.retrieveAllByUserId(paramValidationResult.user.id, IdentityExternalIdHelper.identityExternalIdTypes);
    Map<UserIdentity, ExternalIdentifier> map = IdentityExternalIdHelper.groupActiveIdentifiersByIdentity(list1, paramValidationResult.user, paramValidationResult.tenant);
    paramValidationResult.verificationIds.putAll(map);
    List<ExternalIdentifier> list2 = null;
    for (UserRegistration userRegistration : paramValidationResult.user.getRegistrations()) {
      if (!userRegistration.verified) {
        if (list2 == null)
          list2 = this.externalIdentifierReader.retrieveAllByUserId(paramValidationResult.user.id, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.RegistrationVerification }); 
        ExternalIdentifier externalIdentifier = list2.stream().filter(paramExternalIdentifier -> !paramExternalIdentifier.isExpired(paramValidationResult.tenant)).filter(paramExternalIdentifier -> (paramExternalIdentifier.applicationId != null && paramExternalIdentifier.applicationId.equals(paramUserRegistration.applicationId))).findFirst().orElse((ExternalIdentifier)null);
        if (externalIdentifier != null) {
          paramValidationResult.registrationVerificationIds.put(userRegistration.applicationId, externalIdentifier.id);
          String str = externalIdentifier.getAttribute("otp");
          if (str != null)
            paramValidationResult.registrationVerificationOneTimeCodes.put(userRegistration.applicationId, str); 
        } 
      } 
    } 
  }
  
  private SendResult sendEmailToUser(Tenant paramTenant, Application paramApplication, User paramUser, EmailTemplate paramEmailTemplate, List<Locale> paramList, List<String> paramList1, List<String> paramList2, Map<String, Object> paramMap) {
    return this.emailProxy.sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(paramTenant, paramEmailTemplate.id, paramList1).to(new EmailAddress[] { new EmailAddress(paramUser.email, paramUser.getName()) })).cc((paramList2 == null) ? new String[0] : (String[])paramList2.toArray((Object[])new String[0]))).bcc((paramList3 == null) ? new String[0] : (String[])paramList3.toArray((Object[])new String[0]))).withTemplateParameter("requestData", paramMap)).withTemplateParameter("tenant", (new Tenant(paramTenant)).secure())).withTemplateParameter("application", (paramApplication != null) ? (new Application(paramApplication)).secure() : null)).withTemplateParameter("user", (new User(paramUser)).secure())).withTemplateParameter("baseUrl", TemplateHelper.resolveBaseUrl(paramTenant, paramApplication))).withTemplateParameter("breachResult", "{{For testing purposes only. This template should be configured to be sent by FusionAuth with a generated value.}}")).withTemplateParameter("code", "{{For testing purposes only. This template should be configured to be sent by FusionAuth with a generated value.}}")).withTemplateParameter("changePasswordId", "{{For testing purposes only. This template should be configured to be sent by FusionAuth with a generated value.}}")).withTemplateParameter("method", "{{For testing purposes only. This template should be configured to be sent by FusionAuth with a generated value.}}")).withTemplateParameter("registration", new UserRegistration())).withTemplateParameter("event", new UserLoginSuspiciousEvent())).withTemplateParameter("verificationId", "{{For testing purposes only. This template should be configured to be sent by FusionAuth with a generated value.}}")).withTemplateParameter("verificationOneTimeCode", "{{For testing purposes only. This template should be configured to be sent by FusionAuth with a generated value.}}")).later());
  }
  
  private void sendFamilyEmails(Tenant paramTenant, User paramUser) {
    if (paramUser.parentEmail != null && paramTenant.familyConfiguration.enabled) {
      User user = this.userReader.retrieveByLoginId(paramTenant.id, paramUser.parentEmail, List.of(IdentityType.email));
      if (user == null && paramTenant.familyConfiguration.parentRegistrationEmailTemplateId != null) {
        this.emailProxy.sendParentRegistrationRequestEmail(paramTenant, paramUser.parentEmail, paramUser);
      } else if (user != null && paramTenant.familyConfiguration.confirmChildEmailTemplateId != null) {
        this.emailProxy.sendConfirmChildEmail(paramTenant, user, paramUser);
      } 
    } 
  }
  
  private void sendVerifyWithoutRateLimit(Tenant paramTenant, ExternalIdentifier paramExternalIdentifier, Application paramApplication, User paramUser) {
    String str1 = paramExternalIdentifier.getAttribute("otp");
    String str2 = IdentityExternalIdHelper.getLoginId(paramExternalIdentifier);
    IdentityType identityType = IdentityExternalIdHelper.getLoginIdentityType(paramExternalIdentifier);
    if (identityType.is(IdentityType.email)) {
      this.emailProxy.sendEmailVerificationEmail(paramTenant, paramApplication, paramUser, str2, paramExternalIdentifier.id, str1, paramExternalIdentifier




          
          .getStateHelper());
    } else if (identityType.is(IdentityType.phoneNumber)) {
      Map<String, Object> map = MessageTemplateHelper.getBaseParameters(paramTenant, paramApplication, null, str2);
      map.put("state", paramExternalIdentifier.getStateHelper());
      map.put("verificationOneTimeCode", str1);
      map.put("verificationId", paramExternalIdentifier.id);
      Locale locale = null;
      if (paramUser != null) {
        List<Locale> list = TemplateHelper.getPreferredLanguages(paramUser, paramApplication);
        locale = list.isEmpty() ? null : (Locale)list.getFirst();
      } 
      UUID uUID = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> paramApplication.phoneConfiguration.verificationTemplateId).orElse(paramTenant.phoneConfiguration.verificationTemplateId);
      this.messengerService.send(uUID, paramTenant.phoneConfiguration.messengerId, locale, map);
    } 
  }
  
  private void setDefaults(Tenant paramTenant, User paramUser, boolean paramBoolean) {
    if (paramUser.passwordChangeRequired) {
      paramUser
        
        .passwordChangeReason = (paramBoolean || paramUser.passwordChangeReason == null) ? ChangePasswordReason.Administrative : paramUser.passwordChangeReason;
    } else {
      paramUser.passwordChangeReason = null;
    } 
    if (!paramTenant.familyConfiguration.enabled) {
      paramUser.parentEmail = null;
    } else if (paramUser.birthDate == null || LocalDate.now(ZoneOffset.UTC).minusYears(paramTenant.familyConfiguration.maximumChildAge).isAfter(paramUser.birthDate)) {
      paramUser.parentEmail = null;
    } 
  }
  
  private SendResponse.EmailTemplateErrors toEmailTemplateErrors(SendResult paramSendResult) {
    SendResponse.EmailTemplateErrors emailTemplateErrors = new SendResponse.EmailTemplateErrors();
    paramSendResult.parseErrors.forEach((paramString, paramParseException) -> paramEmailTemplateErrors.parseErrors.put(paramString, paramParseException.toString()));
    paramSendResult.renderErrors.forEach((paramString, paramTemplateException) -> paramEmailTemplateErrors.renderErrors.put(paramString, paramTemplateException.toString()));
    return emailTemplateErrors;
  }
  
  private void updatePasswordWithOverrides(Tenant paramTenant, User paramUser, String paramString, Integer paramInteger) {
    if (paramString != null)
      paramUser.encryptionScheme = paramString; 
    if (paramInteger != null)
      paramUser.factor = paramInteger; 
    this.passwordService.hashPassword(paramTenant, paramUser, paramUser.password);
  }
  
  private UserExists userExistsByIdentity(Tenant paramTenant, User paramUser, IdentityType paramIdentityType) {
    for (byte b = 0; b < paramUser.identities.size(); b++) {
      UserIdentity userIdentity = paramUser.identities.get(b);
      if (userIdentity.type.is(paramIdentityType)) {
        String str = IdentityHelper.canonicalizeValue(userIdentity.value, userIdentity.type);
        User user = this.userReader.retrieveExisting(paramTenant, paramUser.id, str, userIdentity.type);
        if (user != null)
          return new UserExists(user, userIdentity, "user.identities[%d].value".formatted(new Object[] { Integer.valueOf(b) })); 
      } 
    } 
    return null;
  }
  
  private List<UserIdentityWithField> userIdentitiesByType(User paramUser, IdentityType paramIdentityType) {
    ArrayList<UserIdentityWithField> arrayList = new ArrayList();
    for (byte b = 0; b < paramUser.identities.size(); b++) {
      UserIdentity userIdentity = paramUser.identities.get(b);
      if (userIdentity.type.is(paramIdentityType) && userIdentity.value != null && userIdentity.value.trim().length() > 0)
        arrayList.add(new UserIdentityWithField(userIdentity, "user.identities[%d].value".formatted(new Object[] { Integer.valueOf(b) }))); 
    } 
    return arrayList;
  }
  
  private Map<UUID, Tenant> usersToTenantMap(List<User> paramList) {
    return (Map<UUID, Tenant>)paramList
      .stream()
      .map(paramUser -> paramUser.tenantId)
      .distinct()
      .collect(Collectors.toMap(paramUUID -> paramUUID, paramUUID -> {
            Objects.requireNonNull(this.tenantReader);
            return this.tenantCache.get(paramUUID, this.tenantReader::retrieveById);
          }));
  }
  
  private void validateBulkImportDbConstraints(Validator paramValidator, List<User> paramList, UUID paramUUID) {
    Errors errors = new Errors();
    paramList.forEach(paramUser -> {
          String str1 = paramUser.email;
          if (str1 != null && this.userMapper.existsByEmail(paramUUID, str1) != null)
            paramErrors.addFieldError("user.email", "[duplicate]user.email", null, new Object[] { str1 }); 
          String str2 = paramUser.phoneNumber;
          String str3 = PhoneNumberTools.safeToE164format(str2);
          if (str3 != null && this.userMapper.existsByPhoneNumber(paramUUID, str3) != null)
            paramErrors.addFieldError("user.phoneNumber", "[duplicate]user.phoneNumber", null, new Object[] { str2 }); 
          String str4 = paramUser.username;
          if (str4 != null && this.userMapper.existsByUsername(paramUUID, str4) != null)
            paramErrors.addFieldError("user.username", "[duplicate]user.username", null, new Object[] { str4 }); 
          String str5 = paramUser.legacyIdentifier;
          if (str5 != null && this.userMapper.existsByLegacyIdentifier(str5, paramUUID, null) != null)
            paramErrors.addFieldError("user.legacyIdentifier", "[duplicate]user.legacyIdentifier", null, new Object[] { str5 }); 
        });
    paramValidator.withErrors(errors);
  }
  
  private void validateEncryptionScheme(Set<String> paramSet, User paramUser) {
    if (paramUser.encryptionScheme != null && 
      !this.passwordEncryptorLibrary.validateScheme(paramUser.encryptionScheme))
      paramSet.add(paramUser.encryptionScheme); 
  }
  
  private void validateFactor(Set<Integer> paramSet, User paramUser, String paramString) {
    if (paramUser.factor != null && 
      !this.passwordEncryptorLibrary.validateFactor(paramUser.encryptionScheme, paramString, paramUser.factor))
      paramSet.add(paramUser.factor); 
  }
  
  private Errors validateNoDuplicateIdentities(User paramUser) {
    Errors errors = new Errors();
    HashSet<Pair> hashSet = new HashSet();
    for (byte b = 0; b < paramUser.identities.size(); b++) {
      UserIdentity userIdentity = paramUser.identities.get(b);
      if (userIdentity.value != null && !hashSet.add(new Pair(userIdentity.type, userIdentity.value)))
        errors.addFieldError("user.identities[%d].value".formatted(new Object[] { Integer.valueOf(b) }, ), "[duplicate]user.identities", null, new Object[] { userIdentity.type.name, userIdentity.value }); 
    } 
    return errors;
  }
  
  private void validateSalt(Set<String> paramSet, User paramUser) {
    if (paramUser.salt != null && paramUser.salt.length() > 0) {
      PasswordEncryptor passwordEncryptor = this.passwordEncryptorLibrary.lookup(paramUser.encryptionScheme);
      if (passwordEncryptor != null && 
        !passwordEncryptor.validateSalt(paramUser.salt))
        paramSet.add(paramUser.salt); 
    } 
  }
  
  private void validateTwoFactorOnBulkCreate(Set<String> paramSet1, Set<String> paramSet2, Set<String> paramSet3, Set<String> paramSet4, Set<String> paramSet5, Set<String> paramSet6, User paramUser) {
    HashSet<String> hashSet1 = new HashSet(1);
    HashSet<String> hashSet2 = new HashSet(1);
    for (TwoFactorMethod twoFactorMethod : paramUser.twoFactor.methods) {
      if (twoFactorMethod == null)
        continue; 
      if (!MFAService.SupportedMethods.contains(twoFactorMethod.method))
        paramSet1.add(twoFactorMethod.method); 
      if (twoFactorMethod.name != null && twoFactorMethod.name.length() > 256)
        paramSet6.add(twoFactorMethod.name); 
      String str = paramUser.getLogin();
      if (twoFactorMethod.method.equals("email") && twoFactorMethod.email != null) {
        if (!EmailValidator.isValid(twoFactorMethod.email))
          paramSet2.add(twoFactorMethod.email); 
        if (!hashSet1.add(twoFactorMethod.email))
          if (str != null)
            paramSet5.add(str);  
      } 
      if (twoFactorMethod.method.equals("sms") && twoFactorMethod.mobilePhone != null) {
        if (!PhoneNumberValidator.validateE164format(twoFactorMethod.mobilePhone))
          paramSet3.add(twoFactorMethod.mobilePhone); 
        if (!hashSet2.add(PhoneNumberTools.safeToE164format(twoFactorMethod.mobilePhone)))
          if (str != null)
            paramSet5.add(str);  
      } 
      if (twoFactorMethod.method.equals("authenticator") && twoFactorMethod.secret != null && 
        !EncoderTools.Base64.isValid(twoFactorMethod.secret))
        paramSet4.add(twoFactorMethod.secret); 
    } 
  }
  
  private UserService.ValidationResult validateUserCreate(Tenant paramTenant, User paramUser, UUID paramUUID, boolean paramBoolean1, SendSetPasswordIdentityType paramSendSetPasswordIdentityType, @Deprecated boolean paramBoolean2, boolean paramBoolean3, EventInfo paramEventInfo, boolean paramBoolean4) {
    UserService.ValidationResult validationResult = new UserService.ValidationResult();
    boolean bool = (paramSendSetPasswordIdentityType == null) ? true : false;
    validationResult
      .sendSetPasswordIdentityType = Optional.<SendSetPasswordIdentityType>ofNullable(paramSendSetPasswordIdentityType).orElse(paramBoolean2 ? SendSetPasswordIdentityType.email : SendSetPasswordIdentityType.doNotSend);
    if (!paramBoolean4)
      handleUserPrimaryIdentities(paramUser, validationResult.fieldMapping); 
    User user = (paramUser.id != null) ? this.userReader.retrieveById(null, paramUser.id) : null;
    Validator validator = new Validator();
    validator.notDuplicate(user, "userId", new Object[] { paramUser.id }).ifTrue((paramUser.password != null && paramBoolean3), paramValidator -> paramValidator.withErrors(this.passwordService.validatePasswordOnCreate(paramTenant, paramUser, "user.password", paramUser.password)))



      
      .ifTrue((validationResult.sendSetPasswordIdentityType == SendSetPasswordIdentityType.email), paramValidator -> paramValidator.notBlankWithCode(paramUser.email, "user.email", "[required]user.email", new Object[] { paramBoolean ? "sendSetPasswordEmail" : "sendSetPasswordIdentityType", paramBoolean ? "true" : "email" }).ensure((paramTenant.emailConfiguration.setPasswordEmailTemplateId != null), paramBoolean ? "sendSetPasswordEmail" : "sendSetPasswordIdentityType", "[disabled]", new Object[] { paramBoolean ? "true" : "email" })).ifTrue((validationResult.sendSetPasswordIdentityType == SendSetPasswordIdentityType.phone), paramValidator -> paramValidator.notBlankWithCode(paramUser.phoneNumber, "user.phoneNumber", "[required]user.phoneNumber", new Object[0]).ensure((paramTenant.phoneConfiguration.setPasswordTemplateId != null), "sendSetPasswordIdentityType", "[disabled]", new Object[] { SendSetPasswordIdentityType.phone })).forEach(paramUser.getMemberships(), (paramValidator, paramGroupMember, paramInteger) -> paramValidator.notMissingWithCode(this.groupMapper.retrieveById(paramTenant.id, paramGroupMember.groupId), "user.membership[" + paramInteger + "].groupId", "[invalid]user.membership.groupId", new Object[] { paramGroupMember.groupId }).notDuplicateWithCode(this.groupMapper.retrieveMemberById(null, paramGroupMember.id), "user.membership[" + paramInteger + "].id", "[duplicate]user.membership.id", new Object[] { paramGroupMember.id }));
    commonValidation(validator, paramTenant, null, null, true, paramBoolean1, paramEventInfo, paramUser);
    UserIdentity userIdentity = paramUser.resolvePrimaryIdentity(IdentityType.username);
    if (userIdentity != null)
      moderateUsername(paramUser, validator); 
    Objects.requireNonNull(this.applicationReader);
    validationResult.application = (paramUUID != null) ? this.applicationCache.get(paramTenant.id, paramUUID, this.applicationReader::retrieveById) : null;
    validationResult.errors = validator.done();
    validationResult.tenant = paramTenant;
    validationResult.user = paramUser;
    return validationResult;
  }
  
  private UserService.ValidationResult validateUserUpdate(Tenant paramTenant, User paramUser, boolean paramBoolean1, UUID paramUUID, boolean paramBoolean2, boolean paramBoolean3, String paramString, EventInfo paramEventInfo, @Nonnull PasswordType paramPasswordType) {
    UserService.ValidationResult validationResult = new UserService.ValidationResult();
    UUID uUID = (paramTenant != null) ? paramTenant.id : null;
    validationResult.user = paramUser;
    validationResult.existing = this.userReader.retrieveById(uUID, paramUser.id);
    if (paramBoolean1 && validationResult.existing != null)
      paramUser.identities.addAll(validationResult.existing.identities); 
    handleUserPrimaryIdentities(paramUser, validationResult.fieldMapping);
    if (validationResult.existing == null)
      return validationResult; 
    Objects.requireNonNull(this.tenantReader);
    validationResult.tenant = this.tenantCache.resolve(paramTenant, this.tenantReader::retrieveById, new Tenantable[] { validationResult.existing });
    Objects.requireNonNull(this.applicationReader);
    validationResult.application = (paramUUID != null) ? this.applicationCache.get(validationResult.tenant.id, paramUUID, this.applicationReader::retrieveById) : null;
    UserIdentity userIdentity1 = validationResult.existing.resolvePrimaryIdentity(IdentityType.username);
    UserIdentity userIdentity2 = paramUser.resolvePrimaryIdentity(IdentityType.username);
    validationResult







































      
      .errors = (new Validator()).notInactive(validationResult.existing, paramUser -> Boolean.valueOf(paramUser.active), "userId", new Object[] { paramUser.id }).forEach(paramUser.twoFactor.methods, (paramValidator, paramTwoFactorMethod, paramInteger) -> paramValidator.ifTrue((paramTwoFactorMethod.method.equals("authenticator") && paramTwoFactorMethod.id == null), ())).ensure((paramString == null || paramUser.password != null), "user.password", "[missing]", new Object[0]).ifTrue((paramString != null && paramUser.password != null), paramValidator -> paramValidator.valid(this.passwordService.passwordsEqual(paramString, paramValidationResult.existing.password, paramValidationResult.existing.salt, paramValidationResult.existing.encryptionScheme, paramValidationResult.existing.factor), "currentPassword", new Object[0])).notBlank(paramPasswordType, "passwordFieldType", new Object[0]).ifTrue((paramPasswordType == PasswordType.HASHED), paramValidator -> paramValidator.notBlank(paramUser.encryptionScheme, "user.encryptionScheme", new Object[0]).notBlank(paramUser.factor, "user.factor", new Object[0]).notBlank(paramUser.password, "user.password", new Object[0]).notBlank(paramUser.salt, "user.salt", new Object[0])).ifTrue((paramBoolean3 && paramPasswordType == PasswordType.PLAINTEXT), paramValidator -> paramValidator.withErrors(this.passwordService.validatePasswordOnUpdate(paramValidationResult.tenant, paramValidationResult.existing, paramUser, "user.password", paramUser.password))).validate(paramValidator -> commonValidation(paramValidator, paramValidationResult.tenant, paramValidationResult.application, paramValidationResult.existing, false, paramBoolean, paramEventInfo, paramUser)).ifTrue((userIdentity2 != null && (userIdentity1 == null || !userIdentity1.value.equals(userIdentity2.value))), paramValidator -> moderateUsername(paramUser, paramValidator)).done();
    return validationResult;
  }
  
  public static class EmailSendResult {
    public Map<String, SendResponse.EmailTemplateErrors> emailErrors;
    
    public Map<UUID, SendResponse.EmailTemplateErrors> userIdErrors;
  }
  
  private static final class UserExists extends Record {
    private final User user;
    
    private final UserIdentity identity;
    
    private final String field;
    
    private UserExists(User param1User, UserIdentity param1UserIdentity, String param1String) {
      this.user = param1User;
      this.identity = param1UserIdentity;
      this.field = param1String;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/user/DefaultUserService$UserExists;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #4931	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/user/DefaultUserService$UserExists;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #4931	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/user/DefaultUserService$UserExists;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #4931	-> 0
    }
    
    public User user() {
      return this.user;
    }
    
    public UserIdentity identity() {
      return this.identity;
    }
    
    public String field() {
      return this.field;
    }
    
    public String identityValue() {
      return (this.identity != null) ? this.identity.value : null;
    }
  }
  
  private static final class UserIdentityWithField extends Record {
    private final UserIdentity identity;
    
    private final String field;
    
    private UserIdentityWithField(UserIdentity param1UserIdentity, String param1String) {
      this.identity = param1UserIdentity;
      this.field = param1String;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/user/DefaultUserService$UserIdentityWithField;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #4937	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/user/DefaultUserService$UserIdentityWithField;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #4937	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/user/DefaultUserService$UserIdentityWithField;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #4937	-> 0
    }
    
    public UserIdentity identity() {
      return this.identity;
    }
    
    public String field() {
      return this.field;
    }
    
    public String identityValue() {
      return (this.identity != null) ? this.identity.value : null;
    }
  }
}
