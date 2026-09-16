package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.application.ApplicationService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.api.ApplicationRequest;
import io.fusionauth.domain.api.ApplicationResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{applicationId}", requiresAuthentication = true, scheme = {"api"})
public class ApplicationAction extends BaseApplicationAPIAction implements Patchable {
  private final ApplicationReaderService applicationReader;
  
  private final ApplicationService applicationService;
  
  private final UUID tenantManagerApplicationId;
  
  @PreParameter
  public UUID applicationId;
  
  public boolean hardDelete;
  
  public boolean inactive;
  
  public boolean reactivate;
  
  @JSONPatch
  @JSONRequest
  public ApplicationRequest request = new ApplicationRequest();
  
  @JSONResponse
  public ApplicationResponse response;
  
  private ApplicationService.ValidationResult result;
  
  @Inject
  public ApplicationAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ApplicationReaderService paramApplicationReaderService, ApplicationService paramApplicationService, @TenantManagerApplicationId UUID paramUUID) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.applicationReader = paramApplicationReaderService;
    this.applicationService = paramApplicationService;
    this.tenantManagerApplicationId = paramUUID;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    if (this.hardDelete) {
      this.applicationService.delete(getOptionalTenant(), this.result.existing);
    } else {
      this.applicationService.deactivate(getOptionalTenant(), this.result.existing);
    } 
    return "success";
  }
  
  public String get() {
    if (this.applicationId == null) {
      if (this.inactive) {
        this.response = new ApplicationResponse(this.applicationReader.retrieveAllInactive(getOptionalTenantId(), ApplicationReaderService.ApplicationExpansion.all()));
      } else {
        this.response = new ApplicationResponse(this.applicationReader.retrieveAll(getOptionalTenantId(), ApplicationReaderService.ApplicationExpansion.all()));
      } 
      this.response.applications.forEach(paramApplication -> paramApplication.sortRoles().sortOAuthScopes());
      return "render";
    } 
    Application application = this.applicationReader.retrieveByIdIgnoreActive(getOptionalTenantId(), this.applicationId);
    if (application == null)
      return "missing"; 
    this.response = new ApplicationResponse(application);
    this.response.application.sortRoles().sortOAuthScopes();
    return "render";
  }
  
  public void loadExisting() {
    if (this.applicationId != null) {
      this.request.application = this.applicationReader.retrieveById(getOptionalTenantId(), this.applicationId);
      if (this.request.application != null)
        this.request.application.secure(); 
    } 
  }
  
  public String post() {
    this.applicationService.create(this.result.application.universalConfiguration.universal ? null : getTenant(), this.result.application);
    this.response = new ApplicationResponse(this.result.application);
    this.response.application.sortRoles().sortOAuthScopes();
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    if (this.reactivate) {
      this.applicationService.reactivate(this.result.existing);
      this.request.application = this.result.existing;
    } else {
      this.applicationService.update(this.result.existing.universalConfiguration.universal ? getOptionalTenant() : getTenant(), this.result.existing, this.request.application, this.request.eventInfo);
    } 
    if (this.request.application.id.equals(this.tenantManagerApplicationId)) {
      this.response = new ApplicationResponse(this.result.existing);
    } else {
      this.response = new ApplicationResponse(this.request.application);
    } 
    this.response.application.sortRoles().sortOAuthScopes();
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.applicationId == null) {
      this.frontEndSupport.addFieldError("applicationId", "[missing]applicationId", new Object[0]);
      return;
    } 
    if (this.applicationId.equals(Application.FUSIONAUTH_APP_ID)) {
      this.frontEndSupport.addFieldError("applicationId", "[fusionAuth]applicationId", new Object[0]);
      return;
    } 
    if (this.applicationId.equals(this.tenantManagerApplicationId) && this.hardDelete) {
      this.frontEndSupport.addFieldError("applicationId", "[tenantManager]applicationId", new Object[0]);
      return;
    } 
    this.result = this.applicationService.validateApplicationId(getOptionalTenant(), this.applicationId);
    validateTenantScopedWritesForUniversalApplications(this.result.existing);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.application == null) {
      this.frontEndSupport.addFieldError("application", "[missing]application", new Object[0]);
      return;
    } 
    validateTenantScopedWritesForUniversalApplications(this.request.application);
    this.request.application.id = this.applicationId;
    this.request.application.normalize();
    if (this.request.sourceApplicationId != null) {
      this.result = this.applicationService.validateCopy(getOptionalTenant(), this.request.application, this.request.sourceApplicationId);
    } else {
      this.result = this.applicationService.validateCreate(getOptionalTenant(), this.request.application);
    } 
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PATCH", "PUT"})
  public void validatePutAndPatch() {
    if (this.reactivate) {
      this.result = this.applicationService.validateReactivate(getOptionalTenant(), this.applicationId);
      validateTenantScopedWritesForUniversalApplications(this.result.existing);
    } else {
      if (this.request.application == null) {
        this.frontEndSupport.addFieldError("application", "[missing]application", new Object[0]);
        return;
      } 
      this.request.application.id = this.applicationId;
      this.request.application.normalize();
      this.result = this.applicationService.validateUpdate(getOptionalTenant(), this.request.application, this.request.sourceApplicationId);
      validateTenantScopedWritesForUniversalApplications(this.result.existing);
    } 
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
