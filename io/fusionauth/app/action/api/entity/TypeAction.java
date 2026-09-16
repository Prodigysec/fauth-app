package io.fusionauth.app.action.api.entity;

import com.google.inject.Inject;
import io.fusionauth.api.service.entity.EntityService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.action.api.Patchable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.EntityType;
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

@Action(value = "{entityTypeId}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class TypeAction extends BaseAPIAction implements Patchable {
  private final EntityService entityService;
  
  private final ReactorStatusService reactorStatusService;
  
  @PreParameter
  public UUID entityTypeId;
  
  @JSONPatch
  @JSONRequest
  public EntityTypeRequest request = new EntityTypeRequest();
  
  @JSONResponse
  public EntityTypeResponse response;
  
  private EntityService.TypeValidationResult result;
  
  @Inject
  public TypeAction(FrontEndSupport paramFrontEndSupport, EntityService paramEntityService, ReactorStatusService paramReactorStatusService) {
    super(paramFrontEndSupport);
    this.entityService = paramEntityService;
    this.reactorStatusService = paramReactorStatusService;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.entityService.deleteType(this.result.existing);
    return "success";
  }
  
  public String get() {
    if (this.entityTypeId == null) {
      this.response = new EntityTypeResponse(this.entityService.retrieveAllTypes());
    } else {
      EntityType entityType = this.entityService.retrieveTypeById(this.entityTypeId);
      if (entityType == null)
        return "missing"; 
      this.response = new EntityTypeResponse(entityType);
    } 
    return "render";
  }
  
  public void loadExisting() {
    if (this.entityTypeId != null)
      this.request.entityType = this.entityService.retrieveTypeById(this.entityTypeId); 
  }
  
  public String post() {
    this.entityService.createType(this.request.entityType);
    this.response = new EntityTypeResponse(this.request.entityType);
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    this.entityService.updateType(this.result.existing, this.request.entityType);
    this.response = new EntityTypeResponse(this.request.entityType);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.entityTypeId == null) {
      this.frontEndSupport.addFieldError("entityTypeId", "[missing]entityTypeId", new Object[0]);
      return;
    } 
    this.result = this.entityService.validateTypeDelete(this.entityTypeId);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE", "PATCH", "POST", "PUT"})
  public void validateLicense() {
    if (ReactorStatusValidator.isNotLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.entityManagement))
      this.frontEndSupport.addGeneralError("[notLicensed]", new Object[0]); 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.entityType == null) {
      this.frontEndSupport.addFieldError("entityType", "[missing]entityType", new Object[0]);
      return;
    } 
    if (!this.request.entityType.jwtConfiguration.enabled)
      this.request.entityType.jwtConfiguration.accessTokenKeyId = null; 
    this.request.entityType.id = this.entityTypeId;
    this.result = this.entityService.validateTypeCreate(this.request.entityType);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validatePutAndPatch() {
    if (this.request.entityType == null) {
      this.frontEndSupport.addFieldError("entityType", "[missing]entityType", new Object[0]);
      return;
    } 
    if (!this.request.entityType.jwtConfiguration.enabled)
      this.request.entityType.jwtConfiguration.accessTokenKeyId = null; 
    this.request.entityType.id = this.entityTypeId;
    this.result = this.entityService.validateTypeUpdate(this.request.entityType);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
