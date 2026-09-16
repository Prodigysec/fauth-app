package io.fusionauth.app.action.tenantManager;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;

@Action(requiresAuthentication = true, scheme = {"tenant-manager"})
@Redirect(uri = "/tenant-manager/user/?tenantId=${tenantId}")
public class IndexAction extends BaseTenantManagerAction {
  @Inject
  public IndexAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
  }
  
  public String get() {
    return "success";
  }
}
