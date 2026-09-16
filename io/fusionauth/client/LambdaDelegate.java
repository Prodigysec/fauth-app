package io.fusionauth.client;

import com.inversoft.rest.ClientResponse;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class LambdaDelegate {
  public final FusionAuthClient client;
  
  public final Consumer<ClientResponse<?, ?>> errorConsumer;
  
  public final Function<ClientResponse<?, ?>, ?> successFunction;
  
  public LambdaDelegate(FusionAuthClient paramFusionAuthClient, Function<ClientResponse<?, ?>, ?> paramFunction, Consumer<ClientResponse<?, ?>> paramConsumer) {
    Objects.requireNonNull(paramFusionAuthClient, "You can't use the lambda delegate unless you supply a FusionAuthClient");
    Objects.requireNonNull(paramFunction, "You can't use the lambda delegate unless you supply a success Function and error Consumer");
    Objects.requireNonNull(paramConsumer, "You can't use the lambda delegate unless you supply a success Function and error Consumer");
    this.client = paramFusionAuthClient;
    this.errorConsumer = paramConsumer;
    this.successFunction = paramFunction;
  }
  
  public <T, U> T execute(Function<FusionAuthClient, ClientResponse<T, U>> paramFunction) {
    ClientResponse<?, ?> clientResponse = paramFunction.apply(this.client);
    if (clientResponse.wasSuccessful())
      return (T)this.successFunction.apply(clientResponse); 
    this.errorConsumer.accept(clientResponse);
    return null;
  }
}
