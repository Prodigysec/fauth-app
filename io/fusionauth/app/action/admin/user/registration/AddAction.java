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
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.ContentStatus;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.user.RegistrationRequest;
import io.fusionauth.domain.api.user.RegistrationResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{userId}", requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
@Redirect(code = "success", uri = "/admin/user/manage/${userId}?tenantId=${tenantId}")
public class AddAction extends BaseRegistrationAction {
  public List<Application> applications = new ArrayList<>();
  
  public UserRegistration registration = new UserRegistration();
  
  public UUID userId;
  
  @Inject
  public AddAction(CustomFormFrontendService paramCustomFormFrontendService, FrontEndSupport paramFrontEndSupport, Cache<UUID, CachedTheme> paramCache) {
    super(paramCustomFormFrontendService, paramFrontEndSupport, paramCache);
    paramFrontEndSupport.customPreTransferErrorConsumer = (paramErrors -> paramFrontEndSupport.moveFieldErrorToGeneral(paramErrors, "registration.applicationId", "[invalid]registration.applicationId"));
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    this.registration = ((RegistrationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.register(this.userId, new RegistrationRequest(this.frontEndSupport.buildEventInfo(null), null, this.registration)))).registration;
    writeAuditLog("Created user registration with Id [" + String.valueOf(this.registration.id) + "] for user with Id [" + String.valueOf(this.userId) + "] and application with Id [" + String.valueOf(this.registration.applicationId) + "]");
    if (this.registration.usernameStatus == ContentStatus.PENDING)
      this.frontEndSupport.addGeneralInfo("username-pending", new Object[0]); 
    return "success";
  }
  
  @FormPrepareMethod
  public void setup() {
    this.user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(this.userId))).user;
    if (this.frontEndSupport.isGET() && 
      this.applicationId == null) {
      retrieveAvailableApplicationsForRegistration();
      if (this.applications.size() > 1)
        return; 
    } 
    if (this.applicationId != null) {
      this.application = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveApplication(this.applicationId))).application;
      this.registration.applicationId = this.applicationId;
    } 
    if (this.application != null) {
      this.fields = this.customFormFrontendService.retrieveFieldsByFormId(this.application.formConfiguration.adminRegistrationFormId);
      sortRoles();
      setRegistrationRolesField();
      this.canEditRoles = this.customFormFrontendService.checkIfUserCanEditRoles(this.applicationId, this.codeCurrentUser, this.userId);
      if (!this.canEditRoles)
        this.application.roles.stream().filter(paramApplicationRole -> paramApplicationRole.isDefault).forEach(paramApplicationRole -> this.registration.roles.add(paramApplicationRole.name)); 
    } 
    if (this.applicationId == null)
      retrieveAvailableApplicationsForRegistration(); 
    loadTheme();
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.applicationId != null && 
      Application.FUSIONAUTH_APP_ID.equals(this.applicationId) && 
      doesNotHaveRole(new String[] { "admin", "user_manager" })) {
      this.applicationId = null;
      this.frontEndSupport.addFieldError("registration.applicationId", "[unauthorized]registration.applicationId", new Object[0]);
    } 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    CustomFormFrontendService.ValidationResult<UserRegistration> validationResult = this.customFormFrontendService.normalizeAndValidateAdd(this.tenants.get(this.tenantId), this.registration, this.confirm.registration, this.codeCurrentUser, this.userId);
    this.registration = (UserRegistration)validationResult.result;
    this.frontEndSupport.transfer(validationResult.errors);
  }
  
  private void retrieveAvailableApplicationsForRegistration() {
    this.applications = (List<Application>)Objects.requireNonNullElseGet(((ApplicationResponse)this.delegate.execute(FusionAuthClient::retrieveApplications)).applications, Collections::emptyList);
    this.applications.removeIf(paramApplication -> (this.user.getRegistrationForApplication(paramApplication.id) != null));
    this.applications.sort(Comparator.comparing(paramApplication -> paramApplication.name));
    if (this.applications.size() == 1 && (
      hasRole(new String[] { "admin", "user_manager" }) || !((Application)this.applications.get(0)).id.equals(Application.FUSIONAUTH_APP_ID))) {
      this.application = this.applications.get(0);
      this.applicationId = this.application.id;
    } 
  }
}
