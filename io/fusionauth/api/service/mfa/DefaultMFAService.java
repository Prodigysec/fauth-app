package io.fusionauth.api.service.mfa;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.api.service.ImmutableLambdaArgument;
import io.fusionauth.api.domain.api.service.LambdaArgument;
import io.fusionauth.api.domain.api.service.MutableLambdaArgument;
import io.fusionauth.api.security.PhoneNumberValidator;
import io.fusionauth.api.service.TemplateHelper;
import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.email.EmailProxy;
import io.fusionauth.api.service.ip.LocationService;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.message.MessageTemplateHelper;
import io.fusionauth.api.service.messenger.MessengerService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.risk.CompositeRisk;
import io.fusionauth.api.service.risk.RiskLevel;
import io.fusionauth.api.service.risk.RiskSignalService;
import io.fusionauth.api.service.security.RateLimitService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.LambdaInvocationException;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.service.user.IdentityHelper;
import io.fusionauth.api.service.user.IdentityTypeHelper;
import io.fusionauth.api.service.user.IdentityTypeValidator;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.util.MFATools;
import io.fusionauth.api.util.PhoneMessageTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationMultiFactorTrustPolicy;
import io.fusionauth.domain.AuthenticationThreats;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.MultiFactorAction;
import io.fusionauth.domain.MultiFactorLoginPolicy;
import io.fusionauth.domain.RateLimitedRequestType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Tenantable;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.lambda.parameters.mfa.ClientRisk;
import io.fusionauth.domain.lambda.parameters.mfa.Context;
import io.fusionauth.domain.lambda.parameters.mfa.Policies;
import io.fusionauth.domain.lambda.parameters.mfa.RequiredLambdaResult;
import io.fusionauth.domain.lambda.parameters.mfa.Trust;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DefaultMFAService implements MFAService {
  private final ApplicationMapper applicationMapper;
  
  private final EmailProxy emailProxy;
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final ExternalIdentifierService externalIdentifierService;
  
  private final LambdaInvocationService lambdaInvocationService;
  
  private final LocationService locationService;
  
  private final MessengerService messengerService;
  
  private final MFALifecycleService mfaLifecycleService;
  
  private final RateLimitService rateLimitService;
  
  private final ReactorStatusService reactorStatusService;
  
  private final TenantReaderService tenantReader;
  
  private final UserReaderService userReader;
  
  @Inject
  public DefaultMFAService(ApplicationMapper paramApplicationMapper, EmailProxy paramEmailProxy, ExternalIdentifierReaderService paramExternalIdentifierReaderService, RateLimitService paramRateLimitService, ExternalIdentifierService paramExternalIdentifierService, LambdaInvocationService paramLambdaInvocationService, LocationService paramLocationService, MFALifecycleService paramMFALifecycleService, MessengerService paramMessengerService, ReactorStatusService paramReactorStatusService, RiskSignalService paramRiskSignalService, TenantReaderService paramTenantReaderService, UserReaderService paramUserReaderService) {
    this.applicationMapper = paramApplicationMapper;
    this.emailProxy = paramEmailProxy;
    this.externalIdentifierService = paramExternalIdentifierService;
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.lambdaInvocationService = paramLambdaInvocationService;
    this.locationService = paramLocationService;
    this.mfaLifecycleService = paramMFALifecycleService;
    this.messengerService = paramMessengerService;
    this.rateLimitService = paramRateLimitService;
    this.reactorStatusService = paramReactorStatusService;
    this.tenantReader = paramTenantReaderService;
    this.userReader = paramUserReaderService;
  }
  
  private static MultiFactorLoginPolicy lookupMultiFactorPolicy(Tenant paramTenant, MultiFactorAction paramMultiFactorAction, Application paramApplication) {
    if (paramApplication != null) {
      switch (paramMultiFactorAction) {
        default:
          throw new MatchException(null, null);
        case Required:
        case ChallengeOnHighRisk:
        
        case ChallengeOnMediumRisk:
          break;
      } 
      MultiFactorLoginPolicy multiFactorLoginPolicy = 

        
        paramApplication.multiFactorConfiguration.loginPolicy;
      if (multiFactorLoginPolicy != null)
        return multiFactorLoginPolicy; 
    } 
    switch (paramMultiFactorAction) {
      default:
        throw new MatchException(null, null);
      case Required:
      
      case ChallengeOnMediumRisk:
      
      case ChallengeOnHighRisk:
        break;
    } 
    return 



      
      MultiFactorLoginPolicy.Required;
  }
  
  @Nonnull
  private static MessageType messageTypeOrDefault(@Nullable MessageType paramMessageType, @Nullable Tenant paramTenant) {
    if (paramMessageType != null)
      return paramMessageType; 
    return (paramTenant == null || paramTenant.multiFactorConfiguration.sms.enabled || !paramTenant.multiFactorConfiguration.voice.enabled) ? 
      MessageType.SMS : 
      MessageType.Voice;
  }
  
  public MFAService.ChallengeResult determineChallengeRequired(MultiFactorAction paramMultiFactorAction, Tenant paramTenant, User paramUser, Application paramApplication, ExternalIdentifier paramExternalIdentifier, EventInfo paramEventInfo, CompositeRisk paramCompositeRisk, String paramString, AuthenticationType paramAuthenticationType, boolean paramBoolean) {
    MFAPolicyLicenseResult mFAPolicyLicenseResult = resolvePolicyAndLicenses(paramTenant, paramApplication, paramMultiFactorAction);
    paramCompositeRisk = (paramCompositeRisk == null) ? CompositeRisk.NO_RISK : paramCompositeRisk;
    MFAService.ChallengeResult challengeResult1 = determineChallengeRequiredFromPolicies(mFAPolicyLicenseResult, paramTenant, paramUser, paramExternalIdentifier, paramCompositeRisk);
    if (!challengeResult1.advancedLambdaMFALicense() || mFAPolicyLicenseResult.lambdaId == null) {
      if (paramBoolean)
        logChallengeResult(paramTenant, paramUser, paramMultiFactorAction, challengeResult1, mFAPolicyLicenseResult, paramCompositeRisk); 
      return challengeResult1;
    } 
    RequiredLambdaResult requiredLambdaResult = new RequiredLambdaResult(challengeResult1.required());
    if (paramEventInfo != null && paramEventInfo.ipAddress != null)
      paramEventInfo.location = this.locationService.ipToLocation(paramEventInfo.ipAddress); 
    UserRegistration userRegistration = (paramApplication != null) ? paramUser.getRegistrationForApplication(paramApplication.id) : null;
    Trust trust = null;
    if (paramCompositeRisk.signals().get(AuthenticationThreats.ImpossibleTravel) != RiskLevel.HIGH && paramExternalIdentifier != null) {
      Trust.StartInstant startInstant = new Trust.StartInstant(paramExternalIdentifier.data.startInstants.applications, paramExternalIdentifier.data.startInstants.tenant);
      trust = new Trust(paramExternalIdentifier.applicationId, paramExternalIdentifier.expirationInstant, paramExternalIdentifier.id, paramExternalIdentifier.insertInstant, startInstant, paramExternalIdentifier.getStateHelper(), paramExternalIdentifier.tenantId, paramExternalIdentifier.userId);
    } 
    Policies policies = challengeResult1.licensedApplication().<Policies>map(paramApplication -> new Policies(paramApplication.multiFactorConfiguration.loginPolicy, paramApplication.multiFactorConfiguration.trustPolicy, paramTenant.multiFactorConfiguration.loginPolicy)).orElse(new Policies(null, null, paramTenant.multiFactorConfiguration.loginPolicy));
    Application application = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> (new Application(paramApplication)).secure()).orElse((Application)null);
    String str = mFAPolicyLicenseResult.intelligentMFALicensed ? paramCompositeRisk.status().toString() : "NOT_COMPUTED";
    Context context = new Context(paramEventInfo, paramCompositeRisk.getThreatsDetected(), trust, paramString, policies, paramMultiFactorAction, application, (paramAuthenticationType != null) ? paramAuthenticationType.name() : null, new ClientRisk(str));
    try {
      this.lambdaInvocationService.invoke(mFAPolicyLicenseResult.lambdaId, new LambdaArgument[] { new MutableLambdaArgument(requiredLambdaResult), new ImmutableLambdaArgument((new User(paramUser))
              
              .secure()), new ImmutableLambdaArgument(userRegistration), new ImmutableLambdaArgument(context, true) });
    } catch (LambdaInvocationException lambdaInvocationException) {
      requiredLambdaResult.required = true;
    } 
    switch (paramMultiFactorAction) {
      default:
        throw new MatchException(null, null);
      case Required:
      case ChallengeOnMediumRisk:
        if (challengeResult1.configurableMethods().isEmpty());
      case ChallengeOnHighRisk:
        break;
    } 
    MFAService.ChallengeResult challengeResult2 = challengeResult1.updateWithLambdaAnswer(requiredLambdaResult);
    if (paramBoolean)
      logChallengeResult(paramTenant, paramUser, paramMultiFactorAction, challengeResult2, mFAPolicyLicenseResult, paramCompositeRisk); 
    return challengeResult2;
  }
  
  public boolean isMethodRequired(Tenant paramTenant, Application paramApplication) {
    MFAPolicyLicenseResult mFAPolicyLicenseResult = resolvePolicyAndLicenses(paramTenant, paramApplication, MultiFactorAction.login);
    if (mFAPolicyLicenseResult.configurableMethods().isEmpty())
      return false; 
    switch (mFAPolicyLicenseResult.policy) {
      case Required:
      case ChallengeOnHighRisk:
      case ChallengeOnMediumRisk:
      
    } 
    return false;
  }
  
  public MFAService.ProcessLoginResult processLogin(Tenant paramTenant, Application paramApplication, ExternalIdentifier paramExternalIdentifier1, User paramUser, AuthenticationService.AuthenticationResult paramAuthenticationResult, ExternalIdentifier paramExternalIdentifier2, EventInfo paramEventInfo) {
    if (paramExternalIdentifier1 != null && paramExternalIdentifier1.getAttributeAsBoolean("bypass2FA"))
      return new MFAService.ProcessLoginResult(false, false); 
    MFAService.ChallengeResult challengeResult = determineChallengeRequired(MultiFactorAction.login, paramTenant, paramUser, paramApplication, paramExternalIdentifier2, paramEventInfo, paramAuthenticationResult.clientRisk, (String)null, paramAuthenticationResult.type, true);
    if (challengeResult.required()) {
      UUID uUID = (paramApplication == null) ? null : paramApplication.id;
      ExternalIdentifier.ExternalIdData externalIdData = new ExternalIdentifier.ExternalIdData("loginIdType", paramAuthenticationResult.userIdentity.type.name);
      externalIdData.setAttribute("loginId", paramAuthenticationResult.userIdentity.value);
      externalIdData.setAttribute("riskLevel", challengeResult.intelligentMFALicensed() ? paramAuthenticationResult.clientRisk.status() : "NOT_COMPUTED");
      boolean bool = paramUser.twoFactorEnabled();
      externalIdData.setAttribute("twoFactorChallenge", bool);
      if (paramExternalIdentifier2 != null)
        externalIdData.setAttribute("twoFactorTrustId", paramExternalIdentifier2.id); 
      paramAuthenticationResult.twoFactorId = this.externalIdentifierService.createTwoFactor(paramTenant, uUID, paramUser.id, externalIdData);
      paramAuthenticationResult.configurableTwoFactorMethods = challengeResult.configurableMethods();
      if (bool)
        this.mfaLifecycleService.onChallenge(paramApplication, paramEventInfo, this.externalIdentifierReader.retrieveById(paramAuthenticationResult.twoFactorId), paramTenant, paramUser); 
      return new MFAService.ProcessLoginResult(true, challengeResult.sendSuspiciousLoginEvent());
    } 
    return new MFAService.ProcessLoginResult(false, challengeResult.sendSuspiciousLoginEvent());
  }
  
  public void sendTwoFactorCode(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2, MessageType paramMessageType) {
    ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveById(paramString1);
    if (externalIdentifier.data == null)
      externalIdentifier.data = new ExternalIdentifier.ExternalIdData(); 
    String str = this.externalIdentifierService.generateTwoFactorOneTimeCode(paramTenant);
    externalIdentifier.data.setAttribute("otp", str);
    externalIdentifier.data.setAttribute("methodId", paramString2);
    TwoFactorMethod twoFactorMethod = paramUser.twoFactor.getMethodById(paramString2);
    doSendTwoFactorCode(paramTenant, paramApplication, paramUser, twoFactorMethod.method, twoFactorMethod.email, twoFactorMethod.mobilePhone, str, paramMessageType);
    if (twoFactorMethod.method.equals("sms"))
      externalIdentifier.data.setAttribute("twoFactorMessageType", paramMessageType); 
    this.externalIdentifierService.update(externalIdentifier);
  }
  
  public void sendTwoFactorCodeForEnableDisable(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2, String paramString3, String paramString4, MessageType paramMessageType) {
    if (paramString1 == null) {
      ExternalIdentifier.ExternalIdData externalIdData = new ExternalIdentifier.ExternalIdData();
      externalIdData.setAttribute("method", paramString2);
      externalIdData.setAttribute("methodValue", paramString2.equals("email") ? paramString3 : paramString4);
      String str = createTwoFactorOneTimeCode(paramTenant, paramApplication, paramUser, externalIdData);
      doSendTwoFactorCode(paramTenant, paramApplication, paramUser, paramString2, paramString3, paramString4, str, paramMessageType);
    } else {
      ExternalIdentifier.ExternalIdData externalIdData = new ExternalIdentifier.ExternalIdData("methodId", paramString1);
      String str = createTwoFactorOneTimeCode(paramTenant, paramApplication, paramUser, externalIdData);
      TwoFactorMethod twoFactorMethod = paramUser.twoFactor.getMethodById(paramString1);
      doSendTwoFactorCode(paramTenant, paramApplication, paramUser, twoFactorMethod.method, twoFactorMethod.email, twoFactorMethod.mobilePhone, str, paramMessageType);
    } 
  }
  
  public MFAService.StartTwoFactorResult startTwoFactorRequest(Tenant paramTenant, Application paramApplication, User paramUser, UserIdentity paramUserIdentity, String paramString1, String paramString2, Map<String, Object> paramMap, EventInfo paramEventInfo) {
    String str1 = (paramString1 != null) ? paramString1 : this.externalIdentifierService.generateTwoFactorOneTimeCode(paramTenant);
    ExternalIdentifier.ExternalIdData externalIdData = (new ExternalIdentifier.ExternalIdData(paramMap)).setAttribute("otp", str1).setAttribute("trustChallenge", paramString2);
    if (paramUserIdentity != null)
      externalIdData.setAttribute("loginId", paramUserIdentity.value)
        .setAttribute("loginIdType", paramUserIdentity.type); 
    boolean bool = paramUser.twoFactorEnabled();
    externalIdData.setAttribute("twoFactorChallenge", bool);
    String str2 = this.externalIdentifierService.createTwoFactor(paramTenant, 
        (paramApplication == null) ? null : paramApplication.id, paramUser.id, externalIdData);
    if (bool)
      this.mfaLifecycleService.onChallenge(paramApplication, paramEventInfo, this.externalIdentifierReader.retrieveById(str2), paramTenant, paramUser); 
    return new MFAService.StartTwoFactorResult(str1, str2);
  }
  
  public MFAService.ValidationResult validateSend(Tenant paramTenant, String paramString1, String paramString2, MessageType paramMessageType) {
    Objects.requireNonNull(paramString1);
    MFAService.ValidationResult validationResult = new MFAService.ValidationResult();
    ExternalIdentifierReaderService.ValidationResult validationResult1 = this.externalIdentifierReader.validate(paramTenant, paramString1, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.TwoFactor });
    validationResult.errors.add((new Validator()).ensure((validationResult1.id != null), "twoFactorId", "[invalid]", new Object[0])
        .done());
    if (validationResult1.id != null) {
      validationResult.user = this.userReader.retrieveById((paramTenant != null) ? paramTenant.id : null, validationResult1.id.userId);
      validationResult.id = validationResult1.id;
      validationResult.application = (validationResult1.id.applicationId != null) ? this.applicationMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, validationResult1.id.applicationId) : null;
    } 
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.user, validationResult.id });
    validationResult.messageType = messageTypeOrDefault(paramMessageType, validationResult.tenant);
    if (validationResult.tenant == null || !validationResult.errors.empty())
      return validationResult; 
    validationResult.methodId = paramString2;
    validationResult.errors.add((new Validator())
        
        .notBlank(validationResult.methodId, "methodId", new Object[0])
        .ifLastCheckHadNoError(paramValidator -> validateMethodIdForSending(paramValidator, paramValidationResult))
        .done());
    return validationResult;
  }
  
  public MFAService.ValidationResult validateSendForEnableDisable(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString1, String paramString2, String paramString3, String paramString4, MessageType paramMessageType) {
    MFAService.ValidationResult validationResult = new MFAService.ValidationResult();
    validationResult.user = (paramUUID2 != null) ? this.userReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID2) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.user });
    validationResult.messageType = messageTypeOrDefault(paramMessageType, validationResult.tenant);
    validationResult.application = (paramUUID1 != null) ? this.applicationMapper.retrieveById((validationResult.tenant != null) ? validationResult.tenant.id : null, paramUUID1) : null;
    validationResult





      
      .errors = (new Validator()).notMissing(paramUUID2, "userId", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.ensure((paramValidationResult.user != null), "userId", "[invalid]", new Object[] { paramUUID })).ifTrue((paramUUID1 != null), paramValidator -> paramValidator.ensure((paramValidationResult.application != null), "applicationId", "[invalid]", new Object[] { paramUUID })).done();
    if (!validationResult.errors.empty())
      return validationResult; 
    if (paramString1 != null) {
      validationResult.methodId = paramString1;
      validationResult
        
        .errors = (new Validator()).validate(paramValidator -> validateMethodIdForSending(paramValidator, paramValidationResult)).done();
    } else {
      validationResult.method = paramString2;
      validationResult
























        
        .errors = (new Validator()).notBlank(validationResult.method, "method", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.ifTrue(paramValidationResult.method.equals("email"), ()).ifTrue(paramValidationResult.method.equals("sms"), ()).ensure(SupportedMethods.contains(paramValidationResult.method), "method", "[invalid]", new Object[] { paramValidationResult.method, String.join(", ", (Iterable)SupportedMethods) }).ifLastCheckHadNoError(())).done();
    } 
    return validationResult;
  }
  
  public MFAService.ValidationResult validateStart(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString, List<String> paramList) {
    MFAService.ValidationResult validationResult = new MFAService.ValidationResult();
    validationResult.application = (paramUUID1 != null) ? this.applicationMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID1) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.application });
    if (validationResult.tenant == null && paramUUID2 == null)
      return validationResult; 
    if (paramUUID2 != null) {
      validationResult.user = this.userReader.retrieveById((validationResult.tenant != null) ? validationResult.tenant.id : null, paramUUID2);
    } else if (paramString != null) {
      (new Validator())
        .validate(paramValidator -> IdentityTypeValidator.validate(paramValidator, paramList, "loginIdTypes"))
        .ifNoErrors(() -> {
            List<IdentityType> list = IdentityTypeHelper.convert(paramList);
            paramValidationResult.user = this.userReader.retrieveByLoginId(paramValidationResult.tenant.id, paramString, list);
            if (paramValidationResult.user != null)
              paramValidationResult.userIdentity = IdentityHelper.resolveIdentity(paramValidationResult.user, paramString, list); 
          }).done(paramErrors -> paramValidationResult.errors.add(paramErrors));
    } 
    validationResult.errors.add((new Validator())

        
        .ifTrue((paramUUID2 == null), paramValidator -> paramValidator.notBlank(paramString, "loginId", new Object[0]))


        
        .ifTrue((paramUUID1 != null), paramValidator -> paramValidator.valid((paramValidationResult.application != null), "applicationId", new Object[] { paramUUID })).done());
    return validationResult;
  }
  
  public MFAService.ValidationResult validateTwoFactorStatus(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString) {
    MFAService.ValidationResult validationResult = new MFAService.ValidationResult();
    validationResult.application = (paramUUID1 != null) ? this.applicationMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID1) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.application });
    validationResult.user = (paramUUID2 != null) ? this.userReader.retrieveById((validationResult.tenant != null) ? validationResult.tenant.id : null, paramUUID2) : null;
    validationResult.id = (paramString != null) ? (this.externalIdentifierReader.validate(validationResult.tenant, paramString, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.TwoFactorTrust })).id : null;
    validationResult.reactorStatus = this.reactorStatusService.retrieveStatus();
    validationResult




      
      .errors = (new Validator()).notMissing(paramUUID2, "userId", new Object[0]).ifNoFieldErrors("userId", paramValidator -> paramValidator.validObject(paramValidationResult.user, "userId", new Object[] { paramUUID })).ifTrue((paramUUID1 != null), paramValidator -> paramValidator.validObject(paramValidationResult.application, "applicationId", new Object[] { paramUUID })).done();
    return validationResult;
  }
  
  private String createTwoFactorOneTimeCode(Tenant paramTenant, Application paramApplication, User paramUser, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    UUID uUID1 = (paramApplication != null) ? paramApplication.id : null;
    UUID uUID2 = (paramUser != null) ? paramUser.id : null;
    return this.externalIdentifierService.createTwoFactorOneTimeCode(paramTenant, uUID1, uUID2, null, paramExternalIdData);
  }
  
  private MFAService.ChallengeResult determineChallengeRequiredFromPolicies(MFAPolicyLicenseResult paramMFAPolicyLicenseResult, Tenant paramTenant, User paramUser, ExternalIdentifier paramExternalIdentifier, CompositeRisk paramCompositeRisk) {
    if (paramMFAPolicyLicenseResult.configurableMethods.isEmpty())
      return paramMFAPolicyLicenseResult.toRequiredResult(false, paramCompositeRisk); 
    if (paramCompositeRisk != null && MFATools.isIntelligentLoginPolicy(paramMFAPolicyLicenseResult.policy))
      return paramMFAPolicyLicenseResult.toRequiredResult(MFATools.challengeBasedOnClientRisk(paramMFAPolicyLicenseResult.policy, paramCompositeRisk), paramCompositeRisk); 
    boolean bool = !userEnabledForAtLeastOneMethod(paramTenant, paramUser, paramMFAPolicyLicenseResult.advancedMFALicensed) ? true : false;
    if (bool)
      return paramMFAPolicyLicenseResult.toRequiredResult((paramMFAPolicyLicenseResult.policy == MultiFactorLoginPolicy.Required), paramCompositeRisk); 
    if (paramMFAPolicyLicenseResult.policy == MultiFactorLoginPolicy.Disabled)
      return paramMFAPolicyLicenseResult.toRequiredResult(false, paramCompositeRisk); 
    if (paramExternalIdentifier == null || paramExternalIdentifier.userId == null || !paramExternalIdentifier.userId.equals(paramUser.id))
      return paramMFAPolicyLicenseResult.toRequiredResult(true, paramCompositeRisk); 
    if (paramCompositeRisk.signals().get(AuthenticationThreats.ImpossibleTravel) == RiskLevel.HIGH)
      return paramMFAPolicyLicenseResult.toRequiredResult(true, paramCompositeRisk); 
    Optional<?> optional = paramMFAPolicyLicenseResult.application().map(paramApplication -> paramApplication.multiFactorConfiguration.trustPolicy);
    if (optional.isEmpty())
      return paramMFAPolicyLicenseResult.toRequiredResult(false, paramCompositeRisk); 
    if (optional.get() != ApplicationMultiFactorTrustPolicy.None) {
      boolean bool1 = (optional.get() == ApplicationMultiFactorTrustPolicy.This) ? isExpiredForThisApplication(paramExternalIdentifier, paramTenant, paramMFAPolicyLicenseResult.application.get()) : isExpiredForThisAnyApplication(paramExternalIdentifier, paramTenant, paramMFAPolicyLicenseResult.application.get());
      return paramMFAPolicyLicenseResult.toRequiredResult(bool1, paramCompositeRisk);
    } 
    return paramMFAPolicyLicenseResult.toRequiredResult(true, paramCompositeRisk);
  }
  
  private void doSendTwoFactorCode(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2, String paramString3, String paramString4, MessageType paramMessageType) {
    this.rateLimitService.handleAndThrow(paramTenant, RateLimitedRequestType.SendTwoFactor, paramUser.id.toString());
    if (paramString1.equals("email")) {
      this.emailProxy.sendTwoFactorCode(paramTenant, paramApplication, paramUser, paramString2, paramString4);
    } else if (paramString1.equals("sms")) {
      UUID uUID1, uUID2;
      if (paramMessageType == MessageType.Voice) {
        uUID1 = paramTenant.multiFactorConfiguration.voice.messengerId;
        uUID2 = (paramApplication != null && paramApplication.multiFactorConfiguration.voice.templateId != null) ? paramApplication.multiFactorConfiguration.voice.templateId : paramTenant.multiFactorConfiguration.voice.templateId;
      } else {
        uUID1 = paramTenant.multiFactorConfiguration.sms.messengerId;
        uUID2 = (paramApplication != null && paramApplication.multiFactorConfiguration.sms.templateId != null) ? paramApplication.multiFactorConfiguration.sms.templateId : paramTenant.multiFactorConfiguration.sms.templateId;
      } 
      Map<String, Object> map = MessageTemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser, null);
      map.put("phoneNumber", paramString3);
      map.put("email", paramString2);
      map.put("code", paramString4);
      map.put("userId", paramUser.id);
      if (paramMessageType == MessageType.Voice)
        map.put("spokenCode", PhoneMessageTools.formatCodeForVoice(paramString4)); 
      List<Locale> list = TemplateHelper.getPreferredLanguages(paramUser, paramApplication);
      this.messengerService.send(uUID2, uUID1, list.isEmpty() ? null : (Locale)list.getFirst(), map);
    } else {
      throw new IllegalStateException("Unsupported Two Factor method [" + paramString1 + "]. Currently only Email and SMS can be sent to the user.");
    } 
  }
  
  private boolean isExpiredForThisAnyApplication(ExternalIdentifier paramExternalIdentifier, Tenant paramTenant, Application paramApplication) {
    if (paramExternalIdentifier.data != null && paramExternalIdentifier.data.startInstants != null)
      for (UUID uUID : paramExternalIdentifier.data.startInstants.applications.keySet()) {
        ZonedDateTime zonedDateTime = paramExternalIdentifier.data.startInstants.applications.get(uUID);
        if (zonedDateTime != null) {
          int i = ExternalIdentifier.getTTL(paramExternalIdentifier.type, paramTenant, () -> paramApplication);
          boolean bool = zonedDateTime.plusSeconds(i).isBefore(ZonedDateTime.now(ZoneOffset.UTC));
          if (!bool)
            return false; 
          if (!paramApplication.id.equals(uUID)) {
            int j = ExternalIdentifier.getTTL(ExternalIdentifier.ExternalIdType.TwoFactorTrust, paramTenant, () -> this.applicationMapper.retrieveById(paramTenant.id, paramUUID));
            bool = zonedDateTime.plusSeconds(j).isBefore(ZonedDateTime.now(ZoneOffset.UTC));
            if (!bool)
              return false; 
          } 
        } 
      }  
    return true;
  }
  
  private boolean isExpiredForThisApplication(ExternalIdentifier paramExternalIdentifier, Tenant paramTenant, Application paramApplication) {
    if (paramExternalIdentifier.data != null && paramExternalIdentifier.data.startInstants != null) {
      ZonedDateTime zonedDateTime = paramExternalIdentifier.data.startInstants.applications.get(paramApplication.id);
      if (zonedDateTime != null) {
        int i = ExternalIdentifier.getTTL(paramExternalIdentifier.type, paramTenant, () -> paramApplication);
        return zonedDateTime.plusSeconds(i).isBefore(ZonedDateTime.now(ZoneOffset.UTC));
      } 
    } 
    return true;
  }
  
  private void logChallengeResult(Tenant paramTenant, User paramUser, MultiFactorAction paramMultiFactorAction, MFAService.ChallengeResult paramChallengeResult, MFAPolicyLicenseResult paramMFAPolicyLicenseResult, CompositeRisk paramCompositeRisk) {
    if (paramTenant.multiFactorConfiguration.debug) {
      StringBuilder stringBuilder = (new StringBuilder("MFA challenge debug.\n\nUserId: ")).append(paramUser.id).append("\nAction: ").append(paramMultiFactorAction).append("\nChallenge Required: ").append(paramChallengeResult.required());
      if (paramMFAPolicyLicenseResult.intelligentMFALicensed) {
        stringBuilder.append("\n\nComposite Risk: ").append(paramCompositeRisk.status()).append("\n\nSignals:\n");
        paramCompositeRisk.signals().forEach((paramAuthenticationThreats, paramRiskLevel) -> paramStringBuilder.append(paramAuthenticationThreats).append(": ").append(paramRiskLevel).append("\n"));
      } 
      EventLogHelper.create(new EventLog(EventLogType.Debug, stringBuilder.toString()));
    } 
  }
  
  private MFAPolicyLicenseResult resolvePolicyAndLicenses(Tenant paramTenant, Application paramApplication, MultiFactorAction paramMultiFactorAction) {
    UUID uUID;
    ReactorStatus reactorStatus = this.reactorStatusService.retrieveStatus();
    boolean bool1 = ReactorStatusValidator.isLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.advancedMultiFactorAuthentication);
    boolean bool2 = ReactorStatusValidator.isLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.multiFactorLambdas);
    boolean bool3 = ReactorStatusValidator.isLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.intelligentMFA);
    List<String> list = tenantEnabledForMethods(paramTenant, bool1);
    if (list.isEmpty())
      return new MFAPolicyLicenseResult(Optional.empty(), MultiFactorLoginPolicy.Disabled, 
          
          List.of(), bool1, bool2, bool3, paramTenant.lambdaConfiguration.multiFactorRequirementId); 
    Application application = ReactorStatusValidator.isLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.applicationMultiFactorAuthentication) ? paramApplication : null;
    if (paramApplication != null && Application.FUSIONAUTH_APP_ID.equals(paramApplication.id))
      application = paramApplication; 
    if (application != null && application.lambdaConfiguration.multiFactorRequirementId != null) {
      uUID = application.lambdaConfiguration.multiFactorRequirementId;
    } else {
      uUID = paramTenant.lambdaConfiguration.multiFactorRequirementId;
    } 
    MultiFactorLoginPolicy multiFactorLoginPolicy = lookupMultiFactorPolicy(paramTenant, paramMultiFactorAction, application);
    if (!bool3 && MFATools.isIntelligentLoginPolicy(multiFactorLoginPolicy))
      multiFactorLoginPolicy = MultiFactorLoginPolicy.Required; 
    return new MFAPolicyLicenseResult(Optional.ofNullable(application), multiFactorLoginPolicy, list, bool1, bool2, bool3, uUID);
  }
  
  private List<String> tenantEnabledForMethods(Tenant paramTenant, boolean paramBoolean) {
    ArrayList<String> arrayList = new ArrayList(3);
    if (paramTenant.multiFactorConfiguration.authenticator.enabled)
      arrayList.add("authenticator"); 
    if (paramBoolean) {
      if (paramTenant.multiFactorConfiguration.email.enabled)
        arrayList.add("email"); 
      if (paramTenant.multiFactorConfiguration.sms.enabled || paramTenant.multiFactorConfiguration.voice.enabled)
        arrayList.add("sms"); 
    } 
    return arrayList;
  }
  
  private boolean userEnabledForAtLeastOneMethod(Tenant paramTenant, User paramUser, boolean paramBoolean) {
    for (TwoFactorMethod twoFactorMethod : paramUser.twoFactor.methods) {
      if (twoFactorMethod.method.equals("authenticator") && paramTenant.multiFactorConfiguration.authenticator.enabled)
        return true; 
      if (paramBoolean) {
        if (twoFactorMethod.method.equals("email") && paramTenant.multiFactorConfiguration.email.enabled)
          return true; 
        if (twoFactorMethod.method.equals("sms") && (paramTenant.multiFactorConfiguration.sms.enabled || paramTenant.multiFactorConfiguration.voice.enabled))
          return true; 
      } 
    } 
    return false;
  }
  
  private void validateMethodIdForSending(Validator paramValidator, MFAService.ValidationResult paramValidationResult) {
    TwoFactorMethod twoFactorMethod = paramValidationResult.user.twoFactor.getMethodById(paramValidationResult.methodId);
    paramValidationResult.method = (twoFactorMethod != null) ? twoFactorMethod.method : null;
    paramValidator.notMissingWithCode(twoFactorMethod, "methodId", "[invalid]methodId", new Object[] { paramValidationResult.methodId }).ifLastCheckHadNoError(() -> paramValidator.ifTrue(paramTwoFactorMethod.method.equals("authenticator"), ()).ifTrue(paramTwoFactorMethod.method.equals("email"), ()).ifTrue(paramTwoFactorMethod.method.equals("sms"), ()))






      
      .ifNoFieldErrors("methodId", () -> paramValidator.ensure(SupportedMethodsForSend.contains(paramValidationResult.method), "methodId", "[cannotSend]", new Object[] { paramValidationResult.methodId, paramValidationResult.method }));
  }
  
  private static final class MFAPolicyLicenseResult extends Record {
    private final Optional<Application> application;
    
    private final MultiFactorLoginPolicy policy;
    
    private final List<String> configurableMethods;
    
    private final boolean advancedMFALicensed;
    
    private final boolean advancedLambdaMFALicense;
    
    private final boolean intelligentMFALicensed;
    
    private final UUID lambdaId;
    
    private MFAPolicyLicenseResult(Optional<Application> param1Optional, MultiFactorLoginPolicy param1MultiFactorLoginPolicy, List<String> param1List, boolean param1Boolean1, boolean param1Boolean2, boolean param1Boolean3, UUID param1UUID) {
      this.application = param1Optional;
      this.policy = param1MultiFactorLoginPolicy;
      this.configurableMethods = param1List;
      this.advancedMFALicensed = param1Boolean1;
      this.advancedLambdaMFALicense = param1Boolean2;
      this.intelligentMFALicensed = param1Boolean3;
      this.lambdaId = param1UUID;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/mfa/DefaultMFAService$MFAPolicyLicenseResult;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #884	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/mfa/DefaultMFAService$MFAPolicyLicenseResult;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #884	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/mfa/DefaultMFAService$MFAPolicyLicenseResult;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #884	-> 0
    }
    
    public Optional<Application> application() {
      return this.application;
    }
    
    public MultiFactorLoginPolicy policy() {
      return this.policy;
    }
    
    public List<String> configurableMethods() {
      return this.configurableMethods;
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
    
    public UUID lambdaId() {
      return this.lambdaId;
    }
    
    public MFAService.ChallengeResult toRequiredResult(boolean param1Boolean, CompositeRisk param1CompositeRisk) {
      return new MFAService.ChallengeResult(this.configurableMethods, param1Boolean, param1CompositeRisk
          
          .isSuspicious(), 
          advancedMFALicensed(), 
          advancedLambdaMFALicense(), 
          intelligentMFALicensed(), 
          application());
    }
  }
}
