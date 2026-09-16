package io.fusionauth.app.action.api.scim.resource.v2;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.app.action.api.GroupAction;
import io.fusionauth.app.action.api.group.MemberAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.scim.SCIMFrontendService;
import io.fusionauth.scim.domain.SCIMGroup;
import io.fusionauth.scim.domain.SCIMPatchRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.annotation.AlternateMessageResources;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.security.annotation.ConstraintOverride;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(baseURI = "/api/scim/resource/v2/Groups", value = "{resourceId}", requiresAuthentication = true, scheme = {"api-scim"})
@AlternateMessageResources(actions = {GroupAction.class, MemberAction.class})
public class GroupsAction extends BaseSCIMResourceAction {
  public String filter;
  
  @JSONRequest(httpMethods = {"PATCH"})
  public SCIMPatchRequest patchRequest;
  
  @JSONRequest(httpMethods = {"POST", "PUT", "GET", "DELETE"})
  public SCIMGroup request = new SCIMGroup();
  
  @Inject
  public GroupsAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, LambdaInvocationService paramLambdaInvocationService, ReactorStatusService paramReactorStatusService, SCIMFrontendService paramSCIMFrontendService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramLambdaInvocationService, paramReactorStatusService, paramSCIMFrontendService);
  }
  
  @ConstraintOverride({"scim:group:delete"})
  public String delete() {
    this.scimService.deleteGroup(getTenant(), this.resourceId);
    return "no-content";
  }
  
  @ConstraintOverride({"scim:group:read"})
  public String get() {
    this
      
      .response = (this.resourceId != null) ? this.scimService.retrieveGroupById(getTenant(), this.clientEntityId, this.resourceId, this.frontEndSupport.getFusionAuthBaseURL()) : this.scimService.searchGroups(getTenant(), this.clientEntityId, this.filter, this.startIndex, this.count, this.frontEndSupport.getFusionAuthBaseURL());
    return "render";
  }
  
  @ConstraintOverride({"scim:group:update"})
  public String patch() {
    this.response = this.scimService.patchGroup(getTenant(), this.clientEntityId, this.resourceId, this.patchRequest, this.frontEndSupport.getFusionAuthBaseURL(), this.frontEndSupport.buildEventInfo());
    return "render";
  }
  
  @ConstraintOverride({"scim:group:create"})
  public String post() {
    this.response = this.scimService.createGroup(getTenant(), this.clientEntityId, this.request, this.resourceId, this.frontEndSupport.getFusionAuthBaseURL(), this.frontEndSupport.buildEventInfo());
    return "render-created";
  }
  
  @ConstraintOverride({"scim:group:update"})
  public String put() {
    this.response = this.scimService.updateGroup(getTenant(), this.clientEntityId, this.resourceId, this.request, this.frontEndSupport.getFusionAuthBaseURL(), this.frontEndSupport.buildEventInfo());
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"PATCH"})
  public void validatePatch() {
    validatePatchRequest(this.patchRequest);
  }
}
