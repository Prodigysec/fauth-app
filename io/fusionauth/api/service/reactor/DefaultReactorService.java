package io.fusionauth.api.service.reactor;

import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.license.v2.LicenseException;
import com.inversoft.license.v2.LicenseProvider;
import com.inversoft.license.v2.domain.License;
import com.inversoft.license.v2.domain.LicenseContainer;
import com.inversoft.license.v2.domain.LicenseFeatureType;
import com.inversoft.license.v2.domain.LicenseResult;
import io.fusionauth.api.domain.BreachedPasswordMapper;
import io.fusionauth.api.domain.DatasetMapper;
import io.fusionauth.api.domain.IPLocationDatabaseMapper;
import io.fusionauth.api.domain.Instance;
import io.fusionauth.api.domain.InstanceMapper;
import io.fusionauth.api.domain.IpReputationDatabaseMapper;
import io.fusionauth.api.domain.ReactorHealthChecks;
import io.fusionauth.api.domain.UserAgentReputationDatabaseMapper;
import io.fusionauth.api.domain.api.reactor.BreachRequest;
import io.fusionauth.api.domain.api.reactor.BreachResult;
import io.fusionauth.api.domain.api.reactor.CommonPasswords;
import io.fusionauth.api.license.ReactorChangeResult;
import io.fusionauth.api.service.cache.InstanceCacheLoader;
import io.fusionauth.api.service.cache.IpReputationLoader;
import io.fusionauth.api.service.cache.MaxMindDatabaseLoader;
import io.fusionauth.api.service.cache.UserAgentReputationLoader;
import io.fusionauth.api.service.metrics.MetricsSender;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.domain.BreachedPasswordStatus;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.User;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultReactorService implements ReactorService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultReactorService.class);
  
  private static ZonedDateTime BREACH_DATA_LAST_MODIFIED;
  
  private static ZonedDateTime COMMON_PASSWORD_DATA_LAST_MODIFIED;
  
  private final BreachedPasswordMapper breachedPasswordMapper;
  
  private final CacheNotifier cacheNotifier;
  
  private final DatasetMapper datasetMapper;
  
  private final InstanceCacheLoader instanceCacheLoader;
  
  private final InstanceMapper instanceMapper;
  
  private final IPLocationDatabaseMapper ipLocationDatabaseMapper;
  
  private final IpReputationDatabaseMapper ipReputationDatabaseMapper;
  
  private final IpReputationLoader ipReputationLoader;
  
  private final LicenseProvider licenseProvider;
  
  private final MaxMindDatabaseLoader maxMindDatabaseLoader;
  
  private final MetricsSender metricsSender;
  
  private final ReactorCore reactorCore;
  
  private final ReactorStatusService reactorStatusService;
  
  private final UserAgentReputationDatabaseMapper userAgentReputationDatabaseMapper;
  
  private final UserAgentReputationLoader userAgentReputationLoader;
  
  @Inject
  public DefaultReactorService(BreachedPasswordMapper paramBreachedPasswordMapper, CacheNotifier paramCacheNotifier, DatasetMapper paramDatasetMapper, InstanceMapper paramInstanceMapper, IPLocationDatabaseMapper paramIPLocationDatabaseMapper, IpReputationDatabaseMapper paramIpReputationDatabaseMapper, IpReputationLoader paramIpReputationLoader, LicenseProvider paramLicenseProvider, MaxMindDatabaseLoader paramMaxMindDatabaseLoader, MetricsSender paramMetricsSender, ReactorCore paramReactorCore, ReactorStatusService paramReactorStatusService, InstanceCacheLoader paramInstanceCacheLoader, UserAgentReputationDatabaseMapper paramUserAgentReputationDatabaseMapper, UserAgentReputationLoader paramUserAgentReputationLoader) {
    this.breachedPasswordMapper = paramBreachedPasswordMapper;
    this.cacheNotifier = paramCacheNotifier;
    this.datasetMapper = paramDatasetMapper;
    this.instanceMapper = paramInstanceMapper;
    this.ipLocationDatabaseMapper = paramIPLocationDatabaseMapper;
    this.ipReputationDatabaseMapper = paramIpReputationDatabaseMapper;
    this.ipReputationLoader = paramIpReputationLoader;
    this.licenseProvider = paramLicenseProvider;
    this.maxMindDatabaseLoader = paramMaxMindDatabaseLoader;
    this.metricsSender = paramMetricsSender;
    this.reactorCore = paramReactorCore;
    this.reactorStatusService = paramReactorStatusService;
    this.instanceCacheLoader = paramInstanceCacheLoader;
    this.userAgentReputationDatabaseMapper = paramUserAgentReputationDatabaseMapper;
    this.userAgentReputationLoader = paramUserAgentReputationLoader;
  }
  
  @Transactional
  public void _deactivate() {
    logger.debug("Removing license and Reactor status from DB");
    removeLicenseAndReactorStatus();
    logger.debug("Removing common passwords");
    this.breachedPasswordMapper.deleteAllCommonPasswords();
    this.datasetMapper.delete(DatasetMapper.DatasetName.CommonPasswords);
    logger.debug("Removing IP location data");
    this.ipLocationDatabaseMapper.deleteAllIPLocationData();
    this.ipLocationDatabaseMapper.deleteAllIPLocationMetaData();
    logger.debug("Removing local MaxMind database cache");
    this.maxMindDatabaseLoader.removeCache();
    logger.debug("Removing IP reputation data");
    this.ipReputationDatabaseMapper.deleteAllIpReputationData();
    this.ipReputationDatabaseMapper.deleteAllIpReputationMetaData();
    logger.debug("Removing local IP reputation cache");
    this.ipReputationLoader.removeCache();
    logger.debug("Removing user agent reputation data");
    this.userAgentReputationDatabaseMapper.deleteAllUserAgentReputationData();
    this.userAgentReputationDatabaseMapper.deleteAllUserAgentReputationMetaData();
    logger.debug("Removing local user agent reputation cache");
    this.userAgentReputationLoader.removeCache();
  }
  
  public ActivateResult activate(String paramString1, String paramString2) {
    logger.info("Beginning activation.");
    License license = null;
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    this.instanceMapper.activateLicense(zonedDateTime, paramString1, paramString2);
    try {
      LicenseResult licenseResult = this.licenseProvider.reload();
      if (!licenseResult.success() || ReactorService.isLicenseContainerInvalid(licenseResult.container())) {
        removeLicenseAndReactorStatus();
        this.cacheNotifier.reload("Instance");
        logger.error("Failed to activate the Reactor: {}", licenseResult.error());
        EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to activate the Reactor.\n\nError:\n" + licenseResult.error()));
      } else {
        logger.info("License successfully loaded, now performing Reactor health check");
        updateStatusWithHealthCheck();
        logger.info("Reactor health check complete");
        license = licenseResult.container().license();
      } 
    } catch (LicenseException licenseException) {
      logger.error("Failed to activate the Reactor due to an exception while reloading the license provider.", (Throwable)licenseException);
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to activate the Reactor due to an exception while reloading the license provider.\n\nError:\n" + licenseException
            
            .getMessage()));
    } 
    ActivateResult activateResult = createActivateResult(license);
    if (!activateResult.success())
      return activateResult; 
    activateResult.thread().start();
    logger.info("FusionAuth Reactor has been activated.");
    EventLogHelper.create(new EventLog(EventLogType.Information, "FusionAuth Reactor has been activated."));
    this.metricsSender.send(this.instanceMapper.retrieve());
    return activateResult;
  }
  
  public void deactivate() {
    logger.info("Decommissioning Reactor: disconnecting license provider");
    this.licenseProvider.disconnect();
    logger.info("License disconnected, now deactivating Reactor");
    _deactivate();
    String str = "FusionAuth Reactor has been decommissioned.";
    logger.info(str);
    EventLogHelper.create(new EventLog(EventLogType.Information, str));
    this.cacheNotifier.reload("Instance");
  }
  
  public boolean regenerate() {
    logger.info("Beginning regenerate.");
    boolean bool = false;
    Instance instance = this.instanceMapper.retrieve();
    if (instance != null && instance.id != null && this.instanceMapper.retrieveLicenseId() != null) {
      LicenseResult licenseResult = this.licenseProvider.regenerate();
      if (!licenseResult.success() || licenseResult.container() == null) {
        EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to regenerate a reactor encryption key."));
      } else {
        this.instanceMapper.updateActivateInstant(ZonedDateTime.now(ZoneOffset.UTC));
        updateStatusWithHealthCheck();
        bool = true;
      } 
    } 
    logger.info("Step 1 of regeneration complete. Kicking off breached password update thread to monitor health check status.");
    startBreachReconnectAfterKeyRegeneration();
    return bool;
  }
  
  public BreachResult retrieveBreachResultForChange(User paramUser, String paramString) {
    return _retrieveBreachResult(paramUser, paramString, true, true);
  }
  
  public BreachResult retrieveBreachResultForLogin(User paramUser, String paramString) {
    return _retrieveBreachResult(paramUser, paramString, requiresCheck(paramUser, getCommonPasswordDataLastModified()), requiresCheck(paramUser, getBreachDataLastModified()));
  }
  
  @Transactional
  public void setCommonPasswordDatasetVersion(ZonedDateTime paramZonedDateTime) {
    this.datasetMapper.delete(DatasetMapper.DatasetName.CommonPasswords);
    this.datasetMapper.create(DatasetMapper.DatasetName.CommonPasswords, paramZonedDateTime);
    updateDatasetLastUpdateInstant(DatasetMapper.DatasetName.CommonPasswords, paramZonedDateTime);
  }
  
  public void updateBreachMetrics(UUID paramUUID, BreachResult paramBreachResult) {
    switch (paramBreachResult.match) {
      case None:
        this.breachedPasswordMapper.incrementBreachedNoMatch(paramUUID);
        break;
      case ExactMatch:
        this.breachedPasswordMapper.incrementBreachedExactMatch(paramUUID);
        break;
      case SubAddressMatch:
        this.breachedPasswordMapper.incrementBreachedSubAddressMatch(paramUUID);
        break;
      case PasswordOnly:
        this.breachedPasswordMapper.incrementBreachedPasswordMatch(paramUUID);
        break;
      case CommonPassword:
        this.breachedPasswordMapper.incrementBreachedCommonPasswordMatch(paramUUID);
        break;
    } 
  }
  
  @Transactional
  public void updateCommonPasswordDataset() {
    Instance instance = this.instanceMapper.retrieve();
    if (instance.reactorHealthChecks.breachedPasswordDetection == ReactorFeatureStatus.ACTIVE && isNewDataAvailable()) {
      logger.info("New common password dataset is available. Updating...");
      CommonPasswords commonPasswords = this.reactorCore.retrieveCommonPasswords();
      if (commonPasswords != null) {
        this.breachedPasswordMapper.deleteAllCommonPasswords();
        if (commonPasswords.passwords.size() > 0) {
          Objects.requireNonNull(this.breachedPasswordMapper);
          MapperTools.safeCreateUpdate(32000, commonPasswords.passwords, this.breachedPasswordMapper::upsertCommonPasswords);
          setCommonPasswordDatasetVersion(commonPasswords.lastUpdateInstant);
        } 
      } 
      logger.info("Common password dataset update complete.");
    } 
  }
  
  public void updateDatasetLastUpdateInstant(DatasetMapper.DatasetName paramDatasetName, ZonedDateTime paramZonedDateTime) {
    if (paramDatasetName == DatasetMapper.DatasetName.CommonPasswords) {
      COMMON_PASSWORD_DATA_LAST_MODIFIED = paramZonedDateTime;
    } else if (paramDatasetName == DatasetMapper.DatasetName.BreachPasswords) {
      BREACH_DATA_LAST_MODIFIED = paramZonedDateTime;
    } 
  }
  
  public void updateStatusWithHealthCheck() {
    LicenseContainer licenseContainer = this.licenseProvider.getLicense();
    Instance instance = this.instanceMapper.retrieve();
    ReactorHealthChecks reactorHealthChecks1 = instance.reactorHealthChecks;
    ReactorHealthChecks reactorHealthChecks2 = new ReactorHealthChecks(reactorHealthChecks1);
    if (ReactorService.isLicenseContainerInvalid(licenseContainer)) {
      reactorHealthChecks2 = new ReactorHealthChecks();
    } else {
      License license = licenseContainer.license();
      if (license.hasFeature(LicenseFeatureType.FusionAuthBreachedPassword)) {
        boolean bool = this.reactorCore.breachedPasswordHealthCheck(instance.id, license);
        if (!bool) {
          ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
          reactorHealthChecks2.breachedPasswordDetection = instance.activateInstant.isBefore(zonedDateTime.minusMinutes(5L)) ? ReactorFeatureStatus.DISCONNECTED : ReactorFeatureStatus.PENDING;
        } else {
          reactorHealthChecks2.breachedPasswordDetection = ReactorFeatureStatus.ACTIVE;
        } 
      } else {
        reactorHealthChecks2.breachedPasswordDetection = ReactorFeatureStatus.DISABLED;
      } 
      if (license.hasFeature(LicenseFeatureType.FusionAuthThreatDetection)) {
        boolean bool = this.reactorCore.ipGeoLocationHealthCheck();
        if (!bool) {
          reactorHealthChecks2.ipGeoLocation = ReactorFeatureStatus.PENDING;
        } else {
          reactorHealthChecks2.ipGeoLocation = ReactorFeatureStatus.ACTIVE;
        } 
      } else {
        reactorHealthChecks2.ipGeoLocation = ReactorFeatureStatus.DISABLED;
      } 
      if (license.hasFeature(LicenseFeatureType.FusionAuthIntelligentMFA)) {
        boolean bool = this.reactorCore.ipReputationHealthCheck();
        if (!bool) {
          reactorHealthChecks2.ipReputation = ReactorFeatureStatus.PENDING;
        } else {
          reactorHealthChecks2.ipReputation = ReactorFeatureStatus.ACTIVE;
        } 
      } else {
        reactorHealthChecks2.ipReputation = ReactorFeatureStatus.DISABLED;
      } 
      if (license.hasFeature(LicenseFeatureType.FusionAuthIntelligentMFA)) {
        boolean bool = this.reactorCore.userAgentReputationHealthCheck();
        if (!bool) {
          reactorHealthChecks2.userAgentReputation = ReactorFeatureStatus.PENDING;
        } else {
          reactorHealthChecks2.userAgentReputation = ReactorFeatureStatus.ACTIVE;
        } 
      } else {
        reactorHealthChecks2.userAgentReputation = ReactorFeatureStatus.DISABLED;
      } 
    } 
    ReactorChangeResult reactorChangeResult = ReactorChangeResult.getChanges(reactorHealthChecks1, reactorHealthChecks2);
    if (reactorChangeResult.anyChanges() || reactorHealthChecks1.isOutOfDate()) {
      Objects.requireNonNull(logger);
      reactorChangeResult.getChangeDescription().ifPresent(logger::info);
      this.instanceMapper.updateReactorHealthChecks(reactorHealthChecks2);
      this.cacheNotifier.reload("Instance");
    } 
  }
  
  public ActivateResult updateStatusWithNewFeatures() {
    updateStatusWithHealthCheck();
    LicenseContainer licenseContainer = this.licenseProvider.getLicense();
    ActivateResult activateResult = createActivateResult(licenseContainer.license());
    if (!activateResult.success())
      return activateResult; 
    activateResult.thread().start();
    return activateResult;
  }
  
  public boolean userRequiresCheck(User paramUser) {
    return (requiresCheck(paramUser, getCommonPasswordDataLastModified()) || requiresCheck(paramUser, getBreachDataLastModified()));
  }
  
  protected Duration getActivationBreachedPasswordSleepInterval() {
    return Duration.ofSeconds(10L);
  }
  
  protected void startBreachReconnectAfterKeyRegeneration() {
    Thread.startVirtualThread(() -> waitForBreachedPasswordReconnectAfterKeyRegeneration(Duration.ofSeconds(30L)));
  }
  
  protected void waitForBreachedPasswordReconnectAfterKeyRegeneration(Duration paramDuration) {
    LicenseContainer licenseContainer = this.licenseProvider.getLicense();
    if (licenseContainer == null || licenseContainer.license() == null)
      return; 
    License license = licenseContainer.license();
    Instance instance = this.instanceMapper.retrieve();
    try {
      if (!license.hasFeature(LicenseFeatureType.FusionAuthBreachedPassword))
        return; 
      byte b = 0;
      while (b < 20) {
        logger.info("Run health check for Breached Password detection after key regeneration. [{}]", Integer.valueOf(b + 1));
        if (this.reactorCore.breachedPasswordHealthCheck(instance.id, license)) {
          logger.info("Health check for Breached Password detection completed successfully after [{}] tries. Updating health check/Reactor Status", Integer.valueOf(b));
          updateStatusWithHealthCheck();
          return;
        } 
        logger.debug("Health check not yet passing, sleeping for {} seconds and retrying.", Long.valueOf(paramDuration.toSeconds()));
        Thread.sleep(paramDuration);
        b++;
      } 
      logger.info("Unable to reconnect Breached Password detection after key regeneration. Status [{}]", Integer.valueOf(b + 1));
    } catch (Exception exception) {
      logger.error("Failed to re-activate Breach Password Detection after key regeneration.", exception);
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to re-activate Breach Password Detection after key regeneration.", exception));
    } 
  }
  
  private BreachResult _retrieveBreachResult(User paramUser, String paramString, boolean paramBoolean1, boolean paramBoolean2) {
    if (ReactorStatusValidator.isNotLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.breachedPasswordDetection))
      return null; 
    if (paramBoolean1) {
      String str = retrieveSinglePassword(paramString);
      if (str != null)
        return new BreachResult(BreachedPasswordStatus.CommonPassword); 
    } 
    if (paramBoolean2) {
      ArrayList<String> arrayList = new ArrayList();
      String str1 = paramUser.email;
      if (str1 != null)
        arrayList.add(str1); 
      String str2 = paramUser.username;
      if (str2 != null)
        arrayList.add(str2); 
      String str3 = paramUser.phoneNumber;
      if (str3 != null)
        arrayList.add(str3); 
      LicenseContainer licenseContainer = this.licenseProvider.getLicense();
      BreachResult breachResult = this.reactorCore.retrieveResult(new BreachRequest(arrayList, paramString), licenseContainer.license());
      if (breachResult != null)
        handleUpdateLastModifiedDataset(breachResult.breachDataLastModified); 
      return breachResult;
    } 
    return new BreachResult(BreachedPasswordStatus.None);
  }
  
  private ActivateResult createActivateResult(License paramLicense) {
    if (paramLicense == null)
      return new ActivateResult(false, null); 
    this.instanceCacheLoader.run();
    Instance instance = this.instanceMapper.retrieve();
    return new ActivateResult(true, new ActivateThread(instance.id, paramLicense, this.ipReputationLoader, this.maxMindDatabaseLoader, this.userAgentReputationLoader, this.reactorCore, this, 


          
          getActivationBreachedPasswordSleepInterval()));
  }
  
  private ZonedDateTime getBreachDataLastModified() {
    if (BREACH_DATA_LAST_MODIFIED == null)
      BREACH_DATA_LAST_MODIFIED = this.datasetMapper.retrieve(DatasetMapper.DatasetName.BreachPasswords); 
    return BREACH_DATA_LAST_MODIFIED;
  }
  
  private ZonedDateTime getCommonPasswordDataLastModified() {
    if (COMMON_PASSWORD_DATA_LAST_MODIFIED == null)
      COMMON_PASSWORD_DATA_LAST_MODIFIED = this.datasetMapper.retrieve(DatasetMapper.DatasetName.CommonPasswords); 
    return COMMON_PASSWORD_DATA_LAST_MODIFIED;
  }
  
  private void handleUpdateLastModifiedDataset(ZonedDateTime paramZonedDateTime) {
    if (!getBreachDataLastModified().equals(paramZonedDateTime)) {
      BREACH_DATA_LAST_MODIFIED = paramZonedDateTime;
      this.datasetMapper.update(DatasetMapper.DatasetName.BreachPasswords, BREACH_DATA_LAST_MODIFIED);
    } 
  }
  
  private boolean isNewDataAvailable() {
    ZonedDateTime zonedDateTime1 = this.datasetMapper.retrieve(DatasetMapper.DatasetName.CommonPasswords);
    if (zonedDateTime1 == null)
      return true; 
    ZonedDateTime zonedDateTime2 = this.reactorCore.retrieveCommonPasswordsLastUpdateInstant();
    if (zonedDateTime2 == null)
      return false; 
    return zonedDateTime2.isAfter(zonedDateTime1);
  }
  
  private void removeLicenseAndReactorStatus() {
    this.instanceMapper.removeLicense();
    this.instanceMapper.updateReactorHealthChecks(new ReactorHealthChecks());
  }
  
  private boolean requiresCheck(User paramUser, ZonedDateTime paramZonedDateTime) {
    if (paramZonedDateTime == null)
      return false; 
    if (paramUser.breachedPasswordLastCheckedInstant == null)
      return true; 
    return paramUser.breachedPasswordLastCheckedInstant.isBefore(paramZonedDateTime);
  }
  
  private String retrieveSinglePassword(String paramString) {
    return this.breachedPasswordMapper.retrieveCommonPassword(paramString);
  }
}
