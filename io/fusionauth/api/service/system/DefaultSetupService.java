package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.error.Errors;
import com.inversoft.license.v2.LicenseProvider;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.Instance;
import io.fusionauth.api.domain.InstanceMapper;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.api.security.SecurityTools;
import io.fusionauth.api.service.application.ApplicationService;
import io.fusionauth.api.service.cache.KeyCacheLoader;
import io.fusionauth.api.service.cache.TenantCacheLoader;
import io.fusionauth.api.service.consent.ConsentService;
import io.fusionauth.api.service.email.EmailTemplateService;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.reactor.ReactorService;
import io.fusionauth.api.service.user.PasswordService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.domain.Acquisition;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.client.LambdaDelegate;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.EmailConfiguration;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.SendSetPasswordIdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.APIKeyRequest;
import io.fusionauth.domain.api.APIKeyResponse;
import io.fusionauth.domain.api.ApplicationRequest;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.KeyRequest;
import io.fusionauth.domain.api.KeyResponse;
import io.fusionauth.domain.api.ReactorRequest;
import io.fusionauth.domain.api.TenantRequest;
import io.fusionauth.domain.api.TenantResponse;
import io.fusionauth.domain.email.EmailTemplate;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.oauth2.GrantType;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSetupService implements SetupService {
  public static final String FirstTimeSetupAuditLogReason = "FusionAuth First Time Setup";
  
  private static final Logger logger = LoggerFactory.getLogger(DefaultSetupService.class);
  
  private final ApplicationReaderService applicationReader;
  
  private final ApplicationService applicationService;
  
  private final CacheNotifier cacheNotifier;
  
  private final FusionAuthClient client;
  
  private final FusionAuthConfiguration configuration;
  
  private final ConsentService consentService;
  
  private final LambdaDelegate delegate;
  
  private final EmailTemplateService emailTemplateService;
  
  private final FrontEndSupport frontEndSupport;
  
  private final InstanceMapper instanceMapper;
  
  private final InstanceService instanceService;
  
  private final KeyCacheLoader keyCacheLoader;
  
  private final LicenseProvider licenseProvider;
  
  private final PasswordService passwordService;
  
  private final RefreshTokenService refreshTokenService;
  
  private final SystemDefaultsSingleton systemDefaults;
  
  private final TenantCacheLoader tenantCacheLoader;
  
  private final TenantReaderService tenantReader;
  
  private final TenantService tenantService;
  
  private final UserReaderService userReader;
  
  private final UserService userService;
  
  @Inject
  public DefaultSetupService(ApplicationReaderService paramApplicationReaderService, ApplicationService paramApplicationService, FusionAuthClient paramFusionAuthClient, FusionAuthConfiguration paramFusionAuthConfiguration, ConsentService paramConsentService, EmailTemplateService paramEmailTemplateService, FrontEndSupport paramFrontEndSupport, CacheNotifier paramCacheNotifier, InstanceMapper paramInstanceMapper, InstanceService paramInstanceService, KeyCacheLoader paramKeyCacheLoader, PasswordService paramPasswordService, RefreshTokenService paramRefreshTokenService, SystemDefaultsSingleton paramSystemDefaultsSingleton, TenantCacheLoader paramTenantCacheLoader, TenantReaderService paramTenantReaderService, TenantService paramTenantService, UserReaderService paramUserReaderService, UserService paramUserService, LicenseProvider paramLicenseProvider) {
    this.applicationReader = paramApplicationReaderService;
    this.applicationService = paramApplicationService;
    this.client = paramFusionAuthClient;
    this.configuration = paramFusionAuthConfiguration;
    this.consentService = paramConsentService;
    Objects.requireNonNull(paramFrontEndSupport);
    this.delegate = new LambdaDelegate(this.client, paramClientResponse -> paramClientResponse.successResponse, paramFrontEndSupport::frontEndErrorHandling);
    this.emailTemplateService = paramEmailTemplateService;
    this.frontEndSupport = paramFrontEndSupport;
    this.cacheNotifier = paramCacheNotifier;
    this.instanceMapper = paramInstanceMapper;
    this.instanceService = paramInstanceService;
    this.keyCacheLoader = paramKeyCacheLoader;
    this.passwordService = paramPasswordService;
    this.refreshTokenService = paramRefreshTokenService;
    this.systemDefaults = paramSystemDefaultsSingleton;
    this.tenantCacheLoader = paramTenantCacheLoader;
    this.tenantReader = paramTenantReaderService;
    this.tenantService = paramTenantService;
    this.userReader = paramUserReaderService;
    this.userService = paramUserService;
    this.licenseProvider = paramLicenseProvider;
  }
  
  @Transactional
  public void _firstTimeSetupCompleteStep(Consumer<SetupService.FirstTimeSetup> paramConsumer) {
    Instance instance = this.instanceMapper.retrieve();
    paramConsumer.accept(instance.firstTimeSetup);
    this.instanceMapper.updateData(instance);
    this.cacheNotifier.reload("Instance");
  }
  
  @Transactional
  public SetupService.SetupResult _setup(User paramUser, String paramString, boolean paramBoolean1, Acquisition paramAcquisition, boolean paramBoolean2) {
    Application application = this.applicationReader.retrieveById(null, Application.FUSIONAUTH_APP_ID);
    Tenant tenant1 = this.tenantReader.retrieveById(application.tenantId);
    Tenant tenant2 = new Tenant(tenant1);
    this.userService.create(tenant1, null, paramUser, SendSetPasswordIdentityType.doNotSend, true, false, true, true, null, null);
    UserService.RegistrationResult registrationResult = this.userService.createRegistration(tenant1, application, paramUser, (new UserRegistration())
        .with(paramUserRegistration -> paramUserRegistration.applicationId = Application.FUSIONAUTH_APP_ID), 
        Collections.singletonList(application.getRole("admin")), false, true, false, null);
    paramUser.getRegistrations().add(this.userReader.retrieveRegistration(null, paramUser.id, Application.FUSIONAUTH_APP_ID));
    String str1 = null;
    if (paramBoolean2) {
      long l = registrationResult.registration.insertInstant.toEpochSecond();
      Map map = Map.of("grants", List.of(GrantType.authorization_code.grantName()), "auth_time", 
          Long.valueOf(l), "source", "oauth");
      RefreshToken.MetaData metaData = new RefreshToken.MetaData();
      metaData.device.name = "Setup Wizard";
      metaData.device.type = "Browser";
      metaData.device.lastAccessedAddress = paramString;
      str1 = (this.refreshTokenService.createRefreshToken(null, tenant1, paramUser, application, map, metaData)).token;
    } 
    TreeSet treeSet = new TreeSet();
    treeSet.addAll(SystemDefaultsSingleton.EmailTemplates.keySet());
    treeSet.addAll(SystemDefaultsSingleton.MessageTemplates.keySet());
    treeSet.addAll(SystemDefaultsSingleton.Lambdas.keySet());
    Objects.requireNonNull(this.systemDefaults);
    treeSet.forEach(this.systemDefaults::set);
    tenant1.emailConfiguration.forgotPasswordEmailTemplateId = findEmailTemplateId("[FusionAuth Default] Forgot Password");
    tenant1.emailConfiguration.passwordlessEmailTemplateId = findEmailTemplateId("[FusionAuth Default] Passwordless Login");
    tenant1.emailConfiguration.setPasswordEmailTemplateId = findEmailTemplateId("[FusionAuth Default] Set up Password");
    tenant1.multiFactorConfiguration.email.templateId = findEmailTemplateId("[FusionAuth Default] Two Factor Authentication");
    tenant1.passwordValidationRules.breachDetection.notifyUserEmailTemplateId = findEmailTemplateId("[FusionAuth Default] Breached Password Notification");
    String str2 = this.frontEndSupport.getFusionAuthBaseURL();
    String str3 = str2.substring(str2.indexOf("://") + 3);
    Key key = ((KeyResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.generateKey(null, new KeyRequest((new Key()).with(()).with(()).with(()).with(()))))).key;
    tenant1.issuer = str2;
    tenant1.jwtConfiguration.accessTokenKeyId = key.id;
    tenant1.jwtConfiguration.idTokenKeyId = key.id;
    tenant1.baseURL = URI.create(str2);
    this.tenantService.update(tenant2, tenant1, null, Collections.emptyList());
    UUID uUID1 = findEmailTemplateId("COPPA Notice");
    UUID uUID2 = findEmailTemplateId("COPPA Notice Reminder");
    this.consentService.createConsent((new Consent()).with(paramConsent -> paramConsent.name = "COPPA Email+")
        .with(paramConsent -> paramConsent.consentEmailTemplateId = paramUUID)
        .with(paramConsent -> paramConsent.emailPlus.enabled = true)
        .with(paramConsent -> paramConsent.emailPlus.emailTemplateId = paramUUID)
        .with(paramConsent -> paramConsent.defaultMinimumAgeForSelfConsent = Integer.valueOf(13)));
    this.consentService.createConsent((new Consent()).with(paramConsent -> paramConsent.name = "COPPA VPC")
        .with(paramConsent -> paramConsent.consentEmailTemplateId = paramUUID)
        .with(paramConsent -> paramConsent.defaultMinimumAgeForSelfConsent = Integer.valueOf(13)));
    this.instanceService.setupComplete();
    if (this.configuration.runtimeMode() == RuntimeMode.FusionAuth_Development || this.configuration.runtimeMode() == RuntimeMode.FusionAuth_Demo)
      return new SetupService.SetupResult(str1, false); 
    boolean bool = this.instanceService.activate(paramUser, paramBoolean1, paramAcquisition);
    return new SetupService.SetupResult(str1, (paramBoolean1 && bool));
  }
  
  public void firstTimeSetupAPIKey(APIKey paramAPIKey) {
    paramAPIKey.metaData = new APIKey.APIKeyMetaData("description", "Created by the first time setup wizard.");
    paramAPIKey.name = "First Time Setup API Key";
    paramAPIKey.retrievable = false;
    APIKey aPIKey = ((APIKeyResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.createAPIKey(null, new APIKeyRequest(paramAPIKey)))).apiKey;
    String str = SecurityTools.lastN(aPIKey.key, 7);
    audit("Created the API key with Id [" + String.valueOf(aPIKey.id) + "] and a key ending [" + str + "].");
    _firstTimeSetupCompleteStep(paramFirstTimeSetup -> paramFirstTimeSetup.apiKeyId = paramAPIKey.id);
  }
  
  public void firstTimeSetupApplication(UUID paramUUID, Application paramApplication) {
    paramApplication.jwtConfiguration.enabled = false;
    paramApplication.oauthConfiguration.enabledGrants = Set.of(GrantType.authorization_code, GrantType.refresh_token);
    paramApplication.oauthConfiguration.generateRefreshTokens = true;
    Application application = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.createApplication(null, new ApplicationRequest(this.frontEndSupport.buildEventInfo(), paramApplication)))).application;
    audit("Created the application with Id [" + String.valueOf(application.id) + "] and name [" + application.name + "]");
    _firstTimeSetupCompleteStep(paramFirstTimeSetup -> paramFirstTimeSetup.applicationId = paramApplication.id);
  }
  
  public void firstTimeSetupEmail(EmailConfiguration paramEmailConfiguration) {
    Tenant tenant1 = ((TenantResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTenant(this.frontEndSupport.fusionAuthTenantId))).tenant;
    Tenant tenant2 = new Tenant(tenant1);
    tenant2.emailConfiguration = paramEmailConfiguration;
    Tenant tenant3 = ((TenantResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateTenant(paramTenant1.id, new TenantRequest(this.frontEndSupport.buildEventInfo(), paramTenant2, List.of())))).tenant;
    audit("Updated the tenant with Id [" + String.valueOf(tenant3.id) + "] and name [" + tenant3.name + "]", tenant1, tenant2);
    _firstTimeSetupCompleteStep(paramFirstTimeSetup -> paramFirstTimeSetup.emailConfigured = true);
  }
  
  public boolean firstTimeSetupLicense(String paramString) {
    ClientResponse<Void, Errors> clientResponse = this.client.activateReactor(new ReactorRequest(paramString, null));
    if (clientResponse.wasSuccessful()) {
      _firstTimeSetupCompleteStep(paramFirstTimeSetup -> paramFirstTimeSetup.licenseActivated = true);
      return true;
    } 
    return false;
  }
  
  public SetupService.FirstTimeSetup retrieveFirstTimeSetupState(Tenant paramTenant) {
    Instance instance = this.instanceMapper.retrieve();
    SetupService.FirstTimeSetup firstTimeSetup = instance.firstTimeSetup;
    if (firstTimeSetup == null) {
      SetupService.FirstTimeSetup firstTimeSetup1 = new SetupService.FirstTimeSetup();
      firstTimeSetup1.state = new SetupService.FirstTimeSetupState();
      return firstTimeSetup1;
    } 
    firstTimeSetup.state = new SetupService.FirstTimeSetupState();
    if (firstTimeSetup.complete)
      return firstTimeSetup; 
    if (firstTimeSetup.licenseActivated || !ReactorService.isLicenseContainerInvalid(this.licenseProvider.getLicense()))
      firstTimeSetup.state.license = SetupService.FirstTimeSetupState.State.COMPLETE; 
    if (firstTimeSetup.emailConfigured || !isEmailConfigurationDefault(paramTenant.emailConfiguration))
      firstTimeSetup.state.email = SetupService.FirstTimeSetupState.State.COMPLETE; 
    if (firstTimeSetup.apiKeyId != null) {
      firstTimeSetup.state.apiKey = SetupService.FirstTimeSetupState.State.COMPLETE;
      ClientResponse<APIKeyResponse, Errors> clientResponse = this.client.retrieveAPIKey(firstTimeSetup.apiKeyId);
      if (clientResponse.wasSuccessful()) {
        firstTimeSetup.apiKey = ((APIKeyResponse)clientResponse.successResponse).apiKey;
      } else {
        firstTimeSetup.state.apiKey = SetupService.FirstTimeSetupState.State.DELETED;
      } 
    } 
    if (firstTimeSetup.applicationId != null) {
      firstTimeSetup.state.application = SetupService.FirstTimeSetupState.State.COMPLETE;
      ClientResponse<ApplicationResponse, Void> clientResponse = this.client.retrieveApplication(firstTimeSetup.applicationId);
      if (clientResponse.wasSuccessful()) {
        firstTimeSetup.application = ((ApplicationResponse)clientResponse.successResponse).application;
      } else {
        firstTimeSetup.state.application = SetupService.FirstTimeSetupState.State.DELETED;
      } 
    } 
    return firstTimeSetup;
  }
  
  public SetupService.SetupResult setup(User paramUser, String paramString, boolean paramBoolean1, Acquisition paramAcquisition, boolean paramBoolean2) {
    SetupService.SetupResult setupResult = _setup(paramUser, paramString, paramBoolean1, paramAcquisition, paramBoolean2);
    this.keyCacheLoader.run();
    this.tenantCacheLoader.run();
    return setupResult;
  }
  
  public boolean setupTokenRequiredForDeployment() {
    return (this.configuration.setupToken() != null);
  }
  
  public SetupService.SetupValidationResult validate(User paramUser, String paramString, Acquisition paramAcquisition) {
    Application application = this.applicationReader.retrieveById(null, Application.FUSIONAUTH_APP_ID);
    Tenant tenant = this.tenantReader.retrieveById(application.tenantId);
    Errors errors = (new Validator()).notBlank(paramUser.firstName, "user.firstName", new Object[0]).notBlank(paramUser.lastName, "user.lastName", new Object[0]).notBlank(paramUser.email, "user.email", new Object[0]).notBlank(paramUser.password, "user.password", new Object[0]).notBlank(paramString, "passwordConfirm", new Object[0]).ifTrue((paramUser.email != null), paramValidator -> paramValidator.email(paramUser.email, "user.email", new Object[0])).ifTrue((paramUser.password != null), paramValidator -> paramValidator.ensure(paramUser.password.equals(paramString), "user.password", "[doNotMatch]", new Object[0])).withErrors(this.passwordService.validatePasswordOnCreate(tenant, paramUser, "user.password", paramUser.password)).notBlank(paramAcquisition.channel, "acquisition.channel", new Object[0]).ifNoFieldErrors("acquisition.channel", paramValidator -> paramValidator.ensure(AcquisitionChannels.contains(paramAcquisition.channel), "acquisition.channel", "[invalid]", new Object[0])).ifNoFieldErrors("acquisition.channel", paramValidator -> paramValidator.ifTrue("Other".equals(paramAcquisition.channel), ())).maxLength(paramAcquisition.other, 256, "acquisition.other", new Object[0]).done();
    if (errors.empty()) {
      UserService.ValidationResult validationResult = this.userService.validateCreate(tenant, paramUser, null, true, SendSetPasswordIdentityType.doNotSend, false, null, false);
      errors.add(validationResult.errors);
      return new SetupService.SetupValidationResult(errors, validationResult.user);
    } 
    return new SetupService.SetupValidationResult(errors, paramUser);
  }
  
  public Errors validateFirstTimeSetupApplication(Application paramApplication, Tenant paramTenant) {
    return (new Validator())
      .notBlank(paramTenant.issuer, "tenant.issuer", new Object[0])
      .withErrors((this.applicationService.validateCreate(paramTenant, paramApplication)).errors)
      .done();
  }
  
  public Errors validateFirstTimeSetupEmailConfiguration(EmailConfiguration paramEmailConfiguration) {
    return (new Validator())
      
      .notBlank(paramEmailConfiguration.host, "tenant.emailConfiguration.host", new Object[0])
      .notBlank(paramEmailConfiguration.port, "tenant.emailConfiguration.port", new Object[0])
      .ifNoFieldErrors("tenant.emailConfiguration.port", paramValidator -> paramValidator.ensure((paramEmailConfiguration.port.intValue() > 0), "tenant.emailConfiguration.port", "[invalid]", new Object[0]))
      
      .done();
  }
  
  public Errors validateFirstTimeSetupLicense(String paramString) {
    return (new Validator())
      .notBlank(paramString, "licenseKey", new Object[0])
      .done();
  }
  
  public Errors validateSetupToken(String paramString) {
    if (!setupTokenRequiredForDeployment())
      return new Errors(); 
    return (new Validator()).notBlank(paramString, "setupToken", new Object[0])
      .ifLastCheckHadError(() -> logger.info("Setup incomplete. The setup token is required and not provided."))
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(this.configuration.setupToken().equals(paramString), "setupToken", "[invalid]", new Object[0]).ifLastCheckHadError(()))
      
      .done();
  }
  
  private void audit(String paramString) {
    audit(paramString, null, null);
  }
  
  private void audit(String paramString, Object paramObject1, Object paramObject2) {
    User user = (User)this.frontEndSupport.userLoginSecurityContext.getCurrentUser();
    this.frontEndSupport.writeAuditLog(this.delegate.client, (new AuditLog(user.getLogin(), paramString))
        .with(paramAuditLog -> paramAuditLog.oldValue = paramObject)
        .with(paramAuditLog -> paramAuditLog.newValue = paramObject)
        .with(paramAuditLog -> paramAuditLog.reason = "FusionAuth First Time Setup"));
  }
  
  private UUID findEmailTemplateId(String paramString) {
    EmailTemplate emailTemplate = this.emailTemplateService.retrieveByName(paramString);
    if (emailTemplate == null)
      return null; 
    return emailTemplate.id;
  }
  
  private boolean isEmailConfigurationDefault(EmailConfiguration paramEmailConfiguration) {
    EmailConfiguration emailConfiguration = new EmailConfiguration();
    emailConfiguration.defaultFromEmail = "change-me@example.com";
    emailConfiguration.defaultFromName = "FusionAuth";
    if (!Objects.equals(paramEmailConfiguration.host, emailConfiguration.host))
      return false; 
    if (!Objects.equals(paramEmailConfiguration.port, emailConfiguration.port))
      return false; 
    if (!Objects.equals(paramEmailConfiguration.security, emailConfiguration.security))
      return false; 
    if (!Objects.equals(paramEmailConfiguration.username, emailConfiguration.username))
      return false; 
    if (!Objects.equals(paramEmailConfiguration.password, emailConfiguration.password))
      return false; 
    if (!Objects.equals(paramEmailConfiguration.defaultFromEmail, emailConfiguration.defaultFromEmail))
      return false; 
    if (!Objects.equals(paramEmailConfiguration.defaultFromName, emailConfiguration.defaultFromName))
      return false; 
    return true;
  }
}
