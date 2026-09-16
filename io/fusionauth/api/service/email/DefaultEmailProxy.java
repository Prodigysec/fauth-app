package io.fusionauth.api.service.email;

import com.google.inject.Inject;
import com.inversoft.json.ToString;
import freemarker.core.ParseException;
import freemarker.template.TemplateException;
import io.fusionauth.api.domain.api.reactor.BreachResult;
import io.fusionauth.api.service.TemplateHelper;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.UserConsent;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.email.EmailTemplate;
import io.fusionauth.domain.event.UserActionEvent;
import io.fusionauth.domain.event.UserActionPhase;
import io.fusionauth.domain.event.UserEmailUpdateEvent;
import io.fusionauth.domain.event.UserEmailVerifiedEvent;
import io.fusionauth.domain.event.UserLoginIdDuplicateOnCreateEvent;
import io.fusionauth.domain.event.UserLoginIdDuplicateOnUpdateEvent;
import io.fusionauth.domain.event.UserLoginNewDeviceEvent;
import io.fusionauth.domain.event.UserLoginSuspiciousEvent;
import io.fusionauth.domain.event.UserPasswordResetSuccessEvent;
import io.fusionauth.domain.event.UserPasswordUpdateEvent;
import io.fusionauth.domain.event.UserTwoFactorMethodAddEvent;
import io.fusionauth.domain.event.UserTwoFactorMethodRemoveEvent;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.primeframework.email.domain.EmailAddress;
import org.primeframework.email.domain.SendResult;
import org.primeframework.email.service.EmailService;
import org.primeframework.email.service.SendEmailBuilder;

public class DefaultEmailProxy implements EmailProxy {
  private final EmailService emailService;
  
  @Inject
  public DefaultEmailProxy(EmailService paramEmailService) {
    this.emailService = paramEmailService;
  }
  
