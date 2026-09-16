package io.fusionauth.api.service.application;

import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.error.Errors;
import com.inversoft.util.SecurityTools;
import com.inversoft.util.StringTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ApplicationKeyType;
import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.api.domain.EmailTemplateMapper;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.ExternalIdentifierMapper;
import io.fusionauth.api.domain.FormMapper;
import io.fusionauth.api.domain.GroupMapper;
import io.fusionauth.api.domain.IdentityProviderMapper;
import io.fusionauth.api.domain.LoginMapper;
import io.fusionauth.api.domain.MessageTemplateMapper;
import io.fusionauth.api.domain.RefreshTokenMapper;
import io.fusionauth.api.domain.ThemeMapper;
import io.fusionauth.api.domain.UserActionLogMapper;
import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.api.service.count.RegistrationCountService;
import io.fusionauth.api.service.integrations.IntegrationService;
import io.fusionauth.api.service.ip.IPAccessControlListReaderService;
import io.fusionauth.api.service.oauth2.DefaultOAuthService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.api.service.system.KeyHelper;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.api.service.system.KeyService;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.service.system.TenantService;
import io.fusionauth.api.util.EnumValidator;
import io.fusionauth.api.util.KeyValidator;
import io.fusionauth.api.util.LambdaValidator;
import io.fusionauth.api.util.MFATools;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.api.util.URITools;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationMultiFactorTrustPolicy;
import io.fusionauth.domain.ApplicationOAuthScope;
import io.fusionauth.domain.ApplicationPhoneConfiguration;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Integrations;
import io.fusionauth.domain.JWTConfiguration;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.ObjectState;
import io.fusionauth.domain.RefreshTokenExpirationPolicy;
import io.fusionauth.domain.RefreshTokenUsagePolicy;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Tenantable;
import io.fusionauth.domain.UnverifiedBehavior;
import io.fusionauth.domain.event.JWTPublicKeyUpdateEvent;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormType;
import io.fusionauth.domain.message.MessageTemplate;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.oauth2.ClientAuthenticationPolicy;
import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.domain.oauth2.OAuthApplicationRelationship;
import io.fusionauth.domain.oauth2.OAuthScopeConsentMode;
import io.fusionauth.domain.oauth2.ProvidedScopePolicy;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.domain.util.DefaultTools;
import io.fusionauth.samlv2.domain.DigestAlgorithm;
import io.fusionauth.samlv2.domain.EncryptionAlgorithm;
import io.fusionauth.samlv2.domain.KeyLocation;
import io.fusionauth.samlv2.domain.KeyTransportAlgorithm;
import io.fusionauth.samlv2.domain.MaskGenerationFunction;
import io.fusionauth.samlv2.domain.NameIDFormat;
import java.lang.reflect.Field;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mybatis.guice.transactional.Transactional;
import org.primeframework.mvc.ErrorException;

public class DefaultApplicationService implements ApplicationService {
  private final ApplicationMapper applicationMapper;
  
  private final ApplicationReaderService applicationReader;
  
  private final CacheNotifier cacheNotifier;
  
  private final EmailTemplateMapper emailTemplateMapper;
  
  private final ExternalIdentifierMapper externalIdentifierMapper;
  
  private final FormMapper formMapper;
  
  private final GroupMapper groupMapper;
  
  private final IdentityProviderMapper identityProviderMapper;
  
  private final IntegrationService integrationService;
  
  private final IPAccessControlListReaderService ipAccessControlListReader;
  
  private final KeyReaderService keyReader;
  
  private final KeyService keyService;
  
  private final KeyValidator keyValidator;
  
  private final LambdaValidator lambdaValidator;
  
  private final LoginMapper loginMapper;
  
  private final MessageTemplateMapper messageTemplateMapper;
  
  private final ReactorStatusService reactorStatusService;
  
  private final RefreshTokenMapper refreshTokenMapper;
  
  private final RegistrationCountService registrationCountService;
  
  private final UUID tenantManagerApplicationId;
  
  private final TenantReaderService tenantReader;
  
  private final ThemeMapper themeMapper;
  
  private final UserActionLogMapper userActionLogMapper;
  
  private final UserMapper userMapper;
  
  @Inject
  public DefaultApplicationService(ApplicationMapper paramApplicationMapper, ApplicationReaderService paramApplicationReaderService, CacheNotifier paramCacheNotifier, EmailTemplateMapper paramEmailTemplateMapper, ExternalIdentifierMapper paramExternalIdentifierMapper, FormMapper paramFormMapper, GroupMapper paramGroupMapper, IdentityProviderMapper paramIdentityProviderMapper, IntegrationService paramIntegrationService, IPAccessControlListReaderService paramIPAccessControlListReaderService, KeyReaderService paramKeyReaderService, KeyService paramKeyService, KeyValidator paramKeyValidator, LambdaValidator paramLambdaValidator, LoginMapper paramLoginMapper, MessageTemplateMapper paramMessageTemplateMapper, @TenantManagerApplicationId UUID paramUUID, ReactorStatusService paramReactorStatusService, RefreshTokenMapper paramRefreshTokenMapper, RegistrationCountService paramRegistrationCountService, TenantReaderService paramTenantReaderService, ThemeMapper paramThemeMapper, UserActionLogMapper paramUserActionLogMapper, UserMapper paramUserMapper) {
    this.applicationMapper = paramApplicationMapper;
    this.applicationReader = paramApplicationReaderService;
    this.cacheNotifier = paramCacheNotifier;
    this.emailTemplateMapper = paramEmailTemplateMapper;
    this.externalIdentifierMapper = paramExternalIdentifierMapper;
    this.formMapper = paramFormMapper;
    this.groupMapper = paramGroupMapper;
    this.identityProviderMapper = paramIdentityProviderMapper;
    this.integrationService = paramIntegrationService;
    this.ipAccessControlListReader = paramIPAccessControlListReaderService;
    this.keyReader = paramKeyReaderService;
    this.keyService = paramKeyService;
    this.keyValidator = paramKeyValidator;
    this.lambdaValidator = paramLambdaValidator;
    this.loginMapper = paramLoginMapper;
    this.messageTemplateMapper = paramMessageTemplateMapper;
    this.tenantManagerApplicationId = paramUUID;
    this.reactorStatusService = paramReactorStatusService;
    this.refreshTokenMapper = paramRefreshTokenMapper;
    this.registrationCountService = paramRegistrationCountService;
    this.tenantReader = paramTenantReaderService;
    this.themeMapper = paramThemeMapper;
    this.userActionLogMapper = paramUserActionLogMapper;
    this.userMapper = paramUserMapper;
  }
  
  @Transactional
  public void _create(Tenant paramTenant, Application paramApplication) {
    paramApplication.active = true;
    paramApplication.state = ObjectState.Active;
    if (paramApplication.id == null)
      paramApplication.id = UUID.randomUUID(); 
    if (paramTenant != null)
      paramApplication.tenantId = paramTenant.id; 
    if (paramApplication.cleanSpeakConfiguration != null) {
      Integrations integrations = this.integrationService.retrieve();
      paramApplication.cleanSpeakConfiguration.enabled = integrations.cleanspeak.enabled;
    } 
    paramApplication.oauthConfiguration.clientId = paramApplication.id.toString();
    if (paramApplication.oauthConfiguration.clientSecret == null)
      paramApplication.oauthConfiguration.clientSecret = SecurityTools.secureRandom(); 
    generateSAMLv2Keys(paramApplication);
    if (paramApplication.jwtConfiguration.enabled)
      generateJWTSigningKeys(paramApplication); 
    if (paramApplication.registrationConfiguration.type == null)
      paramApplication.registrationConfiguration.type = Application.RegistrationConfiguration.RegistrationType.basic; 
    setDefaults(paramApplication);
    paramApplication.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramApplication.lastUpdateInstant = paramApplication.insertInstant;
    normalizeVerificationKeyIds(paramApplication, null);
    this.applicationMapper.create(paramApplication);
    writeVerificationKeys(paramApplication);
    if (!paramApplication.scopes.isEmpty()) {
      paramApplication.scopes.forEach(paramApplicationOAuthScope -> {
            if (paramApplicationOAuthScope.id == null)
              paramApplicationOAuthScope.id = UUID.randomUUID(); 
            paramApplicationOAuthScope.applicationId = paramApplication.id;
            paramApplicationOAuthScope.insertInstant = paramApplication.insertInstant;
            paramApplicationOAuthScope.lastUpdateInstant = paramApplicationOAuthScope.insertInstant;
          });
      Objects.requireNonNull(this.applicationMapper);
      MapperTools.safeCreateUpdate(8000, paramApplication.scopes, this.applicationMapper::createOAuthScopes);
    } 
    if (!paramApplication.roles.isEmpty()) {
      paramApplication.roles.forEach(paramApplicationRole -> {
            if (paramApplicationRole.id == null)
              paramApplicationRole.id = UUID.randomUUID(); 
            paramApplicationRole.applicationId = paramApplication.id;
            paramApplicationRole.insertInstant = paramApplication.insertInstant;
            paramApplicationRole.lastUpdateInstant = paramApplicationRole.insertInstant;
          });
      Objects.requireNonNull(this.applicationMapper);
      MapperTools.safeCreateUpdate(5000, paramApplication.roles, this.applicationMapper::createRoles);
    } 
    if (paramApplication.cleanSpeakConfiguration != null)
      MapperTools.safeCreateUpdate(5000, paramApplication.cleanSpeakConfiguration.applicationIds, paramList -> this.applicationMapper.createCleanSpeakApplicationIds(paramApplication.id, paramList)); 
  }
  
