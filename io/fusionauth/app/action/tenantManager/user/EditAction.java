package io.fusionauth.app.action.tenantManager.user;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.app.action.tenantManager.BaseUserFormAction;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.user.CustomFormFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.SendSetPasswordIdentityType;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.UserRequest;
import io.fusionauth.domain.api.UserResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.annotation.ConstraintOverride;
import org.primeframework.mvc.security.csrf.CSRFProvider;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{userId}", requiresAuthentication = true, scheme = {"tenant-manager"})
@Forward(code = "input", page = "/tenant-manager/user/edit.ftl")
@List({@Redirect(uri = "/tenant-manager/user/?tenantId=${tenantId}"), @Redirect(code = "deactivated", uri = "/tenant-manager/user/?tenantId=${tenantId}")})
public class EditAction extends BaseUserFormAction {
  public boolean skipVerification;
  
  public UUID userId;
  
  @Inject
  public EditAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID, @Named("TenantManagerCustomFormFrontendService") CustomFormFrontendService paramCustomFormFrontendService) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID, paramCustomFormFrontendService);
  }
  
  public String get() {
    ClientResponse<UserResponse, Errors> clientResponse = this.client.retrieveUser(this.userId);
    if (!clientResponse.wasSuccessful())
      return "deactivated"; 
    this.user = ((UserResponse)clientResponse.successResponse).user;
    return "input";
  }
  
  @ConstraintOverride({"admin"})
  public String post() throws Exception {
    User user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(this.userId))).user;
    UserRequest userRequest = (new UserRequest()).with(paramUserRequest -> paramUserRequest.disableDomainBlock = true).with(paramUserRequest -> paramUserRequest.eventInfo = this.frontEndSupport.buildEventInfo(null)).with(paramUserRequest -> paramUserRequest.sendSetPasswordIdentityType = SendSetPasswordIdentityType.doNotSend).with(paramUserRequest -> paramUserRequest.skipVerification = this.skipVerification).with(paramUserRequest -> paramUserRequest.user = this.user);
    this.user.encryptionScheme = null;
    this.user.factor = null;
    ClientResponse<UserResponse, Errors> clientResponse = this.client.updateUser(this.userId, userRequest);
    if (clientResponse.wasSuccessful()) {
      User user1 = ((UserResponse)clientResponse.successResponse).user;
      writeAuditLogForUpdate("Updated the user with Id [" + String.valueOf(this.userId) + "] and loginId [" + user1.getLogin() + "]", user, user1);
      return "success";
    } 
    this.frontEndSupport.frontEndErrorHandling(clientResponse);
    return "input";
  }
  
  @ValidationMethod
  public void validatePost() {
    this.user.id = this.userId;
    this.user.tenantId = this.tenantId;
    CustomFormFrontendService.ValidationResult<User> validationResult = this.customFormFrontendService.normalizeAndValidate(this.tenant, this.user, this.confirm.user, this.currentUser, CustomFormFrontendService.EditPasswordOption.useExisting);
    this.user = (User)validationResult.result;
    this.frontEndSupport.transfer(validationResult.errors);
  }
}
