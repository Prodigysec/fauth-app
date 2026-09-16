package io.fusionauth.app.action.admin.user;

import com.google.inject.Inject;
import com.inversoft.cache.Cache;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.user.CustomFormFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.SendSetPasswordIdentityType;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.UserRequest;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
@Redirect(code = "success", uri = "/admin/user/manage/${user.id}?tenantId=${user.tenantId}")
public class AddAction extends BaseFormAction {
  public SendSetPasswordIdentityType sendSetPasswordIdentityType = SendSetPasswordIdentityType.doNotSend;
  
  public boolean skipVerification;
  
  @Inject
  public AddAction(CustomFormFrontendService paramCustomFormFrontendService, FrontEndSupport paramFrontEndSupport, Cache<UUID, CachedTheme> paramCache) {
    super(paramCustomFormFrontendService, paramFrontEndSupport, paramCache);
  }
  
  public String get() {
    this.sendSetPasswordIdentityType = SendSetPasswordIdentityType.email;
    return "input";
  }
  
  public String post() {
    UserRequest userRequest = (new UserRequest()).with(paramUserRequest -> paramUserRequest.disableDomainBlock = true).with(paramUserRequest -> paramUserRequest.eventInfo = this.frontEndSupport.buildEventInfo(null)).with(paramUserRequest -> paramUserRequest.sendSetPasswordIdentityType = this.sendSetPasswordIdentityType).with(paramUserRequest -> paramUserRequest.skipVerification = (hasRole(new String[] { "admin", "user_manager" }) && this.skipVerification)).with(paramUserRequest -> paramUserRequest.user = this.user);
    this.user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.createUser(null, paramUserRequest))).user;
    writeAuditLog("Created user with Id [" + String.valueOf(this.user.id) + "], name [" + this.user.getName() + "] and loginId [" + this.user.getLogin() + "]");
    this.delegate.execute(FusionAuthClient::refreshUserSearchIndex);
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    Validator validator = (new Validator()).ifTrue(doesNotHaveRole(new String[] { "admin", "user_manager" }, ), paramValidator -> paramValidator.ensureWithCode((this.sendSetPasswordIdentityType != SendSetPasswordIdentityType.doNotSend), "sendSetPasswordIdentityType", "[unauthorized]sendSetPasswordIdentityType", new Object[0])).ifTrue((this.tenants.size() > 1), paramValidator -> paramValidator.notBlank(this.tenantId, "tenantId", new Object[0]));
    CustomFormFrontendService.EditPasswordOption editPasswordOption = (this.sendSetPasswordIdentityType != SendSetPasswordIdentityType.doNotSend) ? CustomFormFrontendService.EditPasswordOption.useExisting : CustomFormFrontendService.EditPasswordOption.update;
    String str = this.user.legacyIdentifier;
    CustomFormFrontendService.ValidationResult<User> validationResult = this.customFormFrontendService.normalizeAndValidate(this.tenants.get(this.tenantId), this.user, this.confirm.user, this.codeCurrentUser, editPasswordOption);
    this.user = (User)validationResult.result;
    if (this.frontEndSupport.configuration.allowSubClaimOverride() && ReactorStatusValidator.isLicensedFor(this.reactorStatus, paramReactorStatus -> paramReactorStatus.legacyAdapter))
      this.user.legacyIdentifier = str; 
    validator.withErrors(validationResult.errors);
    this.frontEndSupport.transfer(validator.done());
  }
}
