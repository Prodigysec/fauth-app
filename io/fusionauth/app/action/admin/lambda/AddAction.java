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

@Action(requiresAuthentication = true, constraints = {"admin", "lambda_manager"})
@Redirect(code = "success", uri = "/admin/lambda/")
public class AddAction extends BaseFormAction {
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    if (this.lambdaId != null) {
      this.lambda = ((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambda(this.lambdaId))).lambda;
      this.lambdaId = null;
      this.lambda.name += " - copy";
    } 
    return "input";
  }
  
  public String post() {
    Lambda lambda = ((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createLambda(this.lambdaId, new LambdaRequest(this.lambda)))).lambda;
    writeAuditLog("Created lambda with Id [" + String.valueOf(lambda.id) + "]");
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    warnOnFetchUsage();
  }
}
