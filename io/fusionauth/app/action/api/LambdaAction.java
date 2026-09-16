package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import io.fusionauth.api.service.lambda.LambdaService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.api.LambdaRequest;
import io.fusionauth.domain.api.LambdaResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{lambdaId}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class LambdaAction extends BaseAPIAction implements Patchable {
  private final LambdaService lambdaService;
  
  @PreParameter
  public UUID lambdaId;
  
  @JSONPatch
  @JSONRequest
  public LambdaRequest request = new LambdaRequest();
  
  @JSONResponse
  public LambdaResponse response;
  
  public LambdaType type;
  
  private LambdaService.ValidationResult result;
  
  @Inject
  public LambdaAction(FrontEndSupport paramFrontEndSupport, LambdaService paramLambdaService) {
    super(paramFrontEndSupport);
    this.lambdaService = paramLambdaService;
  }
  
  public String delete() {
    this.lambdaService.delete(this.lambdaId);
    return "success";
  }
  
  public String get() {
    if (this.lambdaId == null) {
      if (this.type == null) {
        this.response = new LambdaResponse(this.lambdaService.retrieveAll());
      } else {
        this.response = new LambdaResponse(this.lambdaService.retrieveEnabledByType(this.type));
      } 
    } else {
      Lambda lambda = this.lambdaService.retrieveById(this.lambdaId);
      if (lambda == null)
        return "missing"; 
      this.response = new LambdaResponse(lambda);
    } 
    return "render";
  }
  
  public void loadExisting() {
    if (this.lambdaId != null)
      this.request.lambda = this.lambdaService.retrieveById(this.lambdaId); 
  }
  
  public String post() {
    this.lambdaService.create(this.request.lambda);
    this.response = new LambdaResponse(this.request.lambda);
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    this.lambdaService.update(this.result.existing, this.request.lambda);
    this.response = new LambdaResponse(this.request.lambda);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST", "PUT", "PATCH"})
  public void validate() {
    if (this.request.lambda == null) {
      this.frontEndSupport.addFieldError("lambda", "[missing]lambda", new Object[0]);
      return;
    } 
    this.request.lambda.id = this.lambdaId;
    this.result = this.lambdaService.validate(this.request.lambda, this.frontEndSupport.isPOST());
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.lambdaId == null) {
      this.frontEndSupport.addFieldError("lambdaId", "[missing]lambdaId", new Object[0]);
      return;
    } 
    this.frontEndSupport.transfer(this.lambdaService.validateDelete(this.lambdaId));
  }
}
