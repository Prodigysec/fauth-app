package io.fusionauth.api.service.email;

import io.fusionauth.api.domain.api.reactor.BreachResult;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.UserConsent;
import io.fusionauth.domain.UserRegistration;
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
import java.util.Map;
import java.util.function.Function;
import org.primeframework.email.domain.SendResult;
import org.primeframework.email.service.EmailService;

public interface EmailProxy {
  void sendActionEmail(Tenant paramTenant, User paramUser, UserActionPhase paramUserActionPhase, UserAction paramUserAction, UserActionEvent paramUserActionEvent);
  
  void sendAdminTwoFactorRemove(Tenant paramTenant, User paramUser, TwoFactorMethod paramTwoFactorMethod);
  
  void sendBreachedPasswordWarning(Tenant paramTenant, Application paramApplication, User paramUser, BreachResult paramBreachResult);
  
  void sendConfirmChildEmail(Tenant paramTenant, User paramUser1, User paramUser2);
  
  void sendConsentNotification(Tenant paramTenant, UserConsent paramUserConsent, User paramUser);
  
  SendResult sendEmail(Tenant paramTenant, Function<EmailService, SendResult> paramFunction);
  
  boolean sendEmailPlusNotification(Tenant paramTenant, User paramUser, UserConsent paramUserConsent);
  
  void sendEmailVerificationEmail(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2, String paramString3, Map<String, Object> paramMap);
  
  void sendFamilyRequest(Tenant paramTenant, String paramString, User paramUser);
  
  void sendForgotPasswordEmail(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2, Map<String, Object> paramMap);
  
  void sendLoginIdDuplicateOnCreate(Tenant paramTenant, Application paramApplication, User paramUser, UserLoginIdDuplicateOnCreateEvent paramUserLoginIdDuplicateOnCreateEvent);
  
  void sendLoginIdDuplicateOnUpdate(Tenant paramTenant, Application paramApplication, User paramUser, UserLoginIdDuplicateOnUpdateEvent paramUserLoginIdDuplicateOnUpdateEvent);
  
  void sendParentRegistrationRequestEmail(Tenant paramTenant, String paramString, User paramUser);
  
  void sendPasswordless(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, Map<String, Object> paramMap, String paramString2);
  
  void sendSetupPasswordEmail(Tenant paramTenant, Application paramApplication, User paramUser, String paramString);
  
  void sendTwoFactorCode(Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, String paramString2);
  
  void sendUserEmailUpdate(Tenant paramTenant, Application paramApplication, User paramUser, UserEmailUpdateEvent paramUserEmailUpdateEvent);
  
  void sendUserEmailVerified(Tenant paramTenant, Application paramApplication, User paramUser, UserEmailVerifiedEvent paramUserEmailVerifiedEvent);
  
  void sendUserLoginNewDevice(Tenant paramTenant, Application paramApplication, User paramUser, UserLoginNewDeviceEvent paramUserLoginNewDeviceEvent);
  
  void sendUserLoginSuspicious(Tenant paramTenant, Application paramApplication, User paramUser, UserLoginSuspiciousEvent paramUserLoginSuspiciousEvent);
  
  void sendUserPasswordResetSuccess(Tenant paramTenant, Application paramApplication, User paramUser, UserPasswordResetSuccessEvent paramUserPasswordResetSuccessEvent);
  
  void sendUserPasswordUpdate(Tenant paramTenant, Application paramApplication, User paramUser, UserPasswordUpdateEvent paramUserPasswordUpdateEvent);
  
  void sendUserTwoFactorAdd(Tenant paramTenant, Application paramApplication, User paramUser, UserTwoFactorMethodAddEvent paramUserTwoFactorMethodAddEvent);
  
  void sendUserTwoFactorRemove(Tenant paramTenant, Application paramApplication, User paramUser, UserTwoFactorMethodRemoveEvent paramUserTwoFactorMethodRemoveEvent);
  
  void sendVerifyRegistrationEmail(Tenant paramTenant, Application paramApplication, UserRegistration paramUserRegistration, User paramUser, String paramString1, String paramString2);
}
