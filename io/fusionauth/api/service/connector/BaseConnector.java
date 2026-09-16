package io.fusionauth.api.service.connector;

import com.inversoft.error.Errors;
import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.api.domain.api.reactor.BreachResult;
import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.api.service.email.EmailProxy;
import io.fusionauth.api.service.reactor.ReactorService;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.api.service.user.PasswordService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.BreachedPasswordStatus;
import io.fusionauth.domain.ChangePasswordReason;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.PasswordBreachDetection;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.event.UserPasswordBreachEvent;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public abstract class BaseConnector implements Connector {
  protected final UserMapper userMapper;
  
  protected final UserService userService;
  
  private final EmailProxy emailProxy;
  
  private final PasswordService passwordService;
  
  private final ReactorService reactorService;
  
  protected BaseConnector(EmailProxy paramEmailProxy, PasswordService paramPasswordService, ReactorService paramReactorService, UserMapper paramUserMapper, UserService paramUserService) {
    this.emailProxy = paramEmailProxy;
    this.passwordService = paramPasswordService;
    this.reactorService = paramReactorService;
    this.userMapper = paramUserMapper;
    this.userService = paramUserService;
  }
  
  protected void checkIfPasswordChangeIsRequiredAndUpdateUser(Tenant paramTenant, Application paramApplication, String paramString, AuthenticationService.AuthenticationResult paramAuthenticationResult, EventInfo paramEventInfo) {
    User user = paramAuthenticationResult.user;
    if (!user.passwordChangeRequired && 
      paramTenant.maximumPasswordAge.enabled) {
      ZonedDateTime zonedDateTime1 = ZonedDateTime.now(ZoneOffset.UTC);
      ZonedDateTime zonedDateTime2 = user.passwordLastUpdateInstant.plusDays(paramTenant.maximumPasswordAge.days);
      if (zonedDateTime2.isBefore(zonedDateTime1)) {
        user.passwordChangeRequired = true;
        user.passwordChangeReason = ChangePasswordReason.Expired;
        this.userService.updatePasswordChangeRequired(user);
      } 
    } 
    if (!user.passwordChangeRequired && paramTenant.passwordValidationRules.validateOnLogin)
      checkIfPasswordFailsValidation(paramTenant, user, paramString); 
    if (!user.passwordChangeRequired)
      checkIfPasswordIsBreached(paramTenant, paramApplication, user, paramString, paramEventInfo); 
  }
  
  private void checkIfPasswordFailsValidation(Tenant paramTenant, User paramUser, String paramString) {
    Errors errors = this.passwordService.validatePasswordConstraintsOnLogin(paramTenant, paramUser, paramString);
    if (!errors.empty()) {
      paramUser.passwordChangeRequired = true;
      paramUser.passwordChangeReason = ChangePasswordReason.Validation;
      this.userService.updatePasswordChangeRequired(paramUser);
    } 
  }
  
  private void checkIfPasswordIsBreached(Tenant paramTenant, Application paramApplication, User paramUser, String paramString, EventInfo paramEventInfo) {
    PasswordBreachDetection passwordBreachDetection = paramTenant.passwordValidationRules.breachDetection;
    if (passwordBreachDetection.enabled && passwordBreachDetection.onLogin != PasswordBreachDetection.BreachAction.Off)
      if (this.reactorService.userRequiresCheck(paramUser)) {
        BreachResult breachResult = this.reactorService.retrieveBreachResultForLogin(paramUser, paramString);
        if (breachResult != null) {
          paramUser.breachedPasswordLastCheckedInstant = ZonedDateTime.now(ZoneOffset.UTC);
          paramUser.breachedPasswordStatus = BreachedPasswordStatus.None;
          handleBreachResult(paramTenant, paramApplication, paramUser, breachResult, paramEventInfo);
          this.reactorService.updateBreachMetrics(paramTenant.id, breachResult);
          this.userMapper.updateBreachStatus(paramUser.id, paramUser.breachedPasswordStatus, paramUser.breachedPasswordLastCheckedInstant);
        } 
      } else if (paramUser.breachedPasswordStatus != null) {
        BreachResult breachResult = new BreachResult();
        breachResult.match = paramUser.breachedPasswordStatus;
        handleBreachResult(paramTenant, paramApplication, paramUser, breachResult, paramEventInfo);
      }  
  }
  
  private void handleBreachResult(Tenant paramTenant, Application paramApplication, User paramUser, BreachResult paramBreachResult, EventInfo paramEventInfo) {
    PasswordBreachDetection passwordBreachDetection = paramTenant.passwordValidationRules.breachDetection;
    if (!paramBreachResult.isBreached(passwordBreachDetection))
      return; 
    paramUser.breachedPasswordStatus = paramBreachResult.match;
    if (passwordBreachDetection.onLogin != PasswordBreachDetection.BreachAction.RecordOnly)
      if (passwordBreachDetection.onLogin == PasswordBreachDetection.BreachAction.RequireChange) {
        paramUser.passwordChangeRequired = true;
        paramUser.passwordChangeReason = ChangePasswordReason.Breached;
        this.userService.updatePasswordChangeRequired(paramUser);
      } else if (passwordBreachDetection.onLogin == PasswordBreachDetection.BreachAction.NotifyUser) {
        this.emailProxy.sendBreachedPasswordWarning(paramTenant, paramApplication, paramUser, paramBreachResult);
      }  
    EventHelper.send(paramTenant, paramApplication, new UserPasswordBreachEvent(paramEventInfo, paramUser));
  }
}
