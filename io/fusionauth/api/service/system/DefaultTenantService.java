package io.fusionauth.api.service.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.error.Errors;
import com.inversoft.util.StringTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.AsyncTask;
import io.fusionauth.api.domain.BreachedPasswordMapper;
import io.fusionauth.api.domain.ConnectorConfigurationMapper;
import io.fusionauth.api.domain.EmailTemplateMapper;
import io.fusionauth.api.domain.EntityMapper;
import io.fusionauth.api.domain.FormMapper;
import io.fusionauth.api.domain.IdentityProviderMapper;
import io.fusionauth.api.domain.MFAMetricsMapper;
import io.fusionauth.api.domain.MessageTemplateMapper;
import io.fusionauth.api.domain.MessengerConfigurationMapper;
import io.fusionauth.api.domain.TenantKeyType;
import io.fusionauth.api.domain.TenantMapper;
import io.fusionauth.api.domain.WebhookMapper;
import io.fusionauth.api.security.PasswordEncryptorLibrary;
import io.fusionauth.api.security.guice.SecurityModule;
import io.fusionauth.api.service.application.ApplicationService;
import io.fusionauth.api.service.event.EventService;
import io.fusionauth.api.service.group.GroupService;
import io.fusionauth.api.service.identity.IdentityProviderService;
import io.fusionauth.api.service.ip.IPAccessControlListReaderService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.api.service.useraction.UserActionService;
import io.fusionauth.api.util.KeyValidator;
import io.fusionauth.api.util.LambdaValidator;
import io.fusionauth.api.util.MFATools;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.api.util.PropertiesTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.CaptchaMethod;
import io.fusionauth.domain.EmailConfiguration;
import io.fusionauth.domain.EventConfiguration;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.FIPS;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.MultiFactorLoginPolicy;
import io.fusionauth.domain.ObjectState;
import io.fusionauth.domain.PasswordBreachDetection;
import io.fusionauth.domain.RateLimitedRequestConfiguration;
import io.fusionauth.domain.RefreshTokenExpirationPolicy;
import io.fusionauth.domain.RefreshTokenUsagePolicy;
import io.fusionauth.domain.SecureGeneratorConfiguration;
import io.fusionauth.domain.SecureGeneratorType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TenantPhoneConfiguration;
import io.fusionauth.domain.UnverifiedBehavior;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.VerificationStrategy;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorPolicy;
import io.fusionauth.domain.event.EventType;
import io.fusionauth.domain.event.JWTPublicKeyUpdateEvent;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormType;
import io.fusionauth.domain.message.MessageTemplate;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.domain.util.DefaultTools;
import io.fusionauth.domain.webauthn.AuthenticatorAttachmentPreference;
import io.fusionauth.plugin.spi.security.PasswordEncryptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mybatis.guice.transactional.Transactional;
import org.primeframework.mvc.ErrorException;

public class DefaultTenantService implements TenantService {
  private final APIKeyService apiKeyService;
  
  private final ApplicationReaderService applicationReader;
  
  private final ApplicationService applicationService;
  
  private final AsyncTaskManager asyncTaskManager;
  
  private final BreachedPasswordMapper breachedPasswordMapper;
  
  private final CacheNotifier cacheNotifier;
  
  private final ConnectorConfigurationMapper connectorConfigurationMapper;
  
  private final String defaultEncryptionScheme;
  
  private final EmailTemplateMapper emailTemplateMapper;
  
  private final EntityMapper entityMapper;
  
  private final FormMapper formMapper;
  
  private final GroupService groupService;
  
  private final IdentityProviderMapper identityProviderMapper;
  
  private final IdentityProviderService identityProviderService;
  
  private final IPAccessControlListReaderService ipAccessControlListReader;
  
  private final KeyValidator keyValidator;
  
  private final LambdaValidator lambdaValidator;
  
  private final MessageTemplateMapper messageTemplateMapper;
  
  private final MessengerConfigurationMapper messengerConfigurationMapper;
  
  private final MFAMetricsMapper mfaMetricsMapper;
  
  private final ObjectMapper objectMapper;
  
  private final PasswordEncryptorLibrary passwordEncryptorLibrary;
  
  private final ReactorStatusService reactorStatusService;
  
  private final TenantMapper tenantMapper;
  
  private final TenantReaderService tenantReader;
  
  private final ThemeService themeService;
  
  private final UserActionService userActionService;
  
  private final UserService userService;
  
  private final WebhookMapper webhookMapper;
  
  @Inject
  public DefaultTenantService(IdentityProviderMapper paramIdentityProviderMapper, IdentityProviderService paramIdentityProviderService, ApplicationReaderService paramApplicationReaderService, ApplicationService paramApplicationService, AsyncTaskManager paramAsyncTaskManager, APIKeyService paramAPIKeyService, BreachedPasswordMapper paramBreachedPasswordMapper, CacheNotifier paramCacheNotifier, ConnectorConfigurationMapper paramConnectorConfigurationMapper, @Named("default-password-encryptor-name") String paramString, EmailTemplateMapper paramEmailTemplateMapper, EntityMapper paramEntityMapper, FormMapper paramFormMapper, GroupService paramGroupService, IPAccessControlListReaderService paramIPAccessControlListReaderService, KeyValidator paramKeyValidator, LambdaValidator paramLambdaValidator, MessageTemplateMapper paramMessageTemplateMapper, MessengerConfigurationMapper paramMessengerConfigurationMapper, MFAMetricsMapper paramMFAMetricsMapper, ObjectMapper paramObjectMapper, PasswordEncryptorLibrary paramPasswordEncryptorLibrary, ReactorStatusService paramReactorStatusService, TenantMapper paramTenantMapper, TenantReaderService paramTenantReaderService, ThemeService paramThemeService, UserActionService paramUserActionService, UserService paramUserService, WebhookMapper paramWebhookMapper) {
    this.apiKeyService = paramAPIKeyService;
    this.identityProviderMapper = paramIdentityProviderMapper;
    this.identityProviderService = paramIdentityProviderService;
    this.applicationReader = paramApplicationReaderService;
    this.applicationService = paramApplicationService;
    this.asyncTaskManager = paramAsyncTaskManager;
    this.breachedPasswordMapper = paramBreachedPasswordMapper;
    this.cacheNotifier = paramCacheNotifier;
    this.connectorConfigurationMapper = paramConnectorConfigurationMapper;
    this.defaultEncryptionScheme = paramString;
    this.emailTemplateMapper = paramEmailTemplateMapper;
    this.entityMapper = paramEntityMapper;
    this.formMapper = paramFormMapper;
    this.groupService = paramGroupService;
    this.ipAccessControlListReader = paramIPAccessControlListReaderService;
    this.keyValidator = paramKeyValidator;
    this.lambdaValidator = paramLambdaValidator;
    this.messageTemplateMapper = paramMessageTemplateMapper;
    this.messengerConfigurationMapper = paramMessengerConfigurationMapper;
    this.mfaMetricsMapper = paramMFAMetricsMapper;
    this.objectMapper = paramObjectMapper;
    this.passwordEncryptorLibrary = paramPasswordEncryptorLibrary;
    this.reactorStatusService = paramReactorStatusService;
    this.tenantMapper = paramTenantMapper;
    this.tenantReader = paramTenantReaderService;
    this.themeService = paramThemeService;
    this.userActionService = paramUserActionService;
    this.userService = paramUserService;
    this.webhookMapper = paramWebhookMapper;
  }
  
