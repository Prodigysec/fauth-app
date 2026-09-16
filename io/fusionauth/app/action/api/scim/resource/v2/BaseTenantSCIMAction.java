package io.fusionauth.app.action.api.scim.resource.v2;

import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.scim.SCIMException;
import io.fusionauth.api.service.InvalidTenantIdException;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.scim.SCIMFrontendService;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.JSON.List;

@List({@JSON(code = "render", status = 200, cacheControl = "no-store", contentType = "application/scim+json; charset=UTF-8"), @JSON(code = "render-created", status = 201, cacheControl = "no-store", contentType = "application/scim+json; charset=UTF-8"), @JSON(code = "input", status = 400, cacheControl = "no-store", contentType = "application/scim+json; charset=UTF-8"), @JSON(code = "unauthorized", status = 401, cacheControl = "no-store", contentType = "application/scim+json; charset=UTF-8"), @JSON(code = "missing", status = 404, cacheControl = "no-store", contentType = "application/scim+json; charset=UTF-8"), @JSON(code = "conflict", status = 409, cacheControl = "no-store", contentType = "application/scim+json; charset=UTF-8"), @JSON(code = "error", status = 500, cacheControl = "no-store", contentType = "application/scim+json; charset=UTF-8"), @JSON(code = "lambda-invocation-error", status = 500, cacheControl = "no-store", contentType = "application/scim+json; charset=UTF-8")})
public abstract class BaseTenantSCIMAction extends BaseTenantAPIAction {
  protected final SCIMFrontendService scimService;
  
  protected BaseTenantSCIMAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, SCIMFrontendService paramSCIMFrontendService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.scimService = paramSCIMFrontendService;
  }
  
  protected void handleInvalidTenantId(InvalidTenantIdException paramInvalidTenantIdException) {
    throw new SCIMException(paramInvalidTenantIdException, "The specified value in the X-FusionAuth-TenantId HTTP header is invalid.");
  }
  
  protected boolean resolveDefaultTenant() {
    return false;
  }
}
