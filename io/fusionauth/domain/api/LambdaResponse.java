package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Lambda;
import java.util.List;

public class LambdaResponse {
  public Lambda lambda;
  
  public List<Lambda> lambdas;
  
  @JacksonConstructor
  public LambdaResponse() {}
  
  public LambdaResponse(Lambda paramLambda) {
    this.lambda = paramLambda;
  }
  
  public LambdaResponse(List<Lambda> paramList) {
    this.lambdas = paramList;
  }
}
