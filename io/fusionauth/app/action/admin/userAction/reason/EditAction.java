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
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(requiresAuthentication = true, value = "{userActionReasonId}", constraints = {"admin", "user_action_manager"})
@List({@Redirect(code = "missing", uri = "/admin/user-action/reason/"), @Redirect(code = "success", uri = "/admin/user-action/reason/")})
public class EditAction extends BaseAction {
  public UserActionReason userActionReason = new UserActionReason();
  
  public UUID userActionReasonId;
  
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.userActionReason = ((UserActionReasonResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserActionReason(this.userActionReasonId))).userActionReason;
    return "input";
  }
  
  public String post() {
    UserActionReason userActionReason1 = ((UserActionReasonResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserActionReason(this.userActionReasonId))).userActionReason;
    UserActionReason userActionReason2 = ((UserActionReasonResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateUserActionReason(this.userActionReasonId, new UserActionReasonRequest(this.userActionReason)))).userActionReason;
    writeAuditLogForUpdate("Updated user action reason with Id [" + String.valueOf(this.userActionReasonId) + "], text [" + userActionReason2.text + "] and code [" + userActionReason2.code + "]", userActionReason1, userActionReason2);
    return "success";
  }
}
