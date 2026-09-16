package io.fusionauth.app.action.api.scim.resource.v2;

import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.scim.SCIMException;
import io.fusionauth.api.scim.SCIMResourceMixin;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.scim.SCIMFrontendService;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.scim.domain.SCIMPatchRequest;
import io.fusionauth.scim.domain.SCIMResource;
import io.fusionauth.scim.domain.SCIMResponse;
import java.util.UUID;
import org.primeframework.mvc.content.ValidContentTypes;
import org.primeframework.mvc.content.json.annotation.JSONPropertyFilter;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@ValidContentTypes({"application/json", "application/scim+json"})
public abstract class BaseSCIMResourceAction extends BaseTenantSCIMAction {
  protected final LambdaInvocationService lambdaInvocationService;
  
  private final ReactorStatusService reactorStatusService;
  
  public int count = 25;
  
  public String excludedAttributes;
  
  public UUID resourceId;
  
  @JSONResponse
  public SCIMResponse response;
  
  public int startIndex = 1;
  
  protected UUID clientEntityId;
  
  protected BaseSCIMResourceAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, LambdaInvocationService paramLambdaInvocationService, ReactorStatusService paramReactorStatusService, SCIMFrontendService paramSCIMFrontendService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramSCIMFrontendService);
    this.lambdaInvocationService = paramLambdaInvocationService;
    this.reactorStatusService = paramReactorStatusService;
  }
  
  @JSONPropertyFilter(value = "excludedAttributes", mixinSource = SCIMResourceMixin.class, mixinTarget = SCIMResource.class)
  public PropertyFilter buildFilterProvider() {
    return (this.excludedAttributes != null) ? 
      (PropertyFilter)SimpleBeanPropertyFilter.serializeAllExcept(this.excludedAttributes.split(",")) : 
      (PropertyFilter)SimpleBeanPropertyFilter.serializeAllExcept(new String[0]);
  }
  
  @ValidationMethod(httpMethods = {"DELETE", "GET", "PATCH", "POST", "PUT"})
  public void setupEntityId() {
    Entity entity = (Entity)this.frontEndSupport.request.getAttribute("clientEntity");
    this.tenant = (Tenant)this.frontEndSupport.request.getAttribute("clientEntityTenant");
    this.clientEntityId = entity.id;
    if (ReactorStatusValidator.isNotLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.scimServer))
      throw new SCIMException("input", this.frontEndSupport.messageProvider.getMessage("[notLicensed]", new Object[0])); 
    if (!(getTenant()).scimServerConfiguration.enabled)
      throw new SCIMException("input", this.frontEndSupport.messageProvider.getMessage("[disabled]", new Object[] { (getTenant()).id })); 
  }
  
  protected void validatePatchRequest(SCIMPatchRequest paramSCIMPatchRequest) {
    if (paramSCIMPatchRequest == null || paramSCIMPatchRequest.schemas == null)
      throw new SCIMException("input", this.frontEndSupport.messageProvider.getMessage("[missing]schemas", new Object[0])); 
    if (paramSCIMPatchRequest.schemas.size() > 1 || !paramSCIMPatchRequest.schemas.contains("urn:ietf:params:scim:api:messages:2.0:PatchOp"))
      throw new SCIMException("input", this.frontEndSupport.messageProvider.getMessage("[invalid]schemas", new Object[] { paramSCIMPatchRequest.schemas.get(0), "urn:ietf:params:scim:api:messages:2.0:PatchOp" })); 
  }
}
