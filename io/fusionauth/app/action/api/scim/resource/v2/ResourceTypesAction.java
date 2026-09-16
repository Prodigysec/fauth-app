package io.fusionauth.app.action.api.scim.resource.v2;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.util.SCIMTools;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.scim.SCIMFrontendService;
import io.fusionauth.scim.domain.SCIMResponse;
import java.io.IOException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.security.annotation.ConstraintOverride;

@Action(baseURI = "/api/scim/resource/v2/ResourceTypes", value = "{resourceTypeId}", requiresAuthentication = true, scheme = {"api-scim"})
public class ResourceTypesAction extends BaseSCIMResourceAction {
  @JSONRequest
  public Object request;
  
  public String resourceTypeId;
  
  @Inject
  public ResourceTypesAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, LambdaInvocationService paramLambdaInvocationService, ReactorStatusService paramReactorStatusService, SCIMFrontendService paramSCIMFrontendService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramLambdaInvocationService, paramReactorStatusService, paramSCIMFrontendService);
  }
  
  @ConstraintOverride({"scim:resource-types:read"})
  public String get() throws IOException {
    this
      
      .response = (this.resourceTypeId != null) ? (SCIMResponse)SCIMTools.getResourceTypeById(this.frontEndSupport.getFusionAuthBaseURL(), this.resourceTypeId) : (SCIMResponse)SCIMTools.getResourceTypes(this.frontEndSupport.getFusionAuthBaseURL());
    return "render";
  }
}
