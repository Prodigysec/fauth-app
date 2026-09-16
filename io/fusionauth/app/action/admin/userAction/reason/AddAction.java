package io.fusionauth.app.action.admin.userAction.reason;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.UserActionReason;
import io.fusionauth.domain.api.UserActionReasonRequest;
import io.fusionauth.domain.api.UserActionReasonResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;

@Action(requiresAuthentication = true, constraints = {"admin", "user_action_manager"})
@Redirect(code = "success", uri = "/admin/user-action/reason/")
public class AddAction extends BaseAction {
  public UserActionReason userActionReason = new UserActionReason();
  
  public UUID userActionReasonId;
  
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    this.userActionReason = ((UserActionReasonResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createUserActionReason(this.userActionReasonId, new UserActionReasonRequest(this.userActionReason)))).userActionReason;
    writeAuditLog("Created user action reason with Id [" + String.valueOf(this.userActionReason.id) + "], text [" + this.userActionReason.text + "] and code [" + this.userActionReason.code + "]");
    return "success";
  }
}
