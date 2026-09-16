package io.fusionauth.app.action.api.scim.resource.v2;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.scim.SCIMFrontendService;
import io.fusionauth.scim.domain.SCIMPatchRequest;
import io.fusionauth.scim.domain.SCIMUser;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.security.annotation.ConstraintOverride;

@Action(baseURI = "/api/scim/resource/v2/Me", value = "{resourceId}", requiresAuthentication = true, scheme = {"api-scim"})
public class MeAction extends BaseSCIMResourceAction {
  public String filter;
  
  @JSONRequest(httpMethods = {"PATCH"})
  public SCIMPatchRequest patchRequest;
  
  @JSONRequest(httpMethods = {"POST", "PUT", "GET", "DELETE"})
  public SCIMUser request;
  
  @Inject
  public MeAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, LambdaInvocationService paramLambdaInvocationService, ReactorStatusService paramReactorStatusService, SCIMFrontendService paramSCIMFrontendService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramLambdaInvocationService, paramReactorStatusService, paramSCIMFrontendService);
  }
  
  @ConstraintOverride({"scim:user:delete"})
  public String delete() {
    return "not-implemented";
  }
  
  @ConstraintOverride({"scim:user:read"})
  public String get() {
    return "not-implemented";
  }
  
  @ConstraintOverride({"scim:user:update"})
  public String patch() {
    return "not-implemented";
  }
  
  @ConstraintOverride({"scim:user:create"})
  public String post() {
    return "not-implemented";
  }
  
  @ConstraintOverride({"scim:user:update"})
  public String put() {
    return "not-implemented";
  }
}
