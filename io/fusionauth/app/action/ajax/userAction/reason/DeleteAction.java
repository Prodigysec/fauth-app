package io.fusionauth.app.action.ajax.userAction.reason;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.UserActionReason;
import io.fusionauth.domain.api.UserActionReasonResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, value = "{userActionReasonId}", constraints = {"admin", "user_action_manager"})
public class DeleteAction extends BaseAJAXAction {
  public UUID userActionReasonId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    UserActionReason userActionReason = ((UserActionReasonResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserActionReason(this.userActionReasonId))).userActionReason;
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteUserActionReason(this.userActionReasonId));
    writeAuditLog("Deleted user action reason with Id [" + String.valueOf(this.userActionReasonId) + "], text [" + userActionReason.text + "] and code [" + userActionReason.code + "]");
    return "success";
  }
}
