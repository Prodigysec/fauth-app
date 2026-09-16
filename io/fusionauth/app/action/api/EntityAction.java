package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.entity.EntityService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.api.EntityRequest;
import io.fusionauth.domain.api.EntityResponse;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{entityId}", requiresAuthentication = true, scheme = {"api"})
public class EntityAction extends BaseTenantAPIAction implements Patchable {
  private final EntityService entityService;
  
  private final ReactorStatusService reactorStatusService;
  
  @PreParameter
  public UUID entityId;
  
  @JSONPatch
  @JSONRequest
  public EntityRequest request = new EntityRequest();
  
  @JSONResponse
  public EntityResponse response;
  
  private EntityService.ValidationResult result;
  
  @Inject
  public EntityAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, EntityService paramEntityService, ReactorStatusService paramReactorStatusService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.entityService = paramEntityService;
    this.reactorStatusService = paramReactorStatusService;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.entityService.deleteEntity(getTenant(), this.result.existing, this.frontEndSupport.buildEventInfo());
    return "success";
  }
  
  public String get() {
    Entity entity = this.entityService.retrieveEntityById((getTenant()).id, this.entityId);
    if (entity == null)
      return "missing"; 
    this.response = new EntityResponse(entity);
    return "render";
  }
  
  public void loadExisting() {
    if (this.entityId != null)
      this.request.entity = this.entityService.retrieveEntityById((getTenant()).id, this.entityId); 
  }
  
  public String post() {
    Entity entity = this.entityService.createEntity(getTenant(), this.request.entity, this.frontEndSupport.buildEventInfo());
    this.response = new EntityResponse(entity);
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    this.entityService.updateEntity(getTenant(), this.result.existing, this.request.entity, this.frontEndSupport.buildEventInfo());
    this.response = new EntityResponse(this.request.entity);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.entityId == null) {
      this.frontEndSupport.addFieldError("entityId", "[missing]entityId", new Object[0]);
      return;
    } 
    this.result = this.entityService.validateEntityDelete(getTenant(), this.entityId);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.entityId == null)
      this.frontEndSupport.addFieldError("entityId", "[missing]entityId", new Object[0]); 
  }
  
  @ValidationMethod(httpMethods = {"DELETE", "PATCH", "POST", "PUT"})
  public void validateLicense() {
    if (ReactorStatusValidator.isNotLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.entityManagement))
      this.frontEndSupport.addGeneralError("[notLicensed]", new Object[0]); 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.entity == null) {
      this.frontEndSupport.addFieldError("entity", "[missing]entity", new Object[0]);
      return;
    } 
    this.request.entity.id = this.entityId;
    this.result = this.entityService.validateEntityCreate(this.request.entity);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validatePutAndPatch() {
    if (this.request.entity == null) {
      this.frontEndSupport.addFieldError("entity", "[missing]entity", new Object[0]);
      return;
    } 
    this.request.entity.id = this.entityId;
    this.result = this.entityService.validateEntityUpdate(getTenant(), this.request.entity);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
