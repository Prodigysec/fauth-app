package io.fusionauth.app.action.api.entity.type;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.entity.EntityService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.action.api.Patchable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.EntityTypeRequest;
import io.fusionauth.domain.api.EntityTypeResponse;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(prefixParameters = "{entityTypeId}", value = "{permissionId}", requiresAuthentication = true, scheme = {"api"})
public class PermissionAction extends BaseTenantAPIAction implements Patchable {
  private final EntityService entityService;
  
  private final ReactorStatusService reactorStatusService;
  
  @PreParameter
  public UUID entityTypeId;
  
  public String name;
  
  @PreParameter
  public UUID permissionId;
  
  @JSONPatch
  @JSONRequest
  public EntityTypeRequest request = new EntityTypeRequest();
  
  @JSONResponse
  public EntityTypeResponse response;
  
  private EntityService.PermissionValidationResult result;
  
  @Inject
  public PermissionAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, EntityService paramEntityService, ReactorStatusService paramReactorStatusService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.entityService = paramEntityService;
    this.reactorStatusService = paramReactorStatusService;
  }
  
  public String delete() {
    if (this.result.permission == null)
      return "missing"; 
    this.entityService.deletePermission(this.result.permission);
    return "success";
  }
  
  public void loadExisting() {
    if (this.permissionId != null)
      this.request.permission = this.entityService.retrievePermissionById(this.entityTypeId, this.permissionId); 
  }
  
  public String post() {
    if (this.result.entityType == null)
      return "missing"; 
    this.entityService.createPermission(this.request.permission);
    this.response = new EntityTypeResponse(this.request.permission);
    return "render";
  }
  
  public String put() {
    if (this.result.permission == null)
      return "missing"; 
    this.request.permission = this.entityService.updatePermission(this.request.permission);
    this.response = new EntityTypeResponse(this.request.permission);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.permissionId == null && this.name == null) {
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]);
      return;
    } 
    this.result = this.entityService.validatePermissionDelete(this.entityTypeId, this.permissionId, this.name);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE", "PATCH", "POST", "PUT"})
  public void validateLicense() {
    if (ReactorStatusValidator.isNotLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.entityManagement))
      this.frontEndSupport.addGeneralError("[notLicensed]", new Object[0]); 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.permission == null) {
      this.frontEndSupport.addFieldError("permission", "[missing]permission", new Object[0]);
      return;
    } 
    this.request.permission.id = this.permissionId;
    this.request.permission.entityTypeId = this.entityTypeId;
    this.request.permission.normalize();
    this.result = this.entityService.validatePermissionCreate(this.entityTypeId, this.request.permission);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validatePutAndPatch() {
    if (this.request.permission == null) {
      this.frontEndSupport.addFieldError("permission", "[missing]permission", new Object[0]);
      return;
    } 
    this.request.permission.id = this.permissionId;
    this.request.permission.entityTypeId = this.entityTypeId;
    this.request.permission.normalize();
    this.result = this.entityService.validatePermissionUpdate(this.entityTypeId, this.request.permission);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
