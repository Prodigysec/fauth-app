package io.fusionauth.api.service.authentication;

import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.JSONBodyHandler;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import io.fusionauth.api.domain.api.hypr.DeviceAuthenticationRequest;
import io.fusionauth.api.domain.api.hypr.DeviceAuthenticationResponse;
import io.fusionauth.api.domain.api.hypr.DeviceAuthenticationStatusResponse;
import io.fusionauth.api.domain.api.hypr.HYPRDeviceList;
import io.fusionauth.api.domain.api.hypr.HYPRRequestState;
import io.fusionauth.api.domain.api.hypr.HYPRState;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.domain.provider.HYPRIdentityProvider;
import java.net.URI;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class HYPRClient {
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  private final String relyingPartyApplicationId;
  
  private final URI relyingPartyURL;
  
  public HYPRClient(ProxyInfoSupplier paramProxyInfoSupplier, HYPRIdentityProvider paramHYPRIdentityProvider, UUID paramUUID) {
    this.proxyInfoSupplier = paramProxyInfoSupplier;
    this.relyingPartyURL = paramHYPRIdentityProvider.lookupRelyingPartyURL(paramUUID);
    this.relyingPartyApplicationId = paramHYPRIdentityProvider.lookupRelyingPartyApplicationId(paramUUID);
  }
  
  public HYPRState retrieveAuthenticationRequestStatus(Debugger paramDebugger, String paramString1, String paramString2) {
    Objects.requireNonNull(paramString2);
    paramDebugger.log("Begin a polling request for  [" + paramString2 + "] with request Id [" + paramString1 + "] and Relying Party Application Id [" + this.relyingPartyApplicationId + "].");
    ClientResponse clientResponse = (new RESTClient(DeviceAuthenticationStatusResponse.class, void.class)).url(this.relyingPartyURL.toString()).uri("/rp/oob/client/authentication/requests").connectTimeout(10000).readTimeout(10000).proxy((ProxyInfo)this.proxyInfoSupplier.get()).urlSegment(paramString1).successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(DeviceAuthenticationStatusResponse.class)).get().go();
    if (clientResponse.wasSuccessful()) {
      ((DeviceAuthenticationStatusResponse)clientResponse.successResponse).state.sort(Comparator.comparing(paramHYPRRequestState -> Long.valueOf(paramHYPRRequestState.timestamp)));
      paramDebugger.logObjectToJSON("Device authentication status:\n", clientResponse.successResponse);
      return (((DeviceAuthenticationStatusResponse)clientResponse.successResponse).state.last()).value;
    } 
    paramDebugger.log("Request failed, HYPR returned [" + clientResponse.status + "] from [" + this.relyingPartyApplicationId + "].")
      .log(clientResponse.exception);
    return null;
  }
  
  public HYPRDeviceList retrieveUserDevices(Debugger paramDebugger, String paramString) {
    Objects.requireNonNull(paramString);
    paramDebugger.log("Begin a device list request to HYPR for [" + paramString + "] with Relying Party Application Id [" + this.relyingPartyApplicationId + "].");
    ClientResponse clientResponse = (new RESTClient(HYPRDeviceList.class, void.class)).url(this.relyingPartyURL.toString()).uri("/rp/oob/client/authentication/").connectTimeout(10000).readTimeout(10000).proxy((ProxyInfo)this.proxyInfoSupplier.get()).urlSegment(this.relyingPartyApplicationId).urlSegment(paramString.toLowerCase()).urlSegment("devices").successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(HYPRDeviceList.class)).get().go();
    if (clientResponse.wasSuccessful()) {
      paramDebugger.log("Request succeeded, HYPR returned [" + clientResponse.status + "] from [" + String.valueOf(this.relyingPartyURL) + "].")
        .logObjectToJSON("Full device list response:\n", clientResponse.successResponse);
      return (HYPRDeviceList)clientResponse.successResponse;
    } 
    paramDebugger.log("Request failed, HYPR returned [" + clientResponse.status + "] from [" + String.valueOf(this.relyingPartyURL) + "].")
      .log(clientResponse.exception);
    return new HYPRDeviceList();
  }
  
  public String startAuthenticationRequest(Debugger paramDebugger, String paramString) {
    Objects.requireNonNull(paramString);
    paramDebugger.log("Begin the device authentication request for [" + paramString + "] with Relying Party Application Id [" + this.relyingPartyApplicationId + "].");
    ClientResponse clientResponse = (new RESTClient(DeviceAuthenticationResponse.class, void.class)).url(this.relyingPartyURL.toString()).uri("/rp/oob/client/authentication/requests").connectTimeout(10000).readTimeout(10000).proxy((ProxyInfo)this.proxyInfoSupplier.get()).bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(new DeviceAuthenticationRequest(this.relyingPartyApplicationId, paramString.toLowerCase()))).successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(DeviceAuthenticationResponse.class)).post().go();
    if (clientResponse.wasSuccessful()) {
      String str = ((DeviceAuthenticationResponse)clientResponse.successResponse).response.requestId;
      paramDebugger.log("Request succeeded, HYPR returned [" + clientResponse.status + "] from [" + String.valueOf(this.relyingPartyURL) + "].")
        .log("Request Id : " + str)
        .logObjectToJSON("Full device response:\n", clientResponse.successResponse);
      return str;
    } 
    paramDebugger.log("Request failed, HYPR returned [" + clientResponse.status + "] from [" + String.valueOf(this.relyingPartyURL) + "].");
    if (clientResponse.exception != null)
      paramDebugger.log(clientResponse.exception); 
    return null;
  }
}
