package io.fusionauth.app.action.ajax.user.registration;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.UserResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.message.scope.MessageScope;

@Action(value = "{userId}/{applicationId}", requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
public class ResendVerificationAction extends BaseAJAXAction {
  public UUID applicationId;
  
  public UUID userId;
  
  @Inject
  public ResendVerificationAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    User user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(this.userId))).user;
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.resendRegistrationVerification(paramUser.email, this.applicationId));
    writeAuditLog("Resent registration verification to user with Id [" + String.valueOf(this.userId) + "] and the application with Id [" + String.valueOf(this.applicationId) + "] using email [" + user.email + "]");
    this.frontEndSupport.addGeneralInfo(MessageScope.FLASH, "[SendEmailSuccess]", new Object[0]);
    return "success";
  }
}
