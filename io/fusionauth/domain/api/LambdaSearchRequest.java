package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.search.LambdaSearchCriteria;

public class LambdaSearchRequest implements Buildable<LambdaSearchRequest> {
  public LambdaSearchCriteria search = new LambdaSearchCriteria();
  
  @JacksonConstructor
  public LambdaSearchRequest() {}
  
  public LambdaSearchRequest(LambdaSearchCriteria paramLambdaSearchCriteria) {
    this.search = paramLambdaSearchCriteria;
  }
}
