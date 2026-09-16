package io.fusionauth.app.action.api.application;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Tenantable;
import io.fusionauth.domain.api.OAuthConfigurationResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.security.annotation.AuthorizeMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(prefixParameters = "{applicationId}", requiresAuthentication = true, scheme = {"api", "authorize-method"})
public class OauthConfigurationAction extends BaseTenantAPIAction {
  private final ApplicationCache applicationCache;
  
  public boolean anonymous;
  
  public UUID applicationId;
  
  @JSONResponse
  public OAuthConfigurationResponse response;
  
  @Inject
  public OauthConfigurationAction(FrontEndSupport paramFrontEndSupport, ApplicationCache paramApplicationCache, AuthenticationKeyCache paramAuthenticationKeyCache) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.applicationCache = paramApplicationCache;
  }
  
  @AuthorizeMethod
  public boolean authorize() {
    this.anonymous = true;
    return true;
  }
  
  public String get() {
    Application application = this.applicationCache.get(getOptionalTenantId(), this.applicationId);
    if (application == null)
      return "missing"; 
    if (this.anonymous)
      application = (new Application(application)).secure(); 
    Tenant tenant = this.frontEndSupport.tenantCache.resolve(getOptionalTenant(), new Tenantable[] { application });
    this.response = new OAuthConfigurationResponse(tenant.httpSessionMaxInactiveInterval, tenant.logoutURL, application.oauthConfiguration);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.applicationId == null)
      this.frontEndSupport.addFieldError("applicationId", "[missing]applicationId", new Object[0]); 
  }
}
