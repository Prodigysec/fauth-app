package io.fusionauth.api.service.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.FormDataBodyHandler;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import io.fusionauth.domain.Tenant;

public class CaptchaRESTClient {
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  @Inject
  public CaptchaRESTClient(ProxyInfoSupplier paramProxyInfoSupplier) {
    this.proxyInfoSupplier = paramProxyInfoSupplier;
  }
  
  public ClientResponse<JsonNode, JsonNode> callGoogleRecaptcha(Tenant paramTenant, String paramString1, String paramString2, String paramString3) {
    return (new RESTClient(JsonNode.class, JsonNode.class))
      .url(paramString3)
      .bodyHandler((RESTClient.BodyHandler)(new FormDataBodyHandler()).withParameter("remoteip", paramString2)
        .withParameter("response", paramString1)
        .withParameter("secret", paramTenant.captchaConfiguration.secretKey))
      .successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class))
      .errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class))
      .connectTimeout(3000)
      .readTimeout(3000)
      .proxy((ProxyInfo)this.proxyInfoSupplier.get())
      .post()
      .go();
  }
  
  public ClientResponse<JsonNode, JsonNode> callHCaptcha(Tenant paramTenant, String paramString1, String paramString2, String paramString3) {
    return (new RESTClient(JsonNode.class, JsonNode.class))
      .url(paramString3)
      .bodyHandler((RESTClient.BodyHandler)(new FormDataBodyHandler()).withParameter("remoteip", paramString2)
        .withParameter("response", paramString1)
        .withParameter("secret", paramTenant.captchaConfiguration.secretKey)
        .withParameter("site", paramTenant.captchaConfiguration.siteKey))
      .successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class))
      .errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class))
      .connectTimeout(3000)
      .readTimeout(3000)
      .proxy((ProxyInfo)this.proxyInfoSupplier.get())
      .post()
      .go();
  }
}
