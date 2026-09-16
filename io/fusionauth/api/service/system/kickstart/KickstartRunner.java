package io.fusionauth.api.service.system.kickstart;

import com.fasterxml.jackson.databind.JsonNode;
import com.inversoft.authentication.api.domain.AuthenticationKey;
import com.inversoft.authentication.api.domain.AuthenticationKeyMapper;
import com.inversoft.authentication.api.service.AuthenticationKeyCacheLoader;
import com.inversoft.json.ToString;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.RESTClient;
import com.inversoft.util.LoggerTool;
import io.fusionauth.api.domain.KickstartRequest;
import io.fusionauth.api.domain.api.APIKeyBridge;
import io.fusionauth.api.service.cache.WebhookCacheLoader;
import io.fusionauth.api.service.reactor.ReactorService;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.api.service.system.InstanceService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.event.KickstartSuccessEvent;
import io.fusionauth.domain.util.HTTPMethod;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KickstartRunner implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(KickstartRunner.class);
  
  private final KickstartRequest.KickstartAPIKey apiKey;
  
  private final AuthenticationKeyCacheLoader authenticationKeyCacheLoader;
  
  private final AuthenticationKeyMapper authenticationKeyMapper;
  
  private final String fusionAuthClientURL;
  
  private final InstanceService instanceService;
  
  private final KickstartRequest<KickstartRequest.APIRequest> kickstart;
  
  private final ReactorService reactorService;
  
  private final WebhookCacheLoader webhookCacheLoader;
  
  public KickstartRunner(KickstartRequest.KickstartAPIKey paramKickstartAPIKey, KickstartRequest<KickstartRequest.APIRequest> paramKickstartRequest, AuthenticationKeyCacheLoader paramAuthenticationKeyCacheLoader, AuthenticationKeyMapper paramAuthenticationKeyMapper, String paramString, InstanceService paramInstanceService, ReactorService paramReactorService, WebhookCacheLoader paramWebhookCacheLoader) {
    this.apiKey = paramKickstartAPIKey;
    this.authenticationKeyCacheLoader = paramAuthenticationKeyCacheLoader;
    this.authenticationKeyMapper = paramAuthenticationKeyMapper;
    this.fusionAuthClientURL = paramString;
    this.instanceService = paramInstanceService;
    this.kickstart = paramKickstartRequest;
    this.reactorService = paramReactorService;
    this.webhookCacheLoader = paramWebhookCacheLoader;
  }
  
  public void run() {
    ArrayList<String> arrayList = new ArrayList();
    try {
      waitForServerStart();
      if (this.apiKey != null)
        createAPIKey(this.apiKey, arrayList); 
      this.instanceService.kickstartActivate();
      if (this.kickstart.licenseId != null) {
        String str = "********";
        if (this.kickstart.licenseId.length() > 6)
          str = this.kickstart.licenseId.substring(this.kickstart.licenseId.length() - 6); 
        arrayList.add("Activate License Id [..." + str + "]");
        this.reactorService.activate(this.kickstart.licenseId, this.kickstart.license);
      } 
      for (KickstartRequest.APIRequest aPIRequest : this.kickstart.requests) {
        String str = this.apiKey.key;
        if (aPIRequest.body != null) {
        
        } else {
        
        } 
        RESTClient.BodyHandler bodyHandler = null;
        RESTClient rESTClient = (new RESTClient(JsonNode.class, JsonNode.class)).readTimeout(Integer.parseInt(this.kickstart.settings.readTimeout)).connectTimeout(Integer.parseInt(this.kickstart.settings.connectTimeout)).authorization(str).url(this.fusionAuthClientURL).uri(aPIRequest.url).bodyHandler(bodyHandler).successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class, FusionAuthClient.objectMapper)).errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class, FusionAuthClient.objectMapper));
        if (aPIRequest.tenantId != null)
          rESTClient.header("X-FusionAuth-TenantId", aPIRequest.tenantId.toString()); 
        if (aPIRequest.method == HTTPMethod.POST) {
          rESTClient.post();
        } else if (aPIRequest.method == HTTPMethod.PUT) {
          rESTClient.put();
        } else if (aPIRequest.method == HTTPMethod.PATCH) {
          rESTClient.patch();
        } 
        ClientResponse clientResponse = rESTClient.go();
        if (!clientResponse.wasSuccessful()) {
          logger.error("Failed to execute request to [{}][{}] Status [{}]\nRequest body:\n{}", new Object[] { aPIRequest.method, aPIRequest.url, 

                
                Integer.valueOf(clientResponse.status), 
                ToString.toString(aPIRequest.body) });
          if (clientResponse.exception != null) {
            logger.error("Exception returned", clientResponse.exception);
          } else {
            logger.error("Error response:\n{}", ToString.toString(clientResponse.errorResponse));
          } 
          return;
        } 
        arrayList.add("Completed [" + String.valueOf(aPIRequest.method) + "] request to [" + aPIRequest.url + "]");
      } 
      if (this.kickstart.apiKeys.size() > 1)
        for (KickstartRequest.KickstartAPIKey kickstartAPIKey : this.kickstart.apiKeys.subList(1, this.kickstart.apiKeys.size()))
          createAPIKey(kickstartAPIKey, arrayList);  
      LoggerTool.logPrettyInfoMessage(logger, "Kickstarting 🤘");
      logger.info("Summary:\n  - " + String.join("\n  - ", (Iterable)arrayList));
      UUID uUID = this.instanceService.setupComplete();
      this.instanceService.firstTimeSetupComplete();
      this.webhookCacheLoader.run();
      EventHelper.send(null, null, new KickstartSuccessEvent(uUID));
    } catch (Exception exception) {
      logger.error("Failed to complete kickstart.", exception);
    } 
  }
  
  private void createAPIKey(KickstartRequest.KickstartAPIKey paramKickstartAPIKey, List<String> paramList) {
    APIKey aPIKey = new APIKey();
    aPIKey.id = (paramKickstartAPIKey.id != null) ? UUID.fromString(paramKickstartAPIKey.id) : UUID.randomUUID();
    aPIKey.key = paramKickstartAPIKey.key;
    aPIKey.keyManager = paramKickstartAPIKey.keyManager;
    aPIKey.ipAccessControlListId = (paramKickstartAPIKey.ipAccessControlListId != null) ? UUID.fromString(paramKickstartAPIKey.ipAccessControlListId) : null;
    aPIKey.tenantId = (paramKickstartAPIKey.tenantId != null) ? UUID.fromString(paramKickstartAPIKey.tenantId) : null;
    if (paramKickstartAPIKey.permissions != null) {
      aPIKey.permissions = new APIKey.APIKeyPermissions();
      aPIKey.permissions.endpoints.putAll(paramKickstartAPIKey.permissions.endpoints);
    } 
    if (paramKickstartAPIKey.description != null) {
      aPIKey.metaData = new APIKey.APIKeyMetaData();
      aPIKey.metaData.attributes.put("description", paramKickstartAPIKey.description);
    } 
    aPIKey.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    aPIKey.lastUpdateInstant = aPIKey.insertInstant;
    String str = (paramKickstartAPIKey.key.length() < 10) ? "****" : paramKickstartAPIKey.key.substring(paramKickstartAPIKey.key.length() - 4);
    paramList.add("Created API key ending in [..." + str + "]");
    aPIKey.normalize();
    AuthenticationKey authenticationKey = APIKeyBridge.convert(aPIKey);
    this.authenticationKeyMapper.create(authenticationKey, authenticationKey.key);
    this.authenticationKeyCacheLoader.load();
  }
  
  private void sleep(long paramLong) {
    try {
      Thread.sleep(paramLong);
    } catch (InterruptedException interruptedException) {}
  }
  
  private void waitForServerStart() {
    for (byte b = 0; b < 30; b++) {
      try {
        URL uRL = URI.create(this.fusionAuthClientURL + "/api/status").toURL();
        HttpURLConnection httpURLConnection = (HttpURLConnection)uRL.openConnection();
        httpURLConnection.setReadTimeout(5000);
        httpURLConnection.setConnectTimeout(5000);
        httpURLConnection.setDoOutput(false);
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.addRequestProperty("Connection", "close");
        httpURLConnection.connect();
        String str = httpURLConnection.getHeaderField("Content-Type");
        if (str != null && str.toLowerCase().startsWith("application/json")) {
          sleep(1000L);
          sleep(1000L);
          break;
        } 
        if (b % 5 == 0)
          logger.info("Waiting for FusionAuth to complete startup in order to begin kickstart..."); 
        sleep(1000L);
      } catch (Exception exception) {
      
      } finally {
        sleep(1000L);
      } 
    } 
  }
}