  @Transactional
  public void _reactivate(Application paramApplication) {
    if (paramApplication.state == ObjectState.Active)
      return; 
    paramApplication.active = true;
    paramApplication.state = ObjectState.Active;
    this.applicationMapper.reactivate(paramApplication.id);
  }
  
  @Transactional
  public void _update(Tenant paramTenant, Application paramApplication1, Application paramApplication2) {
    if (paramApplication1.cleanSpeakConfiguration != null) {
      Integrations integrations = this.integrationService.retrieve();
      paramApplication1.cleanSpeakConfiguration.enabled = integrations.cleanspeak.enabled;
      paramApplication1.cleanSpeakConfiguration.apiKey = null;
      paramApplication1.cleanSpeakConfiguration.url = null;
      if (paramApplication1.cleanSpeakConfiguration.applicationIds.size() > 0) {
        this.applicationMapper.deleteCleanSpeakApplicationIds(paramApplication1.id);
        this.applicationMapper.createCleanSpeakApplicationIds(paramApplication1.id, paramApplication1.cleanSpeakConfiguration.applicationIds);
      } 
    } 
    paramApplication1.oauthConfiguration.clientId = paramApplication2.oauthConfiguration.clientId;
    if (StringTools.isTrimmedEmpty(paramApplication1.oauthConfiguration.clientSecret))
      paramApplication1.oauthConfiguration.clientSecret = paramApplication2.oauthConfiguration.clientSecret; 
    if (paramApplication1.id.equals(Application.FUSIONAUTH_APP_ID)) {
      paramApplication1.jwtConfiguration.enabled = paramApplication2.jwtConfiguration.enabled;
      paramApplication1.jwtConfiguration.refreshTokenExpirationPolicy = paramApplication2.jwtConfiguration.refreshTokenExpirationPolicy;
      paramApplication1.jwtConfiguration.refreshTokenUsagePolicy = paramApplication2.jwtConfiguration.refreshTokenUsagePolicy;
      paramApplication1.lambdaConfiguration.samlv2PopulateId = paramApplication2.lambdaConfiguration.samlv2PopulateId;
      paramApplication1.lambdaConfiguration.selfServiceRegistrationValidationId = paramApplication2.lambdaConfiguration.selfServiceRegistrationValidationId;
      paramApplication1.lambdaConfiguration.userinfoPopulateId = paramApplication2.lambdaConfiguration.userinfoPopulateId;
      paramApplication1.oauthConfiguration.authorizedRedirectURLs = paramApplication2.oauthConfiguration.authorizedRedirectURLs;
      paramApplication1.oauthConfiguration.authorizedOriginURLs = paramApplication2.oauthConfiguration.authorizedOriginURLs;
      paramApplication1.oauthConfiguration.authorizedResourceUris = paramApplication2.oauthConfiguration.authorizedResourceUris;
      paramApplication1.oauthConfiguration.authorizedURLValidationPolicy = paramApplication2.oauthConfiguration.authorizedURLValidationPolicy;
      paramApplication1.oauthConfiguration.clientAuthenticationPolicy = paramApplication2.oauthConfiguration.clientAuthenticationPolicy;
      paramApplication1.oauthConfiguration.consentMode = paramApplication2.oauthConfiguration.consentMode;
      paramApplication1.oauthConfiguration.enabledGrants = paramApplication2.oauthConfiguration.enabledGrants;
      paramApplication1.oauthConfiguration.generateRefreshTokens = paramApplication2.oauthConfiguration.generateRefreshTokens;
      paramApplication1.oauthConfiguration.logoutURL = paramApplication2.oauthConfiguration.logoutURL;
      paramApplication1.oauthConfiguration.providedScopePolicy = new ProvidedScopePolicy(paramApplication2.oauthConfiguration.providedScopePolicy);
      paramApplication1.oauthConfiguration.proofKeyForCodeExchangePolicy = paramApplication2.oauthConfiguration.proofKeyForCodeExchangePolicy;
      paramApplication1.oauthConfiguration.relationship = paramApplication2.oauthConfiguration.relationship;
      paramApplication1.oauthConfiguration.requireClientAuthentication = paramApplication2.oauthConfiguration.requireClientAuthentication;
      paramApplication1.oauthConfiguration.scopeHandlingPolicy = paramApplication2.oauthConfiguration.scopeHandlingPolicy;
      paramApplication1.oauthConfiguration.unknownScopePolicy = paramApplication2.oauthConfiguration.unknownScopePolicy;
      paramApplication1.authenticationTokenConfiguration.enabled = false;
      paramApplication1.registrationConfiguration = new Application.RegistrationConfiguration();
      paramApplication1.loginConfiguration.allowTokenRefresh = paramApplication2.loginConfiguration.allowTokenRefresh;
      paramApplication1.loginConfiguration.generateRefreshTokens = paramApplication2.loginConfiguration.generateRefreshTokens;
      paramApplication1.loginConfiguration.requireAuthentication = paramApplication2.loginConfiguration.requireAuthentication;
      paramApplication1.unverified.behavior = UnverifiedBehavior.Allow;
      paramApplication1.samlv2Configuration = paramApplication2.samlv2Configuration;
    } 
    paramApplication1.tenantId = paramApplication2.tenantId;
    paramApplication1.state = ObjectState.Active;
    paramApplication1.roles = paramApplication2.roles;
    paramApplication1.scopes = paramApplication2.scopes;
    paramApplication1.universalConfiguration.universal = paramApplication2.universalConfiguration.universal;
    generateSAMLv2Keys(paramApplication1);
    if (paramApplication1.jwtConfiguration.enabled)
      generateJWTSigningKeys(paramApplication1); 
    setDefaults(paramApplication1);
    if (paramApplication2.registrationDeletePolicy.unverified.enabled && paramApplication1.registrationDeletePolicy.unverified.enabled)
      paramApplication1.registrationDeletePolicy.unverified.enabledInstant = paramApplication2.registrationDeletePolicy.unverified.enabledInstant; 
    paramApplication1.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    normalizeVerificationKeyIds(paramApplication1, paramApplication2);
    this.applicationMapper.update(paramApplication1);
    writeVerificationKeys(paramApplication1);
    if (paramApplication1.oauthConfiguration.consentMode == OAuthScopeConsentMode.AlwaysPrompt)
      this.externalIdentifierMapper.deleteByApplicationIdAndType(paramApplication1.id, ExternalIdentifier.ExternalIdType.RememberOAuthScopeConsentChoice); 
  }
  
  @Transactional
  public void _updateTenantManager(Application paramApplication1, Application paramApplication2) {
    paramApplication2.name = paramApplication1.name;
    paramApplication2.themeId = paramApplication1.themeId;
    paramApplication2.emailConfiguration = new Application.ApplicationEmailConfiguration(paramApplication1.emailConfiguration);
    paramApplication2.jwtConfiguration.timeToLiveInSeconds = paramApplication1.jwtConfiguration.timeToLiveInSeconds;
    paramApplication2.jwtConfiguration.accessTokenKeyId = paramApplication1.jwtConfiguration.accessTokenKeyId;
    paramApplication2.jwtConfiguration.idTokenKeyId = paramApplication1.jwtConfiguration.idTokenKeyId;
    paramApplication2.jwtConfiguration.refreshTokenTimeToLiveInMinutes = paramApplication1.jwtConfiguration.refreshTokenTimeToLiveInMinutes;
    paramApplication2.jwtConfiguration.refreshTokenOneTimeUseConfiguration.gracePeriodInSeconds = paramApplication1.jwtConfiguration.refreshTokenOneTimeUseConfiguration.gracePeriodInSeconds;
    paramApplication2.multiFactorConfiguration.loginPolicy = paramApplication1.multiFactorConfiguration.loginPolicy;
    paramApplication2.multiFactorConfiguration.email = paramApplication1.multiFactorConfiguration.email;
    paramApplication2.multiFactorConfiguration.sms = paramApplication1.multiFactorConfiguration.sms;
    paramApplication2.formConfiguration.adminRegistrationFormId = paramApplication1.formConfiguration.adminRegistrationFormId;
    paramApplication2.formConfiguration.selfServiceFormId = paramApplication1.formConfiguration.selfServiceFormId;
    paramApplication2.formConfiguration.selfServiceFormConfiguration.requireCurrentPasswordOnPasswordChange = paramApplication1.formConfiguration.selfServiceFormConfiguration.requireCurrentPasswordOnPasswordChange;
    paramApplication2.webAuthnConfiguration.enabled = paramApplication1.webAuthnConfiguration.enabled;
    paramApplication2.webAuthnConfiguration.bootstrapWorkflow.enabled = paramApplication1.webAuthnConfiguration.bootstrapWorkflow.enabled;
    paramApplication2.webAuthnConfiguration.reauthenticationWorkflow.enabled = paramApplication1.webAuthnConfiguration.reauthenticationWorkflow.enabled;
    paramApplication2.accessControlConfiguration.uiIPAccessControlListId = paramApplication1.accessControlConfiguration.uiIPAccessControlListId;
    paramApplication2.passwordlessConfiguration.enabled = paramApplication1.passwordlessConfiguration.enabled;
    paramApplication2.externalIdentifierConfiguration.twoFactorTrustIdTimeToLiveInSeconds = paramApplication1.externalIdentifierConfiguration.twoFactorTrustIdTimeToLiveInSeconds;
    paramApplication2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.applicationMapper.update(paramApplication2);
  }
  
