package io.fusionauth.app.action.admin.lambda;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.api.LambdaResponse;
import io.fusionauth.domain.api.LambdaSearchRequest;
import io.fusionauth.domain.api.LambdaSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.LambdaSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "lambda_manager"})
public class IndexAction extends BaseSearchAction<Lambda, LambdaSearchCriteria> {
  @FTLVariable
  public static final LambdaType[] types = LambdaType.values();
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  protected LambdaSearchCriteria defaultSearchCriteria() {
    return new LambdaSearchCriteria();
  }
  
  protected SearchResults<Lambda> search() {
    UUID uUID = parseUUID(this.s.name);
    if (uUID != null) {
      ClientResponse<LambdaResponse, Errors> clientResponse = this.superClient.retrieveLambda(uUID);
      if (clientResponse.wasSuccessful())
        return new SearchResults<>(List.of(((LambdaResponse)clientResponse.getSuccessResponse()).lambda), 1L); 
      return null;
    } 
    LambdaSearchResponse lambdaSearchResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.searchLambdas(new LambdaSearchRequest(this.s)));
    return new SearchResults<>(lambdaSearchResponse.lambdas, lambdaSearchResponse.total);
  }
}
