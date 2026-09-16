package io.fusionauth.api.service.messenger.generic;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.JSONBodyHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import io.fusionauth.api.domain.message.SendMessageResult;
import io.fusionauth.api.service.messenger.Messenger;
import io.fusionauth.api.service.messenger.MessengerException;
import io.fusionauth.domain.message.Message;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.messenger.GenericMessengerConfiguration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericMessenger implements Messenger {
  private static final Logger logger = LoggerFactory.getLogger(GenericMessenger.class);
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  @Inject
  public GenericMessenger(ProxyInfoSupplier paramProxyInfoSupplier) {
    this.proxyInfoSupplier = paramProxyInfoSupplier;
  }
  
  public SendMessageResult send(Message paramMessage, BaseMessengerConfiguration paramBaseMessengerConfiguration) {
    GenericMessengerConfiguration genericMessengerConfiguration = (GenericMessengerConfiguration)paramBaseMessengerConfiguration;
    Map map = (Map)genericMessengerConfiguration.headers.keySet().stream().collect(Collectors.toMap(paramString -> paramString, paramString -> List.of(paramGenericMessengerConfiguration.headers.get(paramString))));
    ClientResponse clientResponse = (new RESTClient(void.class, void.class)).url(genericMessengerConfiguration.url.toString()).certificate(genericMessengerConfiguration.sslCertificate).basicAuthorization(genericMessengerConfiguration.httpAuthenticationUsername, genericMessengerConfiguration.httpAuthenticationPassword).connectTimeout(genericMessengerConfiguration.connectTimeout.intValue()).readTimeout(genericMessengerConfiguration.readTimeout.intValue()).proxy((ProxyInfo)this.proxyInfoSupplier.get()).bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMessage)).headers(map).post().go();
    boolean bool = clientResponse.wasSuccessful();
    if (!bool) {
      if (clientResponse.exception != null) {
        logger.error("Messenger {} [{}] response was not successful", new Object[] { genericMessengerConfiguration.name, genericMessengerConfiguration.url, clientResponse.exception });
        throw new MessengerException(genericMessengerConfiguration.name, genericMessengerConfiguration.url, clientResponse.exception);
      } 
      logger.error("Messenger {} [{}] returned response code [{}]", new Object[] { genericMessengerConfiguration.name, genericMessengerConfiguration.url, 
            Integer.valueOf(clientResponse.status) });
      throw new MessengerException(genericMessengerConfiguration.name, genericMessengerConfiguration.url, null, clientResponse.status);
    } 
    return new SendMessageResult(clientResponse.status);
  }
}
