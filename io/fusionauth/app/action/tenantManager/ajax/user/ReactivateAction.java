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
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.UserResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.annotation.ConstraintOverride;
import org.primeframework.mvc.security.csrf.CSRFProvider;

@Action(value = "{userId}", requiresAuthentication = true, scheme = {"tenant-manager"})
public class ReactivateAction extends BaseTenantManagerAction {
  public UUID userId;
  
  @Inject
  public ReactivateAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
  }
  
  @ConstraintOverride({"admin"})
  public String get() {
    return "input";
  }
  
  @ConstraintOverride({"admin"})
  public String post() {
    ClientResponse<UserResponse, Errors> clientResponse = this.client.reactivateUser(this.userId);
    if (clientResponse.wasSuccessful()) {
      User user = ((UserResponse)clientResponse.successResponse).user;
      writeAuditLog("Reactivated user with Id [" + String.valueOf(user.id) + "], name [" + user.getName() + "] and username/email [" + ((user.email != null) ? user.email : user.username) + "]");
      this.frontEndSupport.addGeneralInfo(MessageScope.FLASH, "[UserReactivated]", new Object[0]);
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
