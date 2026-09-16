package io.fusionauth.api.service.messenger;

import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
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
import javax.annotation.Nonnull;

public interface MessengerServiceProxy {
  void sendAdminTwoFactorMethodRemove(Tenant paramTenant, @Nonnull User paramUser, TwoFactorMethod paramTwoFactorMethod);
  
  void sendUserIdentityUpdateEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserIdentityUpdateEvent paramUserIdentityUpdateEvent);
  
  void sendUserIdentityVerifiedEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserIdentityVerifiedEvent paramUserIdentityVerifiedEvent);
  
  void sendUserLoginIdDuplicateOnCreate(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserLoginIdDuplicateOnCreateEvent paramUserLoginIdDuplicateOnCreateEvent);
  
  void sendUserLoginIdDuplicateOnUpdate(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserLoginIdDuplicateOnUpdateEvent paramUserLoginIdDuplicateOnUpdateEvent);
  
  void sendUserLoginNewDeviceEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserLoginNewDeviceEvent paramUserLoginNewDeviceEvent);
  
  void sendUserLoginSuspiciousEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserLoginSuspiciousEvent paramUserLoginSuspiciousEvent);
  
  void sendUserPasswordResetSuccessEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserPasswordResetSuccessEvent paramUserPasswordResetSuccessEvent);
  
  void sendUserPasswordUpdateEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserPasswordUpdateEvent paramUserPasswordUpdateEvent);
  
  void sendUserTwoFactorMethodAddEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserTwoFactorMethodAddEvent paramUserTwoFactorMethodAddEvent);
  
  void sendUserTwoFactorMethodRemoveEvent(Tenant paramTenant, Application paramApplication, @Nonnull User paramUser, UserTwoFactorMethodRemoveEvent paramUserTwoFactorMethodRemoveEvent);
}