  public void create(Tenant paramTenant, Application paramApplication) {
    _create(paramTenant, paramApplication);
    this.cacheNotifier.reload(new String[] { "Applications", "Keys" });
  }
  
  public void createOAuthScope(Tenant paramTenant, ApplicationOAuthScope paramApplicationOAuthScope) {
    if (paramApplicationOAuthScope.id == null)
      paramApplicationOAuthScope.id = UUID.randomUUID(); 
    paramApplicationOAuthScope.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramApplicationOAuthScope.lastUpdateInstant = paramApplicationOAuthScope.insertInstant;
    this.applicationMapper.createOAuthScopes(List.of(paramApplicationOAuthScope));
    this.cacheNotifier.reload("Applications");
  }
  
  public void createRole(Tenant paramTenant, ApplicationRole paramApplicationRole) {
    if (paramApplicationRole.id == null)
      paramApplicationRole.id = UUID.randomUUID(); 
    paramApplicationRole.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramApplicationRole.lastUpdateInstant = paramApplicationRole.insertInstant;
    this.applicationMapper.createRoles(List.of(paramApplicationRole));
    this.cacheNotifier.reload("Applications");
  }
  
  public boolean deactivate(Tenant paramTenant, Application paramApplication) {
    boolean bool = (this.applicationMapper.deactivate(paramApplication.id) >= 1) ? true : false;
    if (bool)
      this.cacheNotifier.reload(new String[] { "Applications", "IdentityProvider" }); 
    return bool;
  }
  
  public boolean delete(Tenant paramTenant, Application paramApplication) {
    if (paramApplication.roles != null && paramApplication.roles.size() > 0) {
      List<?> list = (List)paramApplication.roles.stream().map(paramApplicationRole -> paramApplicationRole.id).distinct().collect(Collectors.toList());
      Objects.requireNonNull(this.applicationMapper);
      MapperTools.safeDelete(5000, list, this.applicationMapper::deleteRolesFromUsers);
      Objects.requireNonNull(this.groupMapper);
      MapperTools.safeDelete(5000, list, this.groupMapper::deleteApplicationRolesByRoleIds);
    } 
    this.applicationMapper.deleteCleanSpeakApplicationIds(paramApplication.id);
    this.applicationMapper.deleteAllOAuthScopesFromApplication(paramApplication.id);
    this.applicationMapper.deleteAllRolesFromApplication(paramApplication.id);
    this.userActionLogMapper.deleteApplicationAssociationsForApplication(paramApplication.id);
    this.userMapper.deleteRegistrationsForApplication(paramApplication.id);
    this.loginMapper.deleteRawLoginsForApplication(paramApplication.id);
    this.loginMapper.deleteHourlyLoginsForApplication(paramApplication.id);
    this.loginMapper.deleteRawDailyActiveForApplication(paramApplication.id);
    this.loginMapper.deleteRawMonthlyActiveForApplication(paramApplication.id);
    this.loginMapper.deleteDailyActiveForApplication(paramApplication.id);
    this.loginMapper.deleteMonthlyActiveForApplication(paramApplication.id);
    this.registrationCountService.deleteCountsForApplication(paramApplication.id);
    this.refreshTokenMapper.deleteByApplicationId(paramApplication.id);
    this.identityProviderMapper.deleteApplicationConfigurationsByApplicationId(paramApplication.id);
    this.externalIdentifierMapper.deleteByApplicationId(paramApplication.id);
    this.applicationMapper.deleteVerificationKeys(paramApplication.id);
    boolean bool = (this.applicationMapper.delete(paramApplication.id) == 1) ? true : false;
    if (bool)
      this.cacheNotifier.reload(new String[] { "Applications", "IdentityProvider", "Keys" }); 
    return bool;
  }
  
  public void deleteAllByTenantId(UUID paramUUID) {
    if (paramUUID == null)
      return; 
    List<Application> list = this.applicationReader.retrieveAllIgnoreActive(paramUUID, Set.of(ApplicationReaderService.ApplicationExpansion.roles));
    for (Application application : list)
      delete(null, application); 
    this.cacheNotifier.reload("IdentityProvider");
  }
  
  public void deleteOAuthScope(ApplicationOAuthScope paramApplicationOAuthScope) {
    this.applicationMapper.deleteOAuthScope(paramApplicationOAuthScope.id);
    this.cacheNotifier.reload("Applications");
  }
  
  public void deleteRole(ApplicationRole paramApplicationRole) {
    List<UUID> list = Collections.singletonList(paramApplicationRole.id);
    this.applicationMapper.deleteRolesFromUsers(list);
    this.groupMapper.deleteApplicationRolesByRoleIds(list);
    this.applicationMapper.deleteRole(paramApplicationRole.id);
    this.cacheNotifier.reload("Applications");
  }
  
  public void reactivate(Application paramApplication) {
    _reactivate(paramApplication);
    this.cacheNotifier.reload(new String[] { "Applications", "IdentityProvider" });
  }
  
  public void update(Tenant paramTenant, Application paramApplication1, Application paramApplication2, EventInfo paramEventInfo) {
    if (paramApplication2.id.equals(this.tenantManagerApplicationId)) {
      _updateTenantManager(paramApplication2, paramApplication1);
    } else {
      _update(paramTenant, paramApplication2, paramApplication1);
    } 
    this.cacheNotifier.reload(new String[] { "Applications", "Keys" });
    JWTConfiguration jWTConfiguration = (paramApplication1.jwtConfiguration != null) ? paramApplication1.jwtConfiguration : new JWTConfiguration();
    if (!paramApplication2.jwtConfiguration.equals(jWTConfiguration)) {
      JWTPublicKeyUpdateEvent jWTPublicKeyUpdateEvent = new JWTPublicKeyUpdateEvent(paramEventInfo, paramApplication2.id);
      EventHelper.send(paramTenant, paramApplication2, jWTPublicKeyUpdateEvent);
    } 
  }
  
  public ApplicationOAuthScope updateOAuthScope(Tenant paramTenant, ApplicationOAuthScope paramApplicationOAuthScope) {
    paramApplicationOAuthScope.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.applicationMapper.updateOAuthScope(paramApplicationOAuthScope);
    this.cacheNotifier.reload("Applications");
    return this.applicationMapper.retrieveOAuthScopeById(TenantService.optionalTenantId(paramTenant), paramApplicationOAuthScope.applicationId, paramApplicationOAuthScope.id);
  }
  
  public ApplicationRole updateRole(Tenant paramTenant, ApplicationRole paramApplicationRole) {
    paramApplicationRole.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.applicationMapper.updateRole(paramApplicationRole);
    this.cacheNotifier.reload("Applications");
    return this.applicationMapper.retrieveRoleById(TenantService.optionalTenantId(paramTenant), paramApplicationRole.applicationId, paramApplicationRole.id);
  }
  
