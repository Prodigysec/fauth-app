package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.TenantRequest;
import io.fusionauth.api.domain.guice.FusionAuthLocalURLProvider;
import io.fusionauth.api.service.cache.IPAccessControlListCache;
import io.fusionauth.api.service.cache.SystemConfigurationCache;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.security.UnauthorizedException;

public class FusionNoTenantAPIKeySecurityScheme extends FusionAPIKeySecurityScheme {
  @Inject
  public FusionNoTenantAPIKeySecurityScheme(ActionInvocationStore paramActionInvocationStore, HTTPRequest paramHTTPRequest, AuthenticationKeyCache paramAuthenticationKeyCache, FusionAuthConfiguration paramFusionAuthConfiguration, FusionAuthLocalURLProvider paramFusionAuthLocalURLProvider, IPAccessControlListCache paramIPAccessControlListCache, SystemConfigurationCache paramSystemConfigurationCache) {
    super(paramActionInvocationStore, paramHTTPRequest, paramAuthenticationKeyCache, paramFusionAuthConfiguration, paramFusionAuthLocalURLProvider, paramIPAccessControlListCache, paramSystemConfigurationCache);
  }
  
  public void handle(String[] paramArrayOfString) {
    super.handle(paramArrayOfString);
    if (this.request.getAttribute(TenantRequest.KEY) != null)
      throw new UnauthorizedException(); 
  }
}
