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
import org.primeframework.mvc.validation.annotation.PostValidationMethod;

@Action(value = "{lambdaId}", requiresAuthentication = true, constraints = {"admin", "lambda_manager"})
public class DeleteAction extends BaseAJAXAction {
  public Lambda lambda;
  
  public UUID lambdaId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteLambda(this.lambdaId));
    writeAuditLog("Deleted the lambda with Id [" + String.valueOf(this.lambdaId) + "]");
    return "success";
  }
  
  @PostValidationMethod
  public void postValidate() {
    this.lambda = ((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambda(this.lambdaId))).lambda;
  }
}
