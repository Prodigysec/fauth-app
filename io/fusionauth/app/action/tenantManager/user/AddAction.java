package io.fusionauth.app.action.tenantManager.user;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"tenant-manager"}, constraints = {"admin"})
@Forward(code = "input", page = "/tenant-manager/user/add.ftl")
@Redirect(uri = "/tenant-manager/user/?tenantId=${tenantId}")
public class AddAction extends BaseUserFormAction {
  public SendSetPasswordIdentityType sendSetPasswordIdentityType = SendSetPasswordIdentityType.email;
  
  public boolean skipVerification;
  
  @Inject
  public AddAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID, @Named("TenantManagerCustomFormFrontendService") CustomFormFrontendService paramCustomFormFrontendService) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID, paramCustomFormFrontendService);
  }
  
  public String get() throws Exception {
    this.user.active = true;
    return "input";
  }
  
  public String post() throws Exception {
    UserRequest userRequest = (new UserRequest()).with(paramUserRequest -> paramUserRequest.disableDomainBlock = true).with(paramUserRequest -> paramUserRequest.eventInfo = this.frontEndSupport.buildEventInfo(null)).with(paramUserRequest -> paramUserRequest.sendSetPasswordIdentityType = this.sendSetPasswordIdentityType).with(paramUserRequest -> paramUserRequest.skipVerification = this.skipVerification).with(paramUserRequest -> paramUserRequest.user = this.user);
    this.user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.createUser(null, paramUserRequest))).user;
    writeAuditLog("Created user with Id [" + String.valueOf(this.user.id) + "], name [" + this.user.getName() + "] and loginId [" + this.user.getLogin() + "]");
    this.delegate.execute(FusionAuthClient::refreshUserSearchIndex);
    this.frontEndSupport.addGeneralInfo(MessageScope.FLASH, "[UserCreated]", new Object[0]);
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    CustomFormFrontendService.EditPasswordOption editPasswordOption = (this.sendSetPasswordIdentityType != SendSetPasswordIdentityType.doNotSend) ? CustomFormFrontendService.EditPasswordOption.useExisting : CustomFormFrontendService.EditPasswordOption.update;
    CustomFormFrontendService.ValidationResult<User> validationResult = this.customFormFrontendService.normalizeAndValidate(this.tenant, this.user, this.confirm.user, this.currentUser, editPasswordOption);
    this.user = (User)validationResult.result;
    this.user.active = true;
    this.frontEndSupport.transfer(validationResult.errors);
  }
}
