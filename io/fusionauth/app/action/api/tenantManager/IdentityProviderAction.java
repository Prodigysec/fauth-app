package io.fusionauth.app.action.api.tenantManager;

import com.google.inject.Inject;
import io.fusionauth.api.service.tenantManager.TenantManagerService;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.action.api.Patchable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.tenantManager.TenantManagerIdentityProviderTypeConfigurationRequest;
import io.fusionauth.domain.api.tenantManager.TenantManagerIdentityProviderTypeConfigurationResponse;
import io.fusionauth.domain.provider.IdentityProviderType;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{type}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class IdentityProviderAction extends BaseAPIAction implements Patchable {
  private final TenantManagerService tenantManagerService;
  
  @JSONPatch
  @JSONRequest
  public TenantManagerIdentityProviderTypeConfigurationRequest request = new TenantManagerIdentityProviderTypeConfigurationRequest();
  
  @JSONResponse
  public TenantManagerIdentityProviderTypeConfigurationResponse response;
  
  @PreParameter
  public IdentityProviderType type;
  
  private TenantManagerService.TenantManagerIdentityProviderTypeConfigurationValidationResult result;
  
  @Inject
  public IdentityProviderAction(FrontEndSupport paramFrontEndSupport, TenantManagerService paramTenantManagerService) {
    super(paramFrontEndSupport);
    this.tenantManagerService = paramTenantManagerService;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.tenantManagerService.deleteTenantManagerIdentityProviderTypeConfiguration(this.result.existing);
    return "success";
  }
  
  public void loadExisting() {
    if (this.type != null)
      this.request.typeConfiguration = this.tenantManagerService.retrieveTenantManagerIdentityProviderTypeConfiguration(this.type); 
  }
  
  public String post() {
    this.tenantManagerService.createTenantManagerIdentityProviderTypeConfiguration(this.result.typeConfiguration);
    this.response = new TenantManagerIdentityProviderTypeConfigurationResponse(this.result.typeConfiguration);
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    this.tenantManagerService.updateTenantManagerIdentityProviderTypeConfiguration(this.result.existing, this.result.typeConfiguration);
    this.response = new TenantManagerIdentityProviderTypeConfigurationResponse(this.result.typeConfiguration);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.type == null) {
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]);
      return;
    } 
    this.result = this.tenantManagerService.validateTenantManagerIdentityProviderTypeConfigurationDelete(this.type);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.typeConfiguration == null) {
      this.frontEndSupport.addFieldError("typeConfiguration", "[missing]typeConfiguration", new Object[0]);
      return;
    } 
    this.request.typeConfiguration.type = this.type;
    this.result = this.tenantManagerService.validateTenantManagerIdentityProviderTypeConfigurationCreate(this.request.typeConfiguration);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validatePutAndPatch() {
    if (this.request.typeConfiguration == null) {
      this.frontEndSupport.addFieldError("typeConfiguration", "[missing]typeConfiguration", new Object[0]);
      return;
    } 
    this.request.typeConfiguration.type = this.type;
    this.result = this.tenantManagerService.validateTenantManagerIdentityProviderTypeConfigurationUpdate(this.request.typeConfiguration);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
