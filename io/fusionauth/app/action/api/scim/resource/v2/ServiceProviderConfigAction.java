package io.fusionauth.app.action.api.scim.resource.v2;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.util.SCIMTools;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.scim.SCIMFrontendService;
import io.fusionauth.scim.domain.SCIMResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.security.annotation.ConstraintOverride;

@Action(baseURI = "/api/scim/resource/v2/ServiceProviderConfig", requiresAuthentication = true, scheme = {"api-scim"})
public class ServiceProviderConfigAction extends BaseSCIMResourceAction {
  @JSONRequest
  public Object request;
  
  @Inject
  public ServiceProviderConfigAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, LambdaInvocationService paramLambdaInvocationService, ReactorStatusService paramReactorStatusService, SCIMFrontendService paramSCIMFrontendService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramLambdaInvocationService, paramReactorStatusService, paramSCIMFrontendService);
  }
  
  @ConstraintOverride({"scim:service-provider-config:read"})
  public String get() {
    this.response = (SCIMResponse)SCIMTools.getServerProviderConfig(this.frontEndSupport.getFusionAuthBaseURL());
    return "render";
  }
}
