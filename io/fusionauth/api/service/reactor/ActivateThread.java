package io.fusionauth.api.service.reactor;

import com.inversoft.license.v2.domain.License;
import com.inversoft.license.v2.domain.LicenseFeatureType;
import io.fusionauth.api.service.cache.IpReputationLoader;
import io.fusionauth.api.service.cache.MaxMindDatabaseLoader;
import io.fusionauth.api.service.cache.UserAgentReputationLoader;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivateThread extends Thread {
  private static final Logger logger = LoggerFactory.getLogger(ActivateThread.class);
  
  private final Duration breachedPasswordSleep;
  
  private final UUID instanceId;
  
  private final IpReputationLoader ipReputationLoader;
  
  private final License license;
  
  private final MaxMindDatabaseLoader maxMindDatabaseLoader;
  
  private final ReactorCore reactorCore;
  
  private final ReactorService reactorService;
  
  private final UserAgentReputationLoader userAgentReputationLoader;
  
  public ActivateThread(UUID paramUUID, License paramLicense, IpReputationLoader paramIpReputationLoader, MaxMindDatabaseLoader paramMaxMindDatabaseLoader, UserAgentReputationLoader paramUserAgentReputationLoader, ReactorCore paramReactorCore, ReactorService paramReactorService, Duration paramDuration) {
    this.instanceId = paramUUID;
    this.ipReputationLoader = paramIpReputationLoader;
    this.license = paramLicense;
    this.maxMindDatabaseLoader = paramMaxMindDatabaseLoader;
    this.userAgentReputationLoader = paramUserAgentReputationLoader;
    this.reactorCore = paramReactorCore;
    this.reactorService = paramReactorService;
    this.breachedPasswordSleep = paramDuration;
    setDaemon(true);
    setName("Activate " + getName());
  }
  
  public void run() {
    logger.info("Background Reactor activation thread started.");
    activateIPLocationDatabase();
    logger.debug("Background reactor - IP location database completed, performing health check to capture status");
    this.reactorService.updateStatusWithHealthCheck();
    activateIPReputation();
    logger.debug("Background reactor - IP reputation completed, performing health check to capture status");
    this.reactorService.updateStatusWithHealthCheck();
    activateUserAgentReputation();
    logger.debug("Background reactor - user agent reputation completed, performing health check to capture status");
    this.reactorService.updateStatusWithHealthCheck();
    activateBreachedPasswordDetection();
    logger.debug("Background reactor - Breached password completed");
    logger.info("Background Reactor activation thread completed.");
  }
  
  private void activateBreachedPasswordDetection() {
    try {
      if (!this.license.hasFeature(LicenseFeatureType.FusionAuthBreachedPassword))
        return; 
      byte b = 0;
      boolean bool = false;
      while (b < 30) {
        if (this.reactorCore.breachedPasswordHealthCheck(this.instanceId, this.license)) {
          logger.info("Breached password health check successfully completed");
          bool = true;
          break;
        } 
        logger.info("Breached password health check incomplete, waiting [{} seconds] and trying again", 
            Long.valueOf(this.breachedPasswordSleep.toSeconds()));
        Thread.sleep(this.breachedPasswordSleep.toMillis());
        b++;
      } 
      if (bool) {
        this.reactorService.updateStatusWithHealthCheck();
        this.reactorService.updateCommonPasswordDataset();
      } 
    } catch (Exception exception) {
      logger.error("Failed to activate Breach Password Detection.", exception);
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to activate Breach Password Detection.", exception));
    } 
  }
  
  private void activateIPLocationDatabase() {
    try {
      if (!this.license.hasFeature(LicenseFeatureType.FusionAuthThreatDetection))
        return; 
      this.maxMindDatabaseLoader.run();
    } catch (Exception exception) {
      logger.error("Failed to activate Breach Password Detection.", exception);
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to activate IP Location Database.", exception));
    } 
  }
  
  private void activateIPReputation() {
    try {
      if (!this.license.hasFeature(LicenseFeatureType.FusionAuthIntelligentMFA))
        return; 
      this.ipReputationLoader.run();
    } catch (Exception exception) {
      logger.error("Failed to activate IP Reputation.", exception);
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to activate IP Reputation.", exception));
    } 
  }
  
  private void activateUserAgentReputation() {
    try {
      if (!this.license.hasFeature(LicenseFeatureType.FusionAuthIntelligentMFA))
        return; 
      this.userAgentReputationLoader.run();
    } catch (Exception exception) {
      logger.error("Failed to activate User Agent Reputation.", exception);
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to activate User Agent Reputation.", exception));
    } 
  }
}
