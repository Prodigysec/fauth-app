package io.fusionauth.api.service.identity;

import com.fasterxml.jackson.databind.JsonNode;
import com.inversoft.json.ToString;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import com.inversoft.rest.TextResponseHandler;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.provider.OpenIdConnectIdentityProvider;
import io.fusionauth.jwks.JSONWebKeySetHelper;
import io.fusionauth.jwks.domain.JSONWebKey;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.json.Mapper;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

public class OpenIdConnectIdentityProviderHelper {
  public static String JWKSDataKey = "___jwks___";
  
  public static String getClaimWithPointer(JsonNode paramJsonNode, String paramString) {
    if (!paramString.startsWith("/"))
      paramString = "/" + paramString; 
    return paramJsonNode.at(paramString).asText(null);
  }
  
  public static JsonNode jwtToJsonNode(JWT paramJWT) {
    byte[] arrayOfByte = Mapper.serialize(paramJWT);
    return (JsonNode)Mapper.deserialize(arrayOfByte, JsonNode.class);
  }
  
  public static void resolveOpenIDConnectEndpoints(ProxyInfoSupplier paramProxyInfoSupplier, OpenIdConnectIdentityProvider paramOpenIdConnectIdentityProvider) {
    String str = paramOpenIdConnectIdentityProvider.oauth2.issuer.toString();
    if (str.endsWith("/"))
      str = str.substring(0, str.length() - 1); 
    ClientResponse<?, ?> clientResponse = (new RESTClient(JsonNode.class, String.class)).connectTimeout(2000).readTimeout(5000).proxy((ProxyInfo)paramProxyInfoSupplier.get()).url(str + "/.well-known/openid-configuration").successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class)).errorResponseHandler((RESTClient.ResponseHandler)new TextResponseHandler()).get().go();
    if (!clientResponse.wasSuccessful()) {
      String str1 = Debugger.buildMessageFromResponse("Unable to resolve OpenID Connect configuration using issuer [" + str + "] for [" + paramOpenIdConnectIdentityProvider.name + "].\nRequest to the [" + str + "/.well-known/openid-configuration] endpoint failed.", clientResponse);
      EventLogHelper.create(new EventLog(EventLogType.Error, str1));
      return;
    } 
    JsonNode jsonNode1 = ((JsonNode)clientResponse.successResponse).at("/authorization_endpoint");
    JsonNode jsonNode2 = ((JsonNode)clientResponse.successResponse).at("/token_endpoint");
    JsonNode jsonNode3 = ((JsonNode)clientResponse.successResponse).at("/userinfo_endpoint");
    if (jsonNode1.isMissingNode() || jsonNode2.isMissingNode() || jsonNode3.isMissingNode()) {
      String str1 = "Unable to resolve OpenID Connect configuration using issuer [" + str + "] for [" + paramOpenIdConnectIdentityProvider.name + "].\nRequest to the [" + str + "/.well-known/openid-configuration] endpoint returned:\n/authorization_endpoint = " + (jsonNode1.isMissingNode() ? "null" : jsonNode1.asText()) + "\n/token_endpoint = " + (jsonNode2.isMissingNode() ? "null" : jsonNode2.asText()) + "\n/userinfo_endpoint = " + (jsonNode3.isMissingNode() ? "null" : jsonNode3.asText()) + "\n\nFull response:\n" + ToString.toString(clientResponse.successResponse);
      EventLogHelper.create(new EventLog(EventLogType.Error, str1));
      return;
    } 
    paramOpenIdConnectIdentityProvider.oauth2.authorization_endpoint = URI.create(jsonNode1.asText());
    paramOpenIdConnectIdentityProvider.oauth2.token_endpoint = URI.create(jsonNode2.asText());
    paramOpenIdConnectIdentityProvider.oauth2.userinfo_endpoint = URI.create(jsonNode3.asText());
    JsonNode jsonNode4 = ((JsonNode)clientResponse.successResponse).at("/jwks_uri");
    if (!jsonNode4.isMissingNode())
      try {
        Map map = (Map)JSONWebKeySetHelper.retrieveKeysFromJWKS(jsonNode4.asText()).stream().collect(Collectors.toMap(paramJSONWebKey -> paramJSONWebKey.kid, paramJSONWebKey -> paramJSONWebKey));
        paramOpenIdConnectIdentityProvider.data.put(JWKSDataKey, map);
      } catch (Exception exception) {
        String str1 = "Unable to retrieve JSON Web Keys for [" + paramOpenIdConnectIdentityProvider.name + "].\nRequest to the [" + jsonNode4.asText() + "] endpoint failed or the JSON response could not be parsed into JSON Web Keys.";
        EventLogHelper.create(new EventLog(EventLogType.Error, str1, exception));
      }  
  }
}