  public ApplicationService.ValidationResult validateApplicationId(Tenant paramTenant, UUID paramUUID) {
    ApplicationService.ValidationResult validationResult = new ApplicationService.ValidationResult();
    validationResult.existing = (paramUUID != null) ? this.applicationReader.retrieveByIdIgnoreActive((paramTenant != null) ? paramTenant.id : null, paramUUID) : null;
    validationResult.errors = (new Validator()).notMissing(paramUUID, "applicationId", new Object[0]).done();
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.existing });
    return validationResult;
  }
  
  public ApplicationService.ValidationResult validateCopy(Tenant paramTenant, Application paramApplication, UUID paramUUID) {
    ApplicationService.ValidationResult validationResult = new ApplicationService.ValidationResult();
    UUID uUID = (paramTenant == null) ? null : paramTenant.id;
    Application application = this.applicationReader.retrieveById(uUID, paramUUID);
    validationResult.existing = (paramApplication.id == null) ? null : this.applicationReader.retrieveById(uUID, paramApplication.id);
    validationResult.application = application;
    if (validationResult.application != null) {
      validationResult.application.id = paramApplication.id;
      validationResult.application.name = paramApplication.name;
      validationResult.application.oauthConfiguration.clientSecret = null;
      validationResult.application.scopes.forEach(paramApplicationOAuthScope -> paramApplicationOAuthScope.id = null);
      validationResult.application.roles.forEach(paramApplicationRole -> paramApplicationRole.id = null);
      validationResult.application.samlv2Configuration.issuer = null;
    } 
    validationResult











      
      .errors = (new Validator()).notBlank(paramApplication.name, "application.name", new Object[0]).ifLastCheckHadNoError(paramValidator -> findExistingByName(paramUUID, paramApplication).ifPresent(())).ifTrue((paramApplication.id != null), paramValidator -> paramValidator.notDuplicate(paramValidationResult.existing, "applicationId", new Object[] { paramApplication.id })).valid((application != null), "sourceApplicationId", new Object[] { paramUUID }).done();
    return validationResult;
  }
  
  public ApplicationService.ValidationResult validateCreate(Tenant paramTenant, Application paramApplication) {
    ApplicationService.ValidationResult validationResult = new ApplicationService.ValidationResult();
    validationResult.application = paramApplication;
    UUID uUID = (paramTenant != null) ? paramTenant.id : null;
    Application application1 = (paramApplication.id != null) ? this.applicationMapper.retrieveByIdIgnoreActive(null, paramApplication.id) : null;
    Application application2 = findExistingByName(uUID, paramApplication).orElse(null);
    HashSet hashSet1 = new HashSet((paramApplication.roles != null) ? paramApplication.roles.size() : 0);
    LinkedHashSet<? extends CharSequence> linkedHashSet1 = new LinkedHashSet();
    HashSet hashSet2 = new HashSet((paramApplication.roles != null) ? paramApplication.roles.size() : 0);
    LinkedHashSet<? extends CharSequence> linkedHashSet2 = new LinkedHashSet();
    HashSet hashSet3 = new HashSet((paramApplication.scopes != null) ? paramApplication.scopes.size() : 0);
    LinkedHashSet linkedHashSet3 = new LinkedHashSet();
    HashSet hashSet4 = new HashSet((paramApplication.scopes != null) ? paramApplication.scopes.size() : 0);
    LinkedHashSet linkedHashSet4 = new LinkedHashSet();
    validationResult
































      
      .errors = (new Validator()).notDuplicate(application1, "applicationId", new Object[] { paramApplication.id }).forEach(paramApplication.roles, (paramValidator, paramApplicationRole, paramInteger) -> _validateRoleCreate(paramApplicationRole, "application.roles[" + paramInteger + "]").ifNoErrors(()).validate(())).ensure(linkedHashSet1.isEmpty(), "application.roles.name", "[duplicate]", new Object[] { String.join(", ", linkedHashSet1) }).ensure(linkedHashSet2.isEmpty(), "application.roles.id", "[duplicate]", new Object[] { String.join(", ", linkedHashSet2) }).ifTrue((paramApplication.scopes != null && !paramApplication.scopes.isEmpty()), paramValidator -> paramValidator.validate(()).ifLastCheckHadNoError(())).withErrors(commonValidation(paramTenant, paramApplication, application2, null)).done();
    if (paramApplication.universalConfiguration.universal && paramApplication.tenantId != null)
      validationResult.errors.addGeneralError("[notMissing]application.tenantId", "application.tenantId", new Object[0]); 
    return validationResult;
  }
  
  public ApplicationService.ValidationResult validateOAuthScopeCreate(Tenant paramTenant, UUID paramUUID, ApplicationOAuthScope paramApplicationOAuthScope) {
    ApplicationService.ValidationResult validationResult = new ApplicationService.ValidationResult();
    validationResult.application = (paramUUID != null) ? this.applicationReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.application });
    validationResult


      
      .errors = _validateOAuthScopeCreate(paramApplicationOAuthScope, "scope").ifNoErrors(paramValidator -> paramValidator.notDuplicate(this.applicationMapper.retrieveOAuthScopeByName((paramTenant != null) ? paramTenant.id : null, paramUUID, paramApplicationOAuthScope.name), "scope.name", new Object[] { paramApplicationOAuthScope.name })).validObject(validationResult.application, "applicationId", new Object[] { paramUUID }).ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(!paramUUID.equals(Application.FUSIONAUTH_APP_ID), "applicationId", "[fusionAuth]", new Object[0])).done();
    ReactorStatus reactorStatus = this.reactorStatusService.retrieveStatus();
    if (!reactorStatus.licensed) {
      validationResult.errors.addGeneralError("[notLicensed]", null, new Object[0]);
    } else if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.advancedOAuthScopes)) {
      validationResult.errors.addGeneralError("[notLicensedFor]", null, new Object[0]);
    } 
    return validationResult;
  }
  
  public ApplicationService.ValidationResult validateOAuthScopeRetrieveById(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2) {
    ApplicationService.ValidationResult validationResult = new ApplicationService.ValidationResult();
    validationResult.scope = this.applicationMapper.retrieveOAuthScopeById((paramTenant != null) ? paramTenant.id : null, paramUUID1, paramUUID2);
    validationResult.application = this.applicationReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID1);
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.application });
    validationResult


      
      .errors = (new Validator()).notMissing(paramUUID2, "scopeId", new Object[0]).validObject(validationResult.application, "applicationId", new Object[] { paramUUID1 }).ifTrue((paramUUID1 != null), paramValidator -> paramValidator.ensure((paramUUID != null && !paramUUID.equals(Application.FUSIONAUTH_APP_ID)), "applicationId", "[fusionAuth]", new Object[0])).done();
    return validationResult;
  }
  
  public ApplicationService.ValidationResult validateOAuthScopeUpdate(Tenant paramTenant, UUID paramUUID, ApplicationOAuthScope paramApplicationOAuthScope) {
    ApplicationService.ValidationResult validationResult = new ApplicationService.ValidationResult();
    validationResult.scope = this.applicationMapper.retrieveOAuthScopeById((paramTenant != null) ? paramTenant.id : null, paramUUID, paramApplicationOAuthScope.id);
    validationResult.application = this.applicationReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID);
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.application });
    validationResult





      
      .errors = (new Validator()).notMissing(paramApplicationOAuthScope.id, "scopeId", new Object[0]).validObject(validationResult.application, "applicationId", new Object[] { paramUUID }).ifTrue((validationResult.scope != null && paramApplicationOAuthScope.name != null), paramValidator -> paramValidator.ensure(paramValidationResult.scope.name.equals(paramApplicationOAuthScope.name), "scope.name", "[invalid]", new Object[] { "The scope name cannot be changed. Create a new scope and then delete this one." })).ifTrue((paramUUID != null), paramValidator -> paramValidator.ensure((paramUUID != null && !paramUUID.equals(Application.FUSIONAUTH_APP_ID)), "applicationId", "[fusionAuth]", new Object[0])).done();
    return validationResult;
  }
  
  public ApplicationService.ValidationResult validateReactivate(Tenant paramTenant, UUID paramUUID) {
    ApplicationService.ValidationResult validationResult = new ApplicationService.ValidationResult();
    UUID uUID = (paramTenant != null) ? paramTenant.id : null;
    validationResult.existing = (paramUUID != null) ? this.applicationReader.retrieveByIdIgnoreActive(uUID, paramUUID) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.existing });
    validationResult
      
      .errors = (new Validator()).notMissing(paramUUID, "applicationId", new Object[0]).done();
    return validationResult;
  }
  
  public ApplicationService.ValidationResult validateRoleCreate(Tenant paramTenant, UUID paramUUID, ApplicationRole paramApplicationRole) {
    ApplicationService.ValidationResult validationResult = new ApplicationService.ValidationResult();
    validationResult.application = (paramUUID != null) ? this.applicationReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.application });
    validationResult


      
      .errors = _validateRoleCreate(paramApplicationRole, "role").ifNoErrors(paramValidator -> paramValidator.notDuplicate(this.applicationMapper.retrieveRoleByName((paramTenant != null) ? paramTenant.id : null, paramUUID, paramApplicationRole.name), "role.name", new Object[] { paramApplicationRole.name })).validObject(validationResult.application, "applicationId", new Object[] { paramUUID }).ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(!paramUUID.equals(Application.FUSIONAUTH_APP_ID), "applicationId", "[fusionAuth]", new Object[0])).done();
    return validationResult;
  }
  
  public ApplicationService.ValidationResult validateRoleDelete(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString) {
    ApplicationService.ValidationResult validationResult = new ApplicationService.ValidationResult();
    if (paramUUID2 != null) {
      validationResult.role = this.applicationMapper.retrieveRoleById((paramTenant != null) ? paramTenant.id : null, paramUUID1, paramUUID2);
    } else if (paramString != null) {
      validationResult.role = this.applicationReader.retrieveRoleByName((paramTenant != null) ? paramTenant.id : null, paramUUID1, paramString);
    } 
    validationResult.application = this.applicationReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID1);
    validationResult
      
      .errors = (new Validator()).notMissing(paramUUID1, "applicationId", new Object[0]).ensure(!paramUUID1.equals(Application.FUSIONAUTH_APP_ID), "applicationId", "[fusionAuth]", new Object[0]).done();
    return validationResult;
  }
  
  public ApplicationService.ValidationResult validateRoleUpdate(Tenant paramTenant, UUID paramUUID, ApplicationRole paramApplicationRole) {
    ApplicationService.ValidationResult validationResult = new ApplicationService.ValidationResult();
    validationResult.role = (paramUUID != null && paramApplicationRole.id != null) ? this.applicationMapper.retrieveRoleById((paramTenant != null) ? paramTenant.id : null, paramUUID, paramApplicationRole.id) : null;
    validationResult.application = this.applicationReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID);
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.application });
    validationResult



      
      .errors = (new Validator()).notMissing(paramApplicationRole.id, "roleId", new Object[0]).maxLength(paramApplicationRole.description, 255, "role.description", new Object[0]).validObject(validationResult.application, "applicationId", new Object[] { paramUUID }).ifTrue((paramUUID != null), paramValidator -> paramValidator.ensure((paramUUID != null && !paramUUID.equals(Application.FUSIONAUTH_APP_ID)), "applicationId", "[fusionAuth]", new Object[0])).done();
    return validationResult;
  }
  
  public ApplicationService.ValidationResult validateUpdate(Tenant paramTenant, Application paramApplication, UUID paramUUID) {
    ApplicationService.ValidationResult validationResult = new ApplicationService.ValidationResult();
    UUID uUID = (paramTenant != null) ? paramTenant.id : null;
    Application application1 = (paramApplication.id != null) ? this.applicationReader.retrieveByIdIgnoreActive(uUID, paramApplication.id) : null;
    if (uUID == null && application1 != null)
      uUID = application1.tenantId; 
    Application application2 = null;
    if (paramApplication.universalConfiguration.universal) {
      List<Application> list = (paramApplication.name != null) ? this.applicationMapper.retrieveAllExistingByName(null, paramApplication.name, paramApplication.id) : null;
      if (list != null && !list.isEmpty())
        application2 = (Application)list.getFirst(); 
    } else {
      application2 = (paramApplication.name != null) ? this.applicationMapper.retrieveExisting(uUID, paramApplication.name, paramApplication.id) : null;
    } 
    validationResult.application = paramApplication;
    validationResult.existing = application1;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { application1 });
    if (validationResult.existing != null) {
      validationResult.application.tenantId = validationResult.existing.tenantId;
      validationResult.application.universalConfiguration.universal = validationResult.existing.universalConfiguration.universal;
    } 
    validationResult











      
      .errors = (new Validator()).notMissing(paramApplication.id, "applicationId", new Object[0]).missing(paramUUID, "sourceApplicationId", new Object[0]).notInactive(application1, paramApplication -> Boolean.valueOf((paramApplication.state == ObjectState.Active)), "applicationId", new Object[] { paramApplication.id }).withErrors(commonValidation(paramTenant, paramApplication, application2, application1)).done();
    if (paramApplication.universalConfiguration.universal && paramApplication.tenantId != null)
      validationResult.errors.addGeneralError("[notMissing]application.tenantId", "application.tenantId", new Object[0]); 
    return validationResult;
  }
  
  private Validator _validateOAuthScopeCreate(ApplicationOAuthScope paramApplicationOAuthScope, String paramString) {
    String str = paramString.equals("scope") ? (paramString + ".name") : paramString;
    return (new Validator())
      .maxLengthWithCode(paramApplicationOAuthScope.name, MapperTools.MaximumIndexedColumnLength, str, "[tooLong]scope.name", new Object[] { Integer.valueOf(MapperTools.MaximumIndexedColumnLength) }).notBlankWithCode(paramApplicationOAuthScope.name, str, "[blank]scope.name", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ensureWithCode(!DefaultOAuthService.ReservedScopeNames.contains(paramApplicationOAuthScope.name), paramString, "[invalid]scope.name", new Object[] { "The [" + String.join(", ", (Iterable)DefaultOAuthService.ReservedScopeNames) + "] scope names are reserved. Please select a different name." })).ifLastCheckHadNoError(paramValidator -> {
          Objects.requireNonNull(DefaultOAuthService.AllowedScopeChars);
          paramValidator.ensureWithCode(paramApplicationOAuthScope.name.chars().allMatch(DefaultOAuthService.AllowedScopeChars::contains), paramString, "[invalid]scope.name", new Object[] { "The requested scope name contains invalid characters. Please select a different name." });
        }).ifLastCheckHadNoError(paramValidator -> paramValidator.ensureWithCode(DefaultOAuthService.ReservedScopePrefixes.stream().noneMatch(()), paramString, "[invalid]scope.name", new Object[] { "The [" + String.join(", ", (Iterable)DefaultOAuthService.ReservedScopePrefixes) + "] scope prefixes are reserved. Please select a different name." })).ifTrue((paramApplicationOAuthScope.id != null), paramValidator -> paramValidator.notDuplicateWithCode(this.applicationMapper.retrieveOAuthScopeById(null, null, paramApplicationOAuthScope.id), paramString.equals("scope") ? "scopeId" : (paramString + ".id"), paramString.equals("scope") ? "[duplicate]scopeId" : "[duplicate]scope.id", new Object[] { paramApplicationOAuthScope.id }));
  }
  
  private Validator _validateRoleCreate(ApplicationRole paramApplicationRole, String paramString) {
    boolean bool = paramString.equals("role");
    return (new Validator())
      .notBlankWithCode(paramApplicationRole.name, bool ? (paramString + ".name") : paramString, "[blank]role.name", new Object[0])
      .maxLengthWithCode(paramApplicationRole.description, 255, bool ? (paramString + ".description") : paramString, "[tooLong]role.description", new Object[0])
      
      .ifTrue((paramApplicationRole.id != null), paramValidator -> paramValidator.notDuplicateWithCode(this.applicationMapper.retrieveRoleById(null, null, paramApplicationRole.id), paramString.equals("role") ? "roleId" : (paramString + ".id"), paramString.equals("role") ? "[duplicate]roleId" : "[duplicate]role.id", new Object[] { paramApplicationRole.id }));
  }
  
  private Errors commonValidation(Tenant paramTenant, Application paramApplication1, Application paramApplication2, Application paramApplication3) {
    UUID uUID = (paramTenant != null) ? paramTenant.id : null;
    ReactorStatus reactorStatus = this.reactorStatusService.retrieveStatus();
    boolean bool = ReactorStatusValidator.isLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.advancedRegistration);
    return (new Validator())
      
      .notBlank(paramApplication1.name, "application.name", new Object[0])

      
      .ifTrue((paramApplication1.name != null), paramValidator -> paramValidator.ensure(!paramApplication.name.startsWith("_"), "application.name", "[startsWithUnderscore]", new Object[0]))

      
      .ifTrue((paramApplication1.accessControlConfiguration.uiIPAccessControlListId != null), paramValidator -> paramValidator.validate(()).ensure((this.ipAccessControlListReader.retrieveById(paramApplication.accessControlConfiguration.uiIPAccessControlListId) != null), "application.accessControlConfiguration.uiIPAccessControlListId", "[invalid]", new Object[] { paramApplication.accessControlConfiguration.uiIPAccessControlListId })).ifTrue((paramApplication1.cleanSpeakConfiguration != null && paramApplication1.cleanSpeakConfiguration.usernameModeration.enabled), paramValidator -> paramValidator.notMissing(paramApplication.cleanSpeakConfiguration.usernameModeration.applicationId, "application.cleanSpeakConfiguration.usernameModeration.applicationId", new Object[0]))


      
      .ifTrue((paramApplication1.externalIdentifierConfiguration.twoFactorTrustIdTimeToLiveInSeconds != null), paramValidator -> paramValidator.ifFalse(Application.FUSIONAUTH_APP_ID.equals(paramApplication.id), ()).ifNoFieldErrors("application.externalIdentifierConfiguration.twoFactorTrustIdTimeToLiveInSeconds", ()))





      
      .validAbsoluteHttpURL(paramApplication1.baseURL, "application.baseURL", new Object[] { paramApplication1.baseURL }).ifTrue((!Application.FUSIONAUTH_APP_ID.equals(paramApplication1.id) && !this.tenantManagerApplicationId.equals(paramApplication1.id) && paramApplication1.oauthConfiguration != null), paramValidator -> paramValidator.forEach(paramApplication.oauthConfiguration.authorizedOriginURLs, ()).forEach(paramApplication.oauthConfiguration.authorizedRedirectURLs, ()).forEach(paramApplication.oauthConfiguration.authorizedResourceUris, ()).validAbsoluteHttpURL(paramApplication.oauthConfiguration.logoutURL, "application.oauthConfiguration.logoutURL", new Object[] { paramApplication.oauthConfiguration.logoutURL }).validAbsoluteHttpURL(paramApplication.oauthConfiguration.deviceVerificationURL, "application.oauthConfiguration.deviceVerificationURL", new Object[] { paramApplication.oauthConfiguration.deviceVerificationURL }).ifTrue(paramApplication.oauthConfiguration.enabledGrants.contains(GrantType.device_code), ()).valid(DefaultOAuthService.SupportedGrants.containsAll(paramApplication.oauthConfiguration.enabledGrants), "application.oauthConfiguration.enabledGrants", new Object[] { DefaultOAuthService.SupportedGrants.stream().map(GrantType::grantName).collect(Collectors.joining(", ")) })).ifTrue(paramApplication1.jwtConfiguration.enabled, paramValidator -> paramValidator.ensure((paramApplication.jwtConfiguration.refreshTokenTimeToLiveInMinutes > 0), "application.jwtConfiguration.refreshTokenTimeToLiveInMinutes", "[tooSmall]", new Object[0]).ensure((paramApplication.jwtConfiguration.timeToLiveInSeconds > 0), "application.jwtConfiguration.timeToLiveInSeconds", "[tooSmall]", new Object[0]).ifTrue((paramApplication.jwtConfiguration.refreshTokenExpirationPolicy == RefreshTokenExpirationPolicy.SlidingWindowWithMaximumLifetime), ()).ifTrue((paramApplication.jwtConfiguration.refreshTokenUsagePolicy == RefreshTokenUsagePolicy.OneTimeUse), ()))
















      
      .ifTrue((paramApplication1.jwtConfiguration.accessTokenKeyId != null), paramValidator -> {
          boolean bool = (paramApplication1 == null || !Objects.equals(paramApplication2.jwtConfiguration.accessTokenKeyId, paramApplication1.jwtConfiguration.accessTokenKeyId)) ? true : false;
          if (bool)
            this.keyValidator.validateAccessTokenSigningKey(paramValidator, paramApplication2.jwtConfiguration.accessTokenKeyId, "application.jwtConfiguration.accessTokenKeyId"); 
        }).ifTrue((paramApplication1.jwtConfiguration.idTokenKeyId != null), paramValidator -> {
          boolean bool = (paramApplication1 == null || !Objects.equals(paramApplication2.jwtConfiguration.idTokenKeyId, paramApplication1.jwtConfiguration.idTokenKeyId)) ? true : false;
          if (bool)
            this.keyValidator.validateIdTokenSigningKey(paramValidator, paramApplication2.jwtConfiguration.idTokenKeyId, "application.jwtConfiguration.idTokenKeyId"); 
        }).forEach(paramApplication1.jwtConfiguration.accessTokenVerificationKeyIds, (paramValidator, paramUUID, paramInteger) -> this.keyValidator.validateAccessTokenVerificationKey(paramValidator, paramUUID, "application.jwtConfiguration.accessTokenVerificationKeyIds[" + paramInteger + "]", "[cannotVerify]application.jwtConfiguration.accessTokenVerificationKeyIds"))


      
      .forEach(paramApplication1.jwtConfiguration.idTokenVerificationKeyIds, (paramValidator, paramUUID, paramInteger) -> this.keyValidator.validateIdTokenVerificationKey(paramValidator, paramUUID, "application.jwtConfiguration.idTokenVerificationKeyIds[" + paramInteger + "]", "[cannotVerify]application.jwtConfiguration.idTokenVerificationKeyIds"))


      
      .ifTrue(paramApplication1.verifyRegistration, paramValidator -> paramValidator.notMissing(paramApplication.verificationEmailTemplateId, "application.verificationEmailTemplateId", new Object[0]))


      
      .ifTrue((paramApplication1.verificationEmailTemplateId != null), paramValidator -> paramValidator.validObject(this.emailTemplateMapper.retrieveById(paramApplication.verificationEmailTemplateId), "application.verificationEmailTemplateId", new Object[] { paramApplication.verificationEmailTemplateId })).forEach(Application.ApplicationEmailConfiguration.EmailTemplateIdFields, (paramValidator, paramField, paramInteger) -> paramValidator.validate(()))


      
      .forEach(ApplicationService.PhoneTemplateIdFields, (paramValidator, paramField, paramInteger) -> paramValidator.validate(()))


      
      .validate(paramValidator -> ReactorStatusValidator.ifNotLicensedForThen(paramReactorStatus, (), ()))




















      
      .ifTrue((paramApplication1.registrationConfiguration.type == Application.RegistrationConfiguration.RegistrationType.advanced && (paramApplication1.registrationConfiguration.enabled || paramApplication1.registrationConfiguration.completeRegistration)), paramValidator -> paramValidator.ensure(paramBoolean, "application.registrationConfiguration.type", "[notLicensed]", new Object[0]).notMissing(paramApplication.registrationConfiguration.formId, "application.registrationConfiguration.formId", new Object[0]))






      
      .ifTrue((paramApplication1.registrationConfiguration.formId != null), paramValidator -> paramValidator.holdMyBeer(this.formMapper.retrieveById(paramApplication.registrationConfiguration.formId)).validObject(paramValidator.barkeep(), "application.registrationConfiguration.formId", new Object[] { paramApplication.registrationConfiguration.formId }).ifLastCheckHadNoError(())).ifTrue((paramApplication1.formConfiguration.adminRegistrationFormId != null), paramValidator -> paramValidator.holdMyBeer(this.formMapper.retrieveById(paramApplication.formConfiguration.adminRegistrationFormId)).validObject(paramValidator.barkeep(), "application.formConfiguration.adminRegistrationFormId", new Object[] { paramApplication.formConfiguration.adminRegistrationFormId }).ifLastCheckHadNoError(())).ifTrue((paramApplication1.formConfiguration.selfServiceFormId != null), paramValidator -> paramValidator.holdMyBeer(this.formMapper.retrieveById(paramApplication.formConfiguration.selfServiceFormId)).validObject(paramValidator.barkeep(), "application.formConfiguration.selfServiceFormId", new Object[] { paramApplication.formConfiguration.selfServiceFormId }).ifLastCheckHadNoError(())).ifTrue((paramApplication1.verifyRegistration && paramApplication1.unverified.behavior == UnverifiedBehavior.Gated), paramValidator -> paramValidator.ensure(paramBoolean, "application.unverified.behavior", "[notLicensed]", new Object[0]))


      
      .ifTrue(paramApplication1.registrationDeletePolicy.unverified.enabled, paramValidator -> paramValidator.ensure((paramApplication.registrationDeletePolicy.unverified.numberOfDaysToRetain > 0), "application.registrationDeletePolicy.unverified.numberOfDaysToRetain", "[tooSmall]", new Object[0]))

      
      .ifTrue(paramApplication1.samlv2Configuration.enabled, paramValidator -> paramValidator.notEmpty(paramApplication.samlv2Configuration.authorizedRedirectURLs, "application.samlv2Configuration.authorizedRedirectURLs", new Object[0]).ifLastCheckHadNoError(()).notBlank(paramApplication.samlv2Configuration.issuer, "application.samlv2Configuration.issuer", new Object[0]).ifTrue(paramApplication.samlv2Configuration.requireSignedRequests, ()).notMissing(paramApplication.samlv2Configuration.xmlSignatureC14nMethod, "application.samlv2Configuration.xmlSignatureC14nMethod", new Object[0]).ifTrue((paramApplication.samlv2Configuration.logoutURL != null), ()).ifTrue((paramApplication.samlv2Configuration.logout.singleLogout.url != null), ()).ifTrue(paramApplication.samlv2Configuration.logout.singleLogout.enabled, ()).ifTrue(paramApplication.samlv2Configuration.logout.requireSignedRequests, ()).ifTrue((paramApplication.samlv2Configuration.initiatedLogin.nameIdFormat != null), ()).ifTrue(paramApplication.samlv2Configuration.assertionEncryptionConfiguration.enabled, ()).ifTrue(paramApplication.samlv2Configuration.loginHintConfiguration.enabled, ()))






















































      
      .validate(paramValidator -> EnumValidator.validate(paramValidator, paramApplication.samlv2Configuration.assertionEncryptionConfiguration.digestAlgorithm, (Enum<? extends Enum<?>>[])DigestAlgorithm.values(), "application.samlv2Configuration.assertionEncryptionConfiguration.digestAlgorithm"))
      .validate(paramValidator -> EnumValidator.validate(paramValidator, paramApplication.samlv2Configuration.assertionEncryptionConfiguration.encryptionAlgorithm, (Enum<? extends Enum<?>>[])EncryptionAlgorithm.values(), "application.samlv2Configuration.assertionEncryptionConfiguration.encryptionAlgorithm"))
      .validate(paramValidator -> EnumValidator.validate(paramValidator, paramApplication.samlv2Configuration.assertionEncryptionConfiguration.keyLocation, (Enum<? extends Enum<?>>[])KeyLocation.values(), "application.samlv2Configuration.assertionEncryptionConfiguration.keyLocation"))
      .validate(paramValidator -> EnumValidator.validate(paramValidator, paramApplication.samlv2Configuration.assertionEncryptionConfiguration.keyTransportAlgorithm, (Enum<? extends Enum<?>>[])KeyTransportAlgorithm.values(), "application.samlv2Configuration.assertionEncryptionConfiguration.keyTransportAlgorithm"))
      .validate(paramValidator -> EnumValidator.validate(paramValidator, paramApplication.samlv2Configuration.assertionEncryptionConfiguration.maskGenerationFunction, (Enum<? extends Enum<?>>[])MaskGenerationFunction.values(), "application.samlv2Configuration.assertionEncryptionConfiguration.maskGenerationFunction"))

      
      .ifNoFieldErrors("application.samlv2Configuration.issuer", paramValidator -> paramValidator.holdMyBeer(this.applicationMapper.retrieveExistingBySAMLv2Issuer(paramUUID, paramApplication.samlv2Configuration.issuer, paramApplication.id)).notDuplicate(paramValidator.barkeep(), "application.samlv2Configuration.issuer", new Object[] { paramApplication.samlv2Configuration.issuer })).ifTrue((paramApplication1.samlv2Configuration.keyId != null), paramValidator -> this.keyValidator.validateSigningKeyForSAML(paramValidator, paramApplication.samlv2Configuration.keyId, "application.samlv2Configuration.keyId"))
      
      .ifTrue((paramApplication1.samlv2Configuration.logout.keyId != null), paramValidator -> this.keyValidator.validateSigningKeyForSAML(paramValidator, paramApplication.samlv2Configuration.logout.keyId, "application.samlv2Configuration.logout.keyId"))
      
      .ifTrue((paramApplication1.samlv2Configuration.logout.singleLogout.keyId != null), paramValidator -> this.keyValidator.validateSigningKeyForSAML(paramValidator, paramApplication.samlv2Configuration.logout.singleLogout.keyId, "application.samlv2Configuration.logout.singleLogout.keyId"))


      
      .ifTrue((paramApplication1.samlv2Configuration.defaultVerificationKeyId != null), paramValidator -> this.keyValidator.validateVerifyKeyForSAML(paramValidator, paramApplication.samlv2Configuration.defaultVerificationKeyId, "application.samlv2Configuration.defaultVerificationKeyId"))


      
      .ifTrue((paramApplication1.samlv2Configuration.logout.defaultVerificationKeyId != null), paramValidator -> this.keyValidator.validateVerifyKeyForSAML(paramValidator, paramApplication.samlv2Configuration.logout.defaultVerificationKeyId, "application.samlv2Configuration.logout.defaultVerificationKeyId"))


      
      .forEach(paramApplication1.samlv2Configuration.verificationKeyIds, (paramValidator, paramUUID, paramInteger) -> this.keyValidator.validateVerifyKeyForSAML(paramValidator, paramUUID, "application.samlv2Configuration.verificationKeyIds[" + paramInteger + "]", "[cannotVerify]application.samlv2Configuration.verificationKeyIds"))


      
      .forEach(paramApplication1.samlv2Configuration.logout.verificationKeyIds, (paramValidator, paramUUID, paramInteger) -> this.keyValidator.validateVerifyKeyForSAML(paramValidator, paramUUID, "application.samlv2Configuration.logout.verificationKeyIds[" + paramInteger + "]", "[cannotVerify]application.samlv2Configuration.logout.verificationKeyIds"))


      
      .ifTrue((paramApplication1.samlv2Configuration.assertionEncryptionConfiguration.keyTransportEncryptionKeyId != null), paramValidator -> this.keyValidator.validateEncryptionKeyForSAML(paramValidator, paramApplication.samlv2Configuration.assertionEncryptionConfiguration.keyTransportEncryptionKeyId, "application.samlv2Configuration.assertionEncryptionConfiguration.keyTransportEncryptionKeyId"))




      
      .validate(paramValidator -> this.lambdaValidator.validateOptional(paramValidator, paramApplication.lambdaConfiguration.accessTokenPopulateId, LambdaType.JWTPopulate, "application.lambdaConfiguration.accessTokenPopulateId"))

      
      .validate(paramValidator -> this.lambdaValidator.validateOptional(paramValidator, paramApplication.lambdaConfiguration.idTokenPopulateId, LambdaType.JWTPopulate, "application.lambdaConfiguration.idTokenPopulateId"))

      
      .validate(paramValidator -> this.lambdaValidator.validateOptional(paramValidator, paramApplication.lambdaConfiguration.samlv2PopulateId, LambdaType.SAMLv2Populate, "application.lambdaConfiguration.samlv2PopulateId"))

      
      .validate(paramValidator -> this.lambdaValidator.validateOptional(paramValidator, paramApplication.lambdaConfiguration.selfServiceRegistrationValidationId, LambdaType.SelfServiceRegistrationValidation, "application.lambdaConfiguration.selfServiceRegistrationValidationId"))

      
      .validate(paramValidator -> this.lambdaValidator.validateOptional(paramValidator, paramApplication.lambdaConfiguration.userinfoPopulateId, LambdaType.UserInfoPopulate, "application.lambdaConfiguration.userinfoPopulateId"))

      
      .ifTrue((paramApplication1.lambdaConfiguration.multiFactorRequirementId != null), paramValidator -> paramValidator.validate(()).ifLastCheckHadNoError(()))



      
      .validate(paramValidator -> this.lambdaValidator.validateOptional(paramValidator, paramApplication.lambdaConfiguration.multiFactorRequirementId, LambdaType.MFARequirement, "application.lambdaConfiguration.multiFactorRequirementId"))
      
      .notInactive(paramApplication2, paramApplication -> Boolean.valueOf((paramApplication.state == ObjectState.Active)), "application.name", new Object[] { paramApplication1.name }).ifNoErrors(paramValidator -> paramValidator.notDuplicate(paramApplication1, "application.name", new Object[] { paramApplication2.name })).ifTrue(paramApplication1.oauthConfiguration.relationship.equals(OAuthApplicationRelationship.ThirdParty), paramValidator -> paramValidator.validate(()))



      
      .ifTrue((paramApplication1.multiFactorConfiguration.sms.templateId != null), paramValidator -> paramValidator.holdMyBeer(this.messageTemplateMapper.retrieveById(paramApplication.multiFactorConfiguration.sms.templateId)).validObject(paramValidator.barkeep(), "application.multiFactorConfiguration.sms.templateId", new Object[] { paramApplication.multiFactorConfiguration.sms.templateId }).ifLastCheckHadNoError(())).ifTrue((paramApplication1.multiFactorConfiguration.voice.templateId != null), paramValidator -> paramValidator.holdMyBeer(this.messageTemplateMapper.retrieveById(paramApplication.multiFactorConfiguration.voice.templateId)).validObject(paramValidator.barkeep(), "application.multiFactorConfiguration.voice.templateId", new Object[] { paramApplication.multiFactorConfiguration.voice.templateId }).ifLastCheckHadNoError(())).ifTrue((paramApplication1.multiFactorConfiguration.email.templateId != null), paramValidator -> paramValidator.validObject(this.emailTemplateMapper.retrieveById(paramApplication.multiFactorConfiguration.email.templateId), "application.multiFactorConfiguration.email.templateId", new Object[] { paramApplication.multiFactorConfiguration.email.templateId })).ifFalse(Application.FUSIONAUTH_APP_ID.equals(paramApplication1.id), paramValidator -> paramValidator.ifTrue((paramApplication.multiFactorConfiguration.loginPolicy != null), ()).ifTrue((MFATools.loginPolicyEnabled(paramApplication.multiFactorConfiguration.loginPolicy) && paramApplication.multiFactorConfiguration.trustPolicy != null), ()))







      
      .ifTrue((paramApplication1.themeId != null), paramValidator -> paramValidator.ensure(ReactorStatusValidator.isLicensedFor(paramReactorStatus, ()), "application.themeId", "[notLicensed]", new Object[0]).validObject(this.themeMapper.retrieveById(paramApplication.themeId), "application.themeId", new Object[] { paramApplication.themeId })).ifTrue(paramApplication1.webAuthnConfiguration.enabled, paramValidator -> paramValidator.validate(()))


      
      .ifTrue((paramApplication1.universalConfiguration.universal && (paramApplication1.id == null || !paramApplication1.id.equals(this.tenantManagerApplicationId))), paramValidator -> paramValidator.validate(()))


      
      .ifTrue(paramApplication1.universalConfiguration.universal, paramValidator -> paramValidator.missing(paramApplication.tenantId, "application.tenantId", new Object[0]))

      
      .ifTrue((paramApplication1.id != null && paramApplication1.id.equals(this.tenantManagerApplicationId)), paramValidator -> paramValidator.validate(()))

      
      .done();
  }
  
  private boolean doesNotContainPathOrQueryString(URI paramURI) {
    if (paramURI == null)
      return true; 
    return (StringTools.isTrimmedEmpty(paramURI.getPath()) && StringTools.isTrimmedEmpty(paramURI.getQuery()));
  }
  
  private Optional<Application> findExistingByName(UUID paramUUID, Application paramApplication) {
    if (paramApplication.name == null)
      return Optional.empty(); 
    UUID uUID = paramApplication.universalConfiguration.universal ? null : paramUUID;
    List<Application> list = this.applicationMapper.retrieveAllExistingByName(uUID, paramApplication.name, null);
    if (!list.isEmpty())
      return Optional.of((Application)list.getFirst()); 
    if (!paramApplication.universalConfiguration.universal)
      return Optional.ofNullable(this.applicationMapper.retrieveExistingUniversal(paramApplication.name, paramUUID)); 
    return Optional.empty();
  }
  
  private void generateJWTSigningKeys(Application paramApplication) {
    if (paramApplication.jwtConfiguration.accessTokenKeyId == null)
      paramApplication.jwtConfiguration.accessTokenKeyId = (generateKey(paramApplication, "Access token signing key generated for application ")).id; 
    if (paramApplication.jwtConfiguration.idTokenKeyId == null)
      paramApplication.jwtConfiguration.idTokenKeyId = (generateKey(paramApplication, "Id token signing key generated for application ")).id; 
  }
  
  private Key generateKey(Application paramApplication, String paramString) {
    String str1 = paramString + paramString;
    byte b = 0;
    while (this.keyReader.retrieveByName(str1) != null)
      str1 = paramString + paramString + " (" + paramApplication.name + ")"; 
    String str2 = str1;
    Key key = (new Key()).with(paramKey -> paramKey.algorithm = Key.KeyAlgorithm.RS256).with(paramKey -> paramKey.name = paramString).with(paramKey -> paramKey.length = Integer.valueOf(2048));
    this.keyService.create(key);
    return key;
  }
  
  private void generateSAMLv2Keys(Application paramApplication) {
    if (paramApplication.samlv2Configuration.enabled) {
      if (paramApplication.samlv2Configuration.keyId == null)
        paramApplication.samlv2Configuration.keyId = (generateKey(paramApplication, "SAML key generated for application ")).id; 
      if (paramApplication.samlv2Configuration.logout.keyId == null)
        paramApplication.samlv2Configuration.logout.keyId = paramApplication.samlv2Configuration.keyId; 
      if (paramApplication.samlv2Configuration.logout.singleLogout.enabled && paramApplication.samlv2Configuration.logout.singleLogout.keyId == null)
        paramApplication.samlv2Configuration.logout.singleLogout.keyId = paramApplication.samlv2Configuration.keyId; 
    } 
  }
  
  private boolean isAbsoluteWithSchema(URI paramURI) {
    if (paramURI == null)
      return false; 
    if (!paramURI.isAbsolute())
      return false; 
    URI uRI = URI.create(paramURI.toString()
        .replaceFirst(":\\*+", ":42")
        .replace("*", "x"));
    if (uRI.getScheme().equals("http") || uRI.getScheme().equals("https"))
      return (uRI.getHost() != null && uRI.getSchemeSpecificPart().startsWith("//")); 
    if (uRI.getSchemeSpecificPart().startsWith("//"))
      return (uRI.getHost() != null); 
    if (uRI.getSchemeSpecificPart().startsWith("/"))
      return (uRI.getSchemeSpecificPart().substring(1).length() > 0); 
    return false;
  }
  
  private boolean isValidRedirect(URI paramURI) {
    if (paramURI == null)
      return false; 
    if (paramURI.toString().equals("urn:ietf:wg:oauth:2.0:oob") || paramURI.toString().equals("urn:ietf:wg:oauth:2.0:oob:auto"))
      return true; 
    return isAbsoluteWithSchema(paramURI);
  }
  
  private void normalizeVerificationKeyIds(Application paramApplication1, Application paramApplication2) {
    Application.SAMLv2Configuration sAMLv2Configuration1 = paramApplication1.samlv2Configuration;
    Application.SAMLv2Configuration sAMLv2Configuration2 = (paramApplication2 == null) ? null : paramApplication2.samlv2Configuration;
    sAMLv2Configuration1
      
      .verificationKeyIds = (sAMLv2Configuration2 == null) ? KeyHelper.normalizeVerificationKeyIds(sAMLv2Configuration1.defaultVerificationKeyId, sAMLv2Configuration1.verificationKeyIds) : KeyHelper.normalizeVerificationKeyIds(sAMLv2Configuration1.defaultVerificationKeyId, sAMLv2Configuration1.verificationKeyIds, sAMLv2Configuration2.defaultVerificationKeyId, sAMLv2Configuration2.verificationKeyIds);
    sAMLv2Configuration1.defaultVerificationKeyId = sAMLv2Configuration1.verificationKeyIds.isEmpty() ? null : (UUID)sAMLv2Configuration1.verificationKeyIds.getFirst();
    sAMLv2Configuration1.logout
      
      .verificationKeyIds = (sAMLv2Configuration2 == null) ? KeyHelper.normalizeVerificationKeyIds(sAMLv2Configuration1.logout.defaultVerificationKeyId, sAMLv2Configuration1.logout.verificationKeyIds) : KeyHelper.normalizeVerificationKeyIds(sAMLv2Configuration1.logout.defaultVerificationKeyId, sAMLv2Configuration1.logout.verificationKeyIds, sAMLv2Configuration2.logout.defaultVerificationKeyId, sAMLv2Configuration2.logout.verificationKeyIds);
    sAMLv2Configuration1.logout.defaultVerificationKeyId = sAMLv2Configuration1.logout.verificationKeyIds.isEmpty() ? null : (UUID)sAMLv2Configuration1.logout.verificationKeyIds.getFirst();
  }
  
  private void optionalTemplate(Validator paramValidator, ApplicationPhoneConfiguration paramApplicationPhoneConfiguration, Field paramField, String paramString) {
    try {
      Optional.<UUID>ofNullable((UUID)paramField.get(paramApplicationPhoneConfiguration))
        .map(paramUUID -> paramValidator.validObject(this.messageTemplateMapper.retrieveById(paramUUID), paramString, new Object[] { paramUUID }));
    } catch (IllegalAccessException illegalAccessException) {
      throw new ErrorException(illegalAccessException, new Object[0]);
    } 
  }
  
  private void optionalTemplate(Validator paramValidator, Application.ApplicationEmailConfiguration paramApplicationEmailConfiguration, Field paramField, String paramString) {
    try {
      UUID uUID = (UUID)paramField.get(paramApplicationEmailConfiguration);
      if (uUID != null)
        paramValidator.validObject(this.emailTemplateMapper.retrieveById(uUID), paramString, new Object[] { uUID }); 
    } catch (IllegalAccessException illegalAccessException) {
      throw new ErrorException(illegalAccessException, new Object[0]);
    } 
  }
  
  private void setDefaults(Application paramApplication) {
    UUID uUID = (this.applicationMapper.retrieveById(null, Application.FUSIONAUTH_APP_ID)).formConfiguration.adminRegistrationFormId;
    paramApplication.formConfiguration.adminRegistrationFormId = DefaultTools.<UUID>defaultIfNull(paramApplication.formConfiguration.adminRegistrationFormId, uUID);
    if (paramApplication.oauthConfiguration.clientAuthenticationPolicy == null) {
      paramApplication.oauthConfiguration
        .clientAuthenticationPolicy = paramApplication.oauthConfiguration.requireClientAuthentication ? ClientAuthenticationPolicy.Required : ClientAuthenticationPolicy.NotRequired;
    } else {
      paramApplication.oauthConfiguration.requireClientAuthentication = (paramApplication.oauthConfiguration.clientAuthenticationPolicy != ClientAuthenticationPolicy.NotRequired);
    } 
    if (MFATools.loginPolicyEnabled(paramApplication.multiFactorConfiguration.loginPolicy) && paramApplication.multiFactorConfiguration.trustPolicy == null)
      paramApplication.multiFactorConfiguration.trustPolicy = ApplicationMultiFactorTrustPolicy.Any; 
    paramApplication.registrationDeletePolicy.unverified
      
      .enabledInstant = paramApplication.registrationDeletePolicy.unverified.enabled ? ZonedDateTime.now(ZoneOffset.UTC) : null;
  }
  
  private void validateAuthorizedOrigin(Validator paramValidator, URI paramURI, int paramInt) {
    paramValidator.ensureWithCode(isAbsoluteWithSchema(paramURI), "application.oauthConfiguration.authorizedOriginURLs[" + paramInt + "]", "[invalidURL]application.oauthConfiguration.authorizedOriginURLs", new Object[] { paramURI }).ifNoFieldErrors("application.oauthConfiguration.authorizedOriginURLs[" + paramInt + "]", () -> paramValidator.ensureWithCode(doesNotContainPathOrQueryString(paramURI), "application.oauthConfiguration.authorizedOriginURLs[" + paramInt + "]", "[invalidOriginURL]application.oauthConfiguration.authorizedOriginURLs", new Object[] { paramURI })).ifNoFieldErrors("application.oauthConfiguration.authorizedOriginURLs[" + paramInt + "]", () -> paramValidator.ifTrue(paramURI.toString().contains("*"), ()));
  }
  
  private void validateAuthorizedRedirect(Validator paramValidator, URI paramURI, int paramInt) {
    paramValidator.ensureWithCode(isValidRedirect(paramURI), "application.oauthConfiguration.authorizedRedirectURLs[" + paramInt + "]", "[invalidURL]application.oauthConfiguration.authorizedRedirectURLs", new Object[] { paramURI }).ifLastCheckHadNoError(() -> paramValidator.ifTrue(paramURI.toString().contains("*"), ()));
  }
  
  private void validateAuthorizedResourceUri(Validator paramValidator, URI paramURI, int paramInt) {
    paramValidator.ensureWithCode((paramURI != null && paramURI.isAbsolute() && paramURI.getFragment() == null), "application.oauthConfiguration.authorizedResourceUris[" + paramInt + "]", "[invalidURI]application.oauthConfiguration.authorizedResourceUris", new Object[] { paramURI });
  }
  
  private boolean validateFormIdEqualsDefaultApplicationFormId(UUID paramUUID) {
    Application application = this.applicationMapper.retrieveById(null, Application.FUSIONAUTH_APP_ID);
    return application.formConfiguration.adminRegistrationFormId.equals(paramUUID);
  }
  
  private void writeVerificationKeys(Application paramApplication) {
    UUID uUID = paramApplication.id;
    this.applicationMapper.deleteVerificationKeys(uUID);
    if (!paramApplication.samlv2Configuration.verificationKeyIds.isEmpty())
      this.applicationMapper.createVerificationKeys(uUID, ApplicationKeyType.Samlv2Verification.name(), paramApplication.samlv2Configuration.verificationKeyIds, true); 
    if (!paramApplication.samlv2Configuration.logout.verificationKeyIds.isEmpty())
      this.applicationMapper.createVerificationKeys(uUID, ApplicationKeyType.Samlv2LogoutVerification.name(), paramApplication.samlv2Configuration.logout.verificationKeyIds, true); 
    if (!paramApplication.jwtConfiguration.accessTokenVerificationKeyIds.isEmpty())
      this.applicationMapper.createVerificationKeys(uUID, ApplicationKeyType.AccessTokenVerification.name(), paramApplication.jwtConfiguration.accessTokenVerificationKeyIds, false); 
    if (!paramApplication.jwtConfiguration.idTokenVerificationKeyIds.isEmpty())
      this.applicationMapper.createVerificationKeys(uUID, ApplicationKeyType.IdTokenVerification.name(), paramApplication.jwtConfiguration.idTokenVerificationKeyIds, false); 
  }
}
