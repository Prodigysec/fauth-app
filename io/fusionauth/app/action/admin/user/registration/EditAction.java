package io.fusionauth.app.action.admin.user.registration;

import com.google.inject.Inject;
import com.inversoft.cache.Cache;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.user.CustomFormFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.user.RegistrationRequest;
import io.fusionauth.domain.api.user.RegistrationResponse;
import java.util.Locale;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, value = "{userId}/{registration.applicationId}", constraints = {"admin", "user_manager", "user_support_manager"})
@Redirect(code = "success", uri = "/admin/user/manage/${userId}?tenantId=${tenantId}")
public class EditAction extends BaseRegistrationAction {
  public UserRegistration registration = new UserRegistration();
  
  public UUID userId;
  
  @Inject
  public EditAction(CustomFormFrontendService paramCustomFormFrontendService, FrontEndSupport paramFrontEndSupport, Cache<UUID, CachedTheme> paramCache) {
    super(paramCustomFormFrontendService, paramFrontEndSupport, paramCache);
    paramFrontEndSupport.customPreTransferErrorConsumer = (paramErrors -> paramFrontEndSupport.moveFieldErrorToGeneral(paramErrors, "registration.applicationId", "[unauthorized]registration.applicationId"));
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    UserRegistration userRegistration1 = ((RegistrationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveRegistration(this.userId, this.registration.applicationId))).registration;
    UserRegistration userRegistration2 = ((RegistrationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateRegistration(this.userId, new RegistrationRequest(this.frontEndSupport.buildEventInfo(null), null, this.registration)))).registration;
    writeAuditLogForUpdate("Updated user registration with Id [" + String.valueOf(this.registration.id) + "] for user with Id [" + String.valueOf(this.userId) + "] and application with Id [" + String.valueOf(this.registration.applicationId) + "]", userRegistration1, userRegistration2);
    if (this.userId.equals(this.codeCurrentUser.id) && this.registration.applicationId.equals(Application.FUSIONAUTH_APP_ID)) {
      Locale locale = (new User(this.codeCurrentUser)).with(paramUser -> paramUser.getRegistrations().removeIf(())).with(paramUser -> paramUser.getRegistrations().add(paramUserRegistration)).lookupPreferredLanguage(Application.FUSIONAUTH_APP_ID);
      this.frontEndSupport.localeProvider.set(locale);
    } 
    return "success";
  }
  
  @FormPrepareMethod
  public void prepare() {
    this.user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(this.userId))).user;
    this.registration = this.user.getRegistrationForApplication(this.registration.applicationId);
    checkForGroupManagedRoles(this.user, this.registration);
    this.canEditRoles = this.customFormFrontendService.checkIfUserCanEditRoles(this.registration.applicationId, this.codeCurrentUser, this.userId);
    this.application = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveApplication(this.registration.applicationId))).application;
    this.applicationId = this.application.id;
    this.fields = this.customFormFrontendService.retrieveFieldsByFormId(this.application.formConfiguration.adminRegistrationFormId);
    sortRoles();
    setRegistrationRolesField();
    loadTheme();
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    CustomFormFrontendService.ValidationResult<UserRegistration> validationResult = this.customFormFrontendService.normalizeAndValidateEdit(this.tenants.get(this.tenantId), this.registration, this.confirm.registration, this.codeCurrentUser, this.userId);
    this.registration = (UserRegistration)validationResult.result;
    this.frontEndSupport.transfer(validationResult.errors);
  }
}
