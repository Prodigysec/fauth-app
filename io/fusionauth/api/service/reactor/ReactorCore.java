package io.fusionauth.api.service.reactor;

import com.inversoft.license.v2.domain.License;
import io.fusionauth.api.domain.SequencedMetaData;
import io.fusionauth.api.domain.api.reactor.BreachRequest;
import io.fusionauth.api.domain.api.reactor.BreachResult;
import io.fusionauth.api.domain.api.reactor.CommonPasswords;
import io.fusionauth.api.domain.ip.maxmind.IPLocationMetaData;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.function.Consumer;

public interface ReactorCore {
  boolean breachedPasswordHealthCheck(UUID paramUUID, License paramLicense);
  
  boolean ipGeoLocationHealthCheck();
  
  boolean ipReputationHealthCheck();
  
  CommonPasswords retrieveCommonPasswords();
  
  ZonedDateTime retrieveCommonPasswordsLastUpdateInstant();
  
  void retrieveIPLocationDatabase(Consumer<InputStream> paramConsumer);
  
  IPLocationMetaData retrieveIPLocationDatabaseLastModified();
  
  void retrieveIpReputationFile(Consumer<InputStream> paramConsumer);
  
  SequencedMetaData retrieveIpReputationFileLastModified();
  
  BreachResult retrieveResult(BreachRequest paramBreachRequest, License paramLicense);
  
  void retrieveUserAgentReputationFile(Consumer<InputStream> paramConsumer);
  
  SequencedMetaData retrieveUserAgentReputationFileLastModified();
  
  boolean userAgentReputationHealthCheck();
}
