package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import io.fusionauth.api.service.system.SystemConfigurationService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.SystemConfigurationRequest;
import io.fusionauth.domain.api.SystemConfigurationResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(scheme = {"api-no-tenant"}, requiresAuthentication = true)
public class SystemConfigurationAction extends BaseAPIAction implements Patchable {
  private final SystemConfigurationService systemConfigurationService;
  
  @JSONPatch
  @JSONRequest
  public SystemConfigurationRequest request = new SystemConfigurationRequest();
  
  @JSONResponse
  public SystemConfigurationResponse response;
  
  @Inject
  public SystemConfigurationAction(FrontEndSupport paramFrontEndSupport, SystemConfigurationService paramSystemConfigurationService) {
    super(paramFrontEndSupport);
    this.systemConfigurationService = paramSystemConfigurationService;
  }
  
  public String get() {
    this.response = new SystemConfigurationResponse(this.systemConfigurationService.retrieve().secure());
    return "render";
  }
  
  public void loadExisting() {
    this.request.systemConfiguration = this.systemConfigurationService.retrieve();
  }
  
  public String post() {
    put();
    return "render";
  }
  
  public String put() {
    this.systemConfigurationService.update(this.request.systemConfiguration);
    this.response = new SystemConfigurationResponse(this.request.systemConfiguration.secure());
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST", "PUT", "PATCH"})
  public void validate() {
    if (this.request == null || this.request.systemConfiguration == null) {
      this.frontEndSupport.addFieldError("systemConfiguration", "[missing]systemConfiguration", new Object[0]);
      return;
    } 
    this.request.systemConfiguration.normalize();
    Errors errors = this.systemConfigurationService.validate(this.request.systemConfiguration);
    this.frontEndSupport.transfer(errors);
  }
}
