package io.fusionauth.api.service.usage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.JSONBodyHandler;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import com.inversoft.support.service.guice.ProductVersionString;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.CollectedUsageStats;
import io.fusionauth.api.domain.InstanceMapper;
import io.fusionauth.usagestats.shared.domain.CurrentStats;
import io.fusionauth.usagestats.shared.domain.CurrentStatsRequest;
import jakarta.inject.Inject;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultUsageStatsClient implements UsageStatsClient {
  private static final Logger logger = LoggerFactory.getLogger(DefaultUsageStatsClient.class);
  
  public final ObjectMapper objectMapper;
  
  private final FusionAuthConfiguration configuration;
  
  private final InstanceMapper instanceMapper;
  
  private final String productVersion;
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  @Inject
  public DefaultUsageStatsClient(ObjectMapper paramObjectMapper, FusionAuthConfiguration paramFusionAuthConfiguration, @ProductVersionString String paramString, ProxyInfoSupplier paramProxyInfoSupplier, InstanceMapper paramInstanceMapper) {
    this.configuration = paramFusionAuthConfiguration;
    this.instanceMapper = paramInstanceMapper;
    this.objectMapper = paramObjectMapper;
    this.productVersion = paramString;
    this.proxyInfoSupplier = paramProxyInfoSupplier;
  }
  
  public CurrentStats fetchCurrentStats() {
    logger.debug("Calling GET on url [{}{}]", this.configuration.statsBaseURL(), "/api/stats/current");
    CurrentStatsRequest currentStatsRequest = new CurrentStatsRequest((this.instanceMapper.retrieve()).id, this.productVersion);
    ClientResponse clientResponse = (new RESTClient(CurrentStats.class, void.class)).url(this.configuration.statsBaseURL()).uri("/api/stats/current").header("Connection", "close").proxy((ProxyInfo)this.proxyInfoSupplier.get()).bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(currentStatsRequest, this.objectMapper)).successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(CurrentStats.class)).readTimeout(4000).connectTimeout(4000).post().go();
    if (!clientResponse.wasSuccessful()) {
      if (clientResponse.exception != null) {
        logger.warn("Unable to call the current stats endpoint. [{}]", clientResponse.exception.getMessage());
      } else {
        logger.warn("Unable to fetch the current stats - status [{}].", Integer.valueOf(clientResponse.status));
      } 
      return null;
    } 
    return (CurrentStats)clientResponse.getSuccessResponse();
  }
  
  public ZonedDateTime fetchStatsLastModified() {
    ClientResponse clientResponse = (new RESTClient(void.class, void.class)).url(this.configuration.statsBaseURL()).uri("/api/stats/current").header("Connection", "close").proxy((ProxyInfo)this.proxyInfoSupplier.get()).readTimeout(4000).connectTimeout(4000).head().go();
    if (!clientResponse.wasSuccessful()) {
      if (clientResponse.exception != null) {
        logger.warn("Unable to call the current stats endpoint with head request. [{}]", clientResponse.exception.getMessage());
      } else {
        logger.warn("Unable to fetch the current stats with head request- status [{}].", Integer.valueOf(clientResponse.status));
      } 
      return null;
    } 
    return clientResponse.lastModified;
  }
  
  public boolean sendStat(CollectedUsageStats paramCollectedUsageStats) {
    ClientResponse clientResponse = (new RESTClient(void.class, void.class)).url(this.configuration.statsBaseURL()).uri("/api/stats/ingest").header("Connection", "close").proxy((ProxyInfo)this.proxyInfoSupplier.get()).bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramCollectedUsageStats, this.objectMapper)).readTimeout(4000).connectTimeout(4000).post().go();
    if (!clientResponse.wasSuccessful()) {
      if (clientResponse.exception != null) {
        logger.warn("Unable to call the stats endpoint. [{}]", clientResponse.exception.getMessage());
      } else {
        logger.warn("Unable to call stats - status [{}].", Integer.valueOf(clientResponse.status));
      } 
      return false;
    } 
    return true;
  }
}