  @Transactional
  public void _create(Tenant paramTenant, List<UUID> paramList) {
    if (paramTenant.id == null)
      paramTenant.id = UUID.randomUUID(); 
    paramTenant.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramTenant.lastUpdateInstant = paramTenant.insertInstant;
    setDefaults(paramTenant);
    paramTenant.configured = true;
    paramTenant.state = ObjectState.Active;
    this.tenantMapper.create(paramTenant);
    writeVerificationKeys(paramTenant.id, paramTenant);
    createConnectorConfiguration(paramTenant);
    MapperTools.safeCreateUpdate(5000, paramList, paramList -> this.webhookMapper.createAssociationsToWebhooks(paramTenant.id, paramList));
  }
  
  @Transactional
  public void _update(Tenant paramTenant1, Tenant paramTenant2, EventInfo paramEventInfo, List<UUID> paramList) {
    this.webhookMapper.deleteAssociationsByTenantId(paramTenant2.id);
    MapperTools.safeCreateUpdate(5000, paramList, paramList -> this.webhookMapper.createAssociationsToWebhooks(paramTenant.id, paramList));
    paramTenant2.configured = paramTenant1.configured;
    if (!paramTenant1.jwtConfiguration.equals(paramTenant2.jwtConfiguration)) {
      Set<UUID> set = (Set)this.applicationReader.retrieveAll(paramTenant2.id, Collections.emptySet()).stream().filter(paramApplication -> (paramApplication.jwtConfiguration == null || !paramApplication.jwtConfiguration.enabled)).map(paramApplication -> paramApplication.id).collect(Collectors.toSet());
      EventHelper.send(paramTenant1, null, new JWTPublicKeyUpdateEvent(paramEventInfo, set));
    } 
    paramTenant2.insertInstant = paramTenant1.insertInstant;
    paramTenant2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramTenant2.state = paramTenant1.state;
    setDefaults(paramTenant2);
    if (paramTenant1.userDeletePolicy.unverified.enabled && paramTenant2.userDeletePolicy.unverified.enabled)
      paramTenant2.userDeletePolicy.unverified.enabledInstant = paramTenant1.userDeletePolicy.unverified.enabledInstant; 
    this.tenantMapper.update(paramTenant2);
    writeVerificationKeys(paramTenant2.id, paramTenant2);
    createConnectorConfiguration(paramTenant2);
  }
  
  public void create(Tenant paramTenant, List<UUID> paramList) {
    _create(paramTenant, paramList);
    this.cacheNotifier.reload(new String[] { "Tenants", "Webhooks" });
  }
  
  public void delete(Tenant paramTenant, EventInfo paramEventInfo) {
    this.userService.deleteAllByTenantId(paramTenant, paramEventInfo);
    this.groupService.deleteAllByTenantId(paramTenant, paramEventInfo);
    this.applicationService.deleteAllByTenantId(paramTenant.id);
    this.apiKeyService.deleteByTenantId(paramTenant.id);
    this.breachedPasswordMapper.deleteMetricsByTenantId(paramTenant.id);
    this.mfaMetricsMapper.deleteMetricsByTenantId(paramTenant.id);
    this.tenantMapper.deleteConnectorPolicies(paramTenant.id);
    this.identityProviderMapper.deleteTenantConfigurationsByTenantId(paramTenant.id);
    this.identityProviderService.deleteAllByTenantId(paramTenant.id);
    this.webhookMapper.deleteAssociationsByTenantId(paramTenant.id);
    this.tenantMapper.deleteVerificationKeys(paramTenant.id);
    this.tenantMapper.delete(paramTenant.id);
    this.cacheNotifier.reload(new String[] { "Tenants", "Webhooks" });
  }
  
  public void deleteAsync(Tenant paramTenant, EventInfo paramEventInfo) {
    this.asyncTaskManager.offer((new AsyncTask()).with(paramAsyncTask -> paramAsyncTask.entityId = paramTenant.id)
        .with(paramAsyncTask -> paramAsyncTask.type = AsyncTask.AsyncTaskType.DeleteTenant));
  }
  
  public void setConfigured(Tenant paramTenant) {
    if (!paramTenant.configured) {
      paramTenant.configured = true;
      this.tenantMapper.update(paramTenant);
    } 
  }
  
  public void update(Tenant paramTenant1, Tenant paramTenant2, EventInfo paramEventInfo, List<UUID> paramList) {
    _update(paramTenant1, paramTenant2, paramEventInfo, paramList);
    this.cacheNotifier.reload(new String[] { "Tenants", "Webhooks" });
  }
  
