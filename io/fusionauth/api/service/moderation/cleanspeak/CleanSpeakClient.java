package io.fusionauth.api.service.moderation.cleanspeak;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.inversoft.error.Errors;
import com.inversoft.json.JacksonModule;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.JSONBodyHandler;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class CleanSpeakClient {
  public static final ObjectMapper objectMapper = (new ObjectMapper()).setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
    .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
    .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .registerModule((Module)new JacksonModule());
  
  public final String apiKey;
  
  public final String baseURL;
  
  public final Consumer<ClientResponse<?, ?>> errorConsumer;
  
  public final Function<ClientResponse<?, ?>, ?> successFunction;
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  public CleanSpeakClient(String paramString1, String paramString2, ProxyInfoSupplier paramProxyInfoSupplier) {
    this.apiKey = paramString1;
    this.baseURL = paramString2;
    this.errorConsumer = null;
    this.successFunction = null;
    this.proxyInfoSupplier = paramProxyInfoSupplier;
  }
  
  public CleanSpeakClient(String paramString1, String paramString2, Function<ClientResponse<?, ?>, ?> paramFunction, Consumer<ClientResponse<?, ?>> paramConsumer, ProxyInfoSupplier paramProxyInfoSupplier) {
    this.apiKey = paramString1;
    this.baseURL = paramString2;
    this.errorConsumer = paramConsumer;
    this.successFunction = paramFunction;
    this.proxyInfoSupplier = paramProxyInfoSupplier;
  }
  
  public ClientResponse<ApplicationResponse, Errors> createApplication(UUID paramUUID, ApplicationRequest paramApplicationRequest) {
    return (new RESTClient(ApplicationResponse.class, Errors.class))
      .url(this.baseURL)
      .uri("/system/application/" + String.valueOf(paramUUID))
      .authorization(this.apiKey)
      .proxy((ProxyInfo)this.proxyInfoSupplier.get())
      .successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(ApplicationResponse.class, objectMapper))
      .errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(Errors.class, objectMapper))
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramApplicationRequest))
      .post()
      .go();
  }
  
  public ClientResponse<ModerateResponse, Errors> moderateCreate(UUID paramUUID, ModerateRequest paramModerateRequest) {
    return (new RESTClient(ModerateResponse.class, Errors.class))
      .url(this.baseURL)
      .uri("/content/item/moderate" + ((paramUUID != null) ? ("/" + String.valueOf(paramUUID)) : ""))
      .authorization(this.apiKey)
      .proxy((ProxyInfo)this.proxyInfoSupplier.get())
      .successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(ModerateResponse.class, objectMapper))
      .errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(Errors.class, objectMapper))
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramModerateRequest))
      .post()
      .go();
  }
  
  public ClientResponse<ApplicationResponse, Errors> retrieveApplication(UUID paramUUID) {
    return (new RESTClient(ApplicationResponse.class, Errors.class))
      .url(this.baseURL)
      .uri("/system/application/" + String.valueOf(paramUUID))
      .authorization(this.apiKey)
      .proxy((ProxyInfo)this.proxyInfoSupplier.get())
      .successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(ApplicationResponse.class, objectMapper))
      .errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(Errors.class, objectMapper))
      .get()
      .go();
  }
  
  public ClientResponse<ApplicationResponse, Errors> retrieveApplications() {
    return (new RESTClient(ApplicationResponse.class, Errors.class))
      .url(this.baseURL)
      .uri("/system/application")
      .authorization(this.apiKey)
      .proxy((ProxyInfo)this.proxyInfoSupplier.get())
      .successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(ApplicationResponse.class, objectMapper))
      .errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(Errors.class, objectMapper))
      .get()
      .go();
  }
}
