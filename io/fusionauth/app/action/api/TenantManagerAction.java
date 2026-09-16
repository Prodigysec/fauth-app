package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import io.fusionauth.api.service.tenantManager.TenantManagerService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.tenantManager.TenantManagerConfigurationRequest;
import io.fusionauth.domain.api.tenantManager.TenantManagerConfigurationResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(scheme = {"api-no-tenant"}, requiresAuthentication = true)
public class TenantManagerAction extends BaseAPIAction implements Patchable {
  private final TenantManagerService tenantManagerService;
  
  @JSONPatch
  @JSONRequest
  public TenantManagerConfigurationRequest request = new TenantManagerConfigurationRequest();
  
  @JSONResponse
  public TenantManagerConfigurationResponse response;
  
  @Inject
  public TenantManagerAction(FrontEndSupport paramFrontEndSupport, TenantManagerService paramTenantManagerService) {
    super(paramFrontEndSupport);
    this.tenantManagerService = paramTenantManagerService;
  }
  
  public String get() {
    this.response = new TenantManagerConfigurationResponse(this.tenantManagerService.retrieve());
    return "render";
  }
  
  public void loadExisting() {
    this.request.tenantManagerConfiguration = this.tenantManagerService.retrieve();
  }
  
  public String post() {
    put();
    return "render";
  }
  
  public String put() {
    this.tenantManagerService.update(this.request.tenantManagerConfiguration);
    this.response = new TenantManagerConfigurationResponse(this.request.tenantManagerConfiguration);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST", "PUT", "PATCH"})
  public void validate() {
    if (this.request == null || this.request.tenantManagerConfiguration == null) {
      this.frontEndSupport.addFieldError("tenantManagerConfiguration", "[missing]tenantManagerConfiguration", new Object[0]);
      return;
    } 
    Errors errors = this.tenantManagerService.validate(this.request.tenantManagerConfiguration);
    this.frontEndSupport.transfer(errors);
  }
}
