package io.fusionauth.app.action.legacy;

import com.google.inject.Inject;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.guice.FusionAuthTenantId;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.app.action.legacy.wellKnown.OpenidConfigurationAction;
import io.fusionauth.http.server.HTTPRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;

@Action(baseURI = "/legacy/.well-known", prefixParameters = "{tenantId}", value = "{*segments}")
@Status(code = "missing", status = 404)
public class WellKnownRouterAction extends OpenidConfigurationAction {
  public List<String> segments = new ArrayList<>(1);
  
  @Inject
  public WellKnownRouterAction(FusionAuthConfiguration paramFusionAuthConfiguration, @FusionAuthTenantId UUID paramUUID, HTTPRequest paramHTTPRequest, ReactorStatusService paramReactorStatusService, TenantCache paramTenantCache, TenantReaderService paramTenantReaderService) {
    super(paramFusionAuthConfiguration, paramUUID, paramHTTPRequest, paramReactorStatusService, paramTenantCache, paramTenantReaderService);
  }
  
  public String get() {
    if (this.segments.size() > 0 && 
      "openid-configuration".equalsIgnoreCase(this.segments.get(0)))
      return super.get(); 
    return "missing";
  }
}
