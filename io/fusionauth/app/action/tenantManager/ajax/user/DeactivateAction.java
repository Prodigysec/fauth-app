package io.fusionauth.app.action.tenantManager.ajax.user;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.app.action.tenantManager.BaseTenantManagerAction;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.annotation.ConstraintOverride;
import org.primeframework.mvc.security.csrf.CSRFProvider;

@Action(value = "{userId}", requiresAuthentication = true, scheme = {"tenant-manager"})
public class DeactivateAction extends BaseTenantManagerAction {
  public UUID userId;
  
  @Inject
  public DeactivateAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
  }
  
  @ConstraintOverride({"admin"})
  public String get() {
    return "input";
  }
  
  @ConstraintOverride({"admin"})
  public String post() {
    ClientResponse<Void, Errors> clientResponse = this.client.deactivateUser(this.userId);
    if (clientResponse.wasSuccessful()) {
      writeAuditLog("Deactivated user with Id [" + String.valueOf(this.userId) + "]");
      this.frontEndSupport.addGeneralInfo(MessageScope.FLASH, "[UserDeactivated]", new Object[0]);
      return "success";
    } 
    if (clientResponse.status == 404) {
      this.frontEndSupport.addGeneralError("[notFound]userId", new Object[0]);
    } else {
      this.frontEndSupport.transfer((Errors)clientResponse.errorResponse);
    } 
    return "error";
  }
}
