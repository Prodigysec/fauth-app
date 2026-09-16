package io.fusionauth.app.action.ajax.lambda;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.api.LambdaResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{lambdaId}", requiresAuthentication = true, constraints = {"admin", "lambda_manager"})
public class ViewAction extends BaseAJAXAction {
  public Lambda lambda;
  
  public UUID lambdaId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.lambda = ((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambda(this.lambdaId))).lambda;
    return "render";
  }
}
