package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import io.fusionauth.api.service.integrations.IntegrationService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Integrations;
import io.fusionauth.domain.api.IntegrationRequest;
import io.fusionauth.domain.api.IntegrationResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class IntegrationAction extends BaseAPIAction implements Patchable {
  private final IntegrationService integrationService;
  
  @JSONPatch
  @JSONRequest
  public IntegrationRequest request = new IntegrationRequest();
  
  @JSONResponse
  public IntegrationResponse response;
  
  @Inject
  public IntegrationAction(FrontEndSupport paramFrontEndSupport, IntegrationService paramIntegrationService) {
    super(paramFrontEndSupport);
    this.integrationService = paramIntegrationService;
  }
  
  public String get() {
    Integrations integrations = this.integrationService.retrieve();
    this.response = new IntegrationResponse(integrations);
    return "render";
  }
  
  public void loadExisting() {
    this.request.integrations = this.integrationService.retrieve();
  }
  
  public String put() {
    this.integrationService.update(this.request.integrations);
    this.response = new IntegrationResponse(this.request.integrations);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validate() {
    if (this.request == null || this.request.integrations == null) {
      this.frontEndSupport.addFieldError("integrations", "[missing]integrations", new Object[0]);
      return;
    } 
    this.frontEndSupport.transfer(this.integrationService.validate(this.request.integrations));
  }
}
