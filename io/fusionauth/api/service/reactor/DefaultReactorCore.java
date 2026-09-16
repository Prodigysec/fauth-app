package io.fusionauth.api.service.reactor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.inversoft.json.ToString;
import com.inversoft.license.v2.LicenseProvider;
import com.inversoft.license.v2.domain.License;
import com.inversoft.license.v2.domain.LicenseFeatureType;
import com.inversoft.license.v2.domain.fusionauth.FusionAuthBreachedPasswordFeature;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import com.inversoft.util.StringTools;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.Instance;
import io.fusionauth.api.domain.SequencedMetaData;
import io.fusionauth.api.domain.api.reactor.BreachRequest;
import io.fusionauth.api.domain.api.reactor.BreachResult;
import io.fusionauth.api.domain.api.reactor.CommonPasswordDatasetVersion;
import io.fusionauth.api.domain.api.reactor.CommonPasswords;
import io.fusionauth.api.domain.ip.maxmind.IPLocationMetaData;
import io.fusionauth.api.service.cache.InstanceCache;
import io.fusionauth.api.service.cache.IpReputationCache;
import io.fusionauth.api.service.cache.MaxMindDatabaseCache;
import io.fusionauth.api.service.cache.UserAgentReputationCache;
import io.fusionauth.api.service.system.CipherService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.util.DownloadTools;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.function.Consumer;

public class DefaultReactorCore implements ReactorCore {
  public static final String BREACH_API = "/api/breach";
  
  public static final int CommonPasswordLastUpdateInstantTimeout = 2000;
  
  public static final int CommonPasswordsTimeout = 5000;
  
  public static final String HELLO_API = "/api/hello";
  
  public static final String IP_LOCATION_API = "/api/ip-location-database";
  
  public static final String IP_REPUTATION_API = "/api/ip-reputation";
  
  public static final int IpLocationModifiedConnectTimeout = 2000;
  
  public static final int IpReputationModifiedConnectTimeout = 2000;
  
  public static final String PASSWORDS_API = "/api/passwords";
  
  public static final String PASSWORDS_VERSION_API = "/api/passwords/version";
  
  public static final String USER_AGENT_REPUTATION_API = "/api/user-agent-reputation";
  
  public static final int UserAgentReputationModifiedConnectTimeout = 2000;
  
  private final CipherService cipherService;
  
  private final FusionAuthConfiguration configuration;
  
  private final InstanceCache instanceCache;
  
  private final IpReputationCache ipReputationCache;
  
  private final LicenseProvider licenseProvider;
  
  private final MaxMindDatabaseCache maxMindDatabaseCache;
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  private final UserAgentReputationCache userAgentReputationCache;
  
  @Inject
  public DefaultReactorCore(IpReputationCache paramIpReputationCache, CipherService paramCipherService, FusionAuthConfiguration paramFusionAuthConfiguration, InstanceCache paramInstanceCache, LicenseProvider paramLicenseProvider, MaxMindDatabaseCache paramMaxMindDatabaseCache, ProxyInfoSupplier paramProxyInfoSupplier, UserAgentReputationCache paramUserAgentReputationCache) {
    this.ipReputationCache = paramIpReputationCache;
    this.cipherService = paramCipherService;
    this.configuration = paramFusionAuthConfiguration;
    this.instanceCache = paramInstanceCache;
    this.licenseProvider = paramLicenseProvider;
    this.maxMindDatabaseCache = paramMaxMindDatabaseCache;
    this.proxyInfoSupplier = paramProxyInfoSupplier;
    this.userAgentReputationCache = paramUserAgentReputationCache;
  }
  
