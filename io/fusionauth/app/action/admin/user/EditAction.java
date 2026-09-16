package io.fusionauth.app.action.admin.user;

import com.google.inject.Inject;
import com.inversoft.cache.Cache;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.user.CustomFormFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.SendSetPasswordIdentityType;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.UserRequest;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.http.Cookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.scope.annotation.ManagedSessionCookie;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{userId}", requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
@List({@Redirect(code = "success", uri = "/admin/user/manage/${userId}?tenantId=${tenantId}"), @Redirect(code = "deactivated", uri = "/admin/user/")})
public class EditAction extends BaseFormAction {
  @ManagedSessionCookie(name = "fa.bypass-c", encrypt = false)
  public Cookie confirmationBypassCookie;
  
  public CustomFormFrontendService.EditPasswordOption editPasswordOption = CustomFormFrontendService.EditPasswordOption.useExisting;
  
  @FTLVariable
  public List<CustomFormFrontendService.EditPasswordOption> editPasswordOptions = new ArrayList<>(
      List.of(CustomFormFrontendService.EditPasswordOption.useExisting));
  
  public boolean skipVerification;
  
  public UUID userId;
  
  @Inject
  public EditAction(CustomFormFrontendService paramCustomFormFrontendService, FrontEndSupport paramFrontEndSupport, Cache<UUID, CachedTheme> paramCache) {
    super(paramCustomFormFrontendService, paramFrontEndSupport, paramCache);
  }
  
  public String get() {
    this.user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(this.userId))).user;
    if (!this.user.active) {
      this.frontEndSupport.addGeneralError("[deactivated]", new Object[] { this.user.id });
      return "deactivated";
    } 
    populateEditPasswordOptions();
    return "input";
  }
  
  public String post() {
    User user1 = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(this.userId))).user;
    UserRequest userRequest = (new UserRequest()).with(paramUserRequest -> paramUserRequest.disableDomainBlock = true).with(paramUserRequest -> paramUserRequest.eventInfo = this.frontEndSupport.buildEventInfo(null)).with(paramUserRequest -> paramUserRequest.sendSetPasswordIdentityType = SendSetPasswordIdentityType.doNotSend).with(paramUserRequest -> paramUserRequest.user = this.user);
    if (hasRole(new String[] { "admin", "user_manager" }) && this.skipVerification)
      userRequest.with(paramUserRequest -> paramUserRequest.skipVerification = true); 
    this.user.encryptionScheme = null;
    this.user.factor = null;
    UserResponse userResponse = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateUser(this.userId, paramUserRequest));
    User user2 = userResponse.user;
    writeAuditLogForUpdate("Updated the user with Id [" + String.valueOf(this.userId) + "] and loginId [" + user2.getLogin() + "]", user1, user2);
    if (this.userId.equals(this.codeCurrentUser.id)) {
      Locale locale = this.user.lookupPreferredLanguage(Application.FUSIONAUTH_APP_ID);
      this.frontEndSupport.localeProvider.set(locale);
      if (userResponse.emailVerificationId != null)
        this.confirmationBypassCookie.value = "a"; 
    } 
    return "success";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    this.user.id = this.userId;
    this.user.tenantId = this.tenantId;
    CustomFormFrontendService.ValidationResult<User> validationResult = this.customFormFrontendService.normalizeAndValidate(this.tenants.get(this.tenantId), this.user, this.confirm.user, this.codeCurrentUser, this.editPasswordOption);
    this.user = (User)validationResult.result;
    populateEditPasswordOptions();
    this.frontEndSupport.transfer(validationResult.errors);
  }
  
  private void populateEditPasswordOptions() {
    if (this.user.passwordLastUpdateInstant != null)
      this.editPasswordOptions.add(CustomFormFrontendService.EditPasswordOption.requireChange); 
    if (hasRole(new String[] { "admin", "user_manager" }))
      this.editPasswordOptions.add(CustomFormFrontendService.EditPasswordOption.update); 
  }
}
