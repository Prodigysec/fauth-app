package io.fusionauth.api.service.authentication;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.IdentityExternalIdHelper;
import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.api.domain.ZonedDateTimeWrapper;
import io.fusionauth.api.domain.api.service.ImmutableLambdaArgument;
import io.fusionauth.api.domain.api.service.LambdaArgument;
import io.fusionauth.api.domain.api.service.MutableLambdaArgument;
import io.fusionauth.api.domain.webauthn.AuthenticatorAssertionResponse;
import io.fusionauth.api.domain.webauthn.PublicKeyCredential;
import io.fusionauth.api.security.PasswordEncryptorLibrary;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.connector.Connector;
import io.fusionauth.api.service.connector.ConnectorConfigurationService;
import io.fusionauth.api.service.connector.ExternalConnector;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.mfa.MFALifecycleService;
import io.fusionauth.api.service.mfa.MFAService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.risk.RiskSignalContext;
import io.fusionauth.api.service.risk.RiskSignalService;
import io.fusionauth.api.service.security.RateLimitService;
import io.fusionauth.api.service.security.RecoveryCodeHasher;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.api.service.system.LambdaInvocationException;
import io.fusionauth.api.service.system.TenantService;
import io.fusionauth.api.service.user.BotDetectionScoreValidator;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.service.user.IdentityHelper;
import io.fusionauth.api.service.user.IdentityTypeHelper;
import io.fusionauth.api.service.user.IdentityTypeValidator;
import io.fusionauth.api.service.user.UserMetricsService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.api.service.useraction.UserActionService;
import io.fusionauth.api.service.webauthn.WebAuthnProviderService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.AuthenticatorConfiguration;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.RateLimitedRequestType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Tenantable;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserActionLog;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserLoginFailedReason;
import io.fusionauth.domain.UserLoginFailedReasonCode;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.VerificationStrategy;
import io.fusionauth.domain.WebAuthnCredential;
import io.fusionauth.domain.api.LoginPreventedResponse;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorPolicy;
import io.fusionauth.domain.connector.ConnectorType;
import io.fusionauth.domain.event.UserLoginFailedEvent;
import io.fusionauth.domain.event.UserLoginSuccessEvent;
import io.fusionauth.domain.event.UserLoginSuspiciousEvent;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.plugin.spi.security.PasswordEncryptor;
import io.fusionauth.twofactor.Algorithm;
import io.fusionauth.twofactor.TwoFactor;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAuthenticationService implements AuthenticationService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultAuthenticationService.class);
  
  private final ApplicationCache applicationCache;
  
  private final ApplicationReaderService applicationReader;
  
  private final ConnectorConfigurationService connectorConfigurationService;
  
  private final Map<ConnectorType, Connector> connectors;
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final ExternalIdentifierService externalIdentifierService;
  
  private final FailedLoginService failedLoginService;
  
  private final LambdaInvocationService lambdaInvocationService;
  
  private final MFALifecycleService mfaLifecycleService;
  
  private final MFAService mfaService;
  
  private final PasswordEncryptorLibrary passwordEncryptorLibrary;
  
  private final RateLimitService rateLimitService;
  
  private final ReactorStatusService reactorStatusService;
  
  private final RecoveryCodeHasher recoveryCodeHasher;
  
  private final RiskSignalService riskSignalService;
  
  private final TenantCache tenantCache;
  
  private final UserActionService userActionService;
  
  private final UserMapper userMapper;
  
  private final UserMetricsService userMetricsService;
  
  private final UserReaderService userReader;
  
  private final UserService userService;
  
  private final WebAuthnProviderService webauthnProviderService;
  
  @Inject
  public DefaultAuthenticationService(ApplicationCache paramApplicationCache, ApplicationReaderService paramApplicationReaderService, ConnectorConfigurationService paramConnectorConfigurationService, Map<ConnectorType, Connector> paramMap, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, FailedLoginService paramFailedLoginService, LambdaInvocationService paramLambdaInvocationService, MFALifecycleService paramMFALifecycleService, MFAService paramMFAService, PasswordEncryptorLibrary paramPasswordEncryptorLibrary, ReactorStatusService paramReactorStatusService, RateLimitService paramRateLimitService, RecoveryCodeHasher paramRecoveryCodeHasher, RiskSignalService paramRiskSignalService, TenantCache paramTenantCache, UserActionService paramUserActionService, UserMapper paramUserMapper, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService, WebAuthnProviderService paramWebAuthnProviderService) {
    this.applicationCache = paramApplicationCache;
    this.applicationReader = paramApplicationReaderService;
    this.connectorConfigurationService = paramConnectorConfigurationService;
    this.connectors = paramMap;
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.externalIdentifierService = paramExternalIdentifierService;
    this.failedLoginService = paramFailedLoginService;
    this.lambdaInvocationService = paramLambdaInvocationService;
    this.mfaLifecycleService = paramMFALifecycleService;
    this.mfaService = paramMFAService;
    this.passwordEncryptorLibrary = paramPasswordEncryptorLibrary;
    this.rateLimitService = paramRateLimitService;
    this.reactorStatusService = paramReactorStatusService;
    this.recoveryCodeHasher = paramRecoveryCodeHasher;
    this.riskSignalService = paramRiskSignalService;
    this.tenantCache = paramTenantCache;
    this.userActionService = paramUserActionService;
    this.userMapper = paramUserMapper;
    this.userMetricsService = paramUserMetricsService;
    this.userReader = paramUserReaderService;
    this.userService = paramUserService;
    this.webauthnProviderService = paramWebAuthnProviderService;
  }
  
  public static void applyLoginValidationLambda(LambdaInvocationService paramLambdaInvocationService, FailedLoginService paramFailedLoginService, Tenant paramTenant, Application paramApplication, AuthenticationService.AuthenticationResult paramAuthenticationResult, AuthenticationService.LoginLambdaValidationContext paramLoginLambdaValidationContext, EventInfo paramEventInfo) {
    paramAuthenticationResult.loginLambdaValidationResult = new AuthenticationService.LoginLambdaValidationResult();
    if (paramTenant.lambdaConfiguration.loginValidationId == null)
      return; 
    UserRegistration userRegistration = (paramApplication != null) ? paramAuthenticationResult.user.getRegistrationForApplication(paramApplication.id) : null;
    User user = (new User(paramAuthenticationResult.user)).secure().sort();
    user.getRegistrations().clear();
    try {
      paramLambdaInvocationService.invoke(paramTenant.lambdaConfiguration.loginValidationId, new LambdaArgument[] { new MutableLambdaArgument(paramAuthenticationResult.loginLambdaValidationResult), new ImmutableLambdaArgument(user), new ImmutableLambdaArgument(userRegistration), new ImmutableLambdaArgument(paramLoginLambdaValidationContext, true) });
    } catch (LambdaInvocationException lambdaInvocationException) {
      logger.debug("Error while applying lambda for login validation.", (Throwable)lambdaInvocationException);
    } 
    if (paramAuthenticationResult.loginLambdaValidationResult.errors.empty())
      return; 
    paramAuthenticationResult.loginValidationId = paramTenant.lambdaConfiguration.loginValidationId;
    UUID uUID = (userRegistration != null) ? userRegistration.applicationId : null;
    EventHelper.send(paramTenant, paramApplication, new UserLoginFailedEvent(paramEventInfo, uUID, paramAuthenticationResult.type
          
          .name(), new UserLoginFailedReason(UserLoginFailedReasonCode.LambdaValidation, paramTenant.lambdaConfiguration.loginValidationId, paramAuthenticationResult.loginLambdaValidationResult.errors), user));
    UserActionLog userActionLog = paramFailedLoginService.handleFailedLoginCountExceeded(paramTenant, user, paramEventInfo);
    if (userActionLog != null)
      paramAuthenticationResult.exception = (RuntimeException)new LoginPreventedException(userActionLog); 
  }
  
  @Transactional
  public void _handleTwoFactorTrust(AuthenticationService.AuthenticationResult paramAuthenticationResult, Tenant paramTenant, Application paramApplication, User paramUser, ExternalIdentifier paramExternalIdentifier) {
    ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveByIdForUpdate(paramExternalIdentifier.getAttribute("twoFactorTrustId"));
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    if (externalIdentifier != null) {
      if (externalIdentifier.data == null)
        externalIdentifier.data = new ExternalIdentifier.ExternalIdData(); 
      if (externalIdentifier.data.startInstants == null)
        externalIdentifier.data.startInstants = new ExternalIdentifier.StartInstant(); 
      if (paramApplication != null)
        externalIdentifier.data.startInstants.applications.put(paramApplication.id, zonedDateTime); 
      externalIdentifier.data.startInstants.tenant = zonedDateTime;
      ZonedDateTime zonedDateTime1 = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(ExternalIdentifier.getTTL(ExternalIdentifier.ExternalIdType.TwoFactorTrust, paramTenant, () -> paramApplication));
      if (externalIdentifier.expirationInstant == null || externalIdentifier.expirationInstant.isBefore(zonedDateTime1))
        externalIdentifier.expirationInstant = zonedDateTime1; 
      this.externalIdentifierService.update(externalIdentifier);
      paramAuthenticationResult.twoFactorTrustId = externalIdentifier.id;
    } else {
      ExternalIdentifier.ExternalIdData externalIdData = (new ExternalIdentifier.ExternalIdData()).with(paramExternalIdData -> paramExternalIdData.startInstants = new ExternalIdentifier.StartInstant());
      if (paramApplication != null)
        externalIdData.startInstants.applications.put(paramApplication.id, zonedDateTime); 
      externalIdData.startInstants.tenant = zonedDateTime;
      ZonedDateTime zonedDateTime1 = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(ExternalIdentifier.getTTL(ExternalIdentifier.ExternalIdType.TwoFactorTrust, paramTenant, () -> paramApplication));
      paramAuthenticationResult.twoFactorTrustId = this.externalIdentifierService.createTwoFactorTrust(paramTenant, paramUser.id, externalIdData, zonedDateTime1);
    } 
  }
  
  public AuthenticationService.AuthenticationResult authenticate(Tenant paramTenant, Application paramApplication, String paramString1, List<IdentityType> paramList, String paramString2, ExternalIdentifier paramExternalIdentifier, EventInfo paramEventInfo, Double paramDouble, boolean paramBoolean) {
    AuthenticationService.AuthenticationResult authenticationResult = authenticateUser(paramTenant, paramApplication, paramString1, paramList, paramString2, paramExternalIdentifier, paramEventInfo, paramDouble, paramBoolean);
    if (authenticationResult == null)
      return null; 
    if (authenticationResult.rawLogin != null)
      this.userMetricsService.addToLoginQueue(authenticationResult.rawLogin); 
    return authenticationResult;
  }
  
  public AuthenticationService.AuthenticationResult authenticateOneTimePassword(Tenant paramTenant, Application paramApplication, ExternalIdentifier paramExternalIdentifier1, ExternalIdentifier paramExternalIdentifier2, EventInfo paramEventInfo, Double paramDouble, boolean paramBoolean) {
    UUID uUID = (paramApplication != null) ? paramApplication.id : null;
    User user = (validateUserForAuthentication(paramTenant, uUID, paramExternalIdentifier1, this.userReader.retrieveById(paramTenant.id, paramExternalIdentifier1.userId))).user;
    if (user == null)
      return null; 
    AuthenticationService.AuthenticationResult authenticationResult = new AuthenticationService.AuthenticationResult(AuthenticationType.ONE_TIME_PASSWORD, BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID, user);
    authenticationResult.externalIdentifier = paramExternalIdentifier1;
    authenticationResult.userIdentity = resolveLoginIdentity(authenticationResult.user, paramExternalIdentifier1);
    this.externalIdentifierService.deleteByUserId(user.id, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.OneTimePassword });
    boolean bool = checkMFAChallenge(paramTenant, paramApplication, user, authenticationResult, paramExternalIdentifier1, paramExternalIdentifier2, paramEventInfo, paramDouble, paramBoolean);
    if (bool)
      return authenticationResult; 
    applyLoginValidationLambda(this.lambdaInvocationService, this.failedLoginService, paramTenant, paramApplication, authenticationResult, new AuthenticationService.LoginLambdaValidationContext(authenticationResult.type), paramEventInfo);
    if (!authenticationResult.loginLambdaValidationResult.errors.empty())
      return authenticationResult; 
    handleUpdatesAndSendLoginSuccessEvent(paramTenant, paramApplication, user, authenticationResult, true, paramEventInfo);
    return authenticationResult;
  }
  
  @Transactional
  public AuthenticationService.AuthenticationResult authenticatePasswordless(Tenant paramTenant, Application paramApplication, ExternalIdentifier paramExternalIdentifier1, ExternalIdentifier paramExternalIdentifier2, EventInfo paramEventInfo, Double paramDouble, boolean paramBoolean) {
    User user = (validateUserForAuthentication(paramTenant, null, paramExternalIdentifier1, this.userReader.retrieveById(paramTenant.id, paramExternalIdentifier1.userId))).user;
    if (user == null)
      return null; 
    AuthenticationService.AuthenticationResult authenticationResult = new AuthenticationService.AuthenticationResult(AuthenticationType.PASSWORDLESS, BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID, user);
    authenticationResult.externalIdentifier = paramExternalIdentifier1;
    authenticationResult.userIdentity = resolveLoginIdentity(user, paramExternalIdentifier1);
    if (authenticationResult.externalIdentifier.wasSentToUser())
      this.userService.handleImplicitVerification(paramTenant, paramApplication, user, authenticationResult.userIdentity, paramEventInfo); 
    this.externalIdentifierService.deleteByUserId(user.id, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.PasswordlessLogin });
    boolean bool = checkMFAChallenge(paramTenant, paramApplication, user, authenticationResult, paramExternalIdentifier1, paramExternalIdentifier2, paramEventInfo, paramDouble, paramBoolean);
    if (bool)
      return authenticationResult; 
    applyLoginValidationLambda(this.lambdaInvocationService, this.failedLoginService, paramTenant, paramApplication, authenticationResult, new AuthenticationService.LoginLambdaValidationContext(authenticationResult.type), paramEventInfo);
    if (!authenticationResult.loginLambdaValidationResult.errors.empty())
      return authenticationResult; 
    handleUpdatesAndSendLoginSuccessEvent(paramTenant, paramApplication, user, authenticationResult, true, paramEventInfo);
    return authenticationResult;
  }
  
  public AuthenticationService.AuthenticationResult authenticateTwoFactor(Tenant paramTenant, Application paramApplication, String paramString, ExternalIdentifier paramExternalIdentifier, boolean paramBoolean, EventInfo paramEventInfo) {
    // Byte code:
    //   0: aload_2
    //   1: ifnull -> 11
    //   4: aload_2
    //   5: getfield id : Ljava/util/UUID;
    //   8: goto -> 12
    //   11: aconst_null
    //   12: astore #7
    //   14: aload_0
    //   15: aload_1
    //   16: aload #7
    //   18: aload #4
    //   20: aload_0
    //   21: getfield userReader : Lio/fusionauth/api/service/user/UserReaderService;
    //   24: aload_1
    //   25: getfield id : Ljava/util/UUID;
    //   28: aload #4
    //   30: getfield userId : Ljava/util/UUID;
    //   33: invokeinterface retrieveById : (Ljava/util/UUID;Ljava/util/UUID;)Lio/fusionauth/domain/User;
    //   38: invokevirtual validateUserForAuthentication : (Lio/fusionauth/domain/Tenant;Ljava/util/UUID;Lio/fusionauth/api/domain/ExternalIdentifier;Lio/fusionauth/domain/User;)Lio/fusionauth/api/service/authentication/DefaultAuthenticationService$UserAuthenticationValidationResult;
    //   41: astore #8
    //   43: aload #8
    //   45: getfield user : Lio/fusionauth/domain/User;
    //   48: astore #9
    //   50: aload #9
    //   52: ifnonnull -> 57
    //   55: aconst_null
    //   56: areturn
    //   57: aload #4
    //   59: ldc_w 'authenticationType'
    //   62: invokevirtual getAttribute : (Ljava/lang/String;)Ljava/lang/String;
    //   65: ifnull -> 82
    //   68: aload #4
    //   70: ldc_w 'authenticationType'
    //   73: invokevirtual getAttribute : (Ljava/lang/String;)Ljava/lang/String;
    //   76: invokestatic valueOf : (Ljava/lang/String;)Lio/fusionauth/api/service/authentication/AuthenticationType;
    //   79: goto -> 85
    //   82: getstatic io/fusionauth/api/service/authentication/AuthenticationType.PASSWORD : Lio/fusionauth/api/service/authentication/AuthenticationType;
    //   85: astore #10
    //   87: new io/fusionauth/api/service/authentication/AuthenticationService$AuthenticationResult
    //   90: dup
    //   91: aload #10
    //   93: getstatic io/fusionauth/domain/connector/BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID : Ljava/util/UUID;
    //   96: aload #9
    //   98: invokespecial <init> : (Lio/fusionauth/api/service/authentication/AuthenticationType;Ljava/util/UUID;Lio/fusionauth/domain/User;)V
    //   101: astore #11
    //   103: aload #11
    //   105: aload #4
    //   107: putfield externalIdentifier : Lio/fusionauth/api/domain/ExternalIdentifier;
    //   110: aload #11
    //   112: aload_0
    //   113: aload #9
    //   115: aload #4
    //   117: invokevirtual resolveLoginIdentity : (Lio/fusionauth/domain/User;Lio/fusionauth/api/domain/ExternalIdentifier;)Lio/fusionauth/domain/UserIdentity;
    //   120: putfield userIdentity : Lio/fusionauth/domain/UserIdentity;
    //   123: aload #11
    //   125: aload #8
    //   127: getfield trustOnly : Z
    //   130: putfield trustOnly : Z
    //   133: aload #11
    //   135: getstatic io/fusionauth/api/service/risk/CompositeRisk.NO_RISK : Lio/fusionauth/api/service/risk/CompositeRisk;
    //   138: putfield clientRisk : Lio/fusionauth/api/service/risk/CompositeRisk;
    //   141: aload_0
    //   142: aload_1
    //   143: aload_2
    //   144: aload #9
    //   146: aload #4
    //   148: getfield id : Ljava/lang/String;
    //   151: aload_3
    //   152: aconst_null
    //   153: aconst_null
    //   154: aconst_null
    //   155: aload #6
    //   157: invokevirtual validateTwoFactorCode : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lio/fusionauth/domain/EventInfo;)Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult;
    //   160: astore #12
    //   162: aload #12
    //   164: instanceof io/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$Failure
    //   167: ifeq -> 289
    //   170: aload_0
    //   171: aload #9
    //   173: aload_3
    //   174: invokevirtual removeMatchingRecoveryCode : (Lio/fusionauth/domain/User;Ljava/lang/String;)Z
    //   177: istore #13
    //   179: iload #13
    //   181: ifne -> 281
    //   184: aload #4
    //   186: ldc_w 'twoFactorChallenge'
    //   189: invokevirtual getAttributeAsBoolean : (Ljava/lang/String;)Z
    //   192: ifeq -> 212
    //   195: aload_0
    //   196: getfield mfaLifecycleService : Lio/fusionauth/api/service/mfa/MFALifecycleService;
    //   199: aload_2
    //   200: aload #6
    //   202: aload #4
    //   204: aload_1
    //   205: aload #9
    //   207: invokeinterface onFailedAttempt : (Lio/fusionauth/domain/Application;Lio/fusionauth/domain/EventInfo;Lio/fusionauth/api/domain/ExternalIdentifier;Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/User;)V
    //   212: aload_0
    //   213: getfield failedLoginService : Lio/fusionauth/api/service/authentication/FailedLoginService;
    //   216: aload_1
    //   217: aload #9
    //   219: aload #6
    //   221: invokeinterface handleFailedLoginCountExceeded : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/User;Lio/fusionauth/domain/EventInfo;)Lio/fusionauth/domain/UserActionLog;
    //   226: astore #14
    //   228: aload #14
    //   230: ifnull -> 281
    //   233: aload #11
    //   235: new io/fusionauth/api/service/authentication/LoginPreventedException
    //   238: dup
    //   239: aload #14
    //   241: invokespecial <init> : (Lio/fusionauth/domain/UserActionLog;)V
    //   244: putfield exception : Ljava/lang/RuntimeException;
    //   247: aload_0
    //   248: getfield externalIdentifierService : Lio/fusionauth/api/service/user/ExternalIdentifierService;
    //   251: aload #9
    //   253: getfield id : Ljava/util/UUID;
    //   256: iconst_2
    //   257: anewarray io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType
    //   260: dup
    //   261: iconst_0
    //   262: getstatic io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType.TwoFactor : Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;
    //   265: aastore
    //   266: dup
    //   267: iconst_1
    //   268: getstatic io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType.TwoFactorOneTimeCode : Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;
    //   271: aastore
    //   272: invokeinterface deleteByUserId : (Ljava/util/UUID;[Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;)I
    //   277: pop
    //   278: aload #11
    //   280: areturn
    //   281: new io/fusionauth/api/service/user/InvalidTwoFactorCode
    //   284: dup
    //   285: invokespecial <init> : ()V
    //   288: athrow
    //   289: aload #12
    //   291: instanceof io/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$RecoveryCodeSuccess
    //   294: ifne -> 324
    //   297: aload #9
    //   299: getfield twoFactor : Lio/fusionauth/domain/UserTwoFactorConfiguration;
    //   302: invokevirtual getLastUsedMethod : ()Lio/fusionauth/domain/TwoFactorMethod;
    //   305: astore #13
    //   307: aload #13
    //   309: ifnull -> 324
    //   312: aload_0
    //   313: getfield userMapper : Lio/fusionauth/api/domain/UserMapper;
    //   316: aload #9
    //   318: invokeinterface update : (Lio/fusionauth/domain/User;)I
    //   323: pop
    //   324: aload #12
    //   326: instanceof io/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$ExternalIdSuccess
    //   329: ifeq -> 417
    //   332: aload #12
    //   334: checkcast io/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult$ExternalIdSuccess
    //   337: astore #13
    //   339: aload #13
    //   341: invokevirtual id : ()Lio/fusionauth/api/domain/ExternalIdentifier;
    //   344: astore #15
    //   346: aload #15
    //   348: astore #14
    //   350: aload #14
    //   352: ldc_w 'twoFactorId'
    //   355: invokevirtual getAttribute : (Ljava/lang/String;)Ljava/lang/String;
    //   358: astore #15
    //   360: aload #15
    //   362: ifnull -> 417
    //   365: aload #15
    //   367: aload #4
    //   369: getfield id : Ljava/lang/String;
    //   372: invokevirtual equals : (Ljava/lang/Object;)Z
    //   375: ifne -> 417
    //   378: aload_0
    //   379: getfield externalIdentifierService : Lio/fusionauth/api/service/user/ExternalIdentifierService;
    //   382: aload #9
    //   384: getfield id : Ljava/util/UUID;
    //   387: iconst_2
    //   388: anewarray io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType
    //   391: dup
    //   392: iconst_0
    //   393: getstatic io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType.TwoFactor : Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;
    //   396: aastore
    //   397: dup
    //   398: iconst_1
    //   399: getstatic io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType.TwoFactorOneTimeCode : Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;
    //   402: aastore
    //   403: invokeinterface deleteByUserId : (Ljava/util/UUID;[Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;)I
    //   408: pop
    //   409: new io/fusionauth/api/service/user/InvalidTwoFactorCode
    //   412: dup
    //   413: invokespecial <init> : ()V
    //   416: athrow
    //   417: iload #5
    //   419: ifeq -> 434
    //   422: aload_0
    //   423: aload #11
    //   425: aload_1
    //   426: aload_2
    //   427: aload #9
    //   429: aload #4
    //   431: invokevirtual _handleTwoFactorTrust : (Lio/fusionauth/api/service/authentication/AuthenticationService$AuthenticationResult;Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Lio/fusionauth/api/domain/ExternalIdentifier;)V
    //   434: aload #11
    //   436: getfield user : Lio/fusionauth/domain/User;
    //   439: getfield passwordChangeRequired : Z
    //   442: ifeq -> 600
    //   445: aload #12
    //   447: invokeinterface success : ()Z
    //   452: ifeq -> 600
    //   455: new io/fusionauth/api/domain/ExternalIdentifier$ExternalIdData
    //   458: dup
    //   459: invokespecial <init> : ()V
    //   462: ldc_w 'implicitTrust'
    //   465: iconst_1
    //   466: invokevirtual setAttribute : (Ljava/lang/String;Z)Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdData;
    //   469: ldc 'twoFactorTrustId'
    //   471: aload #11
    //   473: getfield twoFactorTrustId : Ljava/lang/String;
    //   476: invokevirtual setAttribute : (Ljava/lang/String;Ljava/lang/Object;)Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdData;
    //   479: ldc_w 'loginId'
    //   482: aload #11
    //   484: getfield userIdentity : Lio/fusionauth/domain/UserIdentity;
    //   487: getfield value : Ljava/lang/String;
    //   490: invokevirtual setAttribute : (Ljava/lang/String;Ljava/lang/Object;)Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdData;
    //   493: ldc_w 'loginIdType'
    //   496: aload #11
    //   498: getfield userIdentity : Lio/fusionauth/domain/UserIdentity;
    //   501: getfield type : Lio/fusionauth/domain/IdentityType;
    //   504: getfield name : Ljava/lang/String;
    //   507: invokevirtual setAttribute : (Ljava/lang/String;Ljava/lang/Object;)Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdData;
    //   510: astore #13
    //   512: aload #11
    //   514: aload_0
    //   515: getfield externalIdentifierService : Lio/fusionauth/api/service/user/ExternalIdentifierService;
    //   518: aload_1
    //   519: aload #9
    //   521: getfield id : Ljava/util/UUID;
    //   524: aconst_null
    //   525: iconst_0
    //   526: aload #13
    //   528: invokeinterface createChangePassword : (Lio/fusionauth/domain/Tenant;Ljava/util/UUID;Ljava/lang/String;ZLio/fusionauth/api/domain/ExternalIdentifier$ExternalIdData;)Ljava/lang/String;
    //   533: putfield changePasswordId : Ljava/lang/String;
    //   536: aload_0
    //   537: getfield externalIdentifierService : Lio/fusionauth/api/service/user/ExternalIdentifierService;
    //   540: aload #9
    //   542: getfield id : Ljava/util/UUID;
    //   545: iconst_2
    //   546: anewarray io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType
    //   549: dup
    //   550: iconst_0
    //   551: getstatic io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType.TwoFactor : Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;
    //   554: aastore
    //   555: dup
    //   556: iconst_1
    //   557: getstatic io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType.TwoFactorOneTimeCode : Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;
    //   560: aastore
    //   561: invokeinterface deleteByUserId : (Ljava/util/UUID;[Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;)I
    //   566: pop
    //   567: aload #4
    //   569: ldc_w 'twoFactorChallenge'
    //   572: invokevirtual getAttributeAsBoolean : (Ljava/lang/String;)Z
    //   575: ifeq -> 597
    //   578: aload_0
    //   579: getfield mfaLifecycleService : Lio/fusionauth/api/service/mfa/MFALifecycleService;
    //   582: aload_2
    //   583: aload #6
    //   585: aload #4
    //   587: aload_1
    //   588: aload #12
    //   590: aload #9
    //   592: invokeinterface onSuccess : (Lio/fusionauth/domain/Application;Lio/fusionauth/domain/EventInfo;Lio/fusionauth/api/domain/ExternalIdentifier;Lio/fusionauth/domain/Tenant;Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult;Lio/fusionauth/domain/User;)V
    //   597: aload #11
    //   599: areturn
    //   600: new io/fusionauth/api/domain/ExternalIdentifier$ExternalIdData
    //   603: dup
    //   604: ldc 'twoFactorTrustId'
    //   606: aload #11
    //   608: getfield twoFactorTrustId : Ljava/lang/String;
    //   611: invokespecial <init> : (Ljava/lang/String;Ljava/lang/String;)V
    //   614: astore #13
    //   616: aload #4
    //   618: ldc_w 'trustChallenge'
    //   621: invokevirtual getAttribute : (Ljava/lang/String;)Ljava/lang/String;
    //   624: astore #14
    //   626: aload #14
    //   628: ifnull -> 642
    //   631: aload #13
    //   633: ldc_w 'trustChallenge'
    //   636: aload #14
    //   638: invokevirtual setAttribute : (Ljava/lang/String;Ljava/lang/Object;)Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdData;
    //   641: pop
    //   642: aload #11
    //   644: aload_0
    //   645: getfield externalIdentifierService : Lio/fusionauth/api/service/user/ExternalIdentifierService;
    //   648: aload_1
    //   649: aload #7
    //   651: aload #9
    //   653: getfield id : Ljava/util/UUID;
    //   656: aload #13
    //   658: invokeinterface createTrustToken : (Lio/fusionauth/domain/Tenant;Ljava/util/UUID;Ljava/util/UUID;Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdData;)Ljava/lang/String;
    //   663: putfield trustToken : Ljava/lang/String;
    //   666: aload_0
    //   667: getfield externalIdentifierService : Lio/fusionauth/api/service/user/ExternalIdentifierService;
    //   670: aload #9
    //   672: getfield id : Ljava/util/UUID;
    //   675: iconst_2
    //   676: anewarray io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType
    //   679: dup
    //   680: iconst_0
    //   681: getstatic io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType.TwoFactor : Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;
    //   684: aastore
    //   685: dup
    //   686: iconst_1
    //   687: getstatic io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType.TwoFactorOneTimeCode : Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;
    //   690: aastore
    //   691: invokeinterface deleteByUserId : (Ljava/util/UUID;[Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;)I
    //   696: pop
    //   697: aload_0
    //   698: getfield lambdaInvocationService : Lio/fusionauth/api/service/lambda/LambdaInvocationService;
    //   701: aload_0
    //   702: getfield failedLoginService : Lio/fusionauth/api/service/authentication/FailedLoginService;
    //   705: aload_1
    //   706: aload_2
    //   707: aload #11
    //   709: new io/fusionauth/api/service/authentication/AuthenticationService$LoginLambdaValidationContext
    //   712: dup
    //   713: aload #11
    //   715: getfield type : Lio/fusionauth/api/service/authentication/AuthenticationType;
    //   718: invokespecial <init> : (Lio/fusionauth/api/service/authentication/AuthenticationType;)V
    //   721: aload #6
    //   723: invokestatic applyLoginValidationLambda : (Lio/fusionauth/api/service/lambda/LambdaInvocationService;Lio/fusionauth/api/service/authentication/FailedLoginService;Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/api/service/authentication/AuthenticationService$AuthenticationResult;Lio/fusionauth/api/service/authentication/AuthenticationService$LoginLambdaValidationContext;Lio/fusionauth/domain/EventInfo;)V
    //   726: aload #11
    //   728: getfield loginLambdaValidationResult : Lio/fusionauth/api/service/authentication/AuthenticationService$LoginLambdaValidationResult;
    //   731: getfield errors : Lcom/inversoft/error/Errors;
    //   734: invokevirtual empty : ()Z
    //   737: ifne -> 743
    //   740: aload #11
    //   742: areturn
    //   743: aload #4
    //   745: ldc_w 'twoFactorChallenge'
    //   748: invokevirtual getAttributeAsBoolean : (Ljava/lang/String;)Z
    //   751: ifeq -> 773
    //   754: aload_0
    //   755: getfield mfaLifecycleService : Lio/fusionauth/api/service/mfa/MFALifecycleService;
    //   758: aload_2
    //   759: aload #6
    //   761: aload #4
    //   763: aload_1
    //   764: aload #12
    //   766: aload #9
    //   768: invokeinterface onSuccess : (Lio/fusionauth/domain/Application;Lio/fusionauth/domain/EventInfo;Lio/fusionauth/api/domain/ExternalIdentifier;Lio/fusionauth/domain/Tenant;Lio/fusionauth/api/service/authentication/AuthenticationService$TwoFactorValidationResult;Lio/fusionauth/domain/User;)V
    //   773: aload_0
    //   774: aload_1
    //   775: aload_2
    //   776: aload #9
    //   778: aload #11
    //   780: iconst_1
    //   781: aload #6
    //   783: invokevirtual handleUpdatesAndSendLoginSuccessEvent : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Lio/fusionauth/api/service/authentication/AuthenticationService$AuthenticationResult;ZLio/fusionauth/domain/EventInfo;)V
    //   786: aload #11
    //   788: areturn
    //   789: astore #15
    //   791: new java/lang/MatchException
    //   794: dup
    //   795: aload #15
    //   797: invokevirtual toString : ()Ljava/lang/String;
    //   800: aload #15
    //   802: invokespecial <init> : (Ljava/lang/String;Ljava/lang/Throwable;)V
    //   805: athrow
    // Line number table:
    //   Java source line number -> byte code offset
    //   #383	-> 0
    //   #384	-> 14
    //   #385	-> 43
    //   #386	-> 50
    //   #387	-> 55
    //   #391	-> 57
    //   #392	-> 68
    //   #393	-> 82
    //   #395	-> 87
    //   #396	-> 103
    //   #398	-> 110
    //   #399	-> 123
    //   #400	-> 133
    //   #403	-> 141
    //   #405	-> 162
    //   #406	-> 170
    //   #407	-> 179
    //   #408	-> 184
    //   #409	-> 195
    //   #412	-> 212
    //   #413	-> 228
    //   #414	-> 233
    //   #416	-> 247
    //   #419	-> 278
    //   #423	-> 281
    //   #427	-> 289
    //   #428	-> 297
    //   #429	-> 307
    //   #430	-> 312
    //   #436	-> 324
    //   #437	-> 350
    //   #438	-> 360
    //   #439	-> 378
    //   #440	-> 409
    //   #445	-> 417
    //   #446	-> 422
    //   #450	-> 434
    //   #452	-> 455
    //   #453	-> 466
    //   #454	-> 476
    //   #455	-> 490
    //   #456	-> 507
    //   #457	-> 512
    //   #458	-> 536
    //   #460	-> 567
    //   #461	-> 578
    //   #464	-> 597
    //   #472	-> 600
    //   #473	-> 616
    //   #474	-> 626
    //   #475	-> 631
    //   #478	-> 642
    //   #481	-> 666
    //   #484	-> 697
    //   #485	-> 726
    //   #486	-> 740
    //   #489	-> 743
    //   #490	-> 754
    //   #492	-> 773
    //   #493	-> 786
    //   #436	-> 789
    // Exception table:
    //   from	to	target	type
    //   341	344	789	java/lang/Throwable
  }
  
  public AuthenticationService.AuthenticationResult authenticateWebauthn(Tenant paramTenant, Application paramApplication, User paramUser, WebAuthnCredential paramWebAuthnCredential, ExternalIdentifier paramExternalIdentifier1, ExternalIdentifier paramExternalIdentifier2, byte[] paramArrayOfbyte, PublicKeyCredential<AuthenticatorAssertionResponse> paramPublicKeyCredential, EventInfo paramEventInfo, Double paramDouble, boolean paramBoolean) {
    User user = (validateUserForAuthentication(paramTenant, paramApplication.id, paramExternalIdentifier1, paramUser)).user;
    if (user == null)
      return null; 
    WebAuthnCredential webAuthnCredential = this.webauthnProviderService.completeAssertion(paramTenant, user, paramWebAuthnCredential, paramPublicKeyCredential, paramArrayOfbyte);
    if (webAuthnCredential == null)
      return null; 
    AuthenticationService.AuthenticationResult authenticationResult = new AuthenticationService.AuthenticationResult(AuthenticationType.WebAuthn, BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID, user);
    authenticationResult.externalIdentifier = paramExternalIdentifier1;
    authenticationResult.userIdentity = resolveLoginIdentity(user, authenticationResult.externalIdentifier);
    boolean bool = checkMFAChallenge(paramTenant, paramApplication, paramUser, authenticationResult, paramExternalIdentifier1, paramExternalIdentifier2, paramEventInfo, paramDouble, paramBoolean);
    if (bool)
      return authenticationResult; 
    applyLoginValidationLambda(this.lambdaInvocationService, this.failedLoginService, paramTenant, paramApplication, authenticationResult, new AuthenticationService.LoginLambdaValidationContext(authenticationResult.type), paramEventInfo);
    if (!authenticationResult.loginLambdaValidationResult.errors.empty())
      return authenticationResult; 
    handleUpdatesAndSendLoginSuccessEvent(paramTenant, paramApplication, user, authenticationResult, true, paramEventInfo);
    return authenticationResult;
  }
  
  public AuthenticationService.AuthenticationResult ping(Tenant paramTenant, User paramUser, Application paramApplication, EventInfo paramEventInfo) {
    AuthenticationService.AuthenticationResult authenticationResult = new AuthenticationService.AuthenticationResult(AuthenticationType.PING, BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID, paramUser);
    authenticationResult.userIdentity = paramUser.resolveFirstIdentity();
    handleUpdatesAndSendLoginSuccessEvent(paramTenant, paramApplication, paramUser, authenticationResult, true, paramEventInfo);
    return authenticationResult;
  }
  
  public List<LoginPreventedResponse> retrieveLoginPreventedActions(User paramUser, UUID paramUUID) {
    List<UserActionLog> list = this.userActionService.retrieveAllCurrentPreventLoginActionLogsForUser(paramUser.id);
    if (list.isEmpty())
      return Collections.emptyList(); 
    return (List<LoginPreventedResponse>)list.stream()
      .filter(paramUserActionLog -> (paramUserActionLog.applicationIds.isEmpty() || (paramUUID != null && paramUserActionLog.applicationIds.contains(paramUUID))))
      .map(LoginPreventedResponse::new).collect(Collectors.toList());
  }
  
  public AuthenticationService.ValidationResult validateAuthenticate(Tenant paramTenant, UUID paramUUID, String paramString1, String paramString2, List<String> paramList, Double paramDouble) {
    AuthenticationService.ValidationResult validationResult = new AuthenticationService.ValidationResult();
    Objects.requireNonNull(this.applicationReader);
    validationResult.application = (paramUUID != null) ? this.applicationCache.get((paramTenant != null) ? paramTenant.id : null, paramUUID, this.applicationReader::retrieveById) : null;
    validationResult.tenant = this.tenantCache.resolve(paramTenant, new Tenantable[] { validationResult.application });
    validationResult.identityTypes = IdentityTypeHelper.convert(paramList);
    validationResult












      
      .errors = (new Validator()).ifTrue((paramUUID != null), paramValidator -> paramValidator.validObject(paramValidationResult.application, "applicationId", new Object[] { paramUUID })).ifTrue((validationResult.tenant != null), paramValidator -> paramValidator.ifFalse(ReactorStatusValidator.isLicensedFor(this.reactorStatusService.retrieveStatus(), ()), ())).notBlank(paramString1, "loginId", new Object[0]).notBlank(paramString2, "password", new Object[0]).validate(paramValidator -> IdentityTypeValidator.validate(paramValidator, paramList, "loginIdTypes")).validate(paramValidator -> BotDetectionScoreValidator.validate(paramValidator, paramDouble, "botDetectionScore")).done();
    return validationResult;
  }
  
  public AuthenticationService.ValidationResult validateOneTimePassword(Tenant paramTenant, UUID paramUUID, ExternalIdentifier paramExternalIdentifier) {
    AuthenticationService.ValidationResult validationResult = new AuthenticationService.ValidationResult();
    Objects.requireNonNull(this.applicationReader);
    validationResult.application = (paramUUID != null) ? this.applicationCache.get((paramTenant == null) ? null : paramTenant.id, paramUUID, this.applicationReader::retrieveById) : null;
    validationResult.tenant = this.tenantCache.resolve(paramTenant, new Tenantable[] { validationResult.application });
    validationResult
      
      .errors = (new Validator()).ifTrue((paramUUID != null), paramValidator -> paramValidator.validObject(paramValidationResult.application, "applicationId", new Object[] { paramUUID })).validObject(paramExternalIdentifier, "oneTimePassword", new Object[0]).done();
    return validationResult;
  }
  
  public Errors validatePasswordless(String paramString1, String paramString2, ExternalIdentifier paramExternalIdentifier) {
    Errors errors = (new Validator()).notBlank(paramString1, "code", new Object[0]).done();
    if (!errors.empty())
      return errors; 
    if (paramExternalIdentifier == null || paramExternalIdentifier.data == null)
      return errors; 
    String str = paramExternalIdentifier.data.getAttribute("otp");
    if (str != null)
      errors = (new Validator()).notBlank(paramString2, "oneTimeCode", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(paramString1.equals(paramString2), "code", "[invalid]", new Object[0])).done(); 
    return errors;
  }
  
  public AuthenticationService.ValidationResult validatePing(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2) {
    AuthenticationService.ValidationResult validationResult = new AuthenticationService.ValidationResult();
    validationResult.application = this.applicationReader.retrieveById(TenantService.optionalTenantId(paramTenant), paramUUID2);
    validationResult.user = this.userReader.retrieveById(TenantService.optionalTenantId(paramTenant), paramUUID1);
    validationResult.tenant = this.tenantCache.resolve(paramTenant, new Tenantable[] { validationResult.application, validationResult.user });
    validationResult
      
      .errors = (new Validator()).ifTrue((paramUUID2 != null), paramValidator -> paramValidator.validObject(paramValidationResult.application, "applicationId", new Object[] { paramUUID })).notMissing(paramUUID1, "userId", new Object[0]).done();
    return validationResult;
  }
  
  public AuthenticationService.ValidationResult validateTwoFactor(Tenant paramTenant, UUID paramUUID, String paramString1, String paramString2) {
    AuthenticationService.ValidationResult validationResult = new AuthenticationService.ValidationResult();
    validationResult.application = (paramUUID != null) ? this.applicationCache.get((paramTenant != null) ? paramTenant.id : null, paramUUID) : null;
    validationResult.tenant = this.tenantCache.resolve(paramTenant, new Tenantable[] { validationResult.application });
    validationResult

      
      .errors = (new Validator()).notBlank(paramString1, "code", new Object[0]).ifTrue((paramUUID != null), paramValidator -> paramValidator.validObject(paramValidationResult.application, "applicationId", new Object[] { paramUUID })).notBlank(paramString2, "twoFactorId", new Object[0]).done();
    return validationResult;
  }
  
  public AuthenticationService.TwoFactorValidationResult validateTwoFactorCode(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, EventInfo paramEventInfo) {
    List<TwoFactorMethod> list = paramUser.twoFactor.methods.stream().filter(paramTwoFactorMethod -> paramTwoFactorMethod.method.equals("authenticator")).toList();
    TwoFactorValidation twoFactorValidation = new TwoFactorValidation(paramTenant, paramApplication, paramUser, paramString1, paramString2, paramString3, paramString4, paramString5, paramEventInfo, list);
    if (!twoFactorValidation.hasCode() || twoFactorValidation.authenticatorWithNothingToCheck())
      return new AuthenticationService.TwoFactorValidationResult.Failure(); 
    if (twoFactorValidation.tryAuthenticator()) {
      AuthenticationService.TwoFactorValidationResult twoFactorValidationResult = validateAuthenticator(twoFactorValidation);
      if (!(twoFactorValidationResult instanceof AuthenticationService.TwoFactorValidationResult.Failure) || twoFactorValidation.hasMethod())
        return twoFactorValidationResult; 
    } 
    return validateExternalIdentifier(twoFactorValidation);
  }
  
  private AuthenticationService.AuthenticationResult authenticateUser(Tenant paramTenant, Application paramApplication, String paramString1, List<IdentityType> paramList, String paramString2, ExternalIdentifier paramExternalIdentifier, EventInfo paramEventInfo, Double paramDouble, boolean paramBoolean) {
    UUID uUID = (paramApplication != null) ? paramApplication.id : null;
    AuthenticationService.AuthenticationResult authenticationResult = null;
    User user = (validateUserForAuthentication(paramTenant, uUID, null, this.userReader.retrieveByLoginId(paramTenant.id, paramString1, paramList))).user;
    boolean bool = true;
    if (user != null) {
      BaseConnectorConfiguration baseConnectorConfiguration = this.connectorConfigurationService.retrieveById(user.connectorId);
      if (baseConnectorConfiguration != null) {
        ConnectorPolicy connectorPolicy = paramTenant.getPolicyByConnectorId(user.connectorId);
        if (connectorPolicy != null) {
          bool = false;
          Connector connector1 = this.connectors.get(baseConnectorConfiguration.getType());
          authenticationResult = connector1.authenticate(baseConnectorConfiguration, connectorPolicy, paramTenant, paramApplication, user, paramString1, paramList, paramString2, paramEventInfo);
        } 
      } 
    } 
    if (bool)
      for (ConnectorPolicy connectorPolicy : paramTenant.connectorPolicies) {
        BaseConnectorConfiguration baseConnectorConfiguration = this.connectorConfigurationService.retrieveById(connectorPolicy.connectorId);
        Connector connector1 = this.connectors.get(baseConnectorConfiguration.getType());
        authenticationResult = connector1.authenticate(baseConnectorConfiguration, connectorPolicy, paramTenant, paramApplication, user, paramString1, paramList, paramString2, paramEventInfo);
        if (authenticationResult != null)
          break; 
      }  
    if (authenticationResult == null || authenticationResult.user == null || authenticationResult.exception != null)
      return authenticationResult; 
    Connector connector = authenticationResult.connector;
    if (connector instanceof ExternalConnector) {
      ExternalConnector externalConnector = (ExternalConnector)connector;
      authenticationResult = externalConnector.synchronizeExternalUser(paramTenant, paramApplication, authenticationResult, paramString1, paramList, paramString2, paramEventInfo);
    } else {
      authenticationResult.userIdentity = IdentityHelper.resolveIdentity(authenticationResult.user, paramString1, paramList);
    } 
    if (authenticationResult == null || authenticationResult.user == null || authenticationResult.exception != null)
      return authenticationResult; 
    boolean bool1 = checkMFAChallenge(paramTenant, paramApplication, authenticationResult.user, authenticationResult, null, paramExternalIdentifier, paramEventInfo, paramDouble, paramBoolean);
    if (bool1)
      return authenticationResult; 
    if (authenticationResult.user.passwordChangeRequired) {
      authenticationResult.changePasswordId = this.externalIdentifierService.createChangePassword(paramTenant, authenticationResult.user.id, null, false, null);
      return authenticationResult;
    } 
    this.externalIdentifierService.deleteByUserId(authenticationResult.user.id, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.ChangePassword, ExternalIdentifier.ExternalIdType.SetupPassword, ExternalIdentifier.ExternalIdType.OneTimePassword });
    applyLoginValidationLambda(this.lambdaInvocationService, this.failedLoginService, paramTenant, paramApplication, authenticationResult, new AuthenticationService.LoginLambdaValidationContext(authenticationResult.type), paramEventInfo);
    if (!authenticationResult.loginLambdaValidationResult.errors.empty())
      return authenticationResult; 
    handleUpdatesAndSendLoginSuccessEvent(paramTenant, paramApplication, authenticationResult.user, authenticationResult, false, paramEventInfo);
    return authenticationResult;
  }
  
  private boolean checkMFAChallenge(Tenant paramTenant, Application paramApplication, User paramUser, AuthenticationService.AuthenticationResult paramAuthenticationResult, ExternalIdentifier paramExternalIdentifier1, ExternalIdentifier paramExternalIdentifier2, EventInfo paramEventInfo, Double paramDouble, boolean paramBoolean) {
    String str1 = (paramEventInfo == null) ? null : paramEventInfo.ipAddress;
    String str2 = (paramEventInfo == null) ? null : paramEventInfo.userAgent;
    RiskSignalContext riskSignalContext = new RiskSignalContext(paramUser, str1, str2, paramExternalIdentifier2, paramDouble, paramBoolean, paramTenant.clientRiskConfiguration);
    paramAuthenticationResult.clientRisk = this.riskSignalService.computeClientRisk(riskSignalContext);
    MFAService.ProcessLoginResult processLoginResult = this.mfaService.processLogin(paramTenant, paramApplication, paramExternalIdentifier1, paramUser, paramAuthenticationResult, paramExternalIdentifier2, paramEventInfo);
    if (paramAuthenticationResult.clientRisk.isSuspicious() || processLoginResult.sendSuspiciousLoginEvent()) {
      UUID uUID = (paramApplication != null) ? paramApplication.id : null;
      EventHelper.send(paramTenant, paramApplication, new UserLoginSuspiciousEvent(paramEventInfo, uUID, paramAuthenticationResult.connectorId, paramAuthenticationResult.type.name(), paramAuthenticationResult.user, paramAuthenticationResult.clientRisk
            .getHighRiskAuthenticationThreats()));
    } 
    return processLoginResult.required();
  }
  
  private boolean checkRecoveryCode(User paramUser, String paramString) {
    if (removeMatchingRecoveryCode(paramUser, paramString)) {
      this.userMapper.updateRecoveryCodes(paramUser);
      return true;
    } 
    return false;
  }
  
  private void handleAddingVerificationIdToResponse(Tenant paramTenant, Application paramApplication, AuthenticationService.AuthenticationResult paramAuthenticationResult) {
    if (paramApplication != null) {
      UserRegistration userRegistration = paramAuthenticationResult.user.getRegistrationForApplication(paramApplication.id);
      if (userRegistration != null && !userRegistration.verified && paramApplication.verifyRegistration && paramApplication.verificationStrategy == VerificationStrategy.FormField) {
        ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveByUserId(paramAuthenticationResult.user.id, ExternalIdentifier.ExternalIdType.RegistrationVerification);
        if (externalIdentifier != null && !externalIdentifier.isExpired(paramTenant) && externalIdentifier.getAttribute("otp") != null)
          paramAuthenticationResult.registrationVerificationId = externalIdentifier.id; 
      } 
    } 
    boolean bool1 = (paramTenant.emailConfiguration.verifyEmail && paramTenant.emailConfiguration.verificationStrategy == VerificationStrategy.FormField) ? true : false;
    boolean bool2 = (paramTenant.phoneConfiguration.verifyPhoneNumber && paramTenant.phoneConfiguration.verificationStrategy == VerificationStrategy.FormField) ? true : false;
    if ((bool1 || bool2) && paramAuthenticationResult.user.identities
      .stream().anyMatch(UserIdentity::verificationRequired)) {
      List<ExternalIdentifier> list = this.externalIdentifierReader.retrieveAllByUserId(paramAuthenticationResult.user.id, IdentityExternalIdHelper.identityExternalIdTypes);
      Map<UserIdentity, ExternalIdentifier> map = IdentityExternalIdHelper.groupActiveIdentifiersByIdentity(list, paramAuthenticationResult.user, paramTenant);
      map.forEach((paramUserIdentity, paramExternalIdentifier) -> {
            if (paramExternalIdentifier.getAttribute("otp") == null)
              return; 
            if (paramBoolean1 && paramUserIdentity.type.is(IdentityType.email)) {
              if (paramUserIdentity.primary)
                paramAuthenticationResult.emailVerificationId = paramExternalIdentifier.id; 
              if (paramUserIdentity.equals(paramAuthenticationResult.userIdentity))
                paramAuthenticationResult.identityVerificationId = paramExternalIdentifier.id; 
            } 
            if (paramBoolean2 && paramUserIdentity.type.is(IdentityType.phoneNumber))
              if (paramUserIdentity.equals(paramAuthenticationResult.userIdentity))
                paramAuthenticationResult.identityVerificationId = paramExternalIdentifier.id;  
          });
    } 
  }
  
  private AuthenticationService.TwoFactorValidationResult handleExternalIdentifierMiss(TwoFactorValidation paramTwoFactorValidation) {
    if (paramTwoFactorValidation.disableTwoFactor() && 
      checkRecoveryCode(paramTwoFactorValidation.user(), paramTwoFactorValidation.code()))
      return new AuthenticationService.TwoFactorValidationResult.RecoveryCodeSuccess(); 
    return new AuthenticationService.TwoFactorValidationResult.Failure();
  }
  
  private AuthenticationService.TwoFactorValidationResult handleExternalIdentifierSuccess(TwoFactorValidation paramTwoFactorValidation, ExternalIdentifier paramExternalIdentifier) {
    handleMFAImplicitVerification(paramTwoFactorValidation.tenant(), paramTwoFactorValidation.application(), paramTwoFactorValidation.user(), paramTwoFactorValidation.eventInfo(), paramTwoFactorValidation
        .loginOrStepUp(), paramExternalIdentifier);
    markLastUsedMethod(paramTwoFactorValidation.user(), paramExternalIdentifier.getAttribute("methodId"));
    return new AuthenticationService.TwoFactorValidationResult.ExternalIdSuccess(paramExternalIdentifier);
  }
  
  private void handleMFAImplicitVerification(Tenant paramTenant, Application paramApplication, User paramUser, EventInfo paramEventInfo, boolean paramBoolean, ExternalIdentifier paramExternalIdentifier) {
    Predicate predicate;
    if (!paramBoolean)
      return; 
    TwoFactorMethod twoFactorMethod = paramUser.twoFactor.getMethodById(paramExternalIdentifier.getAttribute("methodId"));
    if (twoFactorMethod == null)
      return; 
    if (twoFactorMethod.method.equals("email")) {
      predicate = (paramUserIdentity -> (paramUserIdentity.type.is(IdentityType.email) && paramUserIdentity.value.equals(paramTwoFactorMethod.email)));
    } else if (twoFactorMethod.method.equals("sms")) {
      predicate = (paramUserIdentity -> (paramUserIdentity.type.is(IdentityType.phoneNumber) && paramUserIdentity.value.equals(IdentityHelper.canonicalizeValue(paramTwoFactorMethod.mobilePhone, IdentityType.phoneNumber))));
    } else {
      predicate = null;
    } 
    if (predicate == null)
      return; 
    Optional optional = paramUser.identities.stream().filter(predicate).findFirst();
    optional.ifPresent(paramUserIdentity -> {
          if (this.userService.handleImplicitVerification(paramTenant, paramApplication, paramUser, paramUserIdentity, paramEventInfo))
            this.userService.reindexUser(paramUser, null); 
        });
  }
  
  private void handleUpdateLoginInstants(AuthenticationService.AuthenticationResult paramAuthenticationResult, Application paramApplication, User paramUser, EventInfo paramEventInfo) {
    UUID uUID = (paramApplication != null) ? paramApplication.id : null;
    paramAuthenticationResult.authenticatedNotRegistered = (uUID != null && paramUser.getRegistrationForApplication(uUID) == null);
    ZonedDateTime zonedDateTime = ZonedDateTimeWrapper.now(ZoneOffset.UTC);
    paramAuthenticationResult.rawLogin = this.userMetricsService.buildRawLogin(paramUser, paramAuthenticationResult.userIdentity, zonedDateTime, uUID, paramEventInfo);
  }
  
  private void handleUpdatesAndSendLoginSuccessEvent(Tenant paramTenant, Application paramApplication, User paramUser, AuthenticationService.AuthenticationResult paramAuthenticationResult, boolean paramBoolean, EventInfo paramEventInfo) {
    handleAddingVerificationIdToResponse(paramTenant, paramApplication, paramAuthenticationResult);
    handleUpdateLoginInstants(paramAuthenticationResult, paramApplication, paramUser, paramEventInfo);
    UUID uUID1 = (paramApplication != null) ? paramApplication.id : null;
    UUID uUID2 = paramAuthenticationResult.authenticatedNotRegistered ? null : uUID1;
    EventHelper.send(paramTenant, paramApplication, new UserLoginSuccessEvent(paramEventInfo, uUID2, paramAuthenticationResult.connectorId, paramAuthenticationResult.type
          .name(), paramUser));
    if (paramBoolean)
      this.userMetricsService.addToLoginQueue(paramAuthenticationResult.rawLogin); 
  }
  
  private void markLastUsedMethod(User paramUser, String paramString) {
    for (TwoFactorMethod twoFactorMethod : paramUser.twoFactor.methods)
      twoFactorMethod.lastUsed = twoFactorMethod.id.equals(paramString) ? Boolean.valueOf(true) : null; 
  }
  
  private boolean removeHashedRecoveryCode(User paramUser, String paramString) {
    String str1 = paramUser.twoFactor.recoveryCodeEncryptionScheme;
    int i = paramUser.twoFactor.recoveryCodeWorkFactor.intValue();
    String str2 = this.recoveryCodeHasher.normalizeRecoveryCode(paramString);
    Objects.requireNonNull(this.recoveryCodeHasher);
    return ((Boolean)paramUser.twoFactor.recoveryCodes.stream().filter(Objects::nonNull).findFirst().flatMap(this.recoveryCodeHasher::extractSalt)
      .map(paramString4 -> Boolean.valueOf(removeMatchingHashedCode(paramUser, paramString1, paramString2, paramString3, paramInt, paramString4)))
      .orElse(Boolean.valueOf(false))).booleanValue();
  }
  
  private boolean removeMatchingHashedCode(User paramUser, String paramString1, String paramString2, String paramString3, int paramInt, String paramString4) {
    Optional<String> optional1 = this.recoveryCodeHasher.hashOnce(paramString2, paramString4, paramString3, paramInt);
    Optional<String> optional2 = this.recoveryCodeHasher.hashOnce(paramString1, paramString4, paramString3, paramInt);
    return paramUser.twoFactor.recoveryCodes
      .removeIf(paramString5 -> ((Boolean)this.recoveryCodeHasher.extractSalt(paramString5).<Boolean>map(()).orElse(Boolean.valueOf(false))).booleanValue());
  }
  
  private boolean removeMatchingRecoveryCode(User paramUser, String paramString) {
    if (paramUser.twoFactor.recoveryCodeEncryptionScheme != null)
      return removeHashedRecoveryCode(paramUser, paramString); 
    return (paramUser.twoFactor.recoveryCodes.remove(this.recoveryCodeHasher.normalizeRecoveryCode(paramString)) || paramUser.twoFactor.recoveryCodes.remove(paramString));
  }
  
  private Optional<ExternalIdentifier> resolveExternalIdentifier(TwoFactorValidation paramTwoFactorValidation) {
    if (!paramTwoFactorValidation.loginOrStepUp())
      return Optional.<ExternalIdentifierReaderService.ValidationResult>ofNullable(this.externalIdentifierReader.validate(paramTwoFactorValidation.tenant(), paramTwoFactorValidation.code(), new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.TwoFactorOneTimeCode })).map(paramValidationResult -> paramValidationResult.id); 
    return Optional.<ExternalIdentifierReaderService.ValidationResult>ofNullable(this.externalIdentifierReader.validate(paramTwoFactorValidation.tenant(), paramTwoFactorValidation.twoFactorId(), new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.TwoFactor })).map(paramValidationResult -> paramValidationResult.id)
      .filter(paramExternalIdentifier -> paramTwoFactorValidation.code().equals(paramExternalIdentifier.getAttribute("otp")));
  }
  
  private UserIdentity resolveLoginIdentity(User paramUser, ExternalIdentifier paramExternalIdentifier) {
    String str = IdentityExternalIdHelper.getLoginId(paramExternalIdentifier);
    IdentityType identityType = IdentityExternalIdHelper.getLoginIdentityType(paramExternalIdentifier);
    if (str == null || identityType == null)
      return paramUser.resolveFirstIdentity(); 
    return IdentityHelper.resolveIdentity(paramUser, str, identityType);
  }
  
  private void slowItDown(Tenant paramTenant) {
    PasswordEncryptor passwordEncryptor = this.passwordEncryptorLibrary.lookup(paramTenant.passwordEncryptionConfiguration.encryptionScheme);
    passwordEncryptor.encrypt("enumerate enumerate", passwordEncryptor.generateSalt(), passwordEncryptor.defaultFactor());
  }
  
  private AuthenticationService.TwoFactorValidationResult validateAuthenticator(TwoFactorValidation paramTwoFactorValidation) {
    if (paramTwoFactorValidation.useSecret()) {
      if (validateTOTP(null, paramTwoFactorValidation.code(), paramTwoFactorValidation.secret()))
        return new AuthenticationService.TwoFactorValidationResult.TOTPSuccess(); 
      return new AuthenticationService.TwoFactorValidationResult.Failure();
    } 
    return paramTwoFactorValidation
      .authenticators()
      .stream()
      .filter(paramTwoFactorMethod -> (paramTwoFactorValidation.methodId() == null || paramTwoFactorValidation.methodId().equals(paramTwoFactorMethod.id)))
      .filter(paramTwoFactorMethod -> validateTOTP(paramTwoFactorMethod.authenticator, paramTwoFactorValidation.code(), paramTwoFactorMethod.secret))
      .findFirst()
      .map(paramTwoFactorMethod -> {
          markLastUsedMethod(paramTwoFactorValidation.user(), paramTwoFactorMethod.id);
          return new AuthenticationService.TwoFactorValidationResult.TOTPSuccess();
        }).orElseGet(() -> checkRecoveryCode(paramTwoFactorValidation.user(), paramTwoFactorValidation.code()) ? new AuthenticationService.TwoFactorValidationResult.RecoveryCodeSuccess() : new AuthenticationService.TwoFactorValidationResult.Failure());
  }
  
  private AuthenticationService.TwoFactorValidationResult validateExternalIdentifier(TwoFactorValidation paramTwoFactorValidation) {
    return resolveExternalIdentifier(paramTwoFactorValidation)
      .<AuthenticationService.TwoFactorValidationResult>map(paramExternalIdentifier -> handleExternalIdentifierSuccess(paramTwoFactorValidation, paramExternalIdentifier))
      .orElseGet(() -> handleExternalIdentifierMiss(paramTwoFactorValidation));
  }
  
  private boolean validateTOTP(AuthenticatorConfiguration paramAuthenticatorConfiguration, String paramString1, String paramString2) {
    if (paramAuthenticatorConfiguration == null) {
      paramAuthenticatorConfiguration = new AuthenticatorConfiguration();
      paramAuthenticatorConfiguration.algorithm = AuthenticatorConfiguration.TOTPAlgorithm.HmacSHA1;
      paramAuthenticatorConfiguration.codeLength = 6;
      paramAuthenticatorConfiguration.timeStep = 30;
    } 
    Algorithm algorithm = Algorithm.valueOf(paramAuthenticatorConfiguration.algorithm.name());
    long l = TwoFactor.getCurrentWindowInstant(paramAuthenticatorConfiguration.timeStep);
    byte[] arrayOfByte = Base64.getDecoder().decode(paramString2);
    return (TwoFactor.validateVerificationCode(arrayOfByte, l, paramString1, algorithm, paramAuthenticatorConfiguration.codeLength) || 
      TwoFactor.validateVerificationCode(arrayOfByte, l - 1L, paramString1, algorithm, paramAuthenticatorConfiguration.codeLength) || 
      TwoFactor.validateVerificationCode(arrayOfByte, l + 1L, paramString1, algorithm, paramAuthenticatorConfiguration.codeLength));
  }
  
  private UserAuthenticationValidationResult validateUserForAuthentication(Tenant paramTenant, UUID paramUUID, ExternalIdentifier paramExternalIdentifier, User paramUser) {
    if (paramUser == null) {
      slowItDown(paramTenant);
      return new UserAuthenticationValidationResult(null);
    } 
    if (!paramUser.active) {
      slowItDown(paramTenant);
      throw new UserLockedException();
    } 
    if (paramUUID == null && paramExternalIdentifier != null)
      paramUUID = paramExternalIdentifier.applicationId; 
    List<LoginPreventedResponse> list = retrieveLoginPreventedActions(paramUser, paramUUID);
    boolean bool = true;
    if (paramTenant.failedAuthenticationConfiguration.actionCancelPolicy.onPasswordReset && list
      .size() == 1 && ((LoginPreventedResponse)list
      .getFirst()).actionId.equals(paramTenant.failedAuthenticationConfiguration.userActionId) && paramExternalIdentifier != null && paramExternalIdentifier.type == ExternalIdentifier.ExternalIdType.TwoFactor) {
      ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveByUserId(paramUser.id, ExternalIdentifier.ExternalIdType.ChangePassword);
      if (externalIdentifier != null && !externalIdentifier.isExpired(paramTenant))
        bool = false; 
    } 
    if (bool && 
      !list.isEmpty()) {
      slowItDown(paramTenant);
      throw new LoginPreventedException(list);
    } 
    if (this.rateLimitService.isRateLimited(paramTenant, RateLimitedRequestType.FailedLogin, paramUser.id.toString())) {
      slowItDown(paramTenant);
      return new UserAuthenticationValidationResult(null);
    } 
    if (paramUser.expiry != null && paramUser.expiry.isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
      slowItDown(paramTenant);
      throw new UserExpiredException();
    } 
    return new UserAuthenticationValidationResult(paramUser, !bool);
  }
  
  static class UserAuthenticationValidationResult {
    public boolean trustOnly;
    
    public User user;
    
    public UserAuthenticationValidationResult(User param1User) {
      this.user = param1User;
    }
    
    public UserAuthenticationValidationResult(User param1User, boolean param1Boolean) {
      this.trustOnly = param1Boolean;
      this.user = param1User;
    }
  }
  
  private static final class TwoFactorValidation extends Record {
    private final Tenant tenant;
    
    private final Application application;
    
    private final User user;
    
    private final String twoFactorId;
    
    private final String code;
    
    private final String method;
    
    private final String methodId;
    
    private final String secret;
    
    private final EventInfo eventInfo;
    
    private final List<TwoFactorMethod> authenticators;
    
    private TwoFactorValidation(Tenant param1Tenant, Application param1Application, User param1User, String param1String1, String param1String2, String param1String3, String param1String4, String param1String5, EventInfo param1EventInfo, List<TwoFactorMethod> param1List) {
      this.tenant = param1Tenant;
      this.application = param1Application;
      this.user = param1User;
      this.twoFactorId = param1String1;
      this.code = param1String2;
      this.method = param1String3;
      this.methodId = param1String4;
      this.secret = param1String5;
      this.eventInfo = param1EventInfo;
      this.authenticators = param1List;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/authentication/DefaultAuthenticationService$TwoFactorValidation;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #1247	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/authentication/DefaultAuthenticationService$TwoFactorValidation;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #1247	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/authentication/DefaultAuthenticationService$TwoFactorValidation;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #1247	-> 0
    }
    
    public Tenant tenant() {
      return this.tenant;
    }
    
    public Application application() {
      return this.application;
    }
    
    public User user() {
      return this.user;
    }
    
    public String twoFactorId() {
      return this.twoFactorId;
    }
    
    public String code() {
      return this.code;
    }
    
    public String method() {
      return this.method;
    }
    
    public String methodId() {
      return this.methodId;
    }
    
    public String secret() {
      return this.secret;
    }
    
    public EventInfo eventInfo() {
      return this.eventInfo;
    }
    
    public List<TwoFactorMethod> authenticators() {
      return this.authenticators;
    }
    
    boolean authenticatorWithNothingToCheck() {
      return (isAuthenticator() && this.authenticators.isEmpty() && !useSecret());
    }
    
    boolean disableTwoFactor() {
      return (this.methodId != null);
    }
    
    boolean hasCode() {
      return (this.code != null);
    }
    
    boolean hasMethod() {
      return (this.method != null);
    }
    
    boolean isAuthenticator() {
      return "authenticator".equals(this.method);
    }
    
    boolean loginOrStepUp() {
      return (this.twoFactorId != null);
    }
    
    boolean tryAuthenticator() {
      return (this.method == null || isAuthenticator());
    }
    
    boolean useSecret() {
      return (this.secret != null);
    }
  }
}
