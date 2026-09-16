package io.fusionauth.app.action.api.lambda;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.lambda.LambdaService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.api.LambdaSearchRequest;
import io.fusionauth.domain.api.LambdaSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.LambdaSearchCriteria;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class SearchAction extends BaseSearchAPIAction<LambdaSearchCriteria> {
  @JSONRequest
  public final LambdaSearchRequest request = new LambdaSearchRequest();
  
  private final LambdaService lambdaService;
  
  @JSONResponse
  public LambdaSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, LambdaService paramLambdaService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.lambdaService = paramLambdaService;
  }
  
  public String body() {
    return (criteria()).body;
  }
  
  public String name() {
    return (criteria()).name;
  }
  
  public void setBody(String paramString) {
    (criteria()).body = paramString;
  }
  
  public void setName(String paramString) {
    (criteria()).name = paramString;
  }
  
  public void setType(LambdaType paramLambdaType) {
    (criteria()).type = paramLambdaType;
  }
  
  public LambdaType type() {
    return (criteria()).type;
  }
  
  protected LambdaSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    this.response = new LambdaSearchResponse(this.lambdaService.search(this.request.search));
    return "render";
  }
}
