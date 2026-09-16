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
import io.fusionauth.domain.api.UserDeleteSingleRequest;
import io.fusionauth.domain.api.UserResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.annotation.ConstraintOverride;
import org.primeframework.mvc.security.csrf.CSRFProvider;

@Action(value = "{userId}", requiresAuthentication = true, scheme = {"tenant-manager"})
@Forward(code = "input", page = "/tenant-manager/ajax/user/delete.ftl")
@List({@Redirect(uri = "/tenant-manager/user/?tenantId=${tenantId}"), @Redirect(code = "missing", uri = "/tenant-manager/user/?tenantId=${tenantId}")})
public class DeleteAction extends BaseTenantManagerAction {
  public User user;
  
  public UUID userId;
  
  @Inject
  public DeleteAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
  }
  
  @ConstraintOverride({"admin"})
  public String get() {
    return "input";
  }
  
  @ConstraintOverride({"admin"})
  public String post() {
    UserDeleteSingleRequest userDeleteSingleRequest = new UserDeleteSingleRequest(this.frontEndSupport.buildEventInfo(null), true);
    ClientResponse<Void, Errors> clientResponse = this.client.deleteUserWithRequest(this.userId, userDeleteSingleRequest);
    if (clientResponse.wasSuccessful()) {
      writeAuditLog("Deleted user with Id [" + String.valueOf(this.userId) + "]");
      return "success";
    } 
    if (clientResponse.status == 404) {
      this.frontEndSupport.addGeneralError("[notFound]userId", new Object[0]);
      return "missing";
    } 
    this.frontEndSupport.transfer((Errors)clientResponse.errorResponse);
    return "input";
  }
  
  @FormPrepareMethod
  public void prepare() {
    ClientResponse<UserResponse, Errors> clientResponse = this.client.retrieveUser(this.userId);
    if (clientResponse.wasSuccessful())
      this.user = ((UserResponse)clientResponse.successResponse).user; 
  }
}