  public void sendActionEmail(Tenant paramTenant, User paramUser, UserActionPhase paramUserActionPhase, UserAction paramUserAction, UserActionEvent paramUserActionEvent) {
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, null, paramUser);
    User user = (User)map.get("user");
    sendEmail(paramTenant, paramEmailService -> {
          switch (paramUserActionPhase) {
            case start:
            
            case modify:
            
            case cancel:
            
            default:
              break;
          } 
          SendEmailBuilder sendEmailBuilder = this.emailService.send(new Tenant(paramTenant), paramUserAction.endEmailTemplateId, paramUser.preferredLanguages);
          if (paramUserActionEvent != null)
            sendEmailBuilder = (SendEmailBuilder)sendEmailBuilder.withTemplateParameter("event", paramUserActionEvent); 
          return ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)sendEmailBuilder.to(new EmailAddress[] { new EmailAddress(paramUser.email, paramUser.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("action", paramUserAction)).withTemplateParameter("phase", paramUserActionPhase)).later();
        });
  }
  
  public void sendAdminTwoFactorRemove(Tenant paramTenant, User paramUser, TwoFactorMethod paramTwoFactorMethod) {
    UserIdentity userIdentity = paramUser.resolvePrimaryIdentity(IdentityType.email);
    if (userIdentity != null && !userIdentity.verificationRequired() && paramTenant.emailConfiguration.adminTwoFactorMethodRemoveEmailTemplateId != null) {
      Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, null, null);
      map.put("user", paramUser);
      sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramTenant.emailConfiguration.adminTwoFactorMethodRemoveEmailTemplateId, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUserIdentity.value, paramUser.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("method", (new TwoFactorMethod(paramTwoFactorMethod)).secure())).later());
    } 
  }
  
  public void sendBreachedPasswordWarning(Tenant paramTenant, Application paramApplication, User paramUser, BreachResult paramBreachResult) {
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
    User user = (User)map.get("user");
    sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramTenant.passwordValidationRules.breachDetection.notifyUserEmailTemplateId, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUser.email) })).withTemplateParameters(paramMap)).withTemplateParameter("breachResult", paramBreachResult)).later());
  }
  
  public void sendConfirmChildEmail(Tenant paramTenant, User paramUser1, User paramUser2) {
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, null, paramUser1);
    User user = (User)map.get("user");
    sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramTenant.familyConfiguration.confirmChildEmailTemplateId, paramUser1.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUser1.email) })).withTemplateParameters(paramMap)).withTemplateParameter("child", (new User(paramUser2)).secure().sort())).withTemplateParameter("parent", paramUser1)).later());
  }
  
  public void sendConsentNotification(Tenant paramTenant, UserConsent paramUserConsent, User paramUser) {
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, null, paramUser);
    User user = (User)map.get("user");
    sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUserConsent.consent.consentEmailTemplateId, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUser.email, paramUser.getName()) })).withTemplateParameters(paramMap)).later());
  }
  
  public SendResult sendEmail(Tenant paramTenant, Function<EmailService, SendResult> paramFunction) {
    SendResult sendResult = paramFunction.apply(this.emailService);
    if (sendResult.wasSuccessful())
      return sendResult; 
    StringBuilder stringBuilder = new StringBuilder("Email send failure. See reasons below.\n\n");
    if (!sendResult.parseErrors.isEmpty()) {
      stringBuilder.append("Parse Errors\n---------------\n");
      sendResult.parseErrors.forEach((paramString, paramParseException) -> paramStringBuilder.append(paramString).append(" : ").append(paramParseException).append("\n\n"));
    } 
    if (!sendResult.renderErrors.isEmpty()) {
      stringBuilder.append("Render Errors\n---------------\n");
      sendResult.renderErrors.forEach((paramString, paramTemplateException) -> paramStringBuilder.append(paramString).append(" : ").append(paramTemplateException).append("\n\n"));
    } 
    if (sendResult.transportError != null) {
      stringBuilder.append("Transport Error\n---------------\n");
      stringBuilder.append(sendResult.transportError);
    } 
    String str = sendResult.email.to.stream().map(paramEmailAddress -> paramEmailAddress.address).collect(Collectors.joining(";"));
    EventLogHelper.create(new EventLog(EventLogType.Error, "An error occurred while sending an email to [" + str + "].\n\nEmail template Id: " + String.valueOf(sendResult.templateId) + "\nEmail template name: " + (
          
          (sendResult.template == null) ? "-" : ((EmailTemplate)sendResult.template).name) + "\nTenantId: " + String.valueOf(paramTenant.id) + "\n\nThe following error was returned:\n" + String.valueOf(stringBuilder) + "\n\nBelow is the email request in JSON:\n" + 

          
          ToString.toString(sendResult.email)));
    return sendResult;
  }
  
  public boolean sendEmailPlusNotification(Tenant paramTenant, User paramUser, UserConsent paramUserConsent) {
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, null, paramUser);
    User user = (User)map.get("user");
    SendResult sendResult = sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUserConsent.consent.emailPlus.emailTemplateId, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUser.email, paramUser.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("consent", paramUserConsent)).now());
    return sendResult.wasSuccessful();
  }
  
  public void sendEmailVerificationEmail(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2, String paramString3, Map<String, Object> paramMap) {
    String str = (paramUser != null) ? paramUser.getName() : null;
    UUID uUID = (paramApplication != null && paramApplication.emailConfiguration.emailVerificationEmailTemplateId != null) ? paramApplication.emailConfiguration.emailVerificationEmailTemplateId : paramTenant.emailConfiguration.verificationEmailTemplateId;
    Application application = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> (new Application(paramApplication)).secure()).orElse(null);
    User user = Optional.<User>ofNullable(paramUser).map(paramUser -> (new User(paramUser)).secure().sort()).orElse((new User()).with(paramUser -> paramUser.tenantId = paramTenant.id));
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, application, user);
    List<Locale> list = TemplateHelper.getPreferredLanguages(user, application);
    sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramList).to(new EmailAddress[] { new EmailAddress(paramString1, paramString2) })).withTemplateParameters(paramMap1)).withOptionalTemplateParameter("state", paramMap2)).withTemplateParameter("verificationId", paramString3)).withTemplateParameter("verificationOneTimeCode", paramString4)).later());
  }
  
  public void sendFamilyRequest(Tenant paramTenant, String paramString, User paramUser) {
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, null, paramUser);
    User user = (User)map.get("user");
    sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramTenant.familyConfiguration.familyRequestEmailTemplateId, (paramUser != null) ? paramUser.preferredLanguages : Collections.emptyList()).to(new String[] { paramString })).withTemplateParameters(paramMap)).withTemplateParameter("parent", paramUser)).later());
  }
  
  public void sendForgotPasswordEmail(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2, Map<String, Object> paramMap) {
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
    User user = (User)map.get("user");
    UUID uUID = (paramApplication != null && paramApplication.emailConfiguration.forgotPasswordEmailTemplateId != null) ? paramApplication.emailConfiguration.forgotPasswordEmailTemplateId : paramTenant.emailConfiguration.forgotPasswordEmailTemplateId;
    sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramString1, paramUser.getName()) })).withTemplateParameters(paramMap1)).withTemplateParameter("changePasswordId", paramString2)).withTemplateParameter("state", paramMap2)).later());
  }
  
  public void sendLoginIdDuplicateOnCreate(Tenant paramTenant, Application paramApplication, User paramUser, UserLoginIdDuplicateOnCreateEvent paramUserLoginIdDuplicateOnCreateEvent) {
    if (paramUserLoginIdDuplicateOnCreateEvent.existing.email != null) {
      Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
      User user = (User)map.get("user");
      UUID uUID = (paramApplication != null && paramApplication.emailConfiguration.loginIdInUseOnCreateEmailTemplateId != null) ? paramApplication.emailConfiguration.loginIdInUseOnCreateEmailTemplateId : paramTenant.emailConfiguration.loginIdInUseOnCreateEmailTemplateId;
      sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUserLoginIdDuplicateOnCreateEvent.existing.email, paramUserLoginIdDuplicateOnCreateEvent.existing.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("event", paramUserLoginIdDuplicateOnCreateEvent)).later());
    } 
  }
  
  public void sendLoginIdDuplicateOnUpdate(Tenant paramTenant, Application paramApplication, User paramUser, UserLoginIdDuplicateOnUpdateEvent paramUserLoginIdDuplicateOnUpdateEvent) {
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
    User user = (User)map.get("user");
    UUID uUID = (paramApplication != null && paramApplication.emailConfiguration.loginIdInUseOnUpdateEmailTemplateId != null) ? paramApplication.emailConfiguration.loginIdInUseOnUpdateEmailTemplateId : paramTenant.emailConfiguration.loginIdInUseOnUpdateEmailTemplateId;
    sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUserLoginIdDuplicateOnUpdateEvent.existing.email, paramUserLoginIdDuplicateOnUpdateEvent.existing.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("event", paramUserLoginIdDuplicateOnUpdateEvent)).later());
  }
  
  public void sendParentRegistrationRequestEmail(Tenant paramTenant, String paramString, User paramUser) {
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, null, paramUser);
    User user = (User)map.get("user");
    sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramTenant.familyConfiguration.parentRegistrationEmailTemplateId, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramString) })).withTemplateParameters(paramMap)).withTemplateParameter("child", paramUser)).later());
  }
  
  public void sendPasswordless(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, Map<String, Object> paramMap, String paramString2) {
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
    User user = (User)map.get("user");
    UUID uUID = (paramApplication != null && paramApplication.emailConfiguration.passwordlessEmailTemplateId != null) ? paramApplication.emailConfiguration.passwordlessEmailTemplateId : paramTenant.emailConfiguration.passwordlessEmailTemplateId;
    sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUser.lookupEmail(), paramUser.getName()) })).withTemplateParameters(paramMap1)).withTemplateParameter("code", paramString1)).withTemplateParameter("state", paramMap2)).withTemplateParameter("oneTimeCode", paramString2)).later());
  }
  
  public void sendSetupPasswordEmail(Tenant paramTenant, Application paramApplication, User paramUser, String paramString) {
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
    User user = (User)map.get("user");
    UUID uUID = (paramApplication != null && paramApplication.emailConfiguration.setPasswordEmailTemplateId != null) ? paramApplication.emailConfiguration.setPasswordEmailTemplateId : paramTenant.emailConfiguration.setPasswordEmailTemplateId;
    sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUser.email, paramUser.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("changePasswordId", paramString)).later());
  }
  
  public void sendTwoFactorCode(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2) {
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
    User user = (User)map.get("user");
    UUID uUID = (paramApplication != null && paramApplication.multiFactorConfiguration.email.templateId != null) ? paramApplication.multiFactorConfiguration.email.templateId : paramTenant.multiFactorConfiguration.email.templateId;
    sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramString1, paramUser.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("code", paramString2)).later());
  }
  
  public void sendUserEmailUpdate(Tenant paramTenant, Application paramApplication, User paramUser, UserEmailUpdateEvent paramUserEmailUpdateEvent) {
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
    User user = (User)map.get("user");
    UUID uUID = (paramApplication != null && paramApplication.emailConfiguration.emailUpdateEmailTemplateId != null) ? paramApplication.emailConfiguration.emailUpdateEmailTemplateId : paramTenant.emailConfiguration.emailUpdateEmailTemplateId;
    if (paramUserEmailUpdateEvent.previousEmail != null)
      sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUserEmailUpdateEvent.previousEmail, paramUser.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("event", paramUserEmailUpdateEvent)).withTemplateParameter("previousEmail", paramUserEmailUpdateEvent.previousEmail)).later()); 
    if (paramUser.lookupEmail() != null)
      sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUser.lookupEmail(), paramUser.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("event", paramUserEmailUpdateEvent)).withTemplateParameter("previousEmail", paramUserEmailUpdateEvent.previousEmail)).later()); 
  }
  
  public void sendUserEmailVerified(Tenant paramTenant, Application paramApplication, User paramUser, UserEmailVerifiedEvent paramUserEmailVerifiedEvent) {
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
    User user = (User)map.get("user");
    UUID uUID = (paramApplication != null && paramApplication.emailConfiguration.emailVerifiedEmailTemplateId != null) ? paramApplication.emailConfiguration.emailVerifiedEmailTemplateId : paramTenant.emailConfiguration.emailVerifiedEmailTemplateId;
    sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUser.email, paramUser.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("event", paramUserEmailVerifiedEvent)).later());
  }
  
  public void sendUserLoginNewDevice(Tenant paramTenant, Application paramApplication, User paramUser, UserLoginNewDeviceEvent paramUserLoginNewDeviceEvent) {
    if (paramUser.lookupEmail() != null) {
      Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
      User user = (User)map.get("user");
      UUID uUID = (paramApplication != null && paramApplication.emailConfiguration.loginNewDeviceEmailTemplateId != null) ? paramApplication.emailConfiguration.loginNewDeviceEmailTemplateId : paramTenant.emailConfiguration.loginNewDeviceEmailTemplateId;
      sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUser.lookupEmail(), paramUser.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("event", paramUserLoginNewDeviceEvent)).later());
    } 
  }
  
  public void sendUserLoginSuspicious(Tenant paramTenant, Application paramApplication, User paramUser, UserLoginSuspiciousEvent paramUserLoginSuspiciousEvent) {
    if (paramUser.lookupEmail() != null) {
      Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
      User user = (User)map.get("user");
      UUID uUID = (paramApplication != null && paramApplication.emailConfiguration.loginSuspiciousEmailTemplateId != null) ? paramApplication.emailConfiguration.loginSuspiciousEmailTemplateId : paramTenant.emailConfiguration.loginSuspiciousEmailTemplateId;
      sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUser.lookupEmail(), paramUser.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("event", paramUserLoginSuspiciousEvent)).later());
    } 
  }
  
  public void sendUserPasswordResetSuccess(Tenant paramTenant, Application paramApplication, User paramUser, UserPasswordResetSuccessEvent paramUserPasswordResetSuccessEvent) {
    if (paramUser.lookupEmail() != null) {
      Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
      User user = (User)map.get("user");
      UUID uUID = (paramApplication != null && paramApplication.emailConfiguration.passwordResetSuccessEmailTemplateId != null) ? paramApplication.emailConfiguration.passwordResetSuccessEmailTemplateId : paramTenant.emailConfiguration.passwordResetSuccessEmailTemplateId;
      sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUser.lookupEmail(), paramUser.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("event", paramUserPasswordResetSuccessEvent)).later());
    } 
  }
  
  public void sendUserPasswordUpdate(Tenant paramTenant, Application paramApplication, User paramUser, UserPasswordUpdateEvent paramUserPasswordUpdateEvent) {
    if (paramUser.lookupEmail() != null) {
      Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
      User user = (User)map.get("user");
      UUID uUID = (paramApplication != null && paramApplication.emailConfiguration.passwordUpdateEmailTemplateId != null) ? paramApplication.emailConfiguration.passwordUpdateEmailTemplateId : paramTenant.emailConfiguration.passwordUpdateEmailTemplateId;
      sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUser.lookupEmail(), paramUser.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("event", paramUserPasswordUpdateEvent)).later());
    } 
  }
  
  public void sendUserTwoFactorAdd(Tenant paramTenant, Application paramApplication, User paramUser, UserTwoFactorMethodAddEvent paramUserTwoFactorMethodAddEvent) {
    if (paramUser.lookupEmail() != null) {
      Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
      User user = (User)map.get("user");
      UUID uUID = (paramApplication != null && paramApplication.emailConfiguration.twoFactorMethodAddEmailTemplateId != null) ? paramApplication.emailConfiguration.twoFactorMethodAddEmailTemplateId : paramTenant.emailConfiguration.twoFactorMethodAddEmailTemplateId;
      sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUser.lookupEmail(), paramUser.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("event", paramUserTwoFactorMethodAddEvent)).withTemplateParameter("method", (new TwoFactorMethod(paramUserTwoFactorMethodAddEvent.method)).secure())).later());
    } 
  }
  
  public void sendUserTwoFactorRemove(Tenant paramTenant, Application paramApplication, User paramUser, UserTwoFactorMethodRemoveEvent paramUserTwoFactorMethodRemoveEvent) {
    if (paramUser.lookupEmail() != null) {
      Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
      User user = (User)map.get("user");
      UUID uUID = (paramApplication != null && paramApplication.emailConfiguration.twoFactorMethodRemoveEmailTemplateId != null) ? paramApplication.emailConfiguration.twoFactorMethodRemoveEmailTemplateId : paramTenant.emailConfiguration.twoFactorMethodRemoveEmailTemplateId;
      sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramUUID, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUser.lookupEmail(), paramUser.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("event", paramUserTwoFactorMethodRemoveEvent)).withTemplateParameter("method", (new TwoFactorMethod(paramUserTwoFactorMethodRemoveEvent.method)).secure())).later());
    } 
  }
  
  public void sendVerifyRegistrationEmail(Tenant paramTenant, Application paramApplication, UserRegistration paramUserRegistration, User paramUser, String paramString1, String paramString2) {
    Map<String, Object> map = TemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser);
    User user = (User)map.get("user");
    sendEmail(paramTenant, paramEmailService -> ((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)((SendEmailBuilder)paramEmailService.send(new Tenant(paramTenant), paramApplication.verificationEmailTemplateId, paramUser.preferredLanguages).to(new EmailAddress[] { new EmailAddress(paramUser.lookupEmail(), paramUser.getName()) })).withTemplateParameters(paramMap)).withTemplateParameter("registration", paramUserRegistration)).withTemplateParameter("verificationId", paramString1)).withTemplateParameter("verificationOneTimeCode", paramString2)).later());
  }
}
