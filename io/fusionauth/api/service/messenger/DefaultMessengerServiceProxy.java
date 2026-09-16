package io.fusionauth.api.service.messenger;

import com.google.inject.Inject;
import io.fusionauth.api.service.TemplateHelper;
import io.fusionauth.api.service.message.MessageTemplateHelper;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.event.BaseUserEvent;
import io.fusionauth.domain.event.UserIdentityUpdateEvent;
import io.fusionauth.domain.event.UserIdentityVerifiedEvent;
import io.fusionauth.domain.event.UserLoginIdDuplicateOnCreateEvent;
import io.fusionauth.domain.event.UserLoginIdDuplicateOnUpdateEvent;
import io.fusionauth.domain.event.UserLoginNewDeviceEvent;
import io.fusionauth.domain.event.UserLoginSuspiciousEvent;
import io.fusionauth.domain.event.UserPasswordResetSuccessEvent;
import io.fusionauth.domain.event.UserPasswordUpdateEvent;
import io.fusionauth.domain.event.UserTwoFactorMethodAddEvent;
import io.fusionauth.domain.event.UserTwoFactorMethodRemoveEvent;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;

public class DefaultMessengerServiceProxy implements MessengerServiceProxy {
  private final MessengerService messengerService;
  
  @Inject
  public DefaultMessengerServiceProxy(MessengerService paramMessengerService) {
    this.messengerService = paramMessengerService;
  }
  
  public void sendAdminTwoFactorMethodRemove(Tenant paramTenant, @Nonnull User paramUser, TwoFactorMethod paramTwoFactorMethod) {
    UUID uUID1 = paramTenant.phoneConfiguration.messengerId;
    UUID uUID2 = paramTenant.phoneConfiguration.adminTwoFactorMethodRemoveTemplateId;
    UserIdentity userIdentity = paramUser.resolvePrimaryIdentity(IdentityType.phoneNumber);
    if (uUID1 == null || userIdentity == null || userIdentity.verificationRequired() || uUID2 == null)
      return; 
    Map<String, Object> map = MessageTemplateHelper.getBaseParameters(paramTenant, null, paramUser, userIdentity.value);
    map.put("method", (new TwoFactorMethod(paramTwoFactorMethod)).secure());
    Locale locale = TemplateHelper.getPreferredLanguage(paramUser, null);
    this.messengerService.send(uUID2, uUID1, locale, map);
  }
  
  public void sendUserIdentityUpdateEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserIdentityUpdateEvent paramUserIdentityUpdateEvent) {
    UUID uUID1 = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> paramApplication.phoneConfiguration.identityUpdateTemplateId).orElse(paramTenant.phoneConfiguration.identityUpdateTemplateId);
    UUID uUID2 = paramTenant.phoneConfiguration.messengerId;
    if (uUID2 == null || uUID1 == null || paramUserIdentityUpdateEvent.previousLoginId == null || !IdentityType.phoneNumber.is(paramUserIdentityUpdateEvent.loginIdType))
      return; 
    Map<String, Object> map = MessageTemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser, paramUserIdentityUpdateEvent.previousLoginId);
    map.put("event", paramUserIdentityUpdateEvent);
    Locale locale = TemplateHelper.getPreferredLanguage(paramUserIdentityUpdateEvent.user, paramApplication);
    this.messengerService.send(uUID1, uUID2, locale, map);
    if (paramUserIdentityUpdateEvent.newLoginId == null)
      return; 
    map = MessageTemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser, paramUserIdentityUpdateEvent.newLoginId);
    map.put("event", paramUserIdentityUpdateEvent);
    this.messengerService.send(uUID1, uUID2, locale, map);
  }
  
  public void sendUserIdentityVerifiedEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserIdentityVerifiedEvent paramUserIdentityVerifiedEvent) {
    if (!IdentityType.phoneNumber.is(paramUserIdentityVerifiedEvent.loginIdType))
      return; 
    UUID uUID = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> paramApplication.phoneConfiguration.verificationCompleteTemplateId).orElse(paramTenant.phoneConfiguration.verificationCompleteTemplateId);
    sendUserEvent(paramTenant, paramApplication, paramUser, paramUserIdentityVerifiedEvent, uUID);
  }
  
  public void sendUserLoginIdDuplicateOnCreate(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserLoginIdDuplicateOnCreateEvent paramUserLoginIdDuplicateOnCreateEvent) {
    UUID uUID = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> paramApplication.phoneConfiguration.loginIdInUseOnCreateTemplateId).orElse(paramTenant.phoneConfiguration.loginIdInUseOnCreateTemplateId);
    sendLoginIdDuplicateEvent(paramTenant, paramApplication, paramUser, paramUserLoginIdDuplicateOnCreateEvent, uUID);
  }
  
  public void sendUserLoginIdDuplicateOnUpdate(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserLoginIdDuplicateOnUpdateEvent paramUserLoginIdDuplicateOnUpdateEvent) {
    UUID uUID = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> paramApplication.phoneConfiguration.loginIdInUseOnUpdateTemplateId).orElse(paramTenant.phoneConfiguration.loginIdInUseOnUpdateTemplateId);
    sendLoginIdDuplicateEvent(paramTenant, paramApplication, paramUser, paramUserLoginIdDuplicateOnUpdateEvent, uUID);
  }
  
  public void sendUserLoginNewDeviceEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserLoginNewDeviceEvent paramUserLoginNewDeviceEvent) {
    UUID uUID = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> paramApplication.phoneConfiguration.loginNewDeviceTemplateId).orElse(paramTenant.phoneConfiguration.loginNewDeviceTemplateId);
    sendUserEvent(paramTenant, paramApplication, paramUser, paramUserLoginNewDeviceEvent, uUID);
  }
  
  public void sendUserLoginSuspiciousEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserLoginSuspiciousEvent paramUserLoginSuspiciousEvent) {
    UUID uUID = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> paramApplication.phoneConfiguration.loginSuspiciousTemplateId).orElse(paramTenant.phoneConfiguration.loginSuspiciousTemplateId);
    sendUserEvent(paramTenant, paramApplication, paramUser, paramUserLoginSuspiciousEvent, uUID);
  }
  
  public void sendUserPasswordResetSuccessEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserPasswordResetSuccessEvent paramUserPasswordResetSuccessEvent) {
    UUID uUID = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> paramApplication.phoneConfiguration.passwordResetSuccessTemplateId).orElse(paramTenant.phoneConfiguration.passwordResetSuccessTemplateId);
    sendUserEvent(paramTenant, paramApplication, paramUser, paramUserPasswordResetSuccessEvent, uUID);
  }
  
  public void sendUserPasswordUpdateEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserPasswordUpdateEvent paramUserPasswordUpdateEvent) {
    UUID uUID = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> paramApplication.phoneConfiguration.passwordUpdateTemplateId).orElse(paramTenant.phoneConfiguration.passwordUpdateTemplateId);
    sendUserEvent(paramTenant, paramApplication, paramUser, paramUserPasswordUpdateEvent, uUID);
  }
  
  public void sendUserTwoFactorMethodAddEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserTwoFactorMethodAddEvent paramUserTwoFactorMethodAddEvent) {
    UUID uUID = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> paramApplication.phoneConfiguration.twoFactorMethodAddTemplateId).orElse(paramTenant.phoneConfiguration.twoFactorMethodAddTemplateId);
    sendTwoFactorMethodEvent(paramTenant, paramApplication, paramUser, paramUserTwoFactorMethodAddEvent, paramUserTwoFactorMethodAddEvent.method, uUID);
  }
  
  public void sendUserTwoFactorMethodRemoveEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserTwoFactorMethodRemoveEvent paramUserTwoFactorMethodRemoveEvent) {
    UUID uUID = Optional.<Application>ofNullable(paramApplication).map(paramApplication -> paramApplication.phoneConfiguration.twoFactorMethodRemoveTemplateId).orElse(paramTenant.phoneConfiguration.twoFactorMethodRemoveTemplateId);
    sendTwoFactorMethodEvent(paramTenant, paramApplication, paramUser, paramUserTwoFactorMethodRemoveEvent, paramUserTwoFactorMethodRemoveEvent.method, uUID);
  }
  
  private void sendLoginIdDuplicateEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserLoginIdDuplicateOnCreateEvent paramUserLoginIdDuplicateOnCreateEvent, UUID paramUUID) {
    UUID uUID = paramTenant.phoneConfiguration.messengerId;
    if (uUID == null || paramUUID == null || paramUserLoginIdDuplicateOnCreateEvent.existing == null || paramUserLoginIdDuplicateOnCreateEvent.existing.phoneNumber == null)
      return; 
    Map<String, Object> map = MessageTemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser, paramUserLoginIdDuplicateOnCreateEvent.existing.phoneNumber);
    map.put("event", paramUserLoginIdDuplicateOnCreateEvent);
    Locale locale = TemplateHelper.getPreferredLanguage(paramUserLoginIdDuplicateOnCreateEvent.existing, paramApplication);
    this.messengerService.send(paramUUID, uUID, locale, map);
  }
  
  private void sendTwoFactorMethodEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, BaseUserEvent paramBaseUserEvent, TwoFactorMethod paramTwoFactorMethod, UUID paramUUID) {
    UUID uUID = paramTenant.phoneConfiguration.messengerId;
    if (uUID == null || paramUser.phoneNumber == null || paramUUID == null)
      return; 
    Map<String, Object> map = MessageTemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser, paramUser.phoneNumber);
    map.put("event", paramBaseUserEvent);
    map.put("method", paramTwoFactorMethod);
    Locale locale = TemplateHelper.getPreferredLanguage(paramUser, paramApplication);
    this.messengerService.send(paramUUID, uUID, locale, map);
  }
  
  private void sendUserEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, BaseUserEvent paramBaseUserEvent, UUID paramUUID) {
    UUID uUID = paramTenant.phoneConfiguration.messengerId;
    if (uUID == null || paramUser.phoneNumber == null || paramUUID == null)
      return; 
    Map<String, Object> map = MessageTemplateHelper.getBaseParameters(paramTenant, paramApplication, paramUser, paramUser.phoneNumber);
    map.put("event", paramBaseUserEvent);
    Locale locale = TemplateHelper.getPreferredLanguage(paramUser, paramApplication);
    this.messengerService.send(paramUUID, uUID, locale, map);
  }
}
