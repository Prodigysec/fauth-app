package io.fusionauth.api.service.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.StringTools;
import io.fusionauth.api.service.BlockedIPAddressException;
import io.fusionauth.api.service.cache.IPAccessControlListCache;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.CaptchaMethod;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TenantCaptchaConfiguration;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import java.util.UUID;

public class DefaultThreatDetectionService implements ThreatDetectionService {
  private final CaptchaRESTClient captchaRESTClient;
  
  private final IPAccessControlListCache ipAccessControlListCache;
  
  @Inject
  public DefaultThreatDetectionService(CaptchaRESTClient paramCaptchaRESTClient, IPAccessControlListCache paramIPAccessControlListCache) {
    this.captchaRESTClient = paramCaptchaRESTClient;
    this.ipAccessControlListCache = paramIPAccessControlListCache;
  }
  
  public CaptchaResult checkCaptchaChallenge(Tenant paramTenant, Application paramApplication, String paramString1, String paramString2, ReactorFeatureStatus paramReactorFeatureStatus) {
    if (!isCaptchaEnabled(paramTenant, paramReactorFeatureStatus))
      return CaptchaResult.Disabled; 
    if (StringTools.isTrimmedEmpty(paramString1))
      return CaptchaResult.TokenRequired; 
    TenantCaptchaConfiguration tenantCaptchaConfiguration = paramTenant.captchaConfiguration;
    Debugger debugger = new Debugger((paramApplication != null && paramApplication.oauthConfiguration.debug), "CAPTCHA verification log for method [" + String.valueOf(tenantCaptchaConfiguration.captchaMethod) + "]");
    switch (tenantCaptchaConfiguration.captchaMethod) {
      default:
        throw new MatchException(null, null);
      case GoogleRecaptchaV2:
      case GoogleRecaptchaV3:
      
      case HCaptcha:
      case HCaptchaEnterprise:
        break;
    } 
    double d1 = 
      
      checkHCaptcha(debugger, paramTenant, paramString1, paramString2);
    switch (tenantCaptchaConfiguration.captchaMethod) {
      default:
        throw new MatchException(null, null);
      case GoogleRecaptchaV3:
      case HCaptchaEnterprise:
      
      case GoogleRecaptchaV2:
      case HCaptcha:
        break;
    } 
    double d2 = 


      
      0.5D;
    boolean bool = (d1 > d2) ? true : false;
    debugger.log("Configured threshold [" + d2 + "]")
      .log("Returned score [" + d1 + "]")
      .log("Challenge is " + (bool ? 
        "valid, the score is greater than the configured threshold." : 
        "invalid, the score is less than the configured threshold."))
      .done();
    return bool ? CaptchaResult.ChallengeSuccess : CaptchaResult.ChallengeFailed;
  }
  
  public void handleBlockedIPAddress(Tenant paramTenant, Application paramApplication, String paramString) {
    UUID uUID = (paramTenant != null) ? paramTenant.accessControlConfiguration.uiIPAccessControlListId : null;
    if (paramApplication != null && paramApplication.accessControlConfiguration.uiIPAccessControlListId != null)
      uUID = paramApplication.accessControlConfiguration.uiIPAccessControlListId; 
    if (this.ipAccessControlListCache.isBlocked(uUID, paramString))
      throw new BlockedIPAddressException(paramString); 
  }
  
  public boolean isCaptchaEnabled(Tenant paramTenant, ReactorFeatureStatus paramReactorFeatureStatus) {
    return (paramTenant.captchaConfiguration.enabled && paramReactorFeatureStatus == ReactorFeatureStatus.ACTIVE);
  }
  
  public boolean isIPAddressBlocked(UUID paramUUID, String paramString) {
    return this.ipAccessControlListCache.isBlocked(paramUUID, paramString);
  }
  
  private double checkGoogleRecaptcha(Debugger paramDebugger, Tenant paramTenant, String paramString1, String paramString2) {
    if (StringTools.isTrimmedEmpty(paramString1))
      return 0.0D; 
    String str = "https://www.google.com/recaptcha/api/siteverify";
    paramDebugger.log("Call the CAPTCHA verification endpoint [" + str + "]")
      .log("IP address: %s", new Object[] { paramString2 }).log("Response: %s", new Object[] { paramString1 }).log("Secret: %s", new Object[] { "{redacted, see configuration}" });
    ClientResponse<JsonNode, JsonNode> clientResponse = this.captchaRESTClient.callGoogleRecaptcha(paramTenant, paramString1, paramString2, str);
    paramDebugger.log("Endpoint returned status code [" + clientResponse.status + "]");
    if (!clientResponse.wasSuccessful()) {
      paramDebugger.handleError(clientResponse, "Request to the [" + str + "] endpoint failed.");
      return 1.0D;
    } 
    paramDebugger.logObjectToJSON("Endpoint response:\n", clientResponse.successResponse);
    double d = 0.0D;
    JsonNode jsonNode = (JsonNode)clientResponse.successResponse;
    if (jsonNode.at("/success").asBoolean())
      if (paramTenant.captchaConfiguration.captchaMethod == CaptchaMethod.GoogleRecaptchaV3 && jsonNode.at("/action").asText().equals("submit")) {
        d = jsonNode.at("/score").asDouble();
      } else {
        d = 1.0D;
      }  
    return d;
  }
  
  private double checkHCaptcha(Debugger paramDebugger, Tenant paramTenant, String paramString1, String paramString2) {
    if (StringTools.isTrimmedEmpty(paramString1))
      return 0.0D; 
    String str = "https://hcaptcha.com/siteverify";
    paramDebugger.log("Call the CAPTCHA verification endpoint [" + str + "]")
      .log("IP address: %s", new Object[] { paramString2 }).log("Response: %s", new Object[] { paramString1 }).log("Secret: %s", new Object[] { "{redacted, see configuration}" }).log("Site: %s", new Object[] { "{redacted, see configuration}" });
    ClientResponse<JsonNode, JsonNode> clientResponse = this.captchaRESTClient.callHCaptcha(paramTenant, paramString1, paramString2, str);
    paramDebugger.log("Endpoint returned status code [" + clientResponse.status + "]");
    if (!clientResponse.wasSuccessful()) {
      paramDebugger.handleError(clientResponse, "Request to the [" + str + "] endpoint failed.");
      return 1.0D;
    } 
    paramDebugger.logObjectToJSON("Endpoint response:\n", clientResponse.successResponse);
    double d = 0.0D;
    JsonNode jsonNode = (JsonNode)clientResponse.successResponse;
    if (jsonNode.at("/success").asBoolean())
      if (paramTenant.captchaConfiguration.captchaMethod == CaptchaMethod.HCaptchaEnterprise) {
        d = 1.0D - jsonNode.at("/score").asDouble();
      } else {
        d = 1.0D;
      }  
    return d;
  }
}
