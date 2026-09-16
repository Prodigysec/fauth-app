package io.fusionauth.app.action.api.entity;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.entity.EntityService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EntityGrant;
import io.fusionauth.domain.api.EntityGrantRequest;
import io.fusionauth.domain.api.EntityGrantResponse;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(prefixParameters = "{entityId}", requiresAuthentication = true, scheme = {"api"})
public class GrantAction extends BaseTenantAPIAction {
  @JSONRequest
  public final EntityGrantRequest request = new EntityGrantRequest();
  
  private final EntityService entityService;
  
  private final ReactorStatusService reactorStatusService;
  
  @PreParameter
  public UUID entityId;
  
  public UUID recipientEntityId;
  
  @JSONResponse
  public EntityGrantResponse response;
  
  public UUID userId;
  
  private Entity entity;
  
  private EntityService.GrantValidationResult result;
  
  @Inject
  public GrantAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, EntityService paramEntityService, ReactorStatusService paramReactorStatusService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.entityService = paramEntityService;
    this.reactorStatusService = paramReactorStatusService;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.entityService.deleteGrant(this.result.existing);
    return "success";
  }
  
  public String get() {
    if (this.result.existing == null && (this.userId != null || this.recipientEntityId != null))
      return "missing"; 
    if (this.result.existing != null) {
      this.response = new EntityGrantResponse(this.result.existing);
    } else {
      List<EntityGrant> list = this.entityService.retrieveGrantsForEntity(this.entityId);
      this.response = new EntityGrantResponse(list);
    } 
    return "render";
  }
  
  @PostParameterMethod
  public void loadEntity() {
    if (this.entityId == null) {
      this.frontEndSupport.addFieldError("entityId", "[missing]entityId", new Object[0]);
      throw new ErrorException("input", false);
    } 
  }
  
  public String post() {
    return put();
  }
  
  public String put() {
    this.entityService.upsertGrant(this.entity, this.result.existing, this.request.grant, this.result.permissions);
    return "success";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    retrieveEntityById();
    validateLicense();
    this.result = this.entityService.validateGrantDelete(this.entity, this.userId, this.recipientEntityId);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    retrieveEntityById();
    this.result = this.entityService.validateGrantRetrieve(this.entity, this.userId, this.recipientEntityId);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"POST", "PUT"})
  public void validatePostPut() {
    retrieveEntityById();
    validateLicense();
    this.result = this.entityService.validateGrantUpsert(this.entity, this.request.grant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  private void retrieveEntityById() {
    this.entity = this.entityService.retrieveEntityById(getOptionalTenantId(), this.entityId);
    if (this.entity == null)
      throw new ErrorException("missing"); 
    conditionallyUpdateTenant(this.frontEndSupport.tenantReader.retrieveById(this.entity.tenantId));
  }
  
  private void validateLicense() {
    if (ReactorStatusValidator.isNotLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.entityManagement))
      this.frontEndSupport.addGeneralError("[notLicensed]", new Object[0]); 
  }
}
