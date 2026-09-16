package io.fusionauth.api.service.messenger.twilio;

import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.FormDataBodyHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import com.inversoft.rest.TextResponseHandler;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class TwilioRESTClient {
  public ClientResponse<String, String> makeCall(ProxyInfoSupplier paramProxyInfoSupplier, Map<String, List<String>> paramMap, String paramString1, String paramString2, URI paramURI) {
    return (new RESTClient(String.class, String.class))
      .basicAuthorization(paramString1, paramString2)
      .url(paramURI.toString())
      .uri(String.format("/2010-04-01/Accounts/%s/Calls", new Object[] { paramString1 })).bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(paramMap))
      .connectTimeout(4000)
      .readTimeout(2000)
      .proxy((ProxyInfo)paramProxyInfoSupplier.get())
      .successResponseHandler((RESTClient.ResponseHandler)new TextResponseHandler())
      .errorResponseHandler((RESTClient.ResponseHandler)new TextResponseHandler())
      .post()
      .go();
  }
  
  public ClientResponse<String, String> sendSMS(ProxyInfoSupplier paramProxyInfoSupplier, Map<String, List<String>> paramMap, String paramString1, String paramString2, URI paramURI) {
    return (new RESTClient(String.class, String.class))
      .basicAuthorization(paramString1, paramString2)
      .url(paramURI.toString())

      
      .uri(String.format("/2010-04-01/Accounts/%s/Messages", new Object[] { paramString1 })).bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(paramMap))

      
      .connectTimeout(4000)
      .readTimeout(2000)
      .proxy((ProxyInfo)paramProxyInfoSupplier.get())
      .successResponseHandler((RESTClient.ResponseHandler)new TextResponseHandler())
      .errorResponseHandler((RESTClient.ResponseHandler)new TextResponseHandler())
      .post()
      .go();
  }
}
