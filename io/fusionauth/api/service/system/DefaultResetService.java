package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import com.inversoft.cache.CacheLoaderProvider;
import com.inversoft.license.v2.LicenseProvider;
import com.inversoft.util.SecurityTools;
import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.api.domain.DatasetMapper;
import io.fusionauth.api.domain.Instance;
import io.fusionauth.api.domain.InstanceMapper;
import io.fusionauth.api.domain.IntegrationMapper;
import io.fusionauth.api.domain.KickstartFile;
import io.fusionauth.api.domain.KickstartMapper;
import io.fusionauth.api.domain.TenantManagerMapper;
import io.fusionauth.api.domain.TenantMapper;
import io.fusionauth.api.domain.UnsafeMapper;
import io.fusionauth.api.service.authentication.LoginQueue;
import io.fusionauth.api.service.form.FormService;
import io.fusionauth.api.service.lock.ResetDistributedLock;
import io.fusionauth.api.service.reindex.ReindexService;
import io.fusionauth.api.service.search.EntitySearchEngine;
import io.fusionauth.api.service.search.UserSearchEngine;
import io.fusionauth.api.service.system.kickstart.KickstartService;
import io.fusionauth.api.util.FileTools;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.CORSConfiguration;
import io.fusionauth.domain.EmailConfiguration;
import io.fusionauth.domain.EventConfiguration;
import io.fusionauth.domain.FIPS;
import io.fusionauth.domain.Integrations;
import io.fusionauth.domain.JWTConfiguration;
import io.fusionauth.domain.ObjectState;
import io.fusionauth.domain.PasswordBreachDetection;
import io.fusionauth.domain.PasswordEncryptionConfiguration;
import io.fusionauth.domain.PasswordValidationRules;
import io.fusionauth.domain.RefreshTokenExpirationPolicy;
import io.fusionauth.domain.RefreshTokenOneTimeUseConfiguration;
import io.fusionauth.domain.RefreshTokenSlidingWindowConfiguration;
import io.fusionauth.domain.RefreshTokenUsagePolicy;
import io.fusionauth.domain.SystemConfiguration;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TenantPhoneConfiguration;
import io.fusionauth.domain.TenantUserDeletePolicy;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.TransactionType;
import io.fusionauth.domain.VerificationStrategy;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorPolicy;
import io.fusionauth.domain.event.EventType;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.FormStep;
import io.fusionauth.domain.form.FormType;
import io.fusionauth.domain.form.ManagedFields;
import io.fusionauth.domain.oauth2.ClientAuthenticationPolicy;
import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.domain.oauth2.OAuth2Configuration;
import io.fusionauth.domain.oauth2.ProofKeyForCodeExchangePolicy;
import io.fusionauth.domain.oauth2.ProvidedScopePolicy;
import io.fusionauth.domain.tenantManager.TenantManagerConfiguration;
import io.fusionauth.domain.util.HTTPMethod;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.primeframework.mvc.ErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultResetService implements ResetService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultResetService.class);
  
  private final ApplicationMapper applicationMapper;
  
  private final CacheLoaderProvider cacheLoaderProvider;
  
  private final DatasetMapper datasetMapper;
  
  private final EntitySearchEngine entitySearchEngine;
  
  private final FormService formService;
  
  private final InstanceMapper instanceMapper;
  
  private final IntegrationMapper integrationMapper;
  
  private final KeyService keyService;
  
  private final KickstartMapper kickstartMapper;
  
  private final KickstartService kickstartService;
  
  private final LicenseProvider licenseProvider;
  
  private final LoginQueue loginQueue;
  
  private final NodeService nodeService;
  
  private final ReindexService reindexService;
  
  private final ResetDistributedLock resetDistributedLock;
  
  private final SystemConfigurationService systemConfigurationService;
  
  private final UUID tenantManagerApplicationId;
  
  private final TenantManagerMapper tenantManagerMapper;
  
  private final TenantMapper tenantMapper;
  
  private final UnsafeMapper unsafeMapper;
  
  private final UserSearchEngine userSearchEngine;
  
  @Inject
  public DefaultResetService(ApplicationMapper paramApplicationMapper, InstanceMapper paramInstanceMapper, CacheLoaderProvider paramCacheLoaderProvider, DatasetMapper paramDatasetMapper, EntitySearchEngine paramEntitySearchEngine, IntegrationMapper paramIntegrationMapper, KeyService paramKeyService, KickstartMapper paramKickstartMapper, KickstartService paramKickstartService, LoginQueue paramLoginQueue, NodeService paramNodeService, ReindexService paramReindexService, ResetDistributedLock paramResetDistributedLock, @TenantManagerApplicationId UUID paramUUID, SystemConfigurationService paramSystemConfigurationService, TenantManagerMapper paramTenantManagerMapper, TenantMapper paramTenantMapper, UnsafeMapper paramUnsafeMapper, FormService paramFormService, LicenseProvider paramLicenseProvider, UserSearchEngine paramUserSearchEngine) {
    this.applicationMapper = paramApplicationMapper;
    this.instanceMapper = paramInstanceMapper;
    this.cacheLoaderProvider = paramCacheLoaderProvider;
    this.datasetMapper = paramDatasetMapper;
    this.entitySearchEngine = paramEntitySearchEngine;
    this.integrationMapper = paramIntegrationMapper;
    this.keyService = paramKeyService;
    this.kickstartMapper = paramKickstartMapper;
    this.kickstartService = paramKickstartService;
    this.loginQueue = paramLoginQueue;
    this.nodeService = paramNodeService;
    this.reindexService = paramReindexService;
    this.resetDistributedLock = paramResetDistributedLock;
    this.tenantManagerApplicationId = paramUUID;
    this.systemConfigurationService = paramSystemConfigurationService;
    this.tenantManagerMapper = paramTenantManagerMapper;
    this.tenantMapper = paramTenantMapper;
    this.unsafeMapper = paramUnsafeMapper;
    this.formService = paramFormService;
    this.licenseProvider = paramLicenseProvider;
    this.userSearchEngine = paramUserSearchEngine;
  }
  
  public void createKickstart(String paramString, Path paramPath) {
    KickstartFile kickstartFile = new KickstartFile();
    kickstartFile.name = paramString;
    kickstartFile.id = UUID.randomUUID();
    try {
      if (paramString.endsWith(".json")) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        try {
          zipOutputStream.setLevel(9);
          zipOutputStream.putNextEntry(new ZipEntry(paramString));
          zipOutputStream.write(Files.readAllBytes(paramPath));
          zipOutputStream.closeEntry();
          kickstartFile.kickstart = byteArrayOutputStream.toByteArray();
          zipOutputStream.close();
        } catch (Throwable throwable) {
          try {
            zipOutputStream.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
      } else if (paramString.endsWith(".zip")) {
        kickstartFile.kickstart = Files.readAllBytes(paramPath);
      } else {
        throw new IllegalArgumentException("Unsupported file type.");
      } 
    } catch (IOException iOException) {
      throw new ErrorException(iOException, new Object[0]);
    } 
    this.kickstartMapper.createKickstart(kickstartFile);
  }
  
  public void reset(String paramString, int paramInt, boolean paramBoolean1, ResetDistributedLock paramResetDistributedLock, boolean paramBoolean2) {
    try {
      Instant instant = Instant.now();
      if (paramBoolean2)
        logger.info("Resetting FusionAuth"); 
      if (paramBoolean2)
        logger.info("  [Clear Login Queue]"); 
      this.loginQueue.clear();
      this.loginQueue.__dangerousFlush();
      if (paramBoolean2)
        logger.info("  [Reset Integration Configuration]"); 
      resetIntegrations();
      if (paramBoolean2)
        logger.info("  [Reset System Configuration]"); 
      resetSystemConfiguration();
      if (paramBoolean2)
        logger.info("  [Reset Tenant Manager Configuration]"); 
      resetTenantManagerConfiguration();
      if (paramBoolean2)
        logger.info("  [Delete Everything]"); 
      keepLockAlive(paramResetDistributedLock);
      this.unsafeMapper.deleteAuthenticationKeysExceptInternalKey();
      this.unsafeMapper.deleteAuditLogs();
      this.unsafeMapper.deleteBreachedPasswordMetrics();
      this.unsafeMapper.deleteMfaMetrics();
      this.unsafeMapper.deleteEventLogs();
      keepLockAlive(paramResetDistributedLock);
      this.unsafeMapper.deleteRawGlobalDailyActiveUsers();
      this.unsafeMapper.deleteRawGlobalMonthlyActiveUsers();
      this.unsafeMapper.deleteRawApplicationDailyActiveUsers();
      this.unsafeMapper.deleteRawApplicationMonthlyActiveUsers();
      keepLockAlive(paramResetDistributedLock);
      this.unsafeMapper.deleteGlobalRegistrationCounts();
      this.unsafeMapper.deleteRawGlobalRegistrationCounts();
      this.unsafeMapper.deleteApplicationDailyActiveUsers();
      this.unsafeMapper.deleteApplicationMonthlyActiveUsers();
      keepLockAlive(paramResetDistributedLock);
      this.unsafeMapper.deleteGlobalDailyActiveUsers();
      this.unsafeMapper.deleteGlobalMonthlyActiveUsers();
      keepLockAlive(paramResetDistributedLock);
      this.unsafeMapper.deleteRawLogins();
      this.unsafeMapper.deleteUserComments();
      this.unsafeMapper.deleteFromUserActionLogsForApplications();
      this.unsafeMapper.deleteUserActionLogs();
      this.unsafeMapper.deleteUserActionReasons();
      keepLockAlive(paramResetDistributedLock);
      this.unsafeMapper.deleteGroupMemberships();
      this.unsafeMapper.deleteGroupApplicationRoles();
      this.unsafeMapper.deleteGroupSCIMExternalIds();
      this.unsafeMapper.deleteGroups();
      keepLockAlive(paramResetDistributedLock);
      this.unsafeMapper.deleteUserRegistrationApplicationRoles();
      this.unsafeMapper.deleteUserRegistrations();
      this.unsafeMapper.deleteRefreshTokens();
      this.unsafeMapper.deletePreviousPasswords();
      this.unsafeMapper.deleteRequestFrequencies();
      this.unsafeMapper.deleteExternalIdentifiers();
      this.loginQueue.clear();
      keepLockAlive(paramResetDistributedLock);
      this.unsafeMapper.deleteEmailPlusUserConsents();
      this.unsafeMapper.deleteUserConsents();
      this.unsafeMapper.deleteIdentityProviderLinks();
      keepLockAlive(paramResetDistributedLock);
      this.unsafeMapper.deleteFamilies();
      this.unsafeMapper.deleteRawLogins();
      this.unsafeMapper.deleteIdentities();
      this.unsafeMapper.deleteUserSCIMExternalIds();
      this.unsafeMapper.deleteWebAuthnCredentials();
      this.unsafeMapper.deleteUsers();
      keepLockAlive(paramResetDistributedLock);
      this.unsafeMapper.deleteConnectorsTenants();
      this.unsafeMapper.deleteConnectorsExceptFor(BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID);
      this.unsafeMapper.deleteWebhookAttemptLogs();
      this.unsafeMapper.deleteWebhookEventLogs();
      this.unsafeMapper.deleteWebhookTenantMappings();
      this.unsafeMapper.deleteWebhooks();
      keepLockAlive(paramResetDistributedLock);
      this.unsafeMapper.deleteHourlyLogins();
      this.unsafeMapper.deleteRawApplicationRegistrationCounts();
      this.unsafeMapper.deleteApplicationRegistrationCounts();
      this.unsafeMapper.deleteFederatedDomains();
      this.unsafeMapper.deleteIdentityProviderApplicationConfigurations();
      this.unsafeMapper.deleteIdentityProviderTenantConfigurations();
      this.unsafeMapper.deleteIdentityProviderVerificationKeys();
      this.unsafeMapper.deleteIdentityProviders();
      keepLockAlive(paramResetDistributedLock);
      this.unsafeMapper.deleteCleanSpeakApplications();
      this.loginQueue.clear();
      this.loginQueue.__dangerousFlush();
      keepLockAlive(paramResetDistributedLock);
      this.unsafeMapper.deleteApplicationDailyActiveUsers();
      this.unsafeMapper.deleteApplicationMonthlyActiveUsers();
      this.unsafeMapper.deleteRawApplicationRegistrationCounts();
      this.unsafeMapper.deleteApplicationRegistrationCounts();
      Application application = resetFusionAuthApplication();
      this.unsafeMapper.deleteOAuthScopes();
      this.unsafeMapper.deleteApplicationVerificationKeys();
      this.unsafeMapper.deleteApplicationsRolesExceptFor(Application.FUSIONAUTH_APP_ID);
      this.unsafeMapper.deleteApplicationsExceptFor(new UUID[] { Application.FUSIONAUTH_APP_ID, this.tenantManagerApplicationId });
      keepLockAlive(paramResetDistributedLock);
      this.unsafeMapper.deleteTenantVerificationKeys();
      Tenant tenant = resetTenants(paramString, paramInt);
      resetTenantManager(application, tenant.jwtConfiguration.accessTokenKeyId);
      this.unsafeMapper.deleteEntities();
      this.unsafeMapper.deleteEntityTypeVerificationKeys();
      this.unsafeMapper.deleteEntityTypePermissions();
      this.unsafeMapper.deleteEntityTypes();
      this.unsafeMapper.deleteKeysExceptForId(tenant.jwtConfiguration.accessTokenKeyId);
      this.unsafeMapper.deleteIPAccessControlLists();
      this.unsafeMapper.deleteLambdas();
      this.unsafeMapper.deleteThemesExceptFor(Theme.FUSIONAUTH_THEME_ID);
      this.unsafeMapper.resetThemeName(Theme.FUSIONAUTH_THEME_ID);
      resetManagedFormFields(tenant, application);
      this.unsafeMapper.deleteConsents();
      this.unsafeMapper.resetInstance();
      Instance instance = new Instance();
      this.licenseProvider.disconnect();
      this.instanceMapper.removeLicense();
      this.instanceMapper.setupComplete();
      this.instanceMapper.updateData(instance);
      this.unsafeMapper.updateReactorHealthChecksData(null);
      keepLockAlive(paramResetDistributedLock);
      this.unsafeMapper.deleteUserActions();
      this.unsafeMapper.deleteEmailTemplates();
      this.unsafeMapper.deleteCommonBreachedPasswords();
      this.unsafeMapper.deleteCommonPasswordsDatasets();
      this.datasetMapper.update(DatasetMapper.DatasetName.BreachPasswords, ZonedDateTime.ofInstant(Instant.ofEpochMilli(1581476456155L), ZoneOffset.UTC));
      this.unsafeMapper.deleteAsyncTasks();
      this.unsafeMapper.deleteNodes();
      this.unsafeMapper.deleteMessengers();
      this.unsafeMapper.deleteMessageTemplates();
      this.unsafeMapper.deleteIPLocationData();
      this.unsafeMapper.deleteIPLocationMetaData();
      this.unsafeMapper.deleteIpReputationData();
      this.unsafeMapper.deleteIpReputationMetaData();
      this.unsafeMapper.deleteUserAgentReputationData();
      this.unsafeMapper.deleteUserAgentReputationMetaData();
      this.unsafeMapper.deleteUsageStats();
      this.unsafeMapper.deleteCurrentUsageStats();
      this.unsafeMapper.deleteKickstartFiles();
      keepLockAlive(paramResetDistributedLock);
      this.nodeService.reset();
      if (paramBoolean1) {
        this.entitySearchEngine.deleteIndexAndRecreate();
        this.userSearchEngine.deleteIndexAndRecreate();
      } else {
        this.entitySearchEngine.refresh();
        this.entitySearchEngine.deleteAll();
        this.userSearchEngine.refresh();
        this.userSearchEngine.deleteAll();
      } 
      this.cacheLoaderProvider.names().forEach(paramString -> this.cacheLoaderProvider.get(paramString).load());
      if (paramBoolean2)
        logger.info("Reset complete. Total duration [" + Duration.between(instant, Instant.now()).toMillis() + "] ms"); 
    } catch (Exception exception) {
      logger.error("Failed to reset FusionAuth", exception);
      throw new RuntimeException(exception);
    } 
  }
  
  public void resetAfterTest() {
    this.unsafeMapper.deleteIdentityProviderLinks();
    this.unsafeMapper.deleteFederatedDomains();
    this.unsafeMapper.deleteIdentityProviderApplicationConfigurations();
    this.unsafeMapper.deleteIdentityProviderTenantConfigurations();
    this.unsafeMapper.deleteIdentityProviderVerificationKeys();
    this.unsafeMapper.deleteIdentityProviders();
  }
  
  public Thread resetToKickstart(UUID paramUUID) {
    KickstartFile kickstartFile = this.kickstartMapper.retrieveById(paramUUID);
    Thread thread = new Thread(new ResetKickstartRunner(kickstartFile, this.kickstartService, this.resetDistributedLock, this, this.reindexService));
    thread.setDaemon(true);
    thread.start();
    try {
      Thread.sleep(250L);
    } catch (InterruptedException interruptedException) {}
    return thread;
  }
  
  public List<KickstartFile> retrieveAllKickstartFiles() {
    return this.kickstartMapper.retrieveAll();
  }
  
  private void keepLockAlive(ResetDistributedLock paramResetDistributedLock) {
    if (paramResetDistributedLock != null)
      paramResetDistributedLock.keepAlive(); 
  }
  
  private Application resetFusionAuthApplication() {
    Application application1 = this.applicationMapper.retrieveById(null, Application.FUSIONAUTH_APP_ID);
    Application application2 = (new Application()).with(paramApplication2 -> paramApplication2.id = paramApplication1.id).with(paramApplication -> paramApplication.setActive(true)).with(paramApplication2 -> paramApplication2.lastUpdateInstant = paramApplication1.insertInstant).with(paramApplication -> paramApplication.name = "FusionAuth").with(paramApplication2 -> paramApplication2.oauthConfiguration.with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(())).with(paramApplication -> paramApplication.jwtConfiguration.with(()).with(())).with(paramApplication -> paramApplication.verificationStrategy = VerificationStrategy.ClickableLink).with(paramApplication2 -> paramApplication2.tenantId = paramApplication1.tenantId).with(paramApplication2 -> paramApplication2.formConfiguration.adminRegistrationFormId = paramApplication1.formConfiguration.adminRegistrationFormId);
    this.applicationMapper.update(application2);
    return application2;
  }
  
  private void resetIntegrations() {
    this.integrationMapper.update(new Integrations());
  }
  
  private void resetManagedFormFields(Tenant paramTenant, Application paramApplication) {
    this.unsafeMapper.deleteFormSteps();
    Form form1 = this.formService.retrieveById(paramTenant.formConfiguration.adminUserFormId);
    if (form1.type != FormType.adminUser)
      throw new IllegalStateException("Unexpected. The currently assigned form for [tenant.formConfiguration.adminUserFormId] is the wrong type. Expected [" + String.valueOf(FormType.adminUser) + "] but found [" + String.valueOf(form1.type) + "]."); 
    Form form2 = this.formService.retrieveById(paramApplication.formConfiguration.adminRegistrationFormId);
    if (form2.type != FormType.adminRegistration)
      throw new IllegalStateException("Unexpected. The currently assigned form for [tenant.formConfiguration.adminRegistrationFormId] is the wrong type. Expected [" + String.valueOf(FormType.adminRegistration) + "] but found [" + String.valueOf(form2.type) + "]."); 
    this.unsafeMapper.deleteFormsExceptFor(new UUID[] { paramTenant.formConfiguration.adminUserFormId, paramApplication.formConfiguration.adminRegistrationFormId });
    this.unsafeMapper.deleteFormFields();
    Objects.requireNonNull(this.formService);
    ManagedFields.Values.values().forEach(this.formService::createField);
    Form form3 = (new Form()).with(paramForm -> paramForm.name = "Default Admin User provided by FusionAuth").with(paramForm -> paramForm.id = paramTenant.formConfiguration.adminUserFormId).with(paramForm -> paramForm.type = FormType.adminUser).with(paramForm -> paramForm.steps.add((new FormStep()).with(()))).with(paramForm -> paramForm.steps.add((new FormStep()).with(())));
    Form form4 = (new Form()).with(paramForm -> paramForm.name = "Default Admin Registration provided by FusionAuth").with(paramForm -> paramForm.id = paramApplication.formConfiguration.adminRegistrationFormId).with(paramForm -> paramForm.type = FormType.adminRegistration).with(paramForm -> paramForm.steps.add((new FormStep()).with(())));
    Form form5 = (new Form()).with(paramForm -> paramForm.name = "Default User Self Service provided by FusionAuth").with(paramForm -> paramForm.id = UUID.randomUUID()).with(paramForm -> paramForm.type = FormType.selfServiceUser).with(paramForm -> paramForm.steps.add((new FormStep()).with(())));
    this.formService.update(form1, form3);
    this.formService.update(form2, form4);
    this.formService.create(form5);
  }
  
  private void resetSystemConfiguration() {
    SystemConfiguration systemConfiguration = this.systemConfigurationService.retrieve();
    this.systemConfigurationService.update((new SystemConfiguration()).with(paramSystemConfiguration -> paramSystemConfiguration.reportTimezone = ZoneId.of("America/Denver"))
        .with(paramSystemConfiguration -> paramSystemConfiguration.corsConfiguration = (new CORSConfiguration()).with(()).with(()).with(()).with(()).with(()).with(()).with(()))








        
        .with(paramSystemConfiguration2 -> paramSystemConfiguration2.insertInstant = paramSystemConfiguration1.insertInstant)
        .with(paramSystemConfiguration2 -> paramSystemConfiguration2.lastUpdateInstant = paramSystemConfiguration1.lastUpdateInstant)
        .with(paramSystemConfiguration -> paramSystemConfiguration.eventLogConfiguration = new SystemConfiguration.EventLogConfiguration()));
  }
  
  private void resetTenantManager(Application paramApplication, UUID paramUUID) {
    this.applicationMapper.update((new Application())
        .with(paramApplication -> paramApplication.name = "Tenant manager")
        .with(paramApplication -> paramApplication.id = this.tenantManagerApplicationId)
        .with(paramApplication -> paramApplication.universalConfiguration.universal = true)
        .with(paramApplication -> paramApplication.oauthConfiguration.with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()))













        
        .with(paramApplication -> paramApplication.state = ObjectState.Active)
        .with(paramApplication2 -> paramApplication2.formConfiguration.adminRegistrationFormId = paramApplication1.formConfiguration.adminRegistrationFormId)
        .with(paramApplication -> paramApplication.insertInstant = ZonedDateTime.now())
        .with(paramApplication -> paramApplication.lastUpdateInstant = ZonedDateTime.now())
        .with(paramApplication -> paramApplication.jwtConfiguration.with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(())));
    this.applicationMapper.createRoles(List.of((new ApplicationRole(UUID.fromString("631ecd9d-8d40-4c13-8277-80cedb823714"), this.tenantManagerApplicationId, "admin", false, true, "Admin"))
          
          .with(paramApplicationRole -> paramApplicationRole.insertInstant = ZonedDateTime.now())
          .with(paramApplicationRole -> paramApplicationRole.lastUpdateInstant = ZonedDateTime.now())));
  }
  
  private void resetTenantManagerConfiguration() {
    this.tenantManagerMapper.update((new TenantManagerConfiguration())
        .with(paramTenantManagerConfiguration -> paramTenantManagerConfiguration.insertInstant = ZonedDateTime.now())
        .with(paramTenantManagerConfiguration -> paramTenantManagerConfiguration.lastUpdateInstant = ZonedDateTime.now()));
    this.tenantManagerMapper.deleteAllTenantManagerApplicationConfigurations();
    this.unsafeMapper.deleteTenantManagerIdentityProviderTypeConfigurations();
  }
  
  private Tenant resetTenants(String paramString, int paramInt) {
    UUID uUID = (this.keyService.resetDefaultKey()).id;
    Application application = this.applicationMapper.retrieveById(null, Application.FUSIONAUTH_APP_ID);
    Tenant tenant = null;
    for (Tenant tenant1 : this.tenantMapper.retrieveAll()) {
      if (tenant1.id.equals(application.tenantId)) {
        Tenant tenant2 = tenant1;
        tenant1 = new Tenant();
        tenant1.id = tenant2.id;
        tenant1.configured = false;
        tenant1.state = ObjectState.Active;
        tenant1.name = "Default";
        tenant1.connectorPolicies = List.of((new ConnectorPolicy())
            .with(paramConnectorPolicy -> paramConnectorPolicy.connectorId = BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID)
            .with(paramConnectorPolicy -> paramConnectorPolicy.domains.add("*")));
        tenant1.emailConfiguration = (new EmailConfiguration()).with(paramEmailConfiguration -> paramEmailConfiguration.defaultFromName = "FusionAuth");
        tenant1

























































          
          .eventConfiguration = (new EventConfiguration()).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.JWTPublicKeyUpdate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.JWTRefreshTokenRevoke, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.JWTRefresh, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.EntityCreate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.EntityCreateComplete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.EntityDelete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.EntityDeleteComplete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.EntityUpdate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.EntityUpdateComplete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.GroupCreate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.GroupCreateComplete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.GroupDelete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.GroupDeleteComplete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.GroupMemberAdd, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.GroupMemberAddComplete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.GroupMemberRemove, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.GroupMemberRemoveComplete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.GroupMemberUpdate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.GroupMemberUpdateComplete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.GroupUpdate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.GroupUpdateComplete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserIdentityVerified, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserAction, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserBulkCreate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserCreate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserCreateComplete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserDeactivate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserDelete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserDeleteComplete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserEmailUpdate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserEmailVerified, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserIdentityProviderLink, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserIdentityProviderUnlink, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserIdentityUpdate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserLoginIdDuplicateOnCreate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserLoginIdDuplicateOnUpdate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserLoginFailed, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserLoginNewDevice, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserLoginSuccess, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserLoginSuspicious, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserPasswordBreach, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserPasswordResetSend, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserPasswordResetStart, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserPasswordResetSuccess, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserPasswordUpdate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserReactivate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserRegistrationCreate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserRegistrationCreateComplete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserRegistrationDelete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserRegistrationDeleteComplete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserRegistrationUpdate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserRegistrationUpdateComplete, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserRegistrationVerified, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserTwoFactorChallenge, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserTwoFactorFailedAttempt, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserTwoFactorMethodAdd, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserTwoFactorMethodRemove, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserTwoFactorSuccess, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserUpdate, new EventConfiguration.EventConfigurationData(false, TransactionType.None))).with(paramEventConfiguration -> paramEventConfiguration.events.put(EventType.UserUpdateComplete, new EventConfiguration.EventConfigurationData(false, TransactionType.None)));
        tenant1.formConfiguration.adminUserFormId = tenant2.formConfiguration.adminUserFormId;
        tenant1.httpSessionMaxInactiveInterval = 3600;
        tenant1.issuer = "fusionauth.io";
        tenant1
          
          .jwtConfiguration = (new JWTConfiguration()).with(paramJWTConfiguration -> paramJWTConfiguration.enabled = true).with(paramJWTConfiguration -> paramJWTConfiguration.accessTokenKeyId = paramUUID).with(paramJWTConfiguration -> paramJWTConfiguration.idTokenKeyId = KeyService.HS256_CLIENT_SECRET_SHADOW);
        tenant1.lastUpdateInstant = tenant2.insertInstant;
        tenant1
          .passwordEncryptionConfiguration = (new PasswordEncryptionConfiguration()).with(paramPasswordEncryptionConfiguration -> paramPasswordEncryptionConfiguration.encryptionScheme = paramString).with(paramPasswordEncryptionConfiguration -> paramPasswordEncryptionConfiguration.encryptionSchemeFactor = paramInt);
        tenant1
          
          .passwordValidationRules = (new PasswordValidationRules()).with(paramPasswordValidationRules -> paramPasswordValidationRules.minLength = FIPS.minimumPasswordLength()).with(paramPasswordValidationRules -> paramPasswordValidationRules.breachDetection = (new PasswordBreachDetection()).with(()).with(()));
        tenant1.phoneConfiguration = new TenantPhoneConfiguration();
        tenant1.themeId = Theme.FUSIONAUTH_THEME_ID;
        tenant1
          .userDeletePolicy = (new TenantUserDeletePolicy()).with(paramTenantUserDeletePolicy -> paramTenantUserDeletePolicy.unverified.enabled = false).with(paramTenantUserDeletePolicy -> paramTenantUserDeletePolicy.unverified.numberOfDaysToRetain = 30);
        this.tenantMapper.update(tenant1);
        MapperTools.safeCreateUpdate(5000, tenant1.connectorPolicies, paramList -> this.tenantMapper.createConnectorPolicies(paramTenant.id, paramList));
        tenant = tenant1;
        continue;
      } 
      this.tenantMapper.delete(tenant1.id);
    } 
    return tenant;
  }
  
  public static class ResetKickstartRunner implements Runnable {
    private final KickstartFile kickstartFile;
    
    private final KickstartService kickstartService;
    
    private final ReindexService reindexService;
    
    private final ResetDistributedLock resetDistributedLock;
    
    private final ResetService resetService;
    
    public ResetKickstartRunner(KickstartFile param1KickstartFile, KickstartService param1KickstartService, ResetDistributedLock param1ResetDistributedLock, ResetService param1ResetService, ReindexService param1ReindexService) {
      this.kickstartFile = param1KickstartFile;
      this.kickstartService = param1KickstartService;
      this.reindexService = param1ReindexService;
      this.resetDistributedLock = param1ResetDistributedLock;
      this.resetService = param1ResetService;
    }
    
    public void run() {
      ResetDistributedLock resetDistributedLock = this.resetDistributedLock.lock();
      try {
        if (resetDistributedLock == null) {
          DefaultResetService.logger.debug("Reset attempted to start but the lock could not be obtained. Perhaps another reset is already in progress.");
          if (resetDistributedLock != null)
            resetDistributedLock.close(); 
          return;
        } 
        this.resetService.reset("salted-pbkdf2-hmac-sha256", 24000, true, resetDistributedLock, true);
        DefaultResetService.logger.info("Delete the entire Entity search index and rebuild it");
        try {
          resetDistributedLock.keepAlive();
          this.reindexService.reindexEntities().join();
        } catch (InterruptedException interruptedException) {}
        DefaultResetService.logger.info("Delete the entire User search index and rebuild it");
        try {
          resetDistributedLock.keepAlive();
          this.reindexService.reindexUsers().join();
        } catch (InterruptedException interruptedException) {}
        resetDistributedLock.keepAlive();
        Path path = null;
        try {
          path = Files.createTempDirectory("_kickstart", (FileAttribute<?>[])new FileAttribute[0]);
          path.toFile().deleteOnExit();
          FileTools.unzip(new ByteArrayInputStream(this.kickstartFile.kickstart), path);
          Path path1 = path.resolve("kickstart.json");
          this.kickstartService.kickstart(path1.toFile());
        } catch (Exception exception) {
          throw new ErrorException(exception, new Object[0]);
        } finally {
          if (path != null)
            try {
              Files.delete(path);
            } catch (IOException iOException) {} 
        } 
        File file = path.toFile();
        FileTools.deleteFolder(file);
        if (resetDistributedLock != null)
          resetDistributedLock.close(); 
      } catch (Throwable throwable) {
        if (resetDistributedLock != null)
          try {
            resetDistributedLock.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    }
  }
}
