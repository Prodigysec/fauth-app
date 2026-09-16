package io.fusionauth.api.service.connector;

import com.google.inject.Inject;
import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.authentication.FailedLoginService;
import io.fusionauth.api.service.authentication.LoginPreventedException;
import io.fusionauth.api.service.email.EmailProxy;
import io.fusionauth.api.service.reactor.ReactorService;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.api.service.user.PasswordService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserActionLog;
import io.fusionauth.domain.UserLoginFailedReason;
import io.fusionauth.domain.UserLoginFailedReasonCode;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorPolicy;
import io.fusionauth.domain.event.UserLoginFailedEvent;
import java.util.List;
import java.util.UUID;

public class FusionAuthConnector extends BaseConnector {
  private final FailedLoginService failedLoginService;
  
  private final PasswordService passwordService;
  
  @Inject
  public FusionAuthConnector(EmailProxy paramEmailProxy, PasswordService paramPasswordService, ReactorService paramReactorService, UserMapper paramUserMapper, UserService paramUserService, FailedLoginService paramFailedLoginService) {
    super(paramEmailProxy, paramPasswordService, paramReactorService, paramUserMapper, paramUserService);
    this.failedLoginService = paramFailedLoginService;
    this.passwordService = paramPasswordService;
  }
  
  public AuthenticationService.AuthenticationResult authenticate(BaseConnectorConfiguration paramBaseConnectorConfiguration, ConnectorPolicy paramConnectorPolicy, Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, List<IdentityType> paramList, String paramString2, EventInfo paramEventInfo) {
    if (paramUser == null)
      return null; 
    AuthenticationService.AuthenticationResult authenticationResult = authenticateUserPassword(paramTenant, paramApplication, paramUser, paramString2, paramEventInfo);
    if (authenticationResult == null || authenticationResult.exception != null)
      return authenticationResult; 
    checkIfPasswordChangeIsRequiredAndUpdateUser(paramTenant, paramApplication, paramString2, authenticationResult, paramEventInfo);
    return authenticationResult;
  }
  
  private AuthenticationService.AuthenticationResult authenticateUserPassword(Tenant paramTenant, Application paramApplication, User paramUser, String paramString, EventInfo paramEventInfo) {
    if (paramApplication != null && paramApplication.authenticationTokenConfiguration.enabled) {
      UserRegistration userRegistration = paramUser.getRegistrationForApplication(paramApplication.id);
      if (userRegistration != null && userRegistration.authenticationToken != null && userRegistration.authenticationToken.equals(paramString))
        return new AuthenticationService.AuthenticationResult(AuthenticationType.APPLICATION_TOKEN, BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID, paramUser); 
    } 
    return confirmPassword(paramTenant, paramApplication, paramUser, paramString, paramEventInfo);
  }
  
  private AuthenticationService.AuthenticationResult confirmPassword(Tenant paramTenant, Application paramApplication, User paramUser, String paramString, EventInfo paramEventInfo) {
    AuthenticationService.AuthenticationResult authenticationResult = new AuthenticationService.AuthenticationResult(AuthenticationType.PASSWORD, BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID, paramUser);
    if (this.passwordService.passwordsEqual(paramString, paramUser.password, paramUser.salt, paramUser.encryptionScheme, paramUser.factor)) {
      if (paramTenant.passwordEncryptionConfiguration.modifyEncryptionSchemeOnLogin && 
        this.passwordService.rehashPasswordOnLogin(paramTenant, paramUser, paramString))
        this.userMapper.updatePasswordFields(paramUser); 
      return authenticationResult;
    } 
    UUID uUID = (paramApplication == null || paramUser.getRegistrationForApplication(paramApplication.id) == null) ? null : paramApplication.id;
    EventHelper.send(paramTenant, paramApplication, new UserLoginFailedEvent(paramEventInfo, uUID, authenticationResult.type.name(), new UserLoginFailedReason(UserLoginFailedReasonCode.Credentials), paramUser));
    UserActionLog userActionLog = this.failedLoginService.handleFailedLoginCountExceeded(paramTenant, paramUser, paramEventInfo);
    if (userActionLog != null) {
      authenticationResult.exception = (RuntimeException)new LoginPreventedException(userActionLog);
      return authenticationResult;
    } 
    return null;
  }
}
