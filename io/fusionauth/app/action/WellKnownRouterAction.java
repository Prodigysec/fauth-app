package io.fusionauth.app.action;

import com.google.inject.Inject;
import io.fusionauth.api.domain.guice.FusionAuthTenantId;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.app.action.wellKnown.OpenidConfigurationAction;
import io.fusionauth.http.server.HTTPRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;

@Action(baseURI = "/.well-known", prefixParameters = "{tenantId}", value = "{*segments}")
@Status(code = "missing", status = 404)
public class WellKnownRouterAction extends OpenidConfigurationAction {
  public List<String> segments = new ArrayList<>(1);
  
  @Inject
  public WellKnownRouterAction(@FusionAuthTenantId UUID paramUUID, HTTPRequest paramHTTPRequest, TenantCache paramTenantCache, TenantReaderService paramTenantReaderService) {
    super(paramUUID, paramHTTPRequest, paramTenantCache, paramTenantReaderService);
  }
  
  public String get() {
    if (this.segments.size() > 0 && 
      "openid-configuration".equalsIgnoreCase(this.segments.get(0)))
      return super.get(); 
    return "missing";
  }
}
