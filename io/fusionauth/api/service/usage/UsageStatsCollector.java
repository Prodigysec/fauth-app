package io.fusionauth.api.service.usage;

import com.google.inject.name.Named;
import com.inversoft.authentication.api.domain.AuthenticationKeyFormat;
import com.inversoft.jdbc.CurrentDatabaseEngine;
import com.inversoft.search.ElasticSearchClient;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.CollectedUsageStats;
import io.fusionauth.api.domain.InstallationType;
import io.fusionauth.api.domain.Instance;
import io.fusionauth.api.domain.InstanceMapper;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.api.domain.SavedCurrentStats;
import io.fusionauth.api.domain.SearchEngineType;
import io.fusionauth.api.domain.UsageStatsCollectorMapper;
import io.fusionauth.api.domain.UsageStatsMapper;
import io.fusionauth.api.domain.VersionMapper;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.search.UserSearchEngine;
import io.fusionauth.api.service.system.NodeService;
import io.fusionauth.api.service.system.SystemConfigurationService;
import io.fusionauth.api.time.TimeUtils;
import io.fusionauth.app.service.MasterNodeRunnable;
import io.fusionauth.usagestats.shared.domain.UsageStatsList;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Set;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsageStatsCollector extends MasterNodeRunnable {
  private static final Logger logger = LoggerFactory.getLogger(UsageStatsCollector.class);
  
  private final Set<RuntimeMode> RunnableRuntimeModes = Set.of(RuntimeMode.Development, RuntimeMode.Production, RuntimeMode.FusionAuth_Development);
  
  private final FusionAuthConfiguration configuration;
  
  private final InstanceMapper instanceMapper;
  
  private final ReactorStatusService reactorStatusService;
  
  private final UserSearchEngine searchEngine;
  
  private final SystemConfigurationService systemConfigurationService;
  
  private final UsageStatsCollectorMapper usageStatsCollectorMapper;
  
  private final UsageStatsMapper usageStatsMapper;
  
  private final VersionMapper versionMapper;
  
  @Inject
  public UsageStatsCollector(FusionAuthConfiguration paramFusionAuthConfiguration, InstanceMapper paramInstanceMapper, NodeService paramNodeService, @Named("background") UsageStatsMapper paramUsageStatsMapper, @Named("background") UsageStatsCollectorMapper paramUsageStatsCollectorMapper, VersionMapper paramVersionMapper, ReactorStatusService paramReactorStatusService, SystemConfigurationService paramSystemConfigurationService, UserSearchEngine paramUserSearchEngine) {
    super(paramNodeService);
    this.configuration = paramFusionAuthConfiguration;
    this.instanceMapper = paramInstanceMapper;
    this.reactorStatusService = paramReactorStatusService;
    this.systemConfigurationService = paramSystemConfigurationService;
    this.searchEngine = paramUserSearchEngine;
    this.usageStatsCollectorMapper = paramUsageStatsCollectorMapper;
    this.usageStatsMapper = paramUsageStatsMapper;
    this.versionMapper = paramVersionMapper;
  }
  
  public void runScheduled() {
    int i = TimeUtils.toDay(ZonedDateTime.now()) - 7;
    CollectedUsageStats collectedUsageStats = new CollectedUsageStats();
    collectedUsageStats.instanceId = (this.instanceMapper.retrieve()).id;
    collectedUsageStats.applicationVersion = this.versionMapper.retrieveDatabaseVersion();
    collectedUsageStats.statsVersion = "0.13.0";
    collectedUsageStats.collectionInstant = ZonedDateTime.now(ZoneOffset.UTC);
    Timer timer = new Timer();
    SavedCurrentStats savedCurrentStats = this.usageStatsMapper.retrieveCurrentUsageStats();
    if (savedCurrentStats == null) {
      logger.warn("No current stats found.  Not collecting");
      return;
    } 
    if (savedCurrentStats.lastCheckedInstant.isBefore(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1L))) {
      logger.warn("Current stats not refreshed for more than one day.  Not collecting");
      return;
    } 
    for (String str : savedCurrentStats.stats) {
      collectStat(str, collectedUsageStats, i);
      try {
        Thread.sleep(this.configuration.usageDataThrottleTime());
      } catch (InterruptedException interruptedException) {}
    } 
    collectedUsageStats.collectionDuration = timer.stop();
    logger.debug("Inserting collected usage stats: [{}]", collectedUsageStats);
    this.usageStatsMapper.create(collectedUsageStats);
  }
  
  protected boolean shouldRun() {
    Instance instance = this.instanceMapper.retrieve();
    return (this.RunnableRuntimeModes.contains(this.configuration.runtimeMode()) && instance.setupComplete && this.systemConfigurationService
      
      .isUsageStatsEnabled());
  }
  
  private void collectStat(String paramString, CollectedUsageStats paramCollectedUsageStats, int paramInt) {
    UsageStatsList.Stat stat = UsageStatsList.Stat.get(paramString);
    if (stat.status == UsageStatsList.Stat.Status.DEPRECATED) {
      logger.debug("Did not collect deprecated stat [{}]", paramString);
      paramCollectedUsageStats.failedStats.add(paramString);
      return;
    } 
    Timer timer = new Timer();
    try {
      InstallationType installationType;
      String str;
      switch (stat) {
        case ApiKeyExpiringEnabled:
          paramCollectedUsageStats.stats.apiKeyExpiringEnabled = this.usageStatsCollectorMapper.apiKeyExpiringEnabled();
          paramCollectedUsageStats.timings.apiKeyExpiringEnabled = Long.valueOf(timer.stop());
          break;
        case ApiKeyHashedEnabled:
          paramCollectedUsageStats.stats.apiKeyHashedEnabled = this.usageStatsCollectorMapper.apiKeyHashedEnabled(AuthenticationKeyFormat.None.ordinal());
          paramCollectedUsageStats.timings.apiKeyHashedEnabled = Long.valueOf(timer.stop());
          break;
        case ApplicationActiveNumber:
          paramCollectedUsageStats.stats.applicationActiveNumber = this.usageStatsCollectorMapper.applicationActiveNumber();
          paramCollectedUsageStats.timings.applicationActiveNumber = Long.valueOf(timer.stop());
          break;
        case ApplicationActiveLoggedIntoNumber:
          paramCollectedUsageStats.stats.applicationActiveLoggedIntoNumber = this.usageStatsCollectorMapper.applicationActiveLoggedIntoNumber(paramInt);
          paramCollectedUsageStats.timings.applicationActiveLoggedIntoNumber = Long.valueOf(timer.stop());
          break;
        case ApplicationActiveRoleNumber:
          paramCollectedUsageStats.stats.applicationActiveRoleNumber = this.usageStatsCollectorMapper.applicationActiveRoleNumber(paramInt);
          paramCollectedUsageStats.timings.applicationActiveRoleNumber = Long.valueOf(timer.stop());
          break;
        case ApplicationActsAsSamlv2Idp:
          paramCollectedUsageStats.stats.applicationActsAsSamlv2Idp = this.usageStatsCollectorMapper.applicationActsAsSamlv2Idp(paramInt);
          paramCollectedUsageStats.timings.applicationActsAsSamlv2Idp = Long.valueOf(timer.stop());
          break;
        case ApplicationAdminMonthlyActiveUsers:
          paramCollectedUsageStats.stats.applicationAdminMonthlyActiveUsers = this.usageStatsCollectorMapper.applicationAdminMonthlyActiveUsers();
          paramCollectedUsageStats.timings.applicationAdminMonthlyActiveUsers = Long.valueOf(timer.stop());
          break;
        case ApplicationBasicSelfServiceRegistrationEnabled:
          paramCollectedUsageStats.stats.applicationBasicSelfServiceRegistrationEnabled = this.usageStatsCollectorMapper.applicationBasicSelfServiceRegistrationEnabled(paramInt);
          paramCollectedUsageStats.timings.applicationBasicSelfServiceRegistrationEnabled = Long.valueOf(timer.stop());
          break;
        case ApplicationAdvancedSelfServiceRegistrationEnabled:
          paramCollectedUsageStats.stats.applicationAdvancedSelfServiceRegistrationEnabled = this.usageStatsCollectorMapper.applicationAdvancedSelfServiceRegistrationEnabled(paramInt);
          paramCollectedUsageStats.timings.applicationAdvancedSelfServiceRegistrationEnabled = Long.valueOf(timer.stop());
          break;
        case ApplicationCustomSelfServiceAccountForm:
          paramCollectedUsageStats.stats.applicationCustomSelfServiceAccountForm = this.usageStatsCollectorMapper.applicationCustomSelfServiceAccountForm(paramInt);
          paramCollectedUsageStats.timings.applicationCustomSelfServiceAccountForm = Long.valueOf(timer.stop());
          break;
        case ApplicationAdvancedSelfServiceRegistrationPreVerified:
          paramCollectedUsageStats.stats.applicationAdvancedSelfServiceRegistrationPreVerified = this.usageStatsCollectorMapper.applicationAdvancedSelfServiceRegistrationPreVerified(paramInt);
          paramCollectedUsageStats.timings.applicationAdvancedSelfServiceRegistrationPreVerified = Long.valueOf(timer.stop());
          break;
        case ApplicationCustomThemeUsed:
          paramCollectedUsageStats.stats.applicationCustomThemeUsed = this.usageStatsCollectorMapper.applicationCustomThemeUsed(paramInt);
          paramCollectedUsageStats.timings.applicationCustomThemeUsed = Long.valueOf(timer.stop());
          break;
        case ApplicationEmailUpdateTemplate:
          paramCollectedUsageStats.stats.applicationEmailUpdateTemplate = this.usageStatsCollectorMapper.applicationEmailUpdateTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationEmailUpdateTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationEmailVerificationTemplate:
          paramCollectedUsageStats.stats.applicationEmailVerificationTemplate = this.usageStatsCollectorMapper.applicationEmailVerificationTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationEmailVerificationTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationEmailVerifiedTemplate:
          paramCollectedUsageStats.stats.applicationEmailVerifiedTemplate = this.usageStatsCollectorMapper.applicationEmailVerifiedTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationEmailVerifiedTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationEnabledIdentityProviders:
          paramCollectedUsageStats.stats.applicationEnabledIdentityProviders = this.usageStatsCollectorMapper.applicationEnabledIdentityProviders(paramInt);
          paramCollectedUsageStats.timings.applicationEnabledIdentityProviders = Long.valueOf(timer.stop());
          break;
        case ApplicationEnabledIdentityProvidersWithLambdas:
          paramCollectedUsageStats.stats.applicationEnabledIdentityProvidersWithLambdas = this.usageStatsCollectorMapper.applicationEnabledIdentityProvidersWithLambdas(paramInt);
          paramCollectedUsageStats.timings.applicationEnabledIdentityProvidersWithLambdas = Long.valueOf(timer.stop());
          break;
        case ApplicationEnabledTenantIdentityProviders:
          paramCollectedUsageStats.stats.applicationEnabledTenantIdentityProviders = this.usageStatsCollectorMapper.applicationEnabledTenantIdentityProviders(paramInt);
          paramCollectedUsageStats.timings.applicationEnabledTenantIdentityProviders = Long.valueOf(timer.stop());
          break;
        case ApplicationForgotPasswordTemplate:
          paramCollectedUsageStats.stats.applicationForgotPasswordTemplate = this.usageStatsCollectorMapper.applicationForgotPasswordTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationForgotPasswordTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationLoggedInApplicationsUsingSimpleThemes:
          paramCollectedUsageStats.stats.applicationLoggedInApplicationsUsingSimpleThemes = this.usageStatsCollectorMapper.applicationLoggedInApplicationsUsingSimpleThemes(paramInt);
          paramCollectedUsageStats.timings.applicationLoggedInApplicationsUsingSimpleThemes = Long.valueOf(timer.stop());
          break;
        case ApplicationLoginApiAuthenticationDisabled:
          paramCollectedUsageStats.stats.applicationLoginApiAuthenticationDisabled = this.usageStatsCollectorMapper.applicationLoginApiAuthenticationDisabled(paramInt);
          paramCollectedUsageStats.timings.applicationLoginApiAuthenticationDisabled = Long.valueOf(timer.stop());
          break;
        case ApplicationLoginApiRefreshTokensEnabled:
          paramCollectedUsageStats.stats.applicationLoginApiRefreshTokensEnabled = this.usageStatsCollectorMapper.applicationLoginApiRefreshTokensEnabled(paramInt);
          paramCollectedUsageStats.timings.applicationLoginApiRefreshTokensEnabled = Long.valueOf(timer.stop());
          break;
        case ApplicationLoginIdInUseOnCreateTemplate:
          paramCollectedUsageStats.stats.applicationLoginIdInUseOnCreateTemplate = this.usageStatsCollectorMapper.applicationLoginIdInUseOnCreateTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationLoginIdInUseOnCreateTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationLoginIdInUseOnUpdateTemplate:
          paramCollectedUsageStats.stats.applicationLoginIdInUseOnUpdateTemplate = this.usageStatsCollectorMapper.applicationLoginIdInUseOnUpdateTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationLoginIdInUseOnUpdateTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationLoginNewDevice:
          paramCollectedUsageStats.stats.applicationLoginNewDevice = this.usageStatsCollectorMapper.applicationLoginNewDevice(paramInt);
          paramCollectedUsageStats.timings.applicationLoginNewDevice = Long.valueOf(timer.stop());
          break;
        case ApplicationLoginSuspiciousEmailTemplate:
          paramCollectedUsageStats.stats.applicationLoginSuspiciousEmailTemplate = this.usageStatsCollectorMapper.applicationLoginSuspiciousEmailTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationLoginSuspiciousEmailTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationMagicLinks:
          paramCollectedUsageStats.stats.applicationMagicLinks = this.usageStatsCollectorMapper.applicationMagicLinks(paramInt);
          paramCollectedUsageStats.timings.applicationMagicLinks = Long.valueOf(timer.stop());
          break;
        case ApplicationMfaPolicy:
          paramCollectedUsageStats.stats.applicationMfaPolicy = this.usageStatsCollectorMapper.applicationMfaPolicy(paramInt);
          paramCollectedUsageStats.timings.applicationMfaPolicy = Long.valueOf(timer.stop());
          break;
        case ApplicationMfaPolicyChallengeOnHighRisk:
          paramCollectedUsageStats.stats.applicationMfaPolicyChallengeOnHighRisk = this.usageStatsCollectorMapper.applicationMfaPolicyChallengeOnHighRisk(paramInt);
          paramCollectedUsageStats.timings.applicationMfaPolicyChallengeOnHighRisk = Long.valueOf(timer.stop());
          break;
        case ApplicationMfaPolicyChallengeOnMediumRisk:
          paramCollectedUsageStats.stats.applicationMfaPolicyChallengeOnMediumRisk = this.usageStatsCollectorMapper.applicationMfaPolicyChallengeOnMediumRisk(paramInt);
          paramCollectedUsageStats.timings.applicationMfaPolicyChallengeOnMediumRisk = Long.valueOf(timer.stop());
          break;
        case ApplicationMfaPolicyDisabled:
          paramCollectedUsageStats.stats.applicationMfaPolicyDisabled = this.usageStatsCollectorMapper.applicationMfaPolicyDisabled(paramInt);
          paramCollectedUsageStats.timings.applicationMfaPolicyDisabled = Long.valueOf(timer.stop());
          break;
        case ApplicationMfaPolicyEnabled:
          paramCollectedUsageStats.stats.applicationMfaPolicyEnabled = this.usageStatsCollectorMapper.applicationMfaPolicyEnabled(paramInt);
          paramCollectedUsageStats.timings.applicationMfaPolicyEnabled = Long.valueOf(timer.stop());
          break;
        case ApplicationMfaPolicyRequired:
          paramCollectedUsageStats.stats.applicationMfaPolicyRequired = this.usageStatsCollectorMapper.applicationMfaPolicyRequired(paramInt);
          paramCollectedUsageStats.timings.applicationMfaPolicyRequired = Long.valueOf(timer.stop());
          break;
        case ApplicationMfaLambdasConfigured:
          paramCollectedUsageStats.stats.applicationMfaLambdasConfigured = this.usageStatsCollectorMapper.applicationMfaLambdasConfigured(paramInt);
          paramCollectedUsageStats.timings.applicationMfaLambdasConfigured = Long.valueOf(timer.stop());
          break;
        case ApplicationMultiFactorEmailMessageTemplate:
          paramCollectedUsageStats.stats.applicationMultiFactorEmailMessageTemplate = this.usageStatsCollectorMapper.applicationMultiFactorEmailMessageTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationMultiFactorEmailMessageTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationMultiFactorSmsMessageTemplate:
          paramCollectedUsageStats.stats.applicationMultiFactorSmsMessageTemplate = this.usageStatsCollectorMapper.applicationMultiFactorSmsMessageTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationMultiFactorSmsMessageTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationNumber:
          paramCollectedUsageStats.stats.applicationNumber = this.usageStatsCollectorMapper.applicationNumber();
          paramCollectedUsageStats.timings.applicationNumber = Long.valueOf(timer.stop());
          break;
        case ApplicationOtherMonthlyActiveUsers:
          paramCollectedUsageStats.stats.applicationOtherMonthlyActiveUsers = this.usageStatsCollectorMapper.applicationOtherMonthlyActiveUsers();
          paramCollectedUsageStats.timings.applicationOtherMonthlyActiveUsers = Long.valueOf(timer.stop());
          break;
        case ApplicationPasswordResetSuccessTemplate:
          paramCollectedUsageStats.stats.applicationPasswordResetSuccessTemplate = this.usageStatsCollectorMapper.applicationPasswordResetSuccessTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationPasswordResetSuccessTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationPasswordUpdateTemplate:
          paramCollectedUsageStats.stats.applicationPasswordUpdateTemplate = this.usageStatsCollectorMapper.applicationPasswordUpdateTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationPasswordUpdateTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationPasswordlessTemplate:
          paramCollectedUsageStats.stats.applicationPasswordlessTemplate = this.usageStatsCollectorMapper.applicationPasswordlessTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationPasswordlessTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationBasicSelfServicePhoneRegistrationEnabled:
          paramCollectedUsageStats.stats.applicationBasicSelfServicePhoneRegistrationEnabled = this.usageStatsCollectorMapper.applicationBasicSelfServicePhoneRegistrationEnabled(paramInt);
          paramCollectedUsageStats.timings.applicationBasicSelfServicePhoneRegistrationEnabled = Long.valueOf(timer.stop());
          break;
        case ApplicationRefreshTokenGracePeriodEnabled:
          paramCollectedUsageStats.stats.applicationRefreshTokenGracePeriodEnabled = this.usageStatsCollectorMapper.applicationRefreshTokenGracePeriodEnabled(paramInt);
          paramCollectedUsageStats.timings.applicationRefreshTokenGracePeriodEnabled = Long.valueOf(timer.stop());
          break;
        case ApplicationSetPasswordTemplate:
          paramCollectedUsageStats.stats.applicationSetPasswordTemplate = this.usageStatsCollectorMapper.applicationSetPasswordTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationSetPasswordTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationRequiredScopes:
          paramCollectedUsageStats.stats.applicationRequiredScopes = this.usageStatsCollectorMapper.applicationRequiredScopes(paramInt);
          paramCollectedUsageStats.timings.applicationRequiredScopes = Long.valueOf(timer.stop());
          break;
        case ApplicationOptionalScopes:
          paramCollectedUsageStats.stats.applicationOptionalScopes = this.usageStatsCollectorMapper.applicationOptionalScopes(paramInt);
          paramCollectedUsageStats.timings.applicationOptionalScopes = Long.valueOf(timer.stop());
          break;
        case ApplicationTenantManagerMonthlyActiveUsers:
          paramCollectedUsageStats.stats.applicationTenantManagerMonthlyActiveUsers = this.usageStatsCollectorMapper.applicationTenantManagerMonthlyActiveUsers();
          paramCollectedUsageStats.timings.applicationTenantManagerMonthlyActiveUsers = Long.valueOf(timer.stop());
          break;
        case ApplicationThirdParty:
          paramCollectedUsageStats.stats.applicationThirdParty = this.usageStatsCollectorMapper.applicationThirdParty(paramInt);
          paramCollectedUsageStats.timings.applicationThirdParty = Long.valueOf(timer.stop());
          break;
        case ApplicationForgotPasswordPhoneTemplate:
          paramCollectedUsageStats.stats.applicationForgotPasswordPhoneTemplate = this.usageStatsCollectorMapper.applicationForgotPasswordPhoneTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationForgotPasswordPhoneTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationIdentityUpdatePhoneTemplate:
          paramCollectedUsageStats.stats.applicationIdentityUpdatePhoneTemplate = this.usageStatsCollectorMapper.applicationIdentityUpdatePhoneTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationIdentityUpdatePhoneTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationLoginIdInUseOnCreatePhoneTemplate:
          paramCollectedUsageStats.stats.applicationLoginIdInUseOnCreatePhoneTemplate = this.usageStatsCollectorMapper.applicationLoginIdInUseOnCreatePhoneTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationLoginIdInUseOnCreatePhoneTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationLoginIdInUseOnUpdatePhoneTemplate:
          paramCollectedUsageStats.stats.applicationLoginIdInUseOnUpdatePhoneTemplate = this.usageStatsCollectorMapper.applicationLoginIdInUseOnUpdatePhoneTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationLoginIdInUseOnUpdatePhoneTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationLoginNewDevicePhoneTemplate:
          paramCollectedUsageStats.stats.applicationLoginNewDevicePhoneTemplate = this.usageStatsCollectorMapper.applicationLoginNewDevicePhoneTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationLoginNewDevicePhoneTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationLoginSuspiciousPhoneTemplate:
          paramCollectedUsageStats.stats.applicationLoginSuspiciousPhoneTemplate = this.usageStatsCollectorMapper.applicationLoginSuspiciousPhoneTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationLoginSuspiciousPhoneTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationPasswordlessPhoneTemplate:
          paramCollectedUsageStats.stats.applicationPasswordlessPhoneTemplate = this.usageStatsCollectorMapper.applicationPasswordlessPhoneTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationPasswordlessPhoneTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationPasswordResetSuccessPhoneTemplate:
          paramCollectedUsageStats.stats.applicationPasswordResetSuccessPhoneTemplate = this.usageStatsCollectorMapper.applicationPasswordResetSuccessPhoneTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationPasswordResetSuccessPhoneTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationPasswordUpdatePhoneTemplate:
          paramCollectedUsageStats.stats.applicationPasswordUpdatePhoneTemplate = this.usageStatsCollectorMapper.applicationPasswordUpdatePhoneTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationPasswordUpdatePhoneTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationSetPasswordPhoneTemplate:
          paramCollectedUsageStats.stats.applicationSetPasswordPhoneTemplate = this.usageStatsCollectorMapper.applicationSetPasswordPhoneTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationSetPasswordPhoneTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationTwoFactorMethodAddPhoneTemplate:
          paramCollectedUsageStats.stats.applicationTwoFactorMethodAddPhoneTemplate = this.usageStatsCollectorMapper.applicationTwoFactorMethodAddPhoneTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationTwoFactorMethodAddPhoneTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationTwoFactorMethodRemovePhoneTemplate:
          paramCollectedUsageStats.stats.applicationTwoFactorMethodRemovePhoneTemplate = this.usageStatsCollectorMapper.applicationTwoFactorMethodRemovePhoneTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationTwoFactorMethodRemovePhoneTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationVerificationCompletePhoneTemplate:
          paramCollectedUsageStats.stats.applicationVerificationCompletePhoneTemplate = this.usageStatsCollectorMapper.applicationVerificationCompletePhoneTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationVerificationCompletePhoneTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationVerificationPhoneTemplate:
          paramCollectedUsageStats.stats.applicationVerificationPhoneTemplate = this.usageStatsCollectorMapper.applicationVerificationPhoneTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationVerificationPhoneTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationTwoFactorMethodAddTemplate:
          paramCollectedUsageStats.stats.applicationTwoFactorMethodAddTemplate = this.usageStatsCollectorMapper.applicationTwoFactorMethodAddTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationTwoFactorMethodAddTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationTwoFactorMethodRemoveTemplate:
          paramCollectedUsageStats.stats.applicationTwoFactorMethodRemoveTemplate = this.usageStatsCollectorMapper.applicationTwoFactorMethodRemoveTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationTwoFactorMethodRemoveTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationVerificationTemplate:
          paramCollectedUsageStats.stats.applicationVerificationTemplate = this.usageStatsCollectorMapper.applicationVerificationTemplate(paramInt);
          paramCollectedUsageStats.timings.applicationVerificationTemplate = Long.valueOf(timer.stop());
          break;
        case ApplicationSelfServiceRegistrationValidationLambda:
          paramCollectedUsageStats.stats.applicationSelfServiceRegistrationValidationLambda = this.usageStatsCollectorMapper.applicationSelfServiceRegistrationValidationLambda(paramInt);
          paramCollectedUsageStats.timings.applicationSelfServiceRegistrationValidationLambda = Long.valueOf(timer.stop());
          break;
        case ApplicationUserinfoPopulateLambda:
          paramCollectedUsageStats.stats.applicationUserinfoPopulateLambda = this.usageStatsCollectorMapper.applicationUserinfoPopulateLambda(paramInt);
          paramCollectedUsageStats.timings.applicationUserinfoPopulateLambda = Long.valueOf(timer.stop());
          break;
        case ApplicationAccessTokenPopulateLambda:
          paramCollectedUsageStats.stats.applicationAccessTokenPopulateLambda = this.usageStatsCollectorMapper.applicationAccessTokenPopulateLambda(paramInt);
          paramCollectedUsageStats.timings.applicationAccessTokenPopulateLambda = Long.valueOf(timer.stop());
          break;
        case ApplicationSamlv2PopulateLambda:
          paramCollectedUsageStats.stats.applicationSamlv2PopulateLambda = this.usageStatsCollectorMapper.applicationSamlv2PopulateLambda(paramInt);
          paramCollectedUsageStats.timings.applicationSamlv2PopulateLambda = Long.valueOf(timer.stop());
          break;
        case ApplicationIdTokenPopulateLambda:
          paramCollectedUsageStats.stats.applicationIdTokenPopulateLambda = this.usageStatsCollectorMapper.applicationIdTokenPopulateLambda(paramInt);
          paramCollectedUsageStats.timings.applicationIdTokenPopulateLambda = Long.valueOf(timer.stop());
          break;
        case AtdCaptcha:
          paramCollectedUsageStats.stats.atdCaptcha = this.usageStatsCollectorMapper.atdCaptcha();
          paramCollectedUsageStats.timings.atdCaptcha = Long.valueOf(timer.stop());
          break;
        case AtdIpAcls:
          paramCollectedUsageStats.stats.atdIpAcls = this.usageStatsCollectorMapper.atdIpAcls();
          paramCollectedUsageStats.timings.atdIpAcls = Long.valueOf(timer.stop());
          break;
        case AtdRateLimiting:
          paramCollectedUsageStats.stats.atdRateLimiting = this.usageStatsCollectorMapper.atdRateLimiting();
          paramCollectedUsageStats.timings.atdRateLimiting = Long.valueOf(timer.stop());
          break;
        case ScimEnabled:
          paramCollectedUsageStats.stats.scimEnabled = this.usageStatsCollectorMapper.scimEnabled();
          paramCollectedUsageStats.timings.scimEnabled = Long.valueOf(timer.stop());
          break;
        case EntitiesEntityGrants:
          paramCollectedUsageStats.stats.entitiesEntityGrants = this.usageStatsCollectorMapper.entitiesEntityGrants();
          paramCollectedUsageStats.timings.entitiesEntityGrants = Long.valueOf(timer.stop());
          break;
        case EntitiesPermissions:
          paramCollectedUsageStats.stats.entitiesPermissions = this.usageStatsCollectorMapper.entitiesPermissions();
          paramCollectedUsageStats.timings.entitiesPermissions = Long.valueOf(timer.stop());
          break;
        case EntitiesTypeNumber:
          paramCollectedUsageStats.stats.entitiesTypeNumber = this.usageStatsCollectorMapper.entitiesTypeNumber();
          paramCollectedUsageStats.timings.entitiesTypeNumber = Long.valueOf(timer.stop());
          break;
        case GroupApplicationRoles:
          paramCollectedUsageStats.stats.groupApplicationRoles = this.usageStatsCollectorMapper.groupApplicationRoles(paramInt);
          paramCollectedUsageStats.timings.groupApplicationRoles = Long.valueOf(timer.stop());
          break;
        case InstallationType:
          installationType = this.configuration.installationType();
          paramCollectedUsageStats.stats.installationType = (installationType != null) ? installationType.name() : InstallationType.standard.name();
          paramCollectedUsageStats.timings.installationType = Long.valueOf(timer.stop());
          break;
        case InstanceCompanyPlan:
          paramCollectedUsageStats.stats.instanceCompanyPlan = (this.reactorStatusService.retrieveStatus()).licenseAttributes.get("LicensedPlan");
          paramCollectedUsageStats.timings.instanceCompanyPlan = Long.valueOf(timer.stop());
          break;
        case InstanceDailyActiveUsers:
          paramCollectedUsageStats.stats.instanceDailyActiveUsers = this.usageStatsCollectorMapper.instanceDailyActiveUsers();
          paramCollectedUsageStats.timings.instanceDailyActiveUsers = Long.valueOf(timer.stop());
          break;
        case InstanceDatabaseConnectionTimeout:
          paramCollectedUsageStats.stats.instanceDatabaseConnectionTimeout = Long.valueOf(this.configuration.databaseConnectionTimeout());
          paramCollectedUsageStats.timings.instanceDatabaseConnectionTimeout = Long.valueOf(timer.stop());
          break;
        case InstanceDatabaseIdleTimeout:
          paramCollectedUsageStats.stats.instanceDatabaseIdleTimeout = Long.valueOf(this.configuration.databaseConnectionIdleTimeout());
          paramCollectedUsageStats.timings.instanceDatabaseIdleTimeout = Long.valueOf(timer.stop());
          break;
        case InstanceDatabaseMaxLifetime:
          paramCollectedUsageStats.stats.instanceDatabaseMaxLifetime = Long.valueOf(this.configuration.databaseConnectionMaxLifetime());
          paramCollectedUsageStats.timings.instanceDatabaseMaxLifetime = Long.valueOf(timer.stop());
          break;
        case InstanceDatabaseMaximumPoolSize:
          paramCollectedUsageStats.stats.instanceDatabaseMaximumPoolSize = Integer.valueOf(this.configuration.databaseMaximumPoolSize());
          paramCollectedUsageStats.timings.instanceDatabaseMaximumPoolSize = Long.valueOf(timer.stop());
          break;
        case InstanceDatabaseMinimumIdle:
          paramCollectedUsageStats.stats.instanceDatabaseMinimumIdle = Integer.valueOf(this.configuration.databaseConnectionMinimumIdle());
          paramCollectedUsageStats.timings.instanceDatabaseMinimumIdle = Long.valueOf(timer.stop());
          break;
        case InstanceDatabaseMysqlEnforceUtf8mb4:
          paramCollectedUsageStats.stats.instanceDatabaseMysqlEnforceUtf8mb4 = Boolean.valueOf(this.configuration.databaseMySQLEnforceUTF8());
          paramCollectedUsageStats.timings.instanceDatabaseMysqlEnforceUtf8mb4 = Long.valueOf(timer.stop());
          break;
        case InstanceDatabaseVersion:
          paramCollectedUsageStats.stats.instanceDatabaseVersion = CurrentDatabaseEngine.version;
          paramCollectedUsageStats.timings.instanceDatabaseVersion = Long.valueOf(timer.stop());
          break;
        case InstanceElasticsearchVersion:
          str = this.configuration.searchEngineType().toString();
          if (str.equals(SearchEngineType.elasticsearch.name())) {
            this.searchEngine.status();
            paramCollectedUsageStats.stats.instanceElasticsearchVersion = ElasticSearchClient.VERSION;
          } 
          paramCollectedUsageStats.timings.instanceElasticsearchVersion = Long.valueOf(timer.stop());
          break;
        case InstanceFirstTimeSetupProgress:
          paramCollectedUsageStats.stats.instanceFirstTimeSetupProgress = this.usageStatsCollectorMapper.instanceFirstTimeSetupProgress();
          paramCollectedUsageStats.timings.instanceFirstTimeSetupProgress = Long.valueOf(timer.stop());
          break;
        case InstanceFusionauthAppHttpReadTimeout:
          paramCollectedUsageStats.stats.instanceFusionauthAppHttpReadTimeout = Integer.valueOf(this.configuration.httpReadTimeout());
          paramCollectedUsageStats.timings.instanceFusionauthAppHttpReadTimeout = Long.valueOf(timer.stop());
          break;
        case InstanceFusionauthAppLocalMetricsEnabled:
          paramCollectedUsageStats.stats.instanceFusionauthAppLocalMetricsEnabled = Boolean.valueOf(this.configuration.localMetricsEnabled());
          paramCollectedUsageStats.timings.instanceFusionauthAppLocalMetricsEnabled = Long.valueOf(timer.stop());
          break;
        case InstanceFusionauthAppMemory:
          paramCollectedUsageStats.stats.instanceFusionauthAppMemory = Long.valueOf(Runtime.getRuntime().maxMemory());
          paramCollectedUsageStats.timings.instanceFusionauthAppMemory = Long.valueOf(timer.stop());
          break;
        case InstanceFusionauthAppReindexBatchSize:
          paramCollectedUsageStats.stats.instanceFusionauthAppReindexBatchSize = Integer.valueOf(this.configuration.reindexBatchSize());
          paramCollectedUsageStats.timings.instanceFusionauthAppReindexBatchSize = Long.valueOf(timer.stop());
          break;
        case InstanceFusionauthAppReindexThreadCount:
          paramCollectedUsageStats.stats.instanceFusionauthAppReindexThreadCount = Integer.valueOf(this.configuration.reindexThreadCount());
          paramCollectedUsageStats.timings.instanceFusionauthAppReindexThreadCount = Long.valueOf(timer.stop());
          break;
        case InstanceFusionauthAppSearchDefaultRefreshInterval:
          paramCollectedUsageStats.stats.instanceFusionauthAppSearchDefaultRefreshInterval = this.configuration.searchEngineDefaultRefreshInterval();
          paramCollectedUsageStats.timings.instanceFusionauthAppSearchDefaultRefreshInterval = Long.valueOf(timer.stop());
          break;
        case InstanceFusionauthSearchHostsCount:
          paramCollectedUsageStats.stats.instanceFusionauthSearchHostsCount = Integer.valueOf((this.configuration.searchServers()).length);
          paramCollectedUsageStats.timings.instanceFusionauthSearchHostsCount = Long.valueOf(timer.stop());
          break;
        case InstanceJavaVersion:
          paramCollectedUsageStats.stats.instanceJavaVersion = Runtime.version().toString();
          paramCollectedUsageStats.timings.instanceJavaVersion = Long.valueOf(timer.stop());
          break;
        case InstanceLicenseType:
          paramCollectedUsageStats.stats.instanceLicenseType = (this.reactorStatusService.retrieveStatus()).licenseAttributes.get("LicenseType");
          paramCollectedUsageStats.timings.instanceLicenseType = Long.valueOf(timer.stop());
          break;
        case InstanceNodes:
          paramCollectedUsageStats.stats.instanceNodes = this.usageStatsCollectorMapper.instanceNodes();
          paramCollectedUsageStats.timings.instanceNodes = Long.valueOf(timer.stop());
          break;
        case InstanceProxyEnabled:
          paramCollectedUsageStats.stats.instanceProxyEnabled = Boolean.valueOf((this.configuration.proxyHost() != null));
          paramCollectedUsageStats.timings.instanceProxyEnabled = Long.valueOf(timer.stop());
          break;
        case InstanceSearchType:
          paramCollectedUsageStats.stats.instanceSearchType = this.configuration.searchEngineType().toString();
          paramCollectedUsageStats.timings.instanceSearchType = Long.valueOf(timer.stop());
          break;
        case IntegrationsCleanspeakEnabled:
          paramCollectedUsageStats.stats.integrationsCleanspeakEnabled = this.usageStatsCollectorMapper.integrationsCleanspeakEnabled();
          paramCollectedUsageStats.timings.integrationsCleanspeakEnabled = Long.valueOf(timer.stop());
          break;
        case IntegrationsKafkaEnabled:
          paramCollectedUsageStats.stats.integrationsKafkaEnabled = this.usageStatsCollectorMapper.integrationsKafkaEnabled();
          paramCollectedUsageStats.timings.integrationsKafkaEnabled = Long.valueOf(timer.stop());
          break;
        case MessengersGeneric:
          paramCollectedUsageStats.stats.messengersGeneric = this.usageStatsCollectorMapper.messengersGeneric();
          paramCollectedUsageStats.timings.messengersGeneric = Long.valueOf(timer.stop());
          break;
        case MessengersKafka:
          paramCollectedUsageStats.stats.messengersKafka = this.usageStatsCollectorMapper.messengersKafka();
          paramCollectedUsageStats.timings.messengersKafka = Long.valueOf(timer.stop());
          break;
        case MessengersTwilio:
          paramCollectedUsageStats.stats.messengersTwilio = this.usageStatsCollectorMapper.messengersTwilio();
          paramCollectedUsageStats.timings.messengersTwilio = Long.valueOf(timer.stop());
          break;
        case MfaChallenge:
          paramCollectedUsageStats.stats.mfaChallenge = this.usageStatsCollectorMapper.mfaChallenge();
          paramCollectedUsageStats.timings.mfaChallenge = Long.valueOf(timer.stop());
          break;
        case MfaFailedAttempt:
          paramCollectedUsageStats.stats.mfaFailedAttempt = this.usageStatsCollectorMapper.mfaFailedAttempt();
          paramCollectedUsageStats.timings.mfaFailedAttempt = Long.valueOf(timer.stop());
          break;
        case MfaSuccess:
          paramCollectedUsageStats.stats.mfaSuccess = this.usageStatsCollectorMapper.mfaSuccess();
          paramCollectedUsageStats.timings.mfaSuccess = Long.valueOf(timer.stop());
          break;
        case TenantBreachedPasswordDetection:
          paramCollectedUsageStats.stats.tenantBreachedPasswordDetection = this.usageStatsCollectorMapper.tenantBreachedPasswordDetection();
          paramCollectedUsageStats.timings.tenantBreachedPasswordDetection = Long.valueOf(timer.stop());
          break;
        case TenantConfirmChildTemplate:
          paramCollectedUsageStats.stats.tenantConfirmChildTemplate = this.usageStatsCollectorMapper.tenantConfirmChildTemplate();
          paramCollectedUsageStats.timings.tenantConfirmChildTemplate = Long.valueOf(timer.stop());
          break;
        case TenantCustomAdminUserForm:
          paramCollectedUsageStats.stats.tenantCustomAdminUserForm = this.usageStatsCollectorMapper.tenantCustomAdminUserForm();
          paramCollectedUsageStats.timings.tenantCustomAdminUserForm = Long.valueOf(timer.stop());
          break;
        case TenantCustomConnectors:
          paramCollectedUsageStats.stats.tenantCustomConnectors = this.usageStatsCollectorMapper.tenantCustomConnectors();
          paramCollectedUsageStats.timings.tenantCustomConnectors = Long.valueOf(timer.stop());
          break;
        case TenantCustomThemeUsed:
          paramCollectedUsageStats.stats.tenantCustomThemeUsed = this.usageStatsCollectorMapper.tenantCustomThemeUsed();
          paramCollectedUsageStats.timings.tenantCustomThemeUsed = Long.valueOf(timer.stop());
          break;
        case TenantEmailMfaEnabled:
          paramCollectedUsageStats.stats.tenantEmailMfaEnabled = this.usageStatsCollectorMapper.tenantEmailMfaEnabled();
          paramCollectedUsageStats.timings.tenantEmailMfaEnabled = Long.valueOf(timer.stop());
          break;
        case TenantEmailUpdateTemplate:
          paramCollectedUsageStats.stats.tenantEmailUpdateTemplate = this.usageStatsCollectorMapper.tenantEmailUpdateTemplate();
          paramCollectedUsageStats.timings.tenantEmailUpdateTemplate = Long.valueOf(timer.stop());
          break;
        case TenantEmailVerifiedTemplate:
          paramCollectedUsageStats.stats.tenantEmailVerifiedTemplate = this.usageStatsCollectorMapper.tenantEmailVerifiedTemplate();
          paramCollectedUsageStats.timings.tenantEmailVerifiedTemplate = Long.valueOf(timer.stop());
          break;
        case TenantFamilyEnabledTemplate:
          paramCollectedUsageStats.stats.tenantFamilyEnabledTemplate = this.usageStatsCollectorMapper.tenantFamilyEnabledTemplate();
          paramCollectedUsageStats.timings.tenantFamilyEnabledTemplate = Long.valueOf(timer.stop());
          break;
        case TenantFamilyRequestTemplate:
          paramCollectedUsageStats.stats.tenantFamilyRequestTemplate = this.usageStatsCollectorMapper.tenantFamilyRequestTemplate();
          paramCollectedUsageStats.timings.tenantFamilyRequestTemplate = Long.valueOf(timer.stop());
          break;
        case TenantForgotPasswordTemplate:
          paramCollectedUsageStats.stats.tenantForgotPasswordTemplate = this.usageStatsCollectorMapper.tenantForgotPasswordTemplate();
          paramCollectedUsageStats.timings.tenantForgotPasswordTemplate = Long.valueOf(timer.stop());
          break;
        case TenantLoggedInApplicationsUsingSimpleThemesFromTenant:
          paramCollectedUsageStats.stats.tenantLoggedInApplicationsUsingSimpleThemesFromTenant = this.usageStatsCollectorMapper.tenantLoggedInApplicationsUsingSimpleThemesFromTenant(paramInt);
          paramCollectedUsageStats.timings.tenantLoggedInApplicationsUsingSimpleThemesFromTenant = Long.valueOf(timer.stop());
          break;
        case TenantLoginIdInUseOnCreateTemplate:
          paramCollectedUsageStats.stats.tenantLoginIdInUseOnCreateTemplate = this.usageStatsCollectorMapper.tenantLoginIdInUseOnCreateTemplate();
          paramCollectedUsageStats.timings.tenantLoginIdInUseOnCreateTemplate = Long.valueOf(timer.stop());
          break;
        case TenantLoginIdInUseOnUpdateTemplate:
          paramCollectedUsageStats.stats.tenantLoginIdInUseOnUpdateTemplate = this.usageStatsCollectorMapper.tenantLoginIdInUseOnUpdateTemplate();
          paramCollectedUsageStats.timings.tenantLoginIdInUseOnUpdateTemplate = Long.valueOf(timer.stop());
          break;
        case TenantLoginNewDeviceTemplate:
          paramCollectedUsageStats.stats.tenantLoginNewDeviceTemplate = this.usageStatsCollectorMapper.tenantLoginNewDeviceTemplate();
          paramCollectedUsageStats.timings.tenantLoginNewDeviceTemplate = Long.valueOf(timer.stop());
          break;
        case TenantLoginSuspiciousEmailTemplate:
          paramCollectedUsageStats.stats.tenantLoginSuspiciousEmailTemplate = this.usageStatsCollectorMapper.tenantLoginSuspiciousEmailTemplate();
          paramCollectedUsageStats.timings.tenantLoginSuspiciousEmailTemplate = Long.valueOf(timer.stop());
          break;
        case TenantMultiFactorEmailMessageTemplate:
          paramCollectedUsageStats.stats.tenantMultiFactorEmailMessageTemplate = this.usageStatsCollectorMapper.tenantMultiFactorEmailMessageTemplate();
          paramCollectedUsageStats.timings.tenantMultiFactorEmailMessageTemplate = Long.valueOf(timer.stop());
          break;
        case TenantClientCredentialsLambdaEnabled:
          paramCollectedUsageStats.stats.tenantClientCredentialsLambdaEnabled = this.usageStatsCollectorMapper.tenantClientCredentialsLambdaEnabled();
          paramCollectedUsageStats.timings.tenantClientCredentialsLambdaEnabled = Long.valueOf(timer.stop());
          break;
        case TenantMfaLambdasConfigured:
          paramCollectedUsageStats.stats.tenantMfaLambdasConfigured = this.usageStatsCollectorMapper.tenantMfaLambdasConfigured();
          paramCollectedUsageStats.timings.tenantMfaLambdasConfigured = Long.valueOf(timer.stop());
          break;
        case TenantMfaPolicyChallengeOnHighRisk:
          paramCollectedUsageStats.stats.tenantMfaPolicyChallengeOnHighRisk = this.usageStatsCollectorMapper.tenantMfaPolicyChallengeOnHighRisk();
          paramCollectedUsageStats.timings.tenantMfaPolicyChallengeOnHighRisk = Long.valueOf(timer.stop());
          break;
        case TenantMfaPolicyChallengeOnMediumRisk:
          paramCollectedUsageStats.stats.tenantMfaPolicyChallengeOnMediumRisk = this.usageStatsCollectorMapper.tenantMfaPolicyChallengeOnMediumRisk();
          paramCollectedUsageStats.timings.tenantMfaPolicyChallengeOnMediumRisk = Long.valueOf(timer.stop());
          break;
        case TenantMfaPolicyDisabled:
          paramCollectedUsageStats.stats.tenantMfaPolicyDisabled = this.usageStatsCollectorMapper.tenantMfaPolicyDisabled();
          paramCollectedUsageStats.timings.tenantMfaPolicyDisabled = Long.valueOf(timer.stop());
          break;
        case TenantMfaPolicyEnabled:
          paramCollectedUsageStats.stats.tenantMfaPolicyEnabled = this.usageStatsCollectorMapper.tenantMfaPolicyEnabled();
          paramCollectedUsageStats.timings.tenantMfaPolicyEnabled = Long.valueOf(timer.stop());
          break;
        case TenantMfaPolicyRequired:
          paramCollectedUsageStats.stats.tenantMfaPolicyRequired = this.usageStatsCollectorMapper.tenantMfaPolicyRequired();
          paramCollectedUsageStats.timings.tenantMfaPolicyRequired = Long.valueOf(timer.stop());
          break;
        case TenantMultiFactorSmsMessageTemplate:
          paramCollectedUsageStats.stats.tenantMultiFactorSmsMessageTemplate = this.usageStatsCollectorMapper.tenantMultiFactorSmsMessageTemplate();
          paramCollectedUsageStats.timings.tenantMultiFactorSmsMessageTemplate = Long.valueOf(timer.stop());
          break;
        case TenantNumber:
          paramCollectedUsageStats.stats.tenantNumber = this.usageStatsCollectorMapper.tenantNumber();
          paramCollectedUsageStats.timings.tenantNumber = Long.valueOf(timer.stop());
          break;
        case TenantParentRegistrationTemplate:
          paramCollectedUsageStats.stats.tenantParentRegistrationTemplate = this.usageStatsCollectorMapper.tenantParentRegistrationTemplate();
          paramCollectedUsageStats.timings.tenantParentRegistrationTemplate = Long.valueOf(timer.stop());
          break;
        case TenantPasswordResetSuccessTemplate:
          paramCollectedUsageStats.stats.tenantPasswordResetSuccessTemplate = this.usageStatsCollectorMapper.tenantPasswordResetSuccessTemplate();
          paramCollectedUsageStats.timings.tenantPasswordResetSuccessTemplate = Long.valueOf(timer.stop());
          break;
        case TenantPasswordUpdateTemplate:
          paramCollectedUsageStats.stats.tenantPasswordUpdateTemplate = this.usageStatsCollectorMapper.tenantPasswordUpdateTemplate();
          paramCollectedUsageStats.timings.tenantPasswordUpdateTemplate = Long.valueOf(timer.stop());
          break;
        case TenantPasswordlessTemplate:
          paramCollectedUsageStats.stats.tenantPasswordlessTemplate = this.usageStatsCollectorMapper.tenantPasswordlessTemplate();
          paramCollectedUsageStats.timings.tenantPasswordlessTemplate = Long.valueOf(timer.stop());
          break;
        case TenantSetPasswordTemplate:
          paramCollectedUsageStats.stats.tenantSetPasswordTemplate = this.usageStatsCollectorMapper.tenantSetPasswordTemplate();
          paramCollectedUsageStats.timings.tenantSetPasswordTemplate = Long.valueOf(timer.stop());
          break;
        case TenantSmsMfaEnabled:
          paramCollectedUsageStats.stats.tenantSmsMfaEnabled = this.usageStatsCollectorMapper.tenantSmsMfaEnabled();
          paramCollectedUsageStats.timings.tenantSmsMfaEnabled = Long.valueOf(timer.stop());
          break;
        case TenantSsoSessionBootstrapEnabled:
          paramCollectedUsageStats.stats.tenantSsoSessionBootstrapEnabled = this.usageStatsCollectorMapper.tenantSsoSessionBootstrapEnabled();
          paramCollectedUsageStats.timings.tenantSsoSessionBootstrapEnabled = Long.valueOf(timer.stop());
          break;
        case TenantTotpMfaEnabled:
          paramCollectedUsageStats.stats.tenantTotpMfaEnabled = this.usageStatsCollectorMapper.tenantTotpMfaEnabled();
          paramCollectedUsageStats.timings.tenantTotpMfaEnabled = Long.valueOf(timer.stop());
          break;
        case TenantTwoFactorMethodAddTemplate:
          paramCollectedUsageStats.stats.tenantTwoFactorMethodAddTemplate = this.usageStatsCollectorMapper.tenantTwoFactorMethodAddTemplate();
          paramCollectedUsageStats.timings.tenantTwoFactorMethodAddTemplate = Long.valueOf(timer.stop());
          break;
        case TenantTwoFactorMethodRemoveTemplate:
          paramCollectedUsageStats.stats.tenantTwoFactorMethodRemoveTemplate = this.usageStatsCollectorMapper.tenantTwoFactorMethodRemoveTemplate();
          paramCollectedUsageStats.timings.tenantTwoFactorMethodRemoveTemplate = Long.valueOf(timer.stop());
          break;
        case TenantVerificationTemplate:
          paramCollectedUsageStats.stats.tenantVerificationTemplate = this.usageStatsCollectorMapper.tenantVerificationTemplate();
          paramCollectedUsageStats.timings.tenantVerificationTemplate = Long.valueOf(timer.stop());
          break;
        case TenantWebauthnEnabled:
          paramCollectedUsageStats.stats.tenantWebauthnEnabled = this.usageStatsCollectorMapper.tenantWebauthnEnabled();
          paramCollectedUsageStats.timings.tenantWebauthnEnabled = Long.valueOf(timer.stop());
          break;
        case TenantForgotPasswordPhoneTemplate:
          paramCollectedUsageStats.stats.tenantForgotPasswordPhoneTemplate = this.usageStatsCollectorMapper.tenantForgotPasswordPhoneTemplate();
          paramCollectedUsageStats.timings.tenantForgotPasswordPhoneTemplate = Long.valueOf(timer.stop());
          break;
        case TenantIdentityUpdatePhoneTemplate:
          paramCollectedUsageStats.stats.tenantIdentityUpdatePhoneTemplate = this.usageStatsCollectorMapper.tenantIdentityUpdatePhoneTemplate();
          paramCollectedUsageStats.timings.tenantIdentityUpdatePhoneTemplate = Long.valueOf(timer.stop());
          break;
        case TenantLoginIdInUseOnCreatePhoneTemplate:
          paramCollectedUsageStats.stats.tenantLoginIdInUseOnCreatePhoneTemplate = this.usageStatsCollectorMapper.tenantLoginIdInUseOnCreatePhoneTemplate();
          paramCollectedUsageStats.timings.tenantLoginIdInUseOnCreatePhoneTemplate = Long.valueOf(timer.stop());
          break;
        case TenantLoginIdInUseOnUpdatePhoneTemplate:
          paramCollectedUsageStats.stats.tenantLoginIdInUseOnUpdatePhoneTemplate = this.usageStatsCollectorMapper.tenantLoginIdInUseOnUpdatePhoneTemplate();
          paramCollectedUsageStats.timings.tenantLoginIdInUseOnUpdatePhoneTemplate = Long.valueOf(timer.stop());
          break;
        case TenantLoginNewDevicePhoneTemplate:
          paramCollectedUsageStats.stats.tenantLoginNewDevicePhoneTemplate = this.usageStatsCollectorMapper.tenantLoginNewDevicePhoneTemplate();
          paramCollectedUsageStats.timings.tenantLoginNewDevicePhoneTemplate = Long.valueOf(timer.stop());
          break;
        case TenantLoginSuspiciousPhoneTemplate:
          paramCollectedUsageStats.stats.tenantLoginSuspiciousPhoneTemplate = this.usageStatsCollectorMapper.tenantLoginSuspiciousPhoneTemplate();
          paramCollectedUsageStats.timings.tenantLoginSuspiciousPhoneTemplate = Long.valueOf(timer.stop());
          break;
        case TenantPhoneConfigurationMessengers:
          paramCollectedUsageStats.stats.tenantPhoneConfigurationMessengers = this.usageStatsCollectorMapper.tenantPhoneConfigurationMessengers();
          paramCollectedUsageStats.timings.tenantPhoneConfigurationMessengers = Long.valueOf(timer.stop());
          break;
        case TenantPasswordlessPhoneTemplate:
          paramCollectedUsageStats.stats.tenantPasswordlessPhoneTemplate = this.usageStatsCollectorMapper.tenantPasswordlessPhoneTemplate();
          paramCollectedUsageStats.timings.tenantPasswordlessPhoneTemplate = Long.valueOf(timer.stop());
          break;
        case TenantPasswordResetSuccessPhoneTemplate:
          paramCollectedUsageStats.stats.tenantPasswordResetSuccessPhoneTemplate = this.usageStatsCollectorMapper.tenantPasswordResetSuccessPhoneTemplate();
          paramCollectedUsageStats.timings.tenantPasswordResetSuccessPhoneTemplate = Long.valueOf(timer.stop());
          break;
        case TenantPasswordUpdatePhoneTemplate:
          paramCollectedUsageStats.stats.tenantPasswordUpdatePhoneTemplate = this.usageStatsCollectorMapper.tenantPasswordUpdatePhoneTemplate();
          paramCollectedUsageStats.timings.tenantPasswordUpdatePhoneTemplate = Long.valueOf(timer.stop());
          break;
        case TenantSetPasswordPhoneTemplate:
          paramCollectedUsageStats.stats.tenantSetPasswordPhoneTemplate = this.usageStatsCollectorMapper.tenantSetPasswordPhoneTemplate();
          paramCollectedUsageStats.timings.tenantSetPasswordPhoneTemplate = Long.valueOf(timer.stop());
          break;
        case TenantTwoFactorMethodAddPhoneTemplate:
          paramCollectedUsageStats.stats.tenantTwoFactorMethodAddPhoneTemplate = this.usageStatsCollectorMapper.tenantTwoFactorMethodAddPhoneTemplate();
          paramCollectedUsageStats.timings.tenantTwoFactorMethodAddPhoneTemplate = Long.valueOf(timer.stop());
          break;
        case TenantTwoFactorMethodRemovePhoneTemplate:
          paramCollectedUsageStats.stats.tenantTwoFactorMethodRemovePhoneTemplate = this.usageStatsCollectorMapper.tenantTwoFactorMethodRemovePhoneTemplate();
          paramCollectedUsageStats.timings.tenantTwoFactorMethodRemovePhoneTemplate = Long.valueOf(timer.stop());
          break;
        case TenantVerificationCompletePhoneTemplate:
          paramCollectedUsageStats.stats.tenantVerificationCompletePhoneTemplate = this.usageStatsCollectorMapper.tenantVerificationCompletePhoneTemplate();
          paramCollectedUsageStats.timings.tenantVerificationCompletePhoneTemplate = Long.valueOf(timer.stop());
          break;
        case TenantVerificationPhoneTemplate:
          paramCollectedUsageStats.stats.tenantVerificationPhoneTemplate = this.usageStatsCollectorMapper.tenantVerificationPhoneTemplate();
          paramCollectedUsageStats.timings.tenantVerificationPhoneTemplate = Long.valueOf(timer.stop());
          break;
        case UniversalApplicationNumber:
          paramCollectedUsageStats.stats.universalApplicationNumber = this.usageStatsCollectorMapper.universalApplicationNumber();
          paramCollectedUsageStats.timings.universalApplicationNumber = Long.valueOf(timer.stop());
          break;
        case WebhookAuditLogCreate:
          paramCollectedUsageStats.stats.webhookAuditLogCreate = this.usageStatsCollectorMapper.webhookAuditLogCreate();
          paramCollectedUsageStats.timings.webhookAuditLogCreate = Long.valueOf(timer.stop());
          break;
        case WebhookEventLogCreate:
          paramCollectedUsageStats.stats.webhookEventLogCreate = this.usageStatsCollectorMapper.webhookEventLogCreate();
          paramCollectedUsageStats.timings.webhookEventLogCreate = Long.valueOf(timer.stop());
          break;
        case WebhookGroupCreate:
          paramCollectedUsageStats.stats.webhookGroupCreate = this.usageStatsCollectorMapper.webhookGroupCreate();
          paramCollectedUsageStats.timings.webhookGroupCreate = Long.valueOf(timer.stop());
          break;
        case WebhookGroupCreateComplete:
          paramCollectedUsageStats.stats.webhookGroupCreateComplete = this.usageStatsCollectorMapper.webhookGroupCreateComplete();
          paramCollectedUsageStats.timings.webhookGroupCreateComplete = Long.valueOf(timer.stop());
          break;
        case WebhookGroupCreateTransactional:
          paramCollectedUsageStats.stats.webhookGroupCreateTransactional = this.usageStatsCollectorMapper.webhookGroupCreateTransactional();
          paramCollectedUsageStats.timings.webhookGroupCreateTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookGroupDelete:
          paramCollectedUsageStats.stats.webhookGroupDelete = this.usageStatsCollectorMapper.webhookGroupDelete();
          paramCollectedUsageStats.timings.webhookGroupDelete = Long.valueOf(timer.stop());
          break;
        case WebhookGroupDeleteComplete:
          paramCollectedUsageStats.stats.webhookGroupDeleteComplete = this.usageStatsCollectorMapper.webhookGroupDeleteComplete();
          paramCollectedUsageStats.timings.webhookGroupDeleteComplete = Long.valueOf(timer.stop());
          break;
        case WebhookGroupDeleteTransactional:
          paramCollectedUsageStats.stats.webhookGroupDeleteTransactional = this.usageStatsCollectorMapper.webhookGroupDeleteTransactional();
          paramCollectedUsageStats.timings.webhookGroupDeleteTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookGroupMemberAdd:
          paramCollectedUsageStats.stats.webhookGroupMemberAdd = this.usageStatsCollectorMapper.webhookGroupMemberAdd();
          paramCollectedUsageStats.timings.webhookGroupMemberAdd = Long.valueOf(timer.stop());
          break;
        case WebhookGroupMemberAddComplete:
          paramCollectedUsageStats.stats.webhookGroupMemberAddComplete = this.usageStatsCollectorMapper.webhookGroupMemberAddComplete();
          paramCollectedUsageStats.timings.webhookGroupMemberAddComplete = Long.valueOf(timer.stop());
          break;
        case WebhookGroupMemberAddTransactional:
          paramCollectedUsageStats.stats.webhookGroupMemberAddTransactional = this.usageStatsCollectorMapper.webhookGroupMemberAddTransactional();
          paramCollectedUsageStats.timings.webhookGroupMemberAddTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookGroupMemberRemove:
          paramCollectedUsageStats.stats.webhookGroupMemberRemove = this.usageStatsCollectorMapper.webhookGroupMemberRemove();
          paramCollectedUsageStats.timings.webhookGroupMemberRemove = Long.valueOf(timer.stop());
          break;
        case WebhookGroupMemberRemoveComplete:
          paramCollectedUsageStats.stats.webhookGroupMemberRemoveComplete = this.usageStatsCollectorMapper.webhookGroupMemberRemoveComplete();
          paramCollectedUsageStats.timings.webhookGroupMemberRemoveComplete = Long.valueOf(timer.stop());
          break;
        case WebhookGroupMemberRemoveTransactional:
          paramCollectedUsageStats.stats.webhookGroupMemberRemoveTransactional = this.usageStatsCollectorMapper.webhookGroupMemberRemoveTransactional();
          paramCollectedUsageStats.timings.webhookGroupMemberRemoveTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookGroupMemberUpdate:
          paramCollectedUsageStats.stats.webhookGroupMemberUpdate = this.usageStatsCollectorMapper.webhookGroupMemberUpdate();
          paramCollectedUsageStats.timings.webhookGroupMemberUpdate = Long.valueOf(timer.stop());
          break;
        case WebhookGroupMemberUpdateComplete:
          paramCollectedUsageStats.stats.webhookGroupMemberUpdateComplete = this.usageStatsCollectorMapper.webhookGroupMemberUpdateComplete();
          paramCollectedUsageStats.timings.webhookGroupMemberUpdateComplete = Long.valueOf(timer.stop());
          break;
        case WebhookGroupMemberUpdateTransactional:
          paramCollectedUsageStats.stats.webhookGroupMemberUpdateTransactional = this.usageStatsCollectorMapper.webhookGroupMemberUpdateTransactional();
          paramCollectedUsageStats.timings.webhookGroupMemberUpdateTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookGroupUpdate:
          paramCollectedUsageStats.stats.webhookGroupUpdate = this.usageStatsCollectorMapper.webhookGroupUpdate();
          paramCollectedUsageStats.timings.webhookGroupUpdate = Long.valueOf(timer.stop());
          break;
        case WebhookGroupUpdateComplete:
          paramCollectedUsageStats.stats.webhookGroupUpdateComplete = this.usageStatsCollectorMapper.webhookGroupUpdateComplete();
          paramCollectedUsageStats.timings.webhookGroupUpdateComplete = Long.valueOf(timer.stop());
          break;
        case WebhookGroupUpdateTransactional:
          paramCollectedUsageStats.stats.webhookGroupUpdateTransactional = this.usageStatsCollectorMapper.webhookGroupUpdateTransactional();
          paramCollectedUsageStats.timings.webhookGroupUpdateTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookJwtPublicKeyUpdate:
          paramCollectedUsageStats.stats.webhookJwtPublicKeyUpdate = this.usageStatsCollectorMapper.webhookJwtPublicKeyUpdate();
          paramCollectedUsageStats.timings.webhookJwtPublicKeyUpdate = Long.valueOf(timer.stop());
          break;
        case WebhookJwtPublicKeyUpdateTransactional:
          paramCollectedUsageStats.stats.webhookJwtPublicKeyUpdateTransactional = this.usageStatsCollectorMapper.webhookJwtPublicKeyUpdateTransactional();
          paramCollectedUsageStats.timings.webhookJwtPublicKeyUpdateTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookJwtRefresh:
          paramCollectedUsageStats.stats.webhookJwtRefresh = this.usageStatsCollectorMapper.webhookJwtRefresh();
          paramCollectedUsageStats.timings.webhookJwtRefresh = Long.valueOf(timer.stop());
          break;
        case WebhookJwtRefreshTransactional:
          paramCollectedUsageStats.stats.webhookJwtRefreshTransactional = this.usageStatsCollectorMapper.webhookJwtRefreshTransactional();
          paramCollectedUsageStats.timings.webhookJwtRefreshTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookJwtRefreshTokenRevoke:
          paramCollectedUsageStats.stats.webhookJwtRefreshTokenRevoke = this.usageStatsCollectorMapper.webhookJwtRefreshTokenRevoke();
          paramCollectedUsageStats.timings.webhookJwtRefreshTokenRevoke = Long.valueOf(timer.stop());
          break;
        case WebhookJwtRefreshTokenRevokeTransactional:
          paramCollectedUsageStats.stats.webhookJwtRefreshTokenRevokeTransactional = this.usageStatsCollectorMapper.webhookJwtRefreshTokenRevokeTransactional();
          paramCollectedUsageStats.timings.webhookJwtRefreshTokenRevokeTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookKickstartSuccess:
          paramCollectedUsageStats.stats.webhookKickstartSuccess = this.usageStatsCollectorMapper.webhookKickstartSuccess();
          paramCollectedUsageStats.timings.webhookKickstartSuccess = Long.valueOf(timer.stop());
          break;
        case WebhookUserAction:
          paramCollectedUsageStats.stats.webhookUserAction = this.usageStatsCollectorMapper.webhookUserAction();
          paramCollectedUsageStats.timings.webhookUserAction = Long.valueOf(timer.stop());
          break;
        case WebhookUserBulkCreate:
          paramCollectedUsageStats.stats.webhookUserBulkCreate = this.usageStatsCollectorMapper.webhookUserBulkCreate();
          paramCollectedUsageStats.timings.webhookUserBulkCreate = Long.valueOf(timer.stop());
          break;
        case WebhookUserBulkCreateTransactional:
          paramCollectedUsageStats.stats.webhookUserBulkCreateTransactional = this.usageStatsCollectorMapper.webhookUserBulkCreateTransactional();
          paramCollectedUsageStats.timings.webhookUserBulkCreateTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookUserCreate:
          paramCollectedUsageStats.stats.webhookUserCreate = this.usageStatsCollectorMapper.webhookUserCreate();
          paramCollectedUsageStats.timings.webhookUserCreate = Long.valueOf(timer.stop());
          break;
        case WebhookUserCreateComplete:
          paramCollectedUsageStats.stats.webhookUserCreateComplete = this.usageStatsCollectorMapper.webhookUserCreateComplete();
          paramCollectedUsageStats.timings.webhookUserCreateComplete = Long.valueOf(timer.stop());
          break;
        case WebhookUserCreateTransactional:
          paramCollectedUsageStats.stats.webhookUserCreateTransactional = this.usageStatsCollectorMapper.webhookUserCreateTransactional();
          paramCollectedUsageStats.timings.webhookUserCreateTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookUserDeactivate:
          paramCollectedUsageStats.stats.webhookUserDeactivate = this.usageStatsCollectorMapper.webhookUserDeactivate();
          paramCollectedUsageStats.timings.webhookUserDeactivate = Long.valueOf(timer.stop());
          break;
        case WebhookUserDeactivateTransactional:
          paramCollectedUsageStats.stats.webhookUserDeactivateTransactional = this.usageStatsCollectorMapper.webhookUserDeactivateTransactional();
          paramCollectedUsageStats.timings.webhookUserDeactivateTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookUserDelete:
          paramCollectedUsageStats.stats.webhookUserDelete = this.usageStatsCollectorMapper.webhookUserDelete();
          paramCollectedUsageStats.timings.webhookUserDelete = Long.valueOf(timer.stop());
          break;
        case WebhookUserDeleteComplete:
          paramCollectedUsageStats.stats.webhookUserDeleteComplete = this.usageStatsCollectorMapper.webhookUserDeleteComplete();
          paramCollectedUsageStats.timings.webhookUserDeleteComplete = Long.valueOf(timer.stop());
          break;
        case WebhookUserDeleteTransactional:
          paramCollectedUsageStats.stats.webhookUserDeleteTransactional = this.usageStatsCollectorMapper.webhookUserDeleteTransactional();
          paramCollectedUsageStats.timings.webhookUserDeleteTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookUserEmailUpdate:
          paramCollectedUsageStats.stats.webhookUserEmailUpdate = this.usageStatsCollectorMapper.webhookUserEmailUpdate();
          paramCollectedUsageStats.timings.webhookUserEmailUpdate = Long.valueOf(timer.stop());
          break;
        case WebhookUserEmailVerified:
          paramCollectedUsageStats.stats.webhookUserEmailVerified = this.usageStatsCollectorMapper.webhookUserEmailVerified();
          paramCollectedUsageStats.timings.webhookUserEmailVerified = Long.valueOf(timer.stop());
          break;
        case WebhookUserEmailVerifiedTransactional:
          paramCollectedUsageStats.stats.webhookUserEmailVerifiedTransactional = this.usageStatsCollectorMapper.webhookUserEmailVerifiedTransactional();
          paramCollectedUsageStats.timings.webhookUserEmailVerifiedTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookUserIdentityProviderLink:
          paramCollectedUsageStats.stats.webhookUserIdentityProviderLink = this.usageStatsCollectorMapper.webhookUserIdentityProviderLink();
          paramCollectedUsageStats.timings.webhookUserIdentityProviderLink = Long.valueOf(timer.stop());
          break;
        case WebhookUserIdentityProviderUnlink:
          paramCollectedUsageStats.stats.webhookUserIdentityProviderUnlink = this.usageStatsCollectorMapper.webhookUserIdentityProviderUnlink();
          paramCollectedUsageStats.timings.webhookUserIdentityProviderUnlink = Long.valueOf(timer.stop());
          break;
        case WebhookUserLoginFailed:
          paramCollectedUsageStats.stats.webhookUserLoginFailed = this.usageStatsCollectorMapper.webhookUserLoginFailed();
          paramCollectedUsageStats.timings.webhookUserLoginFailed = Long.valueOf(timer.stop());
          break;
        case WebhookUserLoginFailedTransactional:
          paramCollectedUsageStats.stats.webhookUserLoginFailedTransactional = this.usageStatsCollectorMapper.webhookUserLoginFailedTransactional();
          paramCollectedUsageStats.timings.webhookUserLoginFailedTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookUserLoginNewDevice:
          paramCollectedUsageStats.stats.webhookUserLoginNewDevice = this.usageStatsCollectorMapper.webhookUserLoginNewDevice();
          paramCollectedUsageStats.timings.webhookUserLoginNewDevice = Long.valueOf(timer.stop());
          break;
        case WebhookUserLoginNewDeviceTransactional:
          paramCollectedUsageStats.stats.webhookUserLoginNewDeviceTransactional = this.usageStatsCollectorMapper.webhookUserLoginNewDeviceTransactional();
          paramCollectedUsageStats.timings.webhookUserLoginNewDeviceTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookUserLoginSuccess:
          paramCollectedUsageStats.stats.webhookUserLoginSuccess = this.usageStatsCollectorMapper.webhookUserLoginSuccess();
          paramCollectedUsageStats.timings.webhookUserLoginSuccess = Long.valueOf(timer.stop());
          break;
        case WebhookUserLoginSuccessTransactional:
          paramCollectedUsageStats.stats.webhookUserLoginSuccessTransactional = this.usageStatsCollectorMapper.webhookUserLoginSuccessTransactional();
          paramCollectedUsageStats.timings.webhookUserLoginSuccessTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookUserLoginSuspicious:
          paramCollectedUsageStats.stats.webhookUserLoginSuspicious = this.usageStatsCollectorMapper.webhookUserLoginSuspicious();
          paramCollectedUsageStats.timings.webhookUserLoginSuspicious = Long.valueOf(timer.stop());
          break;
        case WebhookUserLoginSuspiciousTransactional:
          paramCollectedUsageStats.stats.webhookUserLoginSuspiciousTransactional = this.usageStatsCollectorMapper.webhookUserLoginSuspiciousTransactional();
          paramCollectedUsageStats.timings.webhookUserLoginSuspiciousTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookUserTwoFactorChallenge:
          paramCollectedUsageStats.stats.webhookUserTwoFactorChallenge = this.usageStatsCollectorMapper.webhookUserTwoFactorChallenge();
          paramCollectedUsageStats.timings.webhookUserTwoFactorChallenge = Long.valueOf(timer.stop());
          break;
        case WebhookUserTwoFactorFailedAttempt:
          paramCollectedUsageStats.stats.webhookUserTwoFactorFailedAttempt = this.usageStatsCollectorMapper.webhookUserTwoFactorFailedAttempt();
          paramCollectedUsageStats.timings.webhookUserTwoFactorFailedAttempt = Long.valueOf(timer.stop());
          break;
        case WebhookUserTwoFactorSuccess:
          paramCollectedUsageStats.stats.webhookUserTwoFactorSuccess = this.usageStatsCollectorMapper.webhookUserTwoFactorSuccess();
          paramCollectedUsageStats.timings.webhookUserTwoFactorSuccess = Long.valueOf(timer.stop());
          break;
        case WebhookUserLoginIdDuplicateCreate:
          paramCollectedUsageStats.stats.webhookUserLoginIdDuplicateCreate = this.usageStatsCollectorMapper.webhookUserLoginIdDuplicateCreate();
          paramCollectedUsageStats.timings.webhookUserLoginIdDuplicateCreate = Long.valueOf(timer.stop());
          break;
        case WebhookUserLoginIdDuplicateUpdate:
          paramCollectedUsageStats.stats.webhookUserLoginIdDuplicateUpdate = this.usageStatsCollectorMapper.webhookUserLoginIdDuplicateUpdate();
          paramCollectedUsageStats.timings.webhookUserLoginIdDuplicateUpdate = Long.valueOf(timer.stop());
          break;
        case WebhookUserPasswordBreach:
          paramCollectedUsageStats.stats.webhookUserPasswordBreach = this.usageStatsCollectorMapper.webhookUserPasswordBreach();
          paramCollectedUsageStats.timings.webhookUserPasswordBreach = Long.valueOf(timer.stop());
          break;
        case WebhookUserPasswordBreachTransactional:
          paramCollectedUsageStats.stats.webhookUserPasswordBreachTransactional = this.usageStatsCollectorMapper.webhookUserPasswordBreachTransactional();
          paramCollectedUsageStats.timings.webhookUserPasswordBreachTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookUserPasswordResetSend:
          paramCollectedUsageStats.stats.webhookUserPasswordResetSend = this.usageStatsCollectorMapper.webhookUserPasswordResetSend();
          paramCollectedUsageStats.timings.webhookUserPasswordResetSend = Long.valueOf(timer.stop());
          break;
        case WebhookUserPasswordResetStart:
          paramCollectedUsageStats.stats.webhookUserPasswordResetStart = this.usageStatsCollectorMapper.webhookUserPasswordResetStart();
          paramCollectedUsageStats.timings.webhookUserPasswordResetStart = Long.valueOf(timer.stop());
          break;
        case WebhookUserPasswordResetSuccess:
          paramCollectedUsageStats.stats.webhookUserPasswordResetSuccess = this.usageStatsCollectorMapper.webhookUserPasswordResetSuccess();
          paramCollectedUsageStats.timings.webhookUserPasswordResetSuccess = Long.valueOf(timer.stop());
          break;
        case WebhookUserPasswordUpdate:
          paramCollectedUsageStats.stats.webhookUserPasswordUpdate = this.usageStatsCollectorMapper.webhookUserPasswordUpdate();
          paramCollectedUsageStats.timings.webhookUserPasswordUpdate = Long.valueOf(timer.stop());
          break;
        case WebhookUserReactivate:
          paramCollectedUsageStats.stats.webhookUserReactivate = this.usageStatsCollectorMapper.webhookUserReactivate();
          paramCollectedUsageStats.timings.webhookUserReactivate = Long.valueOf(timer.stop());
          break;
        case WebhookUserReactivateTransactional:
          paramCollectedUsageStats.stats.webhookUserReactivateTransactional = this.usageStatsCollectorMapper.webhookUserReactivateTransactional();
          paramCollectedUsageStats.timings.webhookUserReactivateTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookUserRegistrationCreate:
          paramCollectedUsageStats.stats.webhookUserRegistrationCreate = this.usageStatsCollectorMapper.webhookUserRegistrationCreate();
          paramCollectedUsageStats.timings.webhookUserRegistrationCreate = Long.valueOf(timer.stop());
          break;
        case WebhookUserRegistrationCreateComplete:
          paramCollectedUsageStats.stats.webhookUserRegistrationCreateComplete = this.usageStatsCollectorMapper.webhookUserRegistrationCreateComplete();
          paramCollectedUsageStats.timings.webhookUserRegistrationCreateComplete = Long.valueOf(timer.stop());
          break;
        case WebhookUserRegistrationCreateTransactional:
          paramCollectedUsageStats.stats.webhookUserRegistrationCreateTransactional = this.usageStatsCollectorMapper.webhookUserRegistrationCreateTransactional();
          paramCollectedUsageStats.timings.webhookUserRegistrationCreateTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookUserRegistrationDelete:
          paramCollectedUsageStats.stats.webhookUserRegistrationDelete = this.usageStatsCollectorMapper.webhookUserRegistrationDelete();
          paramCollectedUsageStats.timings.webhookUserRegistrationDelete = Long.valueOf(timer.stop());
          break;
        case WebhookUserRegistrationDeleteComplete:
          paramCollectedUsageStats.stats.webhookUserRegistrationDeleteComplete = this.usageStatsCollectorMapper.webhookUserRegistrationDeleteComplete();
          paramCollectedUsageStats.timings.webhookUserRegistrationDeleteComplete = Long.valueOf(timer.stop());
          break;
        case WebhookUserRegistrationDeleteTransactional:
          paramCollectedUsageStats.stats.webhookUserRegistrationDeleteTransactional = this.usageStatsCollectorMapper.webhookUserRegistrationDeleteTransactional();
          paramCollectedUsageStats.timings.webhookUserRegistrationDeleteTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookUserRegistrationUpdate:
          paramCollectedUsageStats.stats.webhookUserRegistrationUpdate = this.usageStatsCollectorMapper.webhookUserRegistrationUpdate();
          paramCollectedUsageStats.timings.webhookUserRegistrationUpdate = Long.valueOf(timer.stop());
          break;
        case WebhookUserRegistrationUpdateComplete:
          paramCollectedUsageStats.stats.webhookUserRegistrationUpdateComplete = this.usageStatsCollectorMapper.webhookUserRegistrationUpdateComplete();
          paramCollectedUsageStats.timings.webhookUserRegistrationUpdateComplete = Long.valueOf(timer.stop());
          break;
        case WebhookUserRegistrationUpdateTransactional:
          paramCollectedUsageStats.stats.webhookUserRegistrationUpdateTransactional = this.usageStatsCollectorMapper.webhookUserRegistrationUpdateTransactional();
          paramCollectedUsageStats.timings.webhookUserRegistrationUpdateTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookUserRegistrationVerified:
          paramCollectedUsageStats.stats.webhookUserRegistrationVerified = this.usageStatsCollectorMapper.webhookUserRegistrationVerified();
          paramCollectedUsageStats.timings.webhookUserRegistrationVerified = Long.valueOf(timer.stop());
          break;
        case WebhookUserRegistrationVerifiedTransactional:
          paramCollectedUsageStats.stats.webhookUserRegistrationVerifiedTransactional = this.usageStatsCollectorMapper.webhookUserRegistrationVerifiedTransactional();
          paramCollectedUsageStats.timings.webhookUserRegistrationVerifiedTransactional = Long.valueOf(timer.stop());
          break;
        case WebhookUserTwoFactorMethodAdd:
          paramCollectedUsageStats.stats.webhookUserTwoFactorMethodAdd = this.usageStatsCollectorMapper.webhookUserTwoFactorMethodAdd();
          paramCollectedUsageStats.timings.webhookUserTwoFactorMethodAdd = Long.valueOf(timer.stop());
          break;
        case WebhookUserTwoFactorMethodRemove:
          paramCollectedUsageStats.stats.webhookUserTwoFactorMethodRemove = this.usageStatsCollectorMapper.webhookUserTwoFactorMethodRemove();
          paramCollectedUsageStats.timings.webhookUserTwoFactorMethodRemove = Long.valueOf(timer.stop());
          break;
        case WebhookUserUpdate:
          paramCollectedUsageStats.stats.webhookUserUpdate = this.usageStatsCollectorMapper.webhookUserUpdate();
          paramCollectedUsageStats.timings.webhookUserUpdate = Long.valueOf(timer.stop());
          break;
        case WebhookUserUpdateComplete:
          paramCollectedUsageStats.stats.webhookUserUpdateComplete = this.usageStatsCollectorMapper.webhookUserUpdateComplete();
          paramCollectedUsageStats.timings.webhookUserUpdateComplete = Long.valueOf(timer.stop());
          break;
        case WebhookUserUpdateTransactional:
          paramCollectedUsageStats.stats.webhookUserUpdateTransactional = this.usageStatsCollectorMapper.webhookUserUpdateTransactional();
          paramCollectedUsageStats.timings.webhookUserUpdateTransactional = Long.valueOf(timer.stop());
          break;
      } 
    } catch (Exception exception) {
      logger.error("Error collecting usage stat [{}]", paramString, exception);
      paramCollectedUsageStats.failedStats.add(paramString);
    } 
  }
  
  private static class Timer {
    private final long startTime = System.currentTimeMillis();
    
    public long stop() {
      return System.currentTimeMillis() - this.startTime;
    }
  }
}
