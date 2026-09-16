package io.fusionauth.app.action.admin.lambda;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.api.LambdaRequest;
import io.fusionauth.domain.api.LambdaResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{lambdaId}", requiresAuthentication = true, constraints = {"admin", "lambda_manager"})
@Redirect(code = "success", uri = "/admin/lambda/")
public class EditAction extends BaseFormAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.lambda = ((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambda(this.lambdaId))).lambda;
    return "input";
  }
  
  public String post() {
    Lambda lambda1 = ((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambda(this.lambdaId))).lambda;
    this.existingType = lambda1.type;
    Lambda lambda2 = ((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateLambda(this.lambdaId, new LambdaRequest(this.lambda)))).lambda;
    writeAuditLogForUpdate("Updated lambda with Id [" + String.valueOf(this.lambdaId) + "] and name [" + lambda2.name + "]", lambda1, lambda2);
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    warnOnFetchUsage();
  }
}
