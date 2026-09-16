package io.fusionauth.api.service.user;

import com.google.inject.Inject;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.IdentityExternalIdHelper;
import io.fusionauth.api.service.TemplateHelper;
import io.fusionauth.api.service.email.EmailProxy;
import io.fusionauth.api.service.message.MessageTemplateHelper;
import io.fusionauth.api.service.messenger.MessengerService;
import io.fusionauth.api.service.security.RateLimitService;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.PasswordlessStrategy;
import io.fusionauth.domain.RateLimitedRequestType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Tenantable;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DefaultPasswordlessService implements PasswordlessService {
  private static final List<IdentityType> emailEquivalents = List.of(IdentityType.email, IdentityType.username);
  
  private final ApplicationMapper applicationMapper;
  
  private final EmailProxy emailProxy;
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final ExternalIdentifierService externalIdentifierService;
  
  private final MessengerService messengerService;
  
  private final RateLimitService rateLimitService;
  
  private final TenantReaderService tenantReaderService;
  
  private final UserReaderService userReader;
  
  @Inject
  public DefaultPasswordlessService(ApplicationMapper paramApplicationMapper, EmailProxy paramEmailProxy, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, MessengerService paramMessengerService, RateLimitService paramRateLimitService, TenantReaderService paramTenantReaderService, UserReaderService paramUserReaderService) {
    this.applicationMapper = paramApplicationMapper;
    this.emailProxy = paramEmailProxy;
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.messengerService = paramMessengerService;
    this.rateLimitService = paramRateLimitService;
    this.tenantReaderService = paramTenantReaderService;
    this.userReader = paramUserReaderService;
    this.externalIdentifierService = paramExternalIdentifierService;
  }
  
  public PasswordlessService.PasswordlessCode createCode(Tenant paramTenant, Application paramApplication, User paramUser, String paramString, List<IdentityType> paramList, Map<String, Object> paramMap, PasswordlessStrategy paramPasswordlessStrategy) {
    ExternalIdentifier externalIdentifier = createExternalIdentifier(paramTenant, paramApplication, paramUser, paramString, paramList, paramMap, paramPasswordlessStrategy);
    return new PasswordlessService.PasswordlessCode(externalIdentifier.id, externalIdentifier.data.getAttribute("otp"));
  }
  
  public boolean isEnabled(Tenant paramTenant, Application paramApplication) {
    if (paramApplication == null)
      return false; 
    if (!paramApplication.passwordlessConfiguration.enabled)
      return false; 
    return (paramTenant.emailConfiguration.passwordlessEmailTemplateId != null || paramApplication.emailConfiguration.passwordlessEmailTemplateId != null || (paramTenant.phoneConfiguration.messengerId != null && (paramTenant.phoneConfiguration.passwordlessTemplateId != null || paramApplication.phoneConfiguration.passwordlessTemplateId != null)));
  }
  
  public void sendCode(Tenant paramTenant, Application paramApplication, User paramUser, String paramString, ExternalIdentifier paramExternalIdentifier, Map<String, Object> paramMap) {
    RateLimitedRequestType rateLimitedRequestType;
    IdentityType identityType = Optional.<ExternalIdentifier>ofNullable(paramExternalIdentifier).map(IdentityExternalIdHelper::getLoginIdentityType).orElse(IdentityType.email);
    if (identityType.is(IdentityType.email)) {
      rateLimitedRequestType = RateLimitedRequestType.SendPasswordless;
    } else if (identityType.is(IdentityType.phoneNumber)) {
      rateLimitedRequestType = RateLimitedRequestType.SendPhonePasswordless;
    } else {
      throw new IllegalArgumentException("Unsupported identity type: " + String.valueOf(identityType));
    } 
    this.rateLimitService.handleAndThrow(paramTenant, rateLimitedRequestType, paramUser.id.toString());
    if (paramExternalIdentifier == null) {
      paramExternalIdentifier = (new ExternalIdentifier()).with(paramExternalIdentifier -> paramExternalIdentifier.type = ExternalIdentifier.ExternalIdType.PasswordlessLogin);
      paramExternalIdentifier.data = new ExternalIdentifier.ExternalIdData(paramMap);
      paramExternalIdentifier = createExternalIdentifier(paramTenant, paramApplication, paramUser, paramString, DefaultUserReaderService.DefaultIdentityTypes, paramExternalIdentifier.data.state, null);
    } 
    if (identityType.is(IdentityType.phoneNumber)) {
      UUID uUID1 = paramTenant.phoneConfiguration.messengerId;
      UUID uUID2 = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> paramApplication.phoneConfiguration.passwordlessTemplateId).orElse(paramTenant.phoneConfiguration.passwordlessTemplateId);
      Map<String, Object> map = MessageTemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser, 
          IdentityExternalIdHelper.getLoginId(paramExternalIdentifier));
      map.put("oneTimeCode", paramExternalIdentifier.data.getAttribute("otp"));
      map.put("code", paramExternalIdentifier.id);
      map.put("state", paramExternalIdentifier.data.state);
      List<Locale> list = TemplateHelper.getPreferredLanguages(paramUser, paramApplication);
      this.messengerService.send(uUID2, uUID1, 
          list.isEmpty() ? null : (Locale)list.getFirst(), map);
    } else {
      this.emailProxy.sendPasswordless(paramTenant, paramApplication, paramUser, paramExternalIdentifier.id, paramExternalIdentifier.data.state, paramExternalIdentifier.data.getAttribute("otp"));
    } 
    paramExternalIdentifier.setSentToUser();
    this.externalIdentifierService.update(paramExternalIdentifier);
  }
  
  public PasswordlessService.ValidationResult validateSend(Tenant paramTenant, UUID paramUUID, String paramString1, String paramString2) {
    PasswordlessService.ValidationResult validationResult = new PasswordlessService.ValidationResult();
    validationResult.tenant = paramTenant;
    if (paramString2 != null) {
      validationResult.code = (this.externalIdentifierReader.validate(paramTenant, paramString2, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.PasswordlessLogin })).id;
      if (validationResult.code == null)
        return validationResult; 
      validationResult.application = this.applicationMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, validationResult.code.applicationId);
      validationResult.user = this.userReader.retrieveById((paramTenant == null) ? null : paramTenant.id, validationResult.code.userId);
      validationResult.tenant = this.tenantReaderService.resolve(paramTenant, new Tenantable[] { validationResult.code });
      if (validationResult.application != null && !validationResult.application.passwordlessConfiguration.enabled)
        validationResult.errors.addGeneralError("[disabled]applicationId", null, new Object[] { validationResult.code.applicationId }); 
    } else {
      commonValidation(paramTenant, paramUUID, paramString1, null, validationResult);
    } 
    if (validationResult.code != null && IdentityType.phoneNumber.is(IdentityExternalIdHelper.getLoginIdentityType(validationResult.code))) {
      UUID uUID1 = validationResult.tenant.phoneConfiguration.messengerId;
      UUID uUID2 = Optional.<Application>ofNullable(validationResult.application).map(paramApplication -> paramApplication.phoneConfiguration.passwordlessTemplateId).orElse(validationResult.tenant.phoneConfiguration.passwordlessTemplateId);
      if (uUID1 == null || uUID2 == null)
        validationResult.errors.addGeneralError("[invalid]applicationId", null, new Object[] { validationResult.code.applicationId }); 
    } else {
      UUID uUID = (validationResult.application != null) ? validationResult.application.emailConfiguration.passwordlessEmailTemplateId : null;
      if (uUID == null && validationResult.tenant != null)
        uUID = validationResult.tenant.emailConfiguration.passwordlessEmailTemplateId; 
      if (validationResult.application != null && validationResult.application.passwordlessConfiguration.enabled)
        if (validationResult.code != null && uUID == null) {
          validationResult.errors.addGeneralError("[invalid]applicationId", null, new Object[] { validationResult.code.applicationId });
        } else if (uUID == null) {
          validationResult.errors.addFieldError("applicationId", "[invalid]applicationId", null, new Object[] { validationResult.application.id });
        }  
      validationResult.errors.add((new Validator())

          
          .ifTrue((paramString2 == null), paramValidator -> paramValidator.notBlank(paramString, "loginId", new Object[0]).ifLastCheckHadNoError(()))




          
          .ifTrue((paramString2 != null), paramValidator -> paramValidator.ifTrue((paramValidationResult.user != null), ()))
          
          .done());
    } 
    return validationResult;
  }
  
  public PasswordlessService.ValidationResult validateStart(Tenant paramTenant, UUID paramUUID, String paramString, List<String> paramList, PasswordlessStrategy paramPasswordlessStrategy) {
    PasswordlessService.ValidationResult validationResult = new PasswordlessService.ValidationResult();
    validationResult.strategy = paramPasswordlessStrategy;
    validationResult.errors.add((new Validator())
        .notBlank(paramString, "loginId", new Object[0])
        .validate(paramValidator -> IdentityTypeValidator.validate(paramValidator, paramList, "loginIdTypes"))
        .ifLastCheckHadNoError(() -> paramValidationResult.identityTypes.addAll(IdentityTypeHelper.convert(paramList)))
        .done());
    commonValidation(paramTenant, paramUUID, paramString, validationResult.identityTypes, validationResult);
    return validationResult;
  }
  
  private void commonValidation(Tenant paramTenant, UUID paramUUID, String paramString, List<IdentityType> paramList, PasswordlessService.ValidationResult paramValidationResult) {
    paramValidationResult.application = this.applicationMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID);
    paramValidationResult.tenant = this.tenantReaderService.resolve(paramTenant, new Tenantable[] { paramValidationResult.application });
    paramValidationResult.user = this.userReader.retrieveByLoginId((paramValidationResult.tenant != null) ? paramValidationResult.tenant.id : null, paramString, paramList);
    paramValidationResult.errors.add((new Validator())
        .notMissing(paramUUID, "applicationId", new Object[0])
        .ifLastCheckHadNoError(paramValidator -> paramValidator.valid((paramValidationResult.application != null), "applicationId", new Object[] { paramUUID }).ifTrue((paramValidationResult.application != null), ())).done());
  }
  
  private ExternalIdentifier createExternalIdentifier(Tenant paramTenant, Application paramApplication, User paramUser, String paramString, List<IdentityType> paramList, Map<String, Object> paramMap, PasswordlessStrategy paramPasswordlessStrategy) {
    ExternalIdentifier.ExternalIdData externalIdData;
    if (paramMap != null) {
      externalIdData = new ExternalIdentifier.ExternalIdData(paramMap);
    } else {
      externalIdData = new ExternalIdentifier.ExternalIdData();
    } 
    UserIdentity userIdentity1 = IdentityHelper.resolveIdentity(paramUser, paramString, paramList);
    UserIdentity userIdentity2 = paramUser.resolvePrimaryIdentity(IdentityType.email);
    if (userIdentity1.type.is(IdentityType.username) && userIdentity2 != null)
      userIdentity1 = userIdentity2; 
    if (paramPasswordlessStrategy == null)
      if (emailEquivalents.contains(userIdentity1.type)) {
        paramPasswordlessStrategy = paramApplication.passwordlessConfiguration.emailLoginStrategy;
      } else {
        paramPasswordlessStrategy = paramApplication.passwordlessConfiguration.phoneLoginStrategy;
      }  
    externalIdData.setAttribute("loginId", userIdentity1.value)
      .setAttribute("loginIdType", userIdentity1.type);
    return this.externalIdentifierService.createPasswordlessLogin(paramTenant, paramUser.id, paramApplication.id, false, paramPasswordlessStrategy, externalIdData);
  }
}
