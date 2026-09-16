package io.fusionauth.api.service.security;

import com.google.inject.Inject;
import io.fusionauth.api.domain.RequestFrequencyMapper;
import io.fusionauth.api.domain.RequestFrequencyRecord;
import io.fusionauth.api.service.RateLimitedException;
import io.fusionauth.api.service.tenant.RateLimitHelper;
import io.fusionauth.domain.RateLimitedRequestConfiguration;
import io.fusionauth.domain.RateLimitedRequestType;
import io.fusionauth.domain.Tenant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class DefaultRateLimitService implements RateLimitService {
  private final RequestFrequencyMapper requestFrequencyMapper;
  
  @Inject
  public DefaultRateLimitService(RequestFrequencyMapper paramRequestFrequencyMapper) {
    this.requestFrequencyMapper = paramRequestFrequencyMapper;
  }
  
  public void handleAndThrow(Tenant paramTenant, RateLimitedRequestType paramRateLimitedRequestType, String paramString) {
    RateLimitedException rateLimitedException = handle(paramTenant, paramRateLimitedRequestType, paramString);
    if (rateLimitedException != null)
      throw rateLimitedException; 
  }
  
  public void handleDoNotThrow(Tenant paramTenant, RateLimitedRequestType paramRateLimitedRequestType, String paramString) {
    handle(paramTenant, paramRateLimitedRequestType, paramString);
  }
  
  public boolean isRateLimited(Tenant paramTenant, RateLimitedRequestType paramRateLimitedRequestType, String paramString) {
    RateLimitedRequestConfiguration rateLimitedRequestConfiguration = RateLimitHelper.getConfiguration(paramTenant.rateLimitConfiguration, paramRateLimitedRequestType);
    if (!rateLimitedRequestConfiguration.enabled)
      return false; 
    long l = System.currentTimeMillis() - rateLimitedRequestConfiguration.timePeriodInSeconds * 1000L;
    RequestFrequencyRecord requestFrequencyRecord = this.requestFrequencyMapper.retrieveRequestFrequencyRecordNewerThan(paramTenant.id, paramRateLimitedRequestType, paramString, l);
    return (requestFrequencyRecord != null && requestFrequencyRecord.count >= rateLimitedRequestConfiguration.limit);
  }
  
  private RateLimitedException handle(Tenant paramTenant, RateLimitedRequestType paramRateLimitedRequestType, String paramString) {
    RateLimitedRequestConfiguration rateLimitedRequestConfiguration = RateLimitHelper.getConfiguration(paramTenant.rateLimitConfiguration, paramRateLimitedRequestType);
    if (!rateLimitedRequestConfiguration.enabled)
      return null; 
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    RequestFrequencyRecord requestFrequencyRecord1 = new RequestFrequencyRecord(1, paramTenant.id, zonedDateTime, paramString, paramRateLimitedRequestType);
    this.requestFrequencyMapper.upsertRequestFrequencyRecord(requestFrequencyRecord1, rateLimitedRequestConfiguration.timePeriodInSeconds);
    RequestFrequencyRecord requestFrequencyRecord2 = this.requestFrequencyMapper.retrieveRequestFrequencyRecord(paramTenant.id, paramRateLimitedRequestType, paramString);
    if (requestFrequencyRecord2.count > rateLimitedRequestConfiguration.limit)
      return new RateLimitedException(); 
    return null;
  }
}
