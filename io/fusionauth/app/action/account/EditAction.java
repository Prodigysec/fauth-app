package io.fusionauth.app.action.account;

import com.google.inject.Inject;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.app.service.user.CustomFormFrontendService;
import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.PasswordValidationRules;
import io.fusionauth.domain.User;
import io.fusionauth.domain.form.FormField;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;

@Action(requiresAuthentication = true, scheme = {"account-user"})
public class EditAction extends BaseAccountAction {
  @FTLVariable
  public static final SortedSet<String> timezones = new TreeSet<>();
  
  private static final String SelfServiceReason = "FusionAuth Self Service";
  
  private final CustomFormFrontendService customFormFrontendService;
  
  public BaseAccountAction.AccountConfirm confirm = new BaseAccountAction.AccountConfirm();
  
  public Map<UUID, List<String>> consents = new HashMap<>();
  
  public String currentPassword;
  
  public CustomFormFrontendService.EditPasswordOption editPasswordOption = CustomFormFrontendService.EditPasswordOption.useExisting;
  
  @FTLVariable
  public Map<Integer, List<FormField>> fields = new HashMap<>();
  
  public PasswordValidationRules passwordValidationRules;
  
  @Inject
  public EditAction(CustomFormFrontendService paramCustomFormFrontendService, FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.customFormFrontendService = paramCustomFormFrontendService;
  }
  
  public String get() {
    this.registration = this.codeUser.getRegistrationForApplication(this.codeApplication.id);
    return "input";
  }
  
  public String post() {
    this.user.id = this.codeUser.id;
    this.registration.applicationId = this.codeApplication.id;
    CustomFormFrontendService.UpdateResult updateResult = this.customFormFrontendService.updateSelfServiceUser(this.delegate, this.tenant, this.fields, this.user, this.registration, this.consents, this.currentPassword, this.frontEndSupport.buildEventInfo(null));
    if (updateResult.emailVerificationId != null || updateResult.phoneVerificationId != null)
      allowConfirmationBypass(); 
    if (updateResult.user != null) {
      this.user = updateResult.user;
      this.frontEndSupport.writeAuditLog(this.frontEndSupport.fusionAuthClientProvider.get(), (new AuditLog()).with(paramAuditLog -> paramAuditLog.insertUser = this.codeUser.getLogin())
          .with(paramAuditLog -> paramAuditLog.oldValue = paramUpdateResult.oldUser)
          .with(paramAuditLog -> paramAuditLog.newValue = paramUpdateResult.user)
          .with(paramAuditLog -> paramAuditLog.message = "User with Id [" + String.valueOf(this.codeUser.id) + "] and loginId [" + paramUpdateResult.oldUser.getLogin() + "] performed a self-update")
          .with(paramAuditLog -> paramAuditLog.reason = "FusionAuth Self Service"));
    } 
    if (updateResult.registration != null) {
      this.registration = updateResult.registration;
      this.frontEndSupport.writeAuditLog(this.frontEndSupport.fusionAuthClientProvider.get(), (new AuditLog()).with(paramAuditLog -> paramAuditLog.insertUser = this.codeUser.getLogin())
          .with(paramAuditLog -> paramAuditLog.oldValue = paramUpdateResult.oldRegistration)
          .with(paramAuditLog -> paramAuditLog.newValue = paramUpdateResult.registration)
          .with(paramAuditLog -> paramAuditLog.message = "User with Id [" + String.valueOf(this.codeUser.id) + "] self-updated registration with Id [" + String.valueOf(paramUpdateResult.registration.id) + "] for application with Id [" + String.valueOf(paramUpdateResult.registration.applicationId) + "]")
          .with(paramAuditLog -> paramAuditLog.reason = "FusionAuth Self Service"));
    } 
    if (updateResult.user != null || updateResult.registration != null) {
      Locale locale = (updateResult.user != null) ? updateResult.user.lookupPreferredLanguage(this.codeApplication.id) : this.codeUser.lookupPreferredLanguage(this.codeApplication.id);
      this.frontEndSupport.localeProvider.set(locale);
    } 
    return "success";
  }
  
  @PostValidationMethod
  public void setupFields() {
    this.fields = this.customFormFrontendService.retrieveFieldsByFormIdFilterConsents(this.codeApplication.formConfiguration.selfServiceFormId, this.codeUser, this.codeTenant);
    if (this.frontEndSupport.isGET())
      this.consents = this.customFormFrontendService.retrieveUserConsents(this.codeUser); 
    this.passwordValidationRules = this.codeTenant.passwordValidationRules;
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    accountValidation();
    if (this.frontEndSupport.isPOST()) {
      CustomFormFrontendService.UserValidationResult userValidationResult = this.customFormFrontendService.normalizeAndValidateSelfService(this.codeTenant, this.codeApplication, this.user, this.confirm.user, this.registration, this.confirm.registration, this.consents, this.codeUser, this.editPasswordOption, this.currentPassword);
      this.user = userValidationResult.user;
      this.registration = userValidationResult.registration;
      transferErrors(userValidationResult.errors);
      if (userValidationResult.errors.size() > 0)
        this.user.secure(); 
    } 
  }
  
  protected User setupUserForFTL() {
    if (this.frontEndSupport.isGET())
      return super.setupUserForFTL(); 
    return this.user;
  }
}
