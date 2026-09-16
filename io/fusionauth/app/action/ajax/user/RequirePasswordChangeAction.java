package io.fusionauth.app.action.ajax.user;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.ChangePasswordReason;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.UserRequest;
import io.fusionauth.domain.api.UserResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.message.scope.MessageScope;

@Action(value = "{userId}", requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
public class RequirePasswordChangeAction extends BaseAJAXAction {
  public UUID userId;
  
  @Inject
  public RequirePasswordChangeAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    User user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(this.userId))).user;
    user.passwordChangeRequired = true;
    user.passwordChangeReason = ChangePasswordReason.Administrative;
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateUser(this.userId, new UserRequest(this.frontEndSupport.buildEventInfo(null), null, false, true, null, paramUser)));
    writeAuditLog("User with Id [" + String.valueOf(this.userId) + "] and loginId [" + user.getLogin() + "] has been updated to require password change.");
    this.frontEndSupport.addGeneralInfo(MessageScope.FLASH, "[PasswordChangeRequestedSuccessful]", new Object[0]);
    return "success";
  }
}