  public TenantService.ValidationResult validate(Tenant paramTenant, boolean paramBoolean, List<UUID> paramList, UUID paramUUID) {
    UserAction userAction = (paramTenant.failedAuthenticationConfiguration.userActionId != null) ? this.userActionService.retrieveById(paramTenant.failedAuthenticationConfiguration.userActionId) : null;
    ReactorStatus reactorStatus = this.reactorStatusService.retrieveStatus();
    TenantService.ValidationResult validationResult = new TenantService.ValidationResult();
    validationResult.tenant = paramTenant;
    validationResult.existing = (paramTenant.id == null) ? null : this.tenantMapper.retrieveById(paramTenant.id);
    boolean bool1 = SecurityModule.Bcrypt.equals(paramTenant.passwordEncryptionConfiguration.encryptionScheme) ? true : true;
    boolean bool2 = FIPS.isEnabled() ? true : true;
    validationResult




























































































































































































































































































































































































































































































      
      .errors = (new Validator()).notBlank(paramTenant.name, "tenant.name", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.ifTrue(paramBoolean, ()).ifFalse(paramBoolean, ())).ifTrue((paramBoolean && paramTenant.id != null), paramValidator -> paramValidator.notDuplicate(paramValidationResult.existing, "tenantId", new Object[] { paramTenant.id })).ifTrue((paramTenant.themeId != null), paramValidator -> paramValidator.validObject(this.themeService.retrieveById(paramTenant.themeId), "tenant.themeId", new Object[] { paramTenant.themeId })).missing(paramUUID, "sourceTenantId", new Object[0]).ifTrue((paramTenant.formConfiguration.adminUserFormId != null), paramValidator -> paramValidator.holdMyBeer(this.formMapper.retrieveById(paramTenant.formConfiguration.adminUserFormId)).validObject(paramValidator.barkeep(), "tenant.formConfiguration.adminUserFormId", new Object[] { paramTenant.formConfiguration.adminUserFormId }).ifLastCheckHadNoError(())).ifTrue((paramTenant.emailConfiguration.verifyEmail && paramTenant.emailConfiguration.unverified.behavior == UnverifiedBehavior.Gated), paramValidator -> paramValidator.ensure(ReactorStatusValidator.isLicensedFor(paramReactorStatus, ()), "tenant.emailConfiguration.unverified.behavior", "[notLicensed]", new Object[0])).ifTrue((paramTenant.phoneConfiguration.verifyPhoneNumber && paramTenant.phoneConfiguration.unverified.behavior == UnverifiedBehavior.Gated), paramValidator -> paramValidator.ensure(ReactorStatusValidator.isLicensedFor(paramReactorStatus, ()), "tenant.phoneConfiguration.unverified.behavior", "[notLicensed]", new Object[0])).validAbsoluteHttpURL(paramTenant.baseURL, "tenant.baseURL", new Object[] { paramTenant.baseURL }).validAbsoluteHttpURL(paramTenant.logoutURL, "tenant.logoutURL", new Object[] { paramTenant.logoutURL }).ifTrue(paramTenant.captchaConfiguration.enabled, paramValidator -> paramValidator.validate(()).notMissing(paramTenant.captchaConfiguration.captchaMethod, "tenant.captchaConfiguration.captchaMethod", new Object[] { Arrays.<CaptchaMethod>stream(CaptchaMethod.values()).map(Enum::name).collect(Collectors.joining(", ")) }).notBlank(paramTenant.captchaConfiguration.secretKey, "tenant.captchaConfiguration.secretKey", new Object[0]).notBlank(paramTenant.captchaConfiguration.siteKey, "tenant.captchaConfiguration.siteKey", new Object[0]).withinRangeExclusive(paramTenant.captchaConfiguration.threshold, 0.0D, 1.0D, "tenant.captchaConfiguration.threshold", new Object[0])).ifTrue((paramTenant.accessControlConfiguration.uiIPAccessControlListId != null), paramValidator -> paramValidator.validate(()).ensure((this.ipAccessControlListReader.retrieveById(paramTenant.accessControlConfiguration.uiIPAccessControlListId) != null), "tenant.accessControlConfiguration.uiIPAccessControlListId", "[invalid]", new Object[] { paramTenant.accessControlConfiguration.uiIPAccessControlListId })).ifTrue(ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.connectors), paramValidator -> paramValidator.forEach(paramTenant.connectorPolicies, ())).forEach(paramTenant.connectorPolicies, (paramValidator, paramConnectorPolicy, paramInteger) -> paramValidator.ifFalse(paramConnectorPolicy.connectorId.equals(BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID), ()).notMissingWithCode(this.connectorConfigurationMapper.retrieveById(paramConnectorPolicy.connectorId), "tenant.connectorPolicies[" + paramInteger + "].connectorId", "[invalid]tenant.connectorPolicies.connectorId", new Object[] { paramConnectorPolicy.connectorId })).holdMyBeer(paramTenant.connectorPolicies.stream().map(paramConnectorPolicy -> paramConnectorPolicy.connectorId).collect(Collectors.toList())).forEach(paramTenant.connectorPolicies, (paramValidator, paramConnectorPolicy, paramInteger) -> paramValidator.ifLastCheckHadNoError(())).valid(paramTenant.connectorPolicies.stream().anyMatch(paramConnectorPolicy -> paramConnectorPolicy.connectorId.equals(BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID)), "tenant.connectorPolicies", new Object[0]).ifTrue(paramTenant.maximumPasswordAge.enabled, paramValidator -> paramValidator.ensure((paramTenant.maximumPasswordAge.days > 0), "tenant.maximumPasswordAge.days", "[tooSmall]", new Object[0])).ifTrue(paramTenant.minimumPasswordAge.enabled, paramValidator -> paramValidator.ensure((paramTenant.minimumPasswordAge.seconds > 0), "tenant.minimumPasswordAge.seconds", "[tooSmall]", new Object[0])).notMissing(paramTenant.passwordValidationRules, "tenant.passwordValidationRules", new Object[0]).ifTrue((paramTenant.passwordValidationRules != null), paramValidator -> paramValidator.ifTrue(paramTenant.passwordValidationRules.rememberPreviousPasswords.enabled, ()).ensure((paramTenant.passwordValidationRules.minLength >= paramInt1), "tenant.passwordValidationRules.minLength", "[tooSmall]", new Object[] { Integer.valueOf(paramInt1) }).ensure((paramTenant.passwordValidationRules.maxLength > 0), "tenant.passwordValidationRules.maxLength", "[tooSmall]", new Object[0]).ifTrue((paramTenant.passwordEncryptionConfiguration.encryptionScheme != null), ()).ifNoFieldErrors("tenant.passwordValidationRules.maxLength", ())).ensure((paramTenant.jwtConfiguration.timeToLiveInSeconds > 0), "tenant.jwtConfiguration.timeToLiveInSeconds", "[tooSmall]", new Object[0]).ensure((paramTenant.jwtConfiguration.refreshTokenTimeToLiveInMinutes > 0), "tenant.jwtConfiguration.refreshTokenTimeToLiveInMinutes", "[tooSmall]", new Object[0]).ifTrue((paramTenant.jwtConfiguration.refreshTokenExpirationPolicy == RefreshTokenExpirationPolicy.SlidingWindowWithMaximumLifetime), paramValidator -> paramValidator.ensure((paramTenant.jwtConfiguration.refreshTokenSlidingWindowConfiguration.maximumTimeToLiveInMinutes >= paramTenant.jwtConfiguration.refreshTokenTimeToLiveInMinutes), "tenant.jwtConfiguration.refreshTokenSlidingWindowConfiguration.maximumTimeToLiveInMinutes", "[tooSmall]", new Object[] { RefreshTokenExpirationPolicy.SlidingWindowWithMaximumLifetime })).ifTrue((paramTenant.jwtConfiguration.refreshTokenUsagePolicy == RefreshTokenUsagePolicy.OneTimeUse), paramValidator -> paramValidator.ensure((paramTenant.jwtConfiguration.refreshTokenOneTimeUseConfiguration.gracePeriodInSeconds >= 0), "tenant.jwtConfiguration.refreshTokenOneTimeUseConfiguration.gracePeriodInSeconds", "[tooSmall]", new Object[0]).ensure((paramTenant.jwtConfiguration.refreshTokenOneTimeUseConfiguration.gracePeriodInSeconds <= 86400), "tenant.jwtConfiguration.refreshTokenOneTimeUseConfiguration.gracePeriodInSeconds", "[tooLarge]", new Object[0])).ifTrue((paramTenant.jwtConfiguration.accessTokenKeyId != null), paramValidator -> {
          boolean bool = (paramValidationResult.existing == null || !Objects.equals(paramTenant.jwtConfiguration.accessTokenKeyId, paramValidationResult.existing.jwtConfiguration.accessTokenKeyId)) ? true : false;
          if (bool)
            this.keyValidator.validateAccessTokenSigningKey(paramValidator, paramTenant.jwtConfiguration.accessTokenKeyId, "tenant.jwtConfiguration.accessTokenKeyId"); 
        }).ifTrue((paramTenant.jwtConfiguration.idTokenKeyId != null), paramValidator -> {
          boolean bool = (paramValidationResult.existing == null || !Objects.equals(paramTenant.jwtConfiguration.idTokenKeyId, paramValidationResult.existing.jwtConfiguration.idTokenKeyId)) ? true : false;
          if (bool)
            this.keyValidator.validateIdTokenSigningKey(paramValidator, paramTenant.jwtConfiguration.idTokenKeyId, "tenant.jwtConfiguration.idTokenKeyId"); 
        }).forEach(paramTenant.jwtConfiguration.accessTokenVerificationKeyIds, (paramValidator, paramUUID, paramInteger) -> this.keyValidator.validateAccessTokenVerificationKey(paramValidator, paramUUID, "tenant.jwtConfiguration.accessTokenVerificationKeyIds[" + paramInteger + "]", "[cannotVerify]tenant.jwtConfiguration.accessTokenVerificationKeyIds")).forEach(paramTenant.jwtConfiguration.idTokenVerificationKeyIds, (paramValidator, paramUUID, paramInteger) -> this.keyValidator.validateIdTokenVerificationKey(paramValidator, paramUUID, "tenant.jwtConfiguration.idTokenVerificationKeyIds[" + paramInteger + "]", "[cannotVerify]tenant.jwtConfiguration.idTokenVerificationKeyIds")).notMissing(paramTenant.emailConfiguration.port, "tenant.emailConfiguration.port", new Object[0]).ifTrue((paramTenant.emailConfiguration.defaultFromEmail != null), paramValidator -> paramValidator.email(paramTenant.emailConfiguration.defaultFromEmail, "tenant.emailConfiguration.defaultFromEmail", new Object[0])).ifTrue((paramTenant.emailConfiguration.port != null), paramValidator -> paramValidator.ensure((paramTenant.emailConfiguration.port.intValue() > 0 && paramTenant.emailConfiguration.port.intValue() < 65535), "tenant.emailConfiguration.port", "[invalid]", new Object[0])).notBlank(paramTenant.emailConfiguration.host, "tenant.emailConfiguration.host", new Object[0]).ifTrue(paramTenant.emailConfiguration.verifyEmail, paramValidator -> paramValidator.notMissing(paramTenant.emailConfiguration.verificationEmailTemplateId, "tenant.emailConfiguration.verificationEmailTemplateId", new Object[0])).forEach(TenantService.EmailTemplateIdFields, (paramValidator, paramField, paramInteger) -> paramValidator.validate(())).ensure(PropertiesTools.validate(paramTenant.emailConfiguration.properties), "tenant.emailConfiguration.properties", "[invalid]", new Object[0]).ifTrue(paramTenant.passwordValidationRules.breachDetection.enabled, paramValidator -> paramValidator.ensure(ReactorStatusValidator.isLicensedFor(paramReactorStatus, ()), "tenant.passwordValidationRules.breachDetection.enabled", "[notLicensed]", new Object[0]).ifLastCheckHadNoError(())).ifFalse(ReactorStatusValidator.isLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.breachedPasswordDetection), paramValidator -> paramValidator.forEach(EventService.BreachPasswordLicensedEvents, ())).validate(paramValidator -> ReactorStatusValidator.ifNotLicensedForThen(paramReactorStatus, (), ())).validate(paramValidator -> ReactorStatusValidator.ifNotLicensedForThen(paramReactorStatus, (), ())).ifTrue(paramTenant.userDeletePolicy.unverified.enabled, paramValidator -> paramValidator.ensure((paramTenant.userDeletePolicy.unverified.numberOfDaysToRetain > 0), "tenant.userDeletePolicy.unverified.numberOfDaysToRetain", "[tooSmall]", new Object[0])).ensure((paramTenant.failedAuthenticationConfiguration.actionDuration > 0L), "tenant.failedAuthenticationConfiguration.actionDuration", "[tooSmall]", new Object[0]).ensure((paramTenant.failedAuthenticationConfiguration.tooManyAttempts > 0), "tenant.failedAuthenticationConfiguration.tooManyAttempts", "[tooSmall]", new Object[0]).ensure((paramTenant.failedAuthenticationConfiguration.resetCountInSeconds > 0), "tenant.failedAuthenticationConfiguration.resetCountInSeconds", "[tooSmall]", new Object[0]).ifTrue((paramTenant.failedAuthenticationConfiguration.userActionId != null), paramValidator -> paramValidator.notMissing(paramUserAction, "tenant.failedAuthenticationConfiguration.userActionId", new Object[] { paramTenant.failedAuthenticationConfiguration.userActionId }).ifLastCheckHadNoError(()).notMissing(paramTenant.failedAuthenticationConfiguration.actionDurationUnit, "tenant.failedAuthenticationConfiguration.actionDurationUnit", new Object[0])).ifTrue((paramTenant.passwordEncryptionConfiguration.encryptionScheme != null), paramValidator -> paramValidator.ensure(this.passwordEncryptorLibrary.validateScheme(paramTenant.passwordEncryptionConfiguration.encryptionScheme), "tenant.passwordEncryptionConfiguration.encryptionScheme", "[invalid]", new Object[] { paramTenant.passwordEncryptionConfiguration.encryptionScheme }).ensure(this.passwordEncryptorLibrary.validateFactor(paramTenant.passwordEncryptionConfiguration.encryptionScheme, this.defaultEncryptionScheme, Integer.valueOf(paramTenant.passwordEncryptionConfiguration.encryptionSchemeFactor)), "tenant.passwordEncryptionConfiguration.encryptionSchemeFactor", "[invalid]", new Object[] { Integer.valueOf(paramTenant.passwordEncryptionConfiguration.encryptionSchemeFactor) })).ensure((paramTenant.externalIdentifierConfiguration.authorizationGrantIdTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.authorizationGrantIdTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.authorizationGrantIdTimeToLiveInSeconds <= 600), "tenant.externalIdentifierConfiguration.authorizationGrantIdTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.changePasswordIdTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.changePasswordIdTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.loginIntentTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.loginIntentTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.deviceCodeTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.deviceCodeTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.emailVerificationIdTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.emailVerificationIdTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.externalAuthenticationIdTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.externalAuthenticationIdTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.identityProviderConnectionTestTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.identityProviderConnectionTestTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.oneTimePasswordTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.oneTimePasswordTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.passwordlessLoginTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.passwordlessLoginTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.phoneVerificationIdTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.phoneVerificationIdTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.registrationVerificationIdTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.registrationVerificationIdTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.rememberOAuthScopeConsentChoiceTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.rememberOAuthScopeConsentChoiceTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.setupPasswordIdTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.setupPasswordIdTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.trustTokenTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.trustTokenTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.twoFactorIdTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.twoFactorIdTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.twoFactorTrustIdTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.twoFactorTrustIdTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.webAuthnAuthenticationChallengeTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.webAuthnAuthenticationChallengeTimeToLiveInSeconds", "[invalid]", new Object[0]).ensure((paramTenant.externalIdentifierConfiguration.webAuthnRegistrationChallengeTimeToLiveInSeconds > 0), "tenant.externalIdentifierConfiguration.webAuthnRegistrationChallengeTimeToLiveInSeconds", "[invalid]", new Object[0]).validate(paramValidator -> validateExternalIdGenerator(paramValidator, paramTenant.externalIdentifierConfiguration.changePasswordIdGenerator, "tenant.externalIdentifierConfiguration.changePasswordIdGenerator")).validate(paramValidator -> validateExternalIdGenerator(paramValidator, paramTenant.externalIdentifierConfiguration.deviceUserCodeIdGenerator, "tenant.externalIdentifierConfiguration.deviceUserCodeIdGenerator")).validate(paramValidator -> validateExternalIdGenerator(paramValidator, paramTenant.externalIdentifierConfiguration.emailVerificationIdGenerator, "tenant.externalIdentifierConfiguration.emailVerificationIdGenerator")).validate(paramValidator -> validateExternalIdGenerator(paramValidator, paramTenant.externalIdentifierConfiguration.passwordlessLoginGenerator, "tenant.externalIdentifierConfiguration.passwordlessLoginGenerator")).validate(paramValidator -> validateExternalIdGenerator(paramValidator, paramTenant.externalIdentifierConfiguration.passwordlessLoginOneTimeCodeGenerator, "tenant.externalIdentifierConfiguration.passwordlessLoginOneTimeCodeGenerator")).validate(paramValidator -> validateExternalIdGenerator(paramValidator, paramTenant.externalIdentifierConfiguration.registrationVerificationIdGenerator, "tenant.externalIdentifierConfiguration.registrationVerificationIdGenerator")).validate(paramValidator -> validateExternalIdGenerator(paramValidator, paramTenant.externalIdentifierConfiguration.setupPasswordIdGenerator, "tenant.externalIdentifierConfiguration.setupPasswordIdGenerator")).validate(paramValidator -> validateOptionalExternalIdGenerator(paramValidator, paramTenant.externalIdentifierConfiguration.twoFactorOneTimeCodeIdGenerator, "tenant.externalIdentifierConfiguration.twoFactorOneTimeCodeIdGenerator")).validate(paramValidator -> validateOptionalExternalIdGenerator(paramValidator, paramTenant.externalIdentifierConfiguration.emailVerificationOneTimeCodeGenerator, "tenant.externalIdentifierConfiguration.emailVerificationOneTimeCodeGenerator")).validate(paramValidator -> validateOptionalExternalIdGenerator(paramValidator, paramTenant.externalIdentifierConfiguration.phoneVerificationIdGenerator, "tenant.externalIdentifierConfiguration.phoneVerificationIdGenerator")).validate(paramValidator -> validateOptionalExternalIdGenerator(paramValidator, paramTenant.externalIdentifierConfiguration.phoneVerificationOneTimeCodeGenerator, "tenant.externalIdentifierConfiguration.phoneVerificationOneTimeCodeGenerator")).validate(paramValidator -> validateOptionalExternalIdGenerator(paramValidator, paramTenant.externalIdentifierConfiguration.registrationVerificationOneTimeCodeGenerator, "tenant.externalIdentifierConfiguration.registrationVerificationOneTimeCodeGenerator")).ifTrue((paramTenant.familyConfiguration.familyRequestEmailTemplateId != null), paramValidator -> paramValidator.validObject(this.emailTemplateMapper.retrieveById(paramTenant.familyConfiguration.familyRequestEmailTemplateId), "tenant.familyConfiguration.familyRequestEmailTemplateId", new Object[] { paramTenant.familyConfiguration.familyRequestEmailTemplateId })).ifTrue((paramTenant.familyConfiguration.confirmChildEmailTemplateId != null), paramValidator -> paramValidator.validObject(this.emailTemplateMapper.retrieveById(paramTenant.familyConfiguration.confirmChildEmailTemplateId), "tenant.familyConfiguration.confirmChildEmailTemplateId", new Object[] { paramTenant.familyConfiguration.confirmChildEmailTemplateId })).ifTrue((paramTenant.familyConfiguration.parentRegistrationEmailTemplateId != null), paramValidator -> paramValidator.validObject(this.emailTemplateMapper.retrieveById(paramTenant.familyConfiguration.parentRegistrationEmailTemplateId), "tenant.familyConfiguration.parentRegistrationEmailTemplateId", new Object[] { paramTenant.familyConfiguration.parentRegistrationEmailTemplateId })).ifTrue(paramTenant.familyConfiguration.enabled, paramValidator -> paramValidator.ensure((paramTenant.familyConfiguration.maximumChildAge > 0), "tenant.familyConfiguration.maximumChildAge", "[invalid]", new Object[0]).ensure((paramTenant.familyConfiguration.minimumOwnerAge > 0), "tenant.familyConfiguration.minimumOwnerAge", "[invalid]", new Object[0]).ensure((paramTenant.familyConfiguration.deleteOrphanedAccountsDays > 0), "tenant.familyConfiguration.deleteOrphanedAccountsDays", "[invalid]", new Object[0]).ensure((paramTenant.familyConfiguration.maximumChildAge < paramTenant.familyConfiguration.minimumOwnerAge), "tenant.familyConfiguration.minimumOwnerAge", "[tooSmall]", new Object[0])).ifTrue((paramTenant.multiFactorConfiguration.email.templateId != null), paramValidator -> paramValidator.validObject(this.emailTemplateMapper.retrieveById(paramTenant.multiFactorConfiguration.email.templateId), "tenant.multiFactorConfiguration.email.templateId", new Object[] { paramTenant.multiFactorConfiguration.email.templateId })).ifTrue((paramTenant.multiFactorConfiguration.sms.templateId != null), paramValidator -> paramValidator.holdMyBeer(this.messageTemplateMapper.retrieveById(paramTenant.multiFactorConfiguration.sms.templateId)).validObject(paramValidator.barkeep(), "tenant.multiFactorConfiguration.sms.templateId", new Object[] { paramTenant.multiFactorConfiguration.sms.templateId }).ifLastCheckHadNoError(())).ifTrue((paramTenant.multiFactorConfiguration.sms.messengerId != null), paramValidator -> paramValidator.holdMyBeer(this.messengerConfigurationMapper.retrieveById(paramTenant.multiFactorConfiguration.sms.messengerId)).validObject(paramValidator.barkeep(BaseMessengerConfiguration.class), "tenant.multiFactorConfiguration.sms.messengerId", new Object[] { paramTenant.multiFactorConfiguration.sms.messengerId }).ifLastCheckHadNoError(())).ifTrue((paramTenant.multiFactorConfiguration.voice.templateId != null), paramValidator -> paramValidator.holdMyBeer(this.messageTemplateMapper.retrieveById(paramTenant.multiFactorConfiguration.voice.templateId)).validObject(paramValidator.barkeep(), "tenant.multiFactorConfiguration.voice.templateId", new Object[] { paramTenant.multiFactorConfiguration.voice.templateId }).ifLastCheckHadNoError(())).ifTrue((paramTenant.multiFactorConfiguration.voice.messengerId != null), paramValidator -> paramValidator.holdMyBeer(this.messengerConfigurationMapper.retrieveById(paramTenant.multiFactorConfiguration.voice.messengerId)).validObject(paramValidator.barkeep(BaseMessengerConfiguration.class), "tenant.multiFactorConfiguration.voice.messengerId", new Object[] { paramTenant.multiFactorConfiguration.voice.messengerId }).ifLastCheckHadNoError(())).ifTrue(paramTenant.multiFactorConfiguration.email.enabled, paramValidator -> paramValidator.ensure(ReactorStatusValidator.isLicensedFor(paramReactorStatus, ()), "tenant.multiFactorConfiguration.email.enabled", "[notLicensed]", new Object[0]).notMissing(paramTenant.multiFactorConfiguration.email.templateId, "tenant.multiFactorConfiguration.email.templateId", new Object[0])).ifTrue(paramTenant.multiFactorConfiguration.sms.enabled, paramValidator -> paramValidator.ensure(ReactorStatusValidator.isLicensedFor(paramReactorStatus, ()), "tenant.multiFactorConfiguration.sms.enabled", "[notLicensed]", new Object[0]).notMissing(paramTenant.multiFactorConfiguration.sms.templateId, "tenant.multiFactorConfiguration.sms.templateId", new Object[0]).notMissing(paramTenant.multiFactorConfiguration.sms.messengerId, "tenant.multiFactorConfiguration.sms.messengerId", new Object[0])).ifTrue(paramTenant.multiFactorConfiguration.voice.enabled, paramValidator -> paramValidator.ensure(ReactorStatusValidator.isLicensedFor(paramReactorStatus, ()), "tenant.multiFactorConfiguration.voice.enabled", "[notLicensed]", new Object[0]).notMissing(paramTenant.multiFactorConfiguration.voice.templateId, "tenant.multiFactorConfiguration.voice.templateId", new Object[0]).notMissing(paramTenant.multiFactorConfiguration.voice.messengerId, "tenant.multiFactorConfiguration.voice.messengerId", new Object[0])).ifTrue(MFATools.loginPolicyEnabled(paramTenant.multiFactorConfiguration.loginPolicy), paramValidator -> paramValidator.ensure((paramTenant.multiFactorConfiguration.authenticator.enabled || paramTenant.multiFactorConfiguration.email.enabled || paramTenant.multiFactorConfiguration.sms.enabled || paramTenant.multiFactorConfiguration.voice.enabled), "tenant.multiFactorConfiguration.loginPolicy", "[invalid]", new Object[0])).ifTrue((paramTenant.multiFactorConfiguration.loginPolicy == MultiFactorLoginPolicy.ChallengeOnHighRisk || paramTenant.multiFactorConfiguration.loginPolicy == MultiFactorLoginPolicy.ChallengeOnMediumRisk), paramValidator -> paramValidator.ensure(ReactorStatusValidator.isLicensedFor(paramReactorStatus, ()), "tenant.multiFactorConfiguration.loginPolicy", "[notLicensedFor]", new Object[0])).ifTrue(paramTenant.clientRiskConfiguration.enabled, paramValidator -> paramValidator.ensure(ReactorStatusValidator.isLicensedFor(paramReactorStatus, ()), "tenant.clientRiskConfiguration.enabled", "[notLicensedFor]", new Object[0])).validate(paramValidator -> validateRateLimit(paramValidator, paramReactorStatus, paramTenant.rateLimitConfiguration.failedLogin, "tenant.rateLimitConfiguration.failedLogin")).validate(paramValidator -> validateRateLimit(paramValidator, paramReactorStatus, paramTenant.rateLimitConfiguration.forgotPassword, "tenant.rateLimitConfiguration.forgotPassword")).validate(paramValidator -> validateRateLimit(paramValidator, paramReactorStatus, paramTenant.rateLimitConfiguration.sendEmailVerification, "tenant.rateLimitConfiguration.sendEmailVerification")).validate(paramValidator -> validateRateLimit(paramValidator, paramReactorStatus, paramTenant.rateLimitConfiguration.sendPhoneVerification, "tenant.rateLimitConfiguration.sendPhoneVerification")).validate(paramValidator -> validateRateLimit(paramValidator, paramReactorStatus, paramTenant.rateLimitConfiguration.sendRegistrationVerification, "tenant.rateLimitConfiguration.sendRegistrationVerification")).validate(paramValidator -> validateRateLimit(paramValidator, paramReactorStatus, paramTenant.rateLimitConfiguration.sendPasswordless, "tenant.rateLimitConfiguration.sendPasswordless")).validate(paramValidator -> validateRateLimit(paramValidator, paramReactorStatus, paramTenant.rateLimitConfiguration.sendPasswordlessPhone, "tenant.rateLimitConfiguration.sendPasswordlessPhone")).validate(paramValidator -> validateRateLimit(paramValidator, paramReactorStatus, paramTenant.rateLimitConfiguration.sendTwoFactor, "tenant.rateLimitConfiguration.sendTwoFactor")).ifTrue(!paramTenant.registrationConfiguration.blockedDomains.isEmpty(), paramValidator -> paramValidator.validate(())).ifTrue((paramTenant.phoneConfiguration.messengerId != null), paramValidator -> paramValidator.validObject(this.messengerConfigurationMapper.retrieveById(paramTenant.phoneConfiguration.messengerId), "tenant.phoneConfiguration.messengerId", new Object[] { paramTenant.phoneConfiguration.messengerId })).forEach(TenantService.PhoneTemplateIdFields, (paramValidator, paramField, paramInteger) -> paramValidator.validate(())).ifTrue((paramTenant.phoneConfiguration.verifyPhoneNumber || paramTenant.phoneConfiguration.adminTwoFactorMethodRemoveTemplateId != null || paramTenant.phoneConfiguration.forgotPasswordTemplateId != null || paramTenant.phoneConfiguration.identityUpdateTemplateId != null || paramTenant.phoneConfiguration.loginIdInUseOnCreateTemplateId != null || paramTenant.phoneConfiguration.loginIdInUseOnUpdateTemplateId != null || paramTenant.phoneConfiguration.loginNewDeviceTemplateId != null || paramTenant.phoneConfiguration.loginSuspiciousTemplateId != null || paramTenant.phoneConfiguration.passwordlessTemplateId != null || paramTenant.phoneConfiguration.passwordResetSuccessTemplateId != null || paramTenant.phoneConfiguration.passwordUpdateTemplateId != null || paramTenant.phoneConfiguration.setPasswordTemplateId != null || paramTenant.phoneConfiguration.twoFactorMethodAddTemplateId != null || paramTenant.phoneConfiguration.twoFactorMethodRemoveTemplateId != null || paramTenant.phoneConfiguration.verificationCompleteTemplateId != null || paramTenant.phoneConfiguration.verificationTemplateId != null), paramValidator -> paramValidator.notMissing(paramTenant.phoneConfiguration.messengerId, "tenant.phoneConfiguration.messengerId", new Object[0])).ifTrue(paramTenant.phoneConfiguration.verifyPhoneNumber, paramValidator -> paramValidator.notMissing(paramTenant.phoneConfiguration.verificationTemplateId, "tenant.phoneConfiguration.verificationTemplateId", new Object[0])).ifTrue((paramTenant.phoneConfiguration.verifyPhoneNumber && paramTenant.phoneConfiguration.verificationStrategy == VerificationStrategy.FormField), paramValidator -> paramValidator.ensure((paramTenant.phoneConfiguration.unverified.behavior == UnverifiedBehavior.Gated), "tenant.phoneConfiguration.unverified.behavior", "[invalid]", new Object[] { paramTenant.phoneConfiguration.unverified.behavior })).ensure((paramTenant.ssoConfiguration.deviceTrustTimeToLiveInSeconds > 0), "tenant.ssoConfiguration.deviceTrustTimeToLiveInSeconds", "[invalid]", new Object[0]).ifTrue(paramTenant.usernameConfiguration.unique.enabled, paramValidator -> paramValidator.ensure(ReactorStatusValidator.isLicensedFor(paramReactorStatus, ()), "tenant.usernameConfiguration.unique.enabled", "[notLicensed]", new Object[0])).ifTrue((paramTenant.usernameConfiguration.unique.numberOfDigits != 0), paramValidator -> paramValidator.ensure((paramTenant.usernameConfiguration.unique.numberOfDigits >= 3 && paramTenant.usernameConfiguration.unique.numberOfDigits <= 10), "tenant.usernameConfiguration.unique.numberOfDigits", "[invalid]", new Object[] { Integer.valueOf(3), Integer.valueOf(10) })).ifTrue((paramTenant.usernameConfiguration.unique.separator != null), paramValidator -> paramValidator.ensure(StringTools.isSeparator(paramTenant.usernameConfiguration.unique.separator.charValue()), "tenant.usernameConfiguration.unique.separator", "[invalid]", new Object[] { paramTenant.usernameConfiguration.unique.separator.toString() })).validate(paramValidator -> this.lambdaValidator.validateOptional(paramValidator, paramTenant.oauthConfiguration.clientCredentialsAccessTokenPopulateLambdaId, LambdaType.ClientCredentialsJWTPopulate, "tenant.oauthConfiguration.clientCredentialsAccessTokenPopulateLambdaId")).validate(paramValidator -> this.lambdaValidator.validateOptional(paramValidator, paramTenant.lambdaConfiguration.loginValidationId, LambdaType.LoginValidation, "tenant.lambdaConfiguration.loginValidationId")).validate(paramValidator -> this.lambdaValidator.validateOptional(paramValidator, paramTenant.lambdaConfiguration.multiFactorRequirementId, LambdaType.MFARequirement, "tenant.lambdaConfiguration.multiFactorRequirementId")).ifTrue((paramTenant.lambdaConfiguration.multiFactorRequirementId != null), paramValidator -> paramValidator.validate(())).ifTrue(paramTenant.scimServerConfiguration.enabled, paramValidator -> paramValidator.validate(())).validate(paramValidator -> this.lambdaValidator.validate(paramValidator, paramTenant.scimServerConfiguration.enabled, paramTenant.lambdaConfiguration.scimGroupRequestConverterId, LambdaType.SCIMServerGroupRequestConverter, "tenant.lambdaConfiguration.scimGroupRequestConverterId")).validate(paramValidator -> this.lambdaValidator.validate(paramValidator, paramTenant.scimServerConfiguration.enabled, paramTenant.lambdaConfiguration.scimGroupResponseConverterId, LambdaType.SCIMServerGroupResponseConverter, "tenant.lambdaConfiguration.scimGroupResponseConverterId")).validate(paramValidator -> this.lambdaValidator.validate(paramValidator, paramTenant.scimServerConfiguration.enabled, paramTenant.lambdaConfiguration.scimEnterpriseUserRequestConverterId, LambdaType.SCIMServerUserRequestConverter, "tenant.lambdaConfiguration.scimEnterpriseUserRequestConverterId")).validate(paramValidator -> this.lambdaValidator.validate(paramValidator, paramTenant.scimServerConfiguration.enabled, paramTenant.lambdaConfiguration.scimEnterpriseUserResponseConverterId, LambdaType.SCIMServerUserResponseConverter, "tenant.lambdaConfiguration.scimEnterpriseUserResponseConverterId")).validate(paramValidator -> this.lambdaValidator.validate(paramValidator, paramTenant.scimServerConfiguration.enabled, paramTenant.lambdaConfiguration.scimUserRequestConverterId, LambdaType.SCIMServerUserRequestConverter, "tenant.lambdaConfiguration.scimUserRequestConverterId")).validate(paramValidator -> this.lambdaValidator.validate(paramValidator, paramTenant.scimServerConfiguration.enabled, paramTenant.lambdaConfiguration.scimUserResponseConverterId, LambdaType.SCIMServerUserResponseConverter, "tenant.lambdaConfiguration.scimUserResponseConverterId")).ifTrue(paramTenant.scimServerConfiguration.enabled, paramValidator -> paramValidator.notMissing(paramTenant.scimServerConfiguration.clientEntityTypeId, "tenant.scimServerConfiguration.clientEntityTypeId", new Object[0])).ifTrue(paramTenant.scimServerConfiguration.enabled, paramValidator -> paramValidator.notMissing(paramTenant.scimServerConfiguration.serverEntityTypeId, "tenant.scimServerConfiguration.serverEntityTypeId", new Object[0])).ifTrue((paramTenant.scimServerConfiguration.clientEntityTypeId != null), paramValidator -> paramValidator.validObject(this.entityMapper.retrieveTypeById(paramTenant.scimServerConfiguration.clientEntityTypeId), "tenant.scimServerConfiguration.clientEntityTypeId", new Object[] { paramTenant.scimServerConfiguration.clientEntityTypeId })).ifTrue((paramTenant.scimServerConfiguration.serverEntityTypeId != null), paramValidator -> paramValidator.validObject(this.entityMapper.retrieveTypeById(paramTenant.scimServerConfiguration.serverEntityTypeId), "tenant.scimServerConfiguration.serverEntityTypeId", new Object[] { paramTenant.scimServerConfiguration.serverEntityTypeId })).ifTrue(paramTenant.webAuthnConfiguration.enabled, paramValidator -> paramValidator.validate(()).ifLastCheckHadNoError(())).forEach(paramList, (paramValidator, paramUUID, paramInteger) -> paramValidator.holdMyBeer(this.webhookMapper.retrieveById(paramUUID)).validObjectWithCode(paramValidator.barkeep(), "webhookIds[" + paramInteger + "]", "[invalid]webhookIds", new Object[] { paramUUID }).ifLastCheckHadNoError(())).done();
    return validationResult;
  }
  
  public TenantService.ValidationResult validateCopy(Tenant paramTenant, UUID paramUUID) {
    TenantService.ValidationResult validationResult = new TenantService.ValidationResult();
    Tenant tenant = this.tenantMapper.retrieveById(paramUUID);
    validationResult.existing = (paramTenant.id == null) ? null : this.tenantMapper.retrieveById(paramTenant.id);
    validationResult.tenant = tenant;
    if (validationResult.tenant != null) {
      validationResult.tenant.id = paramTenant.id;
      validationResult.tenant.name = paramTenant.name;
    } 
    validationResult









      
      .errors = (new Validator()).notBlank(paramTenant.name, "tenant.name", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.notDuplicate(this.tenantMapper.retrieveExistingByName(null, paramTenant.name), "tenant.name", new Object[] { paramTenant.name })).ifTrue((paramTenant.id != null), paramValidator -> paramValidator.notDuplicate(paramValidationResult.existing, "tenantId", new Object[] { paramTenant.id })).valid((tenant != null), "sourceTenantId", new Object[] { paramUUID }).done();
    return validationResult;
  }
  
  public TenantService.ValidationResult validateDelete(Tenant paramTenant, UUID paramUUID) {
    TenantService.ValidationResult validationResult = new TenantService.ValidationResult();
    validationResult.existing = (paramUUID != null && paramTenant == null) ? this.tenantMapper.retrieveById(paramUUID) : null;
    validationResult






      
      .errors = (new Validator()).notMissing(paramUUID, "tenantId", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(!paramUUID.equals(this.tenantReader.retrieveFusionAuthTenantId()), "tenantId", "[fusionAuth]", new Object[0]).ifTrue((paramValidationResult.existing != null), ()).ensure((paramTenant == null || !paramTenant.id.equals(paramUUID)), "tenantId", "[restricted]", new Object[0])).done();
    return validationResult;
  }
  
  public TenantService.ValidationResult validateGet(Tenant paramTenant, UUID paramUUID) {
    TenantService.ValidationResult validationResult = new TenantService.ValidationResult();
    if (paramTenant != null && paramUUID != null && !paramTenant.id.equals(paramUUID))
      return validationResult; 
    if (paramTenant != null && paramUUID == null) {
      validationResult.errors = new Errors();
      validationResult.errors.addGeneralError("[restricted]", null, new Object[0]);
    } 
    validationResult.existing = this.tenantMapper.retrieveById(paramUUID);
    return validationResult;
  }
  
  private void createConnectorConfiguration(Tenant paramTenant) {
    this.tenantMapper.deleteConnectorPolicies(paramTenant.id);
    this.tenantMapper.createConnectorPolicies(paramTenant.id, paramTenant.connectorPolicies);
  }
  
  private void optionalTemplate(Validator paramValidator, TenantPhoneConfiguration paramTenantPhoneConfiguration, Field paramField, String paramString) {
    try {
      Optional.<UUID>ofNullable((UUID)paramField.get(paramTenantPhoneConfiguration))
        .map(paramUUID -> paramValidator.validObject(this.messageTemplateMapper.retrieveById(paramUUID), paramString, new Object[] { paramUUID }));
    } catch (IllegalAccessException illegalAccessException) {
      throw new ErrorException(illegalAccessException, new Object[0]);
    } 
  }
  
  private void optionalTemplate(Validator paramValidator, EmailConfiguration paramEmailConfiguration, Field paramField, String paramString) {
    try {
      UUID uUID = (UUID)paramField.get(paramEmailConfiguration);
      if (uUID != null)
        paramValidator.validObject(this.emailTemplateMapper.retrieveById(uUID), paramString, new Object[] { uUID }); 
    } catch (IllegalAccessException illegalAccessException) {
      throw new ErrorException(illegalAccessException, new Object[0]);
    } 
  }
  
  private void setDefaults(Tenant paramTenant) {
    Tenant tenant = this.tenantMapper.retrieveTenantByApplicationId(Application.FUSIONAUTH_APP_ID);
    paramTenant.formConfiguration.adminUserFormId = DefaultTools.<UUID>defaultIfNull(paramTenant.formConfiguration.adminUserFormId, tenant.formConfiguration.adminUserFormId);
    paramTenant.issuer = DefaultTools.<String>defaultIfNull(paramTenant.issuer, tenant.issuer);
    paramTenant.jwtConfiguration.accessTokenKeyId = DefaultTools.<UUID>defaultIfNull(paramTenant.jwtConfiguration.accessTokenKeyId, tenant.jwtConfiguration.accessTokenKeyId);
    paramTenant.jwtConfiguration.idTokenKeyId = DefaultTools.<UUID>defaultIfNull(paramTenant.jwtConfiguration.idTokenKeyId, tenant.jwtConfiguration.idTokenKeyId);
    paramTenant.themeId = DefaultTools.<UUID>defaultIfNull(paramTenant.themeId, tenant.themeId);
    PasswordEncryptor passwordEncryptor = this.passwordEncryptorLibrary.lookup(this.defaultEncryptionScheme);
    paramTenant.passwordEncryptionConfiguration.encryptionScheme = DefaultTools.<String>defaultIfNull(paramTenant.passwordEncryptionConfiguration.encryptionScheme, this.defaultEncryptionScheme);
    paramTenant.passwordEncryptionConfiguration.encryptionSchemeFactor = DefaultTools.defaultIfZero(paramTenant.passwordEncryptionConfiguration.encryptionSchemeFactor, passwordEncryptor.defaultFactor());
    if (paramTenant.scimServerConfiguration.enabled && paramTenant.scimServerConfiguration.schemas == null)
      try {
        paramTenant.scimServerConfiguration.schemas = (Map<String, Object>)this.objectMapper.readerFor(LinkedHashMap.class).readValue(SystemDefaultsSingleton.class.getResourceAsStream("/scim/default-schemas.json"));
      } catch (IOException iOException) {
        throw new ErrorException(iOException, new Object[0]);
      }  
    paramTenant.userDeletePolicy.unverified
      
      .enabledInstant = paramTenant.userDeletePolicy.unverified.enabled ? ZonedDateTime.now(ZoneOffset.UTC) : null;
  }
  
  private void validateExternalIdGenerator(Validator paramValidator, SecureGeneratorConfiguration paramSecureGeneratorConfiguration, String paramString) {
    paramValidator
      
      .ifTrue((paramSecureGeneratorConfiguration == null), paramValidator -> paramValidator.notMissing(null, paramString + ".length", new Object[0]).notMissing(null, paramString + ".type", new Object[0]))


      
      .ifFalse((paramSecureGeneratorConfiguration == null), paramValidator -> paramValidator.notMissing(Integer.valueOf(paramSecureGeneratorConfiguration.length), paramString + ".length", new Object[0]).notMissing(paramSecureGeneratorConfiguration.type, paramString + ".type", new Object[0]))


      
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ifTrue((paramSecureGeneratorConfiguration.type == SecureGeneratorType.randomDigits), ()))



      
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ifTrue((paramSecureGeneratorConfiguration.type == SecureGeneratorType.randomBytes), ()))



      
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ifTrue((paramSecureGeneratorConfiguration.type == SecureGeneratorType.randomAlpha), ()))



      
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ifTrue((paramSecureGeneratorConfiguration.type == SecureGeneratorType.randomAlphaNumeric), ()));
  }
  
  private boolean validateFormIdEqualsDefaultTenantFormId(UUID paramUUID) {
    Tenant tenant = this.tenantMapper.retrieveTenantByApplicationId(Application.FUSIONAUTH_APP_ID);
    return tenant.formConfiguration.adminUserFormId.equals(paramUUID);
  }
  
  private void validateOptionalExternalIdGenerator(Validator paramValidator, SecureGeneratorConfiguration paramSecureGeneratorConfiguration, String paramString) {
    if (paramSecureGeneratorConfiguration == null)
      return; 
    validateExternalIdGenerator(paramValidator, paramSecureGeneratorConfiguration, paramString);
  }
  
  private void validateRateLimit(Validator paramValidator, ReactorStatus paramReactorStatus, RateLimitedRequestConfiguration paramRateLimitedRequestConfiguration, String paramString) {
    paramValidator
      .ifTrue(paramRateLimitedRequestConfiguration.enabled, paramValidator2 -> paramValidator2.valid((paramRateLimitedRequestConfiguration.limit > 0), paramString + ".limit", new Object[0]).valid((paramRateLimitedRequestConfiguration.timePeriodInSeconds > 0), paramString + ".timePeriodInSeconds", new Object[0]).validate(()));
  }
  
  private void writeVerificationKeys(UUID paramUUID, Tenant paramTenant) {
    this.tenantMapper.deleteVerificationKeys(paramUUID);
    if (!paramTenant.jwtConfiguration.accessTokenVerificationKeyIds.isEmpty())
      this.tenantMapper.createVerificationKeys(paramUUID, TenantKeyType.AccessTokenVerification.name(), paramTenant.jwtConfiguration.accessTokenVerificationKeyIds, false); 
    if (!paramTenant.jwtConfiguration.idTokenVerificationKeyIds.isEmpty())
      this.tenantMapper.createVerificationKeys(paramUUID, TenantKeyType.IdTokenVerification.name(), paramTenant.jwtConfiguration.idTokenVerificationKeyIds, false); 
  }
}
