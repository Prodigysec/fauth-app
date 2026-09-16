package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Lambda;

public class LambdaRequest {
  public Lambda lambda;
  
  @JacksonConstructor
  public LambdaRequest() {}
  
  public LambdaRequest(Lambda paramLambda) {
    this.lambda = paramLambda;
  }
}
