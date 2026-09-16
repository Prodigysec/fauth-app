package io.fusionauth.app.action.ajax.user;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.UserResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{userId}", requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
public class ReactivateAction extends BaseAJAXAction {
  public UUID userId;
  
  @Inject
  public ReactivateAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    User user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.reactivateUser(this.userId))).user;
    writeAuditLog("Reactivated user with Id [" + String.valueOf(user.id) + "], name [" + user.getName() + "] and username/email [" + ((user.email != null) ? user.email : user.username) + "]");
    return "success";
  }
}
