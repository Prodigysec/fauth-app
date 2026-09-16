package io.fusionauth.app.action.ajax.user.twoFactor;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.email.EmailProxy;
import io.fusionauth.api.service.messenger.MessengerServiceProxy;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.api.TwoFactorDisableRequest;
import io.fusionauth.domain.api.UserRequest;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.message.MessageType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.annotation.UnknownParameters;
import org.primeframework.mvc.security.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Action(requiresAuthentication = true, constraints = {"admin", "mfa_deleter", "user_manager", "user_support_manager", "user_support_viewer"})
public class DisableAction extends BaseAJAXAction {
  private static final Logger logger = LoggerFactory.getLogger(DisableAction.class);
  
  private final EmailProxy emailProxy;
  
  private final MessengerServiceProxy messengerServiceProxy;
  
  public String code;
  
  public List<TwoFactorMethod> configuredMethods = new ArrayList<>();
  
  public MessageType messageType;
  
  public String method;
  
  public String methodId;
  
  public String methodValue;
  
  @UnknownParameters
  public Map<String, Object> unknown = new HashMap<>();
  
  public UUID userId;
  
  @Inject
  public DisableAction(FrontEndSupport paramFrontEndSupport, EmailProxy paramEmailProxy, MessengerServiceProxy paramMessengerServiceProxy) {
    super(paramFrontEndSupport);
    this.emailProxy = paramEmailProxy;
    this.messengerServiceProxy = paramMessengerServiceProxy;
  }
  
  public static boolean canRemoveMFAMethod(UUID paramUUID, @Nonnull User paramUser, @Nonnull Function<String, Boolean> paramFunction) {
    if (paramUUID == null)
      return false; 
    if (((Boolean)paramFunction.apply("admin")).booleanValue() || ((Boolean)paramFunction.apply("mfa_deleter")).booleanValue())
      return true; 
    return paramUUID.equals(paramUser.id);
  }
  
  @FormPrepareMethod
  public void formPrepare() {
    User user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(this.userId))).user;
    this.configuredMethods.addAll(user.twoFactor.methods);
    TwoFactorMethod twoFactorMethod = user.twoFactor.getMethodById(this.methodId);
    this.method = twoFactorMethod.method;
    this
      
      .methodValue = this.method.equals("email") ? twoFactorMethod.email : (this.method.equals("sms") ? twoFactorMethod.mobilePhone : null);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    DisableAction disableAction = this;
    if (!canRemoveMFAMethod(this.userId, this.codeCurrentUser, paramString -> Boolean.valueOf(paramDisableAction.hasRole(new String[] { paramString }))))
      throw new UnauthorizedException(); 
    User user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(this.userId))).user;
    Tenant tenant = this.tenants.get(user.tenantId);
    if (hasRole(new String[] { "admin" }))
      return removeMethodAsAdminAndNotifyUser(user, tenant); 
    if (hasRole(new String[] { "mfa_deleter" }) && !this.userId.equals(this.codeCurrentUser.id)) {
      if (user.getRegistrationForApplication(Application.FUSIONAUTH_APP_ID) != null) {
        this.frontEndSupport.addGeneralError(MessageScope.FLASH, "[UserRegisteredForAdminApplication]", new Object[0]);
        return "success";
      } 
      UserIdentity userIdentity1 = user.resolvePrimaryIdentity(IdentityType.email);
      UserIdentity userIdentity2 = user.resolvePrimaryIdentity(IdentityType.phoneNumber);
      boolean bool1 = (userIdentity1 != null && !userIdentity1.verificationRequired() && tenant.emailConfiguration.adminTwoFactorMethodRemoveEmailTemplateId != null) ? true : false;
      boolean bool2 = (userIdentity2 != null && !userIdentity2.verificationRequired() && tenant.phoneConfiguration.adminTwoFactorMethodRemoveTemplateId != null) ? true : false;
      if (!bool1 && !bool2) {
        this.frontEndSupport.addGeneralError(MessageScope.FLASH, "[UserCannotBeNotified]", new Object[0]);
        return "success";
      } 
      return removeMethodAsAdminAndNotifyUser(user, tenant);
    } 
    TwoFactorMethod twoFactorMethod = user.twoFactor.getMethodById(this.methodId);
    ClientResponse<Void, Errors> clientResponse = this.client.disableTwoFactorWithRequest(user.id, new TwoFactorDisableRequest(this.frontEndSupport.buildEventInfo(null), null, this.code, this.methodId));
    if (clientResponse.wasSuccessful()) {
      writeAuditLog("A user with Id [" + String.valueOf(this.codeCurrentUser.id) + "] removed a two-factor method from themselves. Method Id [" + this.methodId + "], method [" + twoFactorMethod.method + "]" + (
          twoFactorMethod.method.equals("authenticator") ? "" : (", value [" + (twoFactorMethod.method.equals("email") ? twoFactorMethod.email : twoFactorMethod.mobilePhone) + "]")));
      return "success";
    } 
    if (clientResponse.status == 421) {
      this.frontEndSupport.addFieldError("code", "[invalid]code", new Object[0]);
    } else {
      this.frontEndSupport.transfer((Errors)clientResponse.errorResponse);
    } 
    return "input";
  }
  
  private String removeMethodAsAdminAndNotifyUser(User paramUser, Tenant paramTenant) {
    User user1 = new User(paramUser);
    TwoFactorMethod twoFactorMethod = user1.twoFactor.getMethodById(this.methodId);
    user1.twoFactor.methods.removeIf(paramTwoFactorMethod -> paramTwoFactorMethod.id.equals(this.methodId));
    UserRequest userRequest = new UserRequest(this.frontEndSupport.buildEventInfo(null), user1);
    userRequest.disableDomainBlock = true;
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateUser(this.userId, paramUserRequest));
    writeAuditLogForUpdate("A user with Id [" + String.valueOf(this.codeCurrentUser.id) + "] removed the two-factor method for user with Id [" + String.valueOf(this.userId) + "]. Method Id [" + this.methodId + "], method [" + twoFactorMethod.method + "]" + (
        twoFactorMethod.method.equals("authenticator") ? "" : (", value [" + (twoFactorMethod.method.equals("email") ? twoFactorMethod.email : twoFactorMethod.mobilePhone) + "]")), paramUser, user1);
    User user2 = (new User(user1)).secure().sort();
    try {
      this.emailProxy.sendAdminTwoFactorRemove(paramTenant, user2, twoFactorMethod);
    } catch (Exception exception) {
      logger.debug("Email notification failed; check Event Log for details", exception);
    } 
    try {
      this.messengerServiceProxy.sendAdminTwoFactorMethodRemove(paramTenant, user2, twoFactorMethod);
    } catch (Exception exception) {
      logger.debug("SMS notification failed; additional logging available from messenger implementation", exception);
    } 
    return "success";
  }
}
