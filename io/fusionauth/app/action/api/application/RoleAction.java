package io.fusionauth.app.action.api.application;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.application.ApplicationService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.app.action.api.BaseApplicationAPIAction;
import io.fusionauth.app.action.api.Patchable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.ApplicationRequest;
import io.fusionauth.domain.api.ApplicationResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(prefixParameters = "{applicationId}", value = "{roleId}", requiresAuthentication = true, scheme = {"api"})
public class RoleAction extends BaseApplicationAPIAction implements Patchable {
  private final ApplicationReaderService applicationReader;
  
  private final ApplicationService applicationService;
  
  @PreParameter
  public UUID applicationId;
  
  public String name;
  
  @JSONPatch
  @JSONRequest
  public ApplicationRequest request = new ApplicationRequest();
  
  @JSONResponse
  public ApplicationResponse response;
  
  @PreParameter
  public UUID roleId;
  
  private ApplicationService.ValidationResult result;
  
  @Inject
  public RoleAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ApplicationReaderService paramApplicationReaderService, ApplicationService paramApplicationService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.applicationReader = paramApplicationReaderService;
    this.applicationService = paramApplicationService;
  }
  
  public String delete() {
    if (this.result.role == null)
      return "missing"; 
    this.applicationService.deleteRole(this.result.role);
    return "success";
  }
  
  public void loadExisting() {
    if (this.roleId != null)
      this.request.role = this.applicationReader.retrieveRoleById(getOptionalTenantId(), this.applicationId, this.roleId); 
  }
  
  public String post() {
    if (this.result.application == null)
      return "missing"; 
    this.applicationService.createRole(getAppTenant(this.result.application), this.request.role);
    this.response = new ApplicationResponse(this.request.role);
    return "render";
  }
  
  public String put() {
    if (this.result.role == null)
      return "missing"; 
    this.request.role = this.applicationService.updateRole(getAppTenant(this.result.application), this.request.role);
    this.response = new ApplicationResponse(this.request.role);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.roleId == null && this.name == null) {
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]);
      return;
    } 
    this.result = this.applicationService.validateRoleDelete(getOptionalTenant(), this.applicationId, this.roleId, this.name);
    validateTenantScopedWritesForUniversalApplications(this.result.application);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.role == null) {
      this.frontEndSupport.addFieldError("role", "[missing]role", new Object[0]);
      return;
    } 
    this.request.role.id = this.roleId;
    this.request.role.applicationId = this.applicationId;
    this.request.role.normalize();
    this.result = this.applicationService.validateRoleCreate(getOptionalTenant(), this.applicationId, this.request.role);
    validateTenantScopedWritesForUniversalApplications(this.result.application);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validatePutAndPatch() {
    if (this.request.role == null) {
      this.frontEndSupport.addFieldError("role", "[missing]role", new Object[0]);
      return;
    } 
    this.request.role.id = this.roleId;
    this.request.role.applicationId = this.applicationId;
    this.request.role.normalize();
    this.result = this.applicationService.validateRoleUpdate(getOptionalTenant(), this.applicationId, this.request.role);
    validateTenantScopedWritesForUniversalApplications(this.result.application);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
