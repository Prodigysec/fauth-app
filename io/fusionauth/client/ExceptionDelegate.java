package io.fusionauth.client;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import java.util.function.Supplier;

public class ExceptionDelegate {
  public <T, U> T execute(Supplier<ClientResponse<T, U>> paramSupplier) {
    ClientResponse clientResponse = paramSupplier.get();
    if (clientResponse.wasSuccessful())
      return (T)clientResponse.successResponse; 
    if (clientResponse.status == 404)
      return null; 
    if (clientResponse.errorResponse != null && clientResponse.errorResponse instanceof Errors)
      throw new FusionAuthClientException((Errors)clientResponse.errorResponse); 
    if (clientResponse.errorResponse != null)
      throw new FusionAuthClientException("Invalid error response type"); 
    if (clientResponse.exception != null)
      throw new FusionAuthClientException(clientResponse.exception); 
    throw new FusionAuthClientException("ClientResponse didn't contain a error response or an exception - strange");
  }
}