  public boolean breachedPasswordHealthCheck(UUID paramUUID, License paramLicense) {
    FusionAuthBreachedPasswordFeature fusionAuthBreachedPasswordFeature = (FusionAuthBreachedPasswordFeature)paramLicense.features.get(LicenseFeatureType.FusionAuthBreachedPassword);
    if (fusionAuthBreachedPasswordFeature == null || fusionAuthBreachedPasswordFeature.encryptionKey == null)
      return false; 
    String str1 = fusionAuthBreachedPasswordFeature.encryptionKey;
    String str2 = null;
    try {
      str2 = this.cipherService.encrypt(str1, "Hello".getBytes(StandardCharsets.UTF_8));
    } catch (GeneralSecurityException generalSecurityException) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to encrypt a Reactor Hello request.", generalSecurityException));
    } 
    ClientResponse clientResponse = (new RESTClient(void.class, void.class)).authorization(paramUUID.toString()).url(this.configuration.reactorBaseURL()).uri("/api/hello").header("Connection", "close").proxy((ProxyInfo)this.proxyInfoSupplier.get()).bodyHandler(new TextBodyHandler("text/plain", str2)).connectTimeout(2000).readTimeout(3000).post().go();
    return clientResponse.wasSuccessful();
  }
  
  public boolean ipGeoLocationHealthCheck() {
    return (this.maxMindDatabaseCache.get() != null);
  }
  
  public boolean ipReputationHealthCheck() {
    return (this.ipReputationCache.get() != null);
  }
  
  public CommonPasswords retrieveCommonPasswords() {
    Instance instance = this.instanceCache.get();
    ClientResponse clientResponse = (new RESTClient(CommonPasswords.class, void.class)).authorization(instance.id.toString()).url(this.configuration.reactorBaseURL()).uri("/api/passwords").header("Connection", "close").proxy((ProxyInfo)this.proxyInfoSupplier.get()).successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(CommonPasswords.class)).connectTimeout(5000).readTimeout(10000).get().go();
    if (!clientResponse.wasSuccessful()) {
      logErrorEvent(clientResponse.exception, "Unable to retrieve the common passwords dataset. Status code [" + clientResponse.status + "]");
      return null;
    } 
    return (CommonPasswords)clientResponse.successResponse;
  }
  
  public ZonedDateTime retrieveCommonPasswordsLastUpdateInstant() {
    Instance instance = this.instanceCache.get();
    ClientResponse clientResponse = (new RESTClient(CommonPasswordDatasetVersion.class, void.class)).authorization(instance.id.toString()).url(this.configuration.reactorBaseURL()).uri("/api/passwords/version").header("Connection", "close").proxy((ProxyInfo)this.proxyInfoSupplier.get()).successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(CommonPasswordDatasetVersion.class)).connectTimeout(2000).readTimeout(3000).get().go();
    if (!clientResponse.wasSuccessful()) {
      logErrorEvent(clientResponse.exception, "Unable to retrieve the common passwords last update instant. Status code [" + clientResponse.status + "]");
      return null;
    } 
    return ((CommonPasswordDatasetVersion)clientResponse.successResponse).lastUpdateInstant;
  }
  
  public void retrieveIPLocationDatabase(Consumer<InputStream> paramConsumer) {
    String str = (this.licenseProvider.getLicense().license()).id;
    ClientResponse clientResponse = (new RESTClient(void.class, void.class)).authorization(str).url(this.configuration.reactorBaseURL()).uri("/api/ip-location-database").addURLParameter("size", "large").header("Connection", "close").proxy((ProxyInfo)this.proxyInfoSupplier.get()).connectTimeout(5000).readTimeout(10000).successResponseHandler(new DownloadTools.ProxyResponseHandler(paramConsumer)).get().go();
    if (!clientResponse.wasSuccessful())
      logErrorEvent(clientResponse.exception, "Unable to retrieve the IP location database. Status code [" + clientResponse.status + "]"); 
  }
  
  public IPLocationMetaData retrieveIPLocationDatabaseLastModified() {
    String str1 = (this.licenseProvider.getLicense().license()).id;
    ClientResponse clientResponse = (new RESTClient(void.class, void.class)).authorization(str1).url(this.configuration.reactorBaseURL()).uri("/api/ip-location-database").addURLParameter("size", "large").header("Connection", "close").proxy((ProxyInfo)this.proxyInfoSupplier.get()).connectTimeout(2000).readTimeout(3000).head().go();
    String str2 = clientResponse.getHeader("x-fusionauth-digest");
    if (!clientResponse.wasSuccessful() || clientResponse.lastModified == null || StringTools.isBlank(str2)) {
      logErrorEvent(clientResponse.exception, "Unable to retrieve the IP location database last update instant and digest. Status code [" + clientResponse.status + "], last modified date [" + String.valueOf(clientResponse.lastModified) + "], and digest header [" + str2 + "]");
      return null;
    } 
    return new IPLocationMetaData(str2, clientResponse.lastModified);
  }
  
  public void retrieveIpReputationFile(Consumer<InputStream> paramConsumer) {
    String str = (this.licenseProvider.getLicense().license()).id;
    ClientResponse clientResponse = (new RESTClient(void.class, void.class)).authorization(str).url(this.configuration.reactorBaseURL()).uri("/api/ip-reputation").header("Connection", "close").proxy((ProxyInfo)this.proxyInfoSupplier.get()).connectTimeout(5000).readTimeout(10000).successResponseHandler(new DownloadTools.ProxyResponseHandler(paramConsumer)).get().go();
    if (!clientResponse.wasSuccessful())
      logErrorEvent(clientResponse.exception, "Unable to retrieve the IP reputation data set. Status code [" + clientResponse.status + "]"); 
  }
  
  public SequencedMetaData retrieveIpReputationFileLastModified() {
    String str1 = (this.licenseProvider.getLicense().license()).id;
    ClientResponse clientResponse = (new RESTClient(void.class, void.class)).authorization(str1).url(this.configuration.reactorBaseURL()).uri("/api/ip-reputation").header("Connection", "close").proxy((ProxyInfo)this.proxyInfoSupplier.get()).connectTimeout(2000).readTimeout(3000).head().go();
    String str2 = clientResponse.getHeader("x-fusionauth-digest");
    if (!clientResponse.wasSuccessful() || clientResponse.lastModified == null || StringTools.isBlank(str2)) {
      logErrorEvent(clientResponse.exception, "Unable to retrieve the IP reputation last update instant and digest. Status code [" + clientResponse.status + "], last modified date [" + String.valueOf(clientResponse.lastModified) + "], and digest header [" + str2 + "]");
      return null;
    } 
    return new SequencedMetaData(str2, clientResponse.lastModified);
  }
  
  public BreachResult retrieveResult(BreachRequest paramBreachRequest, License paramLicense) {
    paramBreachRequest.licenseId = paramLicense.id;
    String str1 = ((FusionAuthBreachedPasswordFeature)paramLicense.features.get(LicenseFeatureType.FusionAuthBreachedPassword)).encryptionKey;
    String str2 = null;
    try {
      str2 = this.cipherService.encrypt(str1, paramBreachRequest);
    } catch (GeneralSecurityException generalSecurityException) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to encrypt the Reactor Breach request.", generalSecurityException));
    } catch (JsonProcessingException jsonProcessingException) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to serialize the Reactor Breach request.", (Throwable)jsonProcessingException));
    } 
    if (str2 == null)
      return null; 
    ClientResponse clientResponse = (new RESTClient(BreachResult.class, JsonNode.class)).authorization((this.instanceCache.get()).id.toString()).url(this.configuration.reactorBaseURL()).uri("/api/breach").proxy((ProxyInfo)this.proxyInfoSupplier.get()).bodyHandler(new TextBodyHandler("text/plain", str2)).errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class)).successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(BreachResult.class)).connectTimeout(2000).readTimeout(3000).post().go();
    if (!clientResponse.wasSuccessful()) {
      String str = "Failed to retrieve a breach result from Reactor. Status code [" + clientResponse.status + "] while checking loginIds [" + String.join(",", (Iterable)paramBreachRequest.loginIds) + "]";
      if (clientResponse.exception != null) {
        EventLogHelper.create(new EventLog(EventLogType.Error, str, clientResponse.exception));
      } else {
        String str3 = "";
        try {
          str3 = (clientResponse.errorResponse == null) ? "" : ("\nError:\n" + ToString.toString(clientResponse.errorResponse));
        } catch (Exception exception) {}
        EventLogHelper.create(new EventLog(EventLogType.Error, str + str));
      } 
      return null;
    } 
    return (BreachResult)clientResponse.successResponse;
  }
  
  public void retrieveUserAgentReputationFile(Consumer<InputStream> paramConsumer) {
    String str = (this.licenseProvider.getLicense().license()).id;
    ClientResponse clientResponse = (new RESTClient(void.class, void.class)).authorization(str).url(this.configuration.reactorBaseURL()).uri("/api/user-agent-reputation").header("Connection", "close").proxy((ProxyInfo)this.proxyInfoSupplier.get()).connectTimeout(5000).readTimeout(10000).successResponseHandler(new DownloadTools.ProxyResponseHandler(paramConsumer)).get().go();
    if (!clientResponse.wasSuccessful())
      logErrorEvent(clientResponse.exception, "Unable to retrieve the user agent reputation data set. Status code [" + clientResponse.status + "]"); 
  }
  
  public SequencedMetaData retrieveUserAgentReputationFileLastModified() {
    String str1 = (this.licenseProvider.getLicense().license()).id;
    ClientResponse clientResponse = (new RESTClient(void.class, void.class)).authorization(str1).url(this.configuration.reactorBaseURL()).uri("/api/user-agent-reputation").header("Connection", "close").proxy((ProxyInfo)this.proxyInfoSupplier.get()).connectTimeout(2000).readTimeout(3000).head().go();
    String str2 = clientResponse.getHeader("x-fusionauth-digest");
    if (!clientResponse.wasSuccessful() || clientResponse.lastModified == null || StringTools.isBlank(str2)) {
      logErrorEvent(clientResponse.exception, "Unable to retrieve the user agent reputation last update instant and digest. Status code [" + clientResponse.status + "], last modified date [" + String.valueOf(clientResponse.lastModified) + "], and digest header [" + str2 + "]");
      return null;
    } 
    return new SequencedMetaData(str2, clientResponse.lastModified);
  }
  
  public boolean userAgentReputationHealthCheck() {
    return (this.userAgentReputationCache.get() != null);
  }
  
  private void logErrorEvent(Exception paramException, String paramString) {
    if (paramException != null) {
      EventLogHelper.create(new EventLog(EventLogType.Error, paramString, paramException));
    } else {
      EventLogHelper.create(new EventLog(EventLogType.Error, paramString));
    } 
  }
}
