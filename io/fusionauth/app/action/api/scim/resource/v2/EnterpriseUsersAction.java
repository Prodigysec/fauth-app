package io.fusionauth.app.action.api.scim.resource.v2;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.scim.SCIMFrontendService;
import io.fusionauth.scim.domain.SCIMEnterpriseUser;
import io.fusionauth.scim.domain.SCIMPatchRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.security.annotation.ConstraintOverride;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(baseURI = "/api/scim/resource/v2/EnterpriseUsers", value = "{resourceId}", requiresAuthentication = true, scheme = {"api-scim"})
public class EnterpriseUsersAction extends BaseSCIMResourceAction {
  public String filter;
  
  @JSONRequest(httpMethods = {"PATCH"})
  public SCIMPatchRequest patchRequest;
  
  @JSONRequest(httpMethods = {"POST", "PUT", "GET", "DELETE"})
  public SCIMEnterpriseUser request;
  
  @Inject
  public EnterpriseUsersAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, LambdaInvocationService paramLambdaInvocationService, ReactorStatusService paramReactorStatusService, SCIMFrontendService paramSCIMFrontendService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramLambdaInvocationService, paramReactorStatusService, paramSCIMFrontendService);
  }
  
  @ConstraintOverride({"scim:enterprise:user:delete"})
  public String delete() {
    this.scimService.deleteUser(getTenant(), this.resourceId, this.frontEndSupport.buildEventInfo());
    return "no-content";
  }
  
  @ConstraintOverride({"scim:enterprise:user:read"})
  public String get() {
    this
      
      .response = (this.resourceId != null) ? this.scimService.retrieveEnterpriseUserById(getTenant(), this.clientEntityId, this.resourceId, this.frontEndSupport.getFusionAuthBaseURL()) : this.scimService.searchEnterpriseUsers(getTenant(), this.clientEntityId, this.filter, this.startIndex, this.count, this.frontEndSupport.getFusionAuthBaseURL());
    return "render";
  }
  
  @ConstraintOverride({"scim:enterprise:user:update"})
  public String patch() {
    this.response = this.scimService.patchEnterpriseUser(this.tenant, this.clientEntityId, this.resourceId, this.patchRequest, this.frontEndSupport.getFusionAuthBaseURL());
    return "render";
  }
  
  @ConstraintOverride({"scim:enterprise:user:create"})
  public String post() {
    this.response = this.scimService.createEnterpriseUser(getTenant(), this.clientEntityId, this.request, this.frontEndSupport.getFusionAuthBaseURL(), this.frontEndSupport.buildEventInfo());
    return "render-created";
  }
  
  @ConstraintOverride({"scim:enterprise:user:update"})
  public String put() {
    this.response = this.scimService.updateEnterpriseUser(getTenant(), this.clientEntityId, this.resourceId, this.request, this.frontEndSupport.getFusionAuthBaseURL(), this.frontEndSupport.buildEventInfo());
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"PATCH"})
  public void validatePatch() {
    validatePatchRequest(this.patchRequest);
  }
}
