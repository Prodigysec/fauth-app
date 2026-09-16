package io.fusionauth.app.action.api.scim.resource.v2;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.scim.SCIMFrontendService;
import io.fusionauth.scim.domain.SCIMListResponse;
import io.fusionauth.scim.domain.SCIMResponse;
import java.io.IOException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.security.annotation.ConstraintOverride;

@Action(baseURI = "/api/scim/resource/v2/Schemas", value = "{schemaId}", requiresAuthentication = true, scheme = {"api-scim"})
public class SchemasAction extends BaseSCIMResourceAction {
  public String schemaId;
  
  @Inject
  public SchemasAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, LambdaInvocationService paramLambdaInvocationService, ReactorStatusService paramReactorStatusService, SCIMFrontendService paramSCIMFrontendService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramLambdaInvocationService, paramReactorStatusService, paramSCIMFrontendService);
  }
  
  @ConstraintOverride({"scim:schemas:read"})
  public String get() throws IOException {
    this



      
      .response = (this.schemaId != null) ? (SCIMResponse)this.scimService.getSchemaResourceById(getTenant(), this.schemaId, this.frontEndSupport.getFusionAuthBaseURL()) : (SCIMResponse)((SCIMListResponse)((SCIMListResponse)((SCIMListResponse)(new SCIMListResponse()).with(paramSCIMListResponse -> paramSCIMListResponse.Resources = this.scimService.getSchemaResources(getTenant(), this.frontEndSupport.getFusionAuthBaseURL()))).with(paramSCIMListResponse -> paramSCIMListResponse.totalResults = paramSCIMListResponse.Resources.size())).with(paramSCIMListResponse -> paramSCIMListResponse.startIndex = 1)).with(paramSCIMListResponse -> paramSCIMListResponse.itemsPerPage = paramSCIMListResponse.Resources.size());
    return "render";
  }
}
