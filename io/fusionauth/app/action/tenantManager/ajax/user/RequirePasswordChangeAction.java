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
import io.fusionauth.domain.api.UserRequest;
import io.fusionauth.domain.api.UserResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.annotation.ConstraintOverride;
import org.primeframework.mvc.security.csrf.CSRFProvider;

@Action(value = "{userId}", requiresAuthentication = true, scheme = {"tenant-manager"})
public class RequirePasswordChangeAction extends BaseTenantManagerAction {
  public UUID userId;
  
  @Inject
  public RequirePasswordChangeAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
  }
  
  @ConstraintOverride({"admin"})
  public String get() {
    return "input";
  }
  
  @ConstraintOverride({"admin"})
  public String post() {
    ClientResponse<UserResponse, Errors> clientResponse1 = this.client.retrieveUser(this.userId);
    if (!clientResponse1.wasSuccessful()) {
      if (clientResponse1.status == 404) {
        this.frontEndSupport.addGeneralError("[notFound]userId", new Object[0]);
      } else {
        this.frontEndSupport.transfer((Errors)clientResponse1.errorResponse);
      } 
      return "error";
    } 
    User user = ((UserResponse)clientResponse1.successResponse).user;
    user.passwordChangeRequired = true;
    UserRequest userRequest = (new UserRequest()).with(paramUserRequest -> paramUserRequest.user = paramUser);
    ClientResponse<UserResponse, Errors> clientResponse2 = this.client.updateUser(this.userId, userRequest);
    if (clientResponse2.wasSuccessful()) {
      writeAuditLog("User with Id [" + String.valueOf(this.userId) + "] and loginId [" + user.getLogin() + "] has been updated to require password change.");
      this.frontEndSupport.addGeneralInfo(MessageScope.FLASH, "[PasswordChangeRequired]", new Object[0]);
      return "success";
    } 
    this.frontEndSupport.transfer((Errors)clientResponse2.errorResponse);
    return "error";
  }
}
