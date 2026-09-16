package io.fusionauth.api.domain.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.inversoft.authentication.api.domain.LocalKey;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.RESTClient;
import io.fusionauth.client.FusionAuthClient;
import java.util.UUID;

public class FusionAuthClientProvider implements Provider<FusionAuthClient> {
  public static final int defaultConnectTimeout = 5000;
  
  public static final int defaultReadTimeout = 300000;
  
  private final String fusionauthClientURL;
  
  @Inject
  public FusionAuthClientProvider(@FusionAuthLocalClientURL String paramString) {
    this.fusionauthClientURL = paramString;
  }
  
  public FusionAuthClient get() {
    return new FusionAuthClient(getKey(), this.fusionauthClientURL, 5000, 300000, null);
  }
  
  public FusionAuthClient get(UUID paramUUID) {
    if (paramUUID == null)
      return get(); 
    return new FusionAuthClient(getKey(), this.fusionauthClientURL, 5000, 300000, paramUUID.toString());
  }
  
  public <T, U> RESTClient<T, U> newRESTClient(Class<T> paramClass, Class<U> paramClass1) {
    return (new RESTClient(paramClass, paramClass1))
      .authorization(getKey())
      .connectTimeout(5000)
      .readTimeout(300000)
      .successResponseHandler((paramClass != void.class) ? (RESTClient.ResponseHandler)new JSONResponseHandler(paramClass, FusionAuthClient.objectMapper) : null)
      .errorResponseHandler((paramClass1 != void.class) ? (RESTClient.ResponseHandler)new JSONResponseHandler(paramClass1, FusionAuthClient.objectMapper) : null)
      .url(this.fusionauthClientURL);
  }
  
  protected String getKey() {
    return LocalKey.KEY;
  }
}
