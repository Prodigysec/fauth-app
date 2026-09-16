package io.fusionauth.app.action.api.application;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.application.ApplicationService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.app.action.api.BaseApplicationAPIAction;
import io.fusionauth.app.action.api.Patchable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.ApplicationOAuthScopeRequest;
import io.fusionauth.domain.api.ApplicationOAuthScopeResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(prefixParameters = "{applicationId}", value = "{scopeId}", requiresAuthentication = true, scheme = {"api"})
public class ScopeAction extends BaseApplicationAPIAction implements Patchable {
  private final ApplicationReaderService applicationReader;
  
  private final ApplicationService applicationService;
  
  @PreParameter
  public UUID applicationId;
  
  @JSONPatch
  @JSONRequest
  public ApplicationOAuthScopeRequest request = new ApplicationOAuthScopeRequest();
  
  @JSONResponse
  public ApplicationOAuthScopeResponse response;
  
  @PreParameter
  public UUID scopeId;
  
  private ApplicationService.ValidationResult result;
  
  @Inject
  public ScopeAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ApplicationReaderService paramApplicationReaderService, ApplicationService paramApplicationService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.applicationReader = paramApplicationReaderService;
    this.applicationService = paramApplicationService;
  }
  
  public String delete() {
    if (this.result.scope == null)
      return "missing"; 
    this.applicationService.deleteOAuthScope(this.result.scope);
    return "success";
  }
  
  public String get() {
    if (this.result.scope == null)
      return "missing"; 
    this.response = new ApplicationOAuthScopeResponse(this.result.scope);
    return "render";
  }
  
  public void loadExisting() {
    if (this.scopeId != null)
      this.request.scope = this.applicationReader.retrieveOAuthScopeById(getOptionalTenantId(), this.applicationId, this.scopeId); 
  }
  
  public String post() {
    if (this.result.application == null)
      return "missing"; 
    this.applicationService.createOAuthScope(getAppTenant(this.result.application), this.request.scope);
    this.response = new ApplicationOAuthScopeResponse(this.request.scope);
    return "render";
  }
  
  public String put() {
    if (this.result.scope == null)
      return "missing"; 
    this.response = new ApplicationOAuthScopeResponse(this.applicationService.updateOAuthScope(getAppTenant(this.result.application), this.request.scope));
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    this.result = this.applicationService.validateOAuthScopeRetrieveById(getOptionalTenant(), this.applicationId, this.scopeId);
    validateTenantScopedWritesForUniversalApplications(this.result.application);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    this.result = this.applicationService.validateOAuthScopeRetrieveById(getOptionalTenant(), this.applicationId, this.scopeId);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.scope == null) {
      this.frontEndSupport.addFieldError("scope", "[missing]scope", new Object[0]);
      return;
    } 
    this.request.scope.id = this.scopeId;
    this.request.scope.applicationId = this.applicationId;
    this.request.scope.normalize();
    this.result = this.applicationService.validateOAuthScopeCreate(getOptionalTenant(), this.applicationId, this.request.scope);
    validateTenantScopedWritesForUniversalApplications(this.result.application);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validatePutAndPatch() {
    if (this.request.scope == null) {
      this.frontEndSupport.addFieldError("scope", "[missing]scope", new Object[0]);
      return;
    } 
    this.request.scope.id = this.scopeId;
    this.request.scope.applicationId = this.applicationId;
    this.request.scope.normalize();
    this.result = this.applicationService.validateOAuthScopeUpdate(getOptionalTenant(), this.applicationId, this.request.scope);
    validateTenantScopedWritesForUniversalApplications(this.result.application);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
