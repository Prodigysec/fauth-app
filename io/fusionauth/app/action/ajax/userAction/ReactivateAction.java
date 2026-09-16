package io.fusionauth.app.action.ajax.userAction;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.api.UserActionResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, value = "{userActionId}", constraints = {"admin", "user_action_manager"})
public class ReactivateAction extends BaseAJAXAction {
  public UUID userActionId;
  
  @Inject
  public ReactivateAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    UserAction userAction = ((UserActionResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.reactivateUserAction(this.userActionId))).userAction;
    writeAuditLog("Reactivated user action with Id [" + String.valueOf(this.userActionId) + "] and name [" + userAction.name + "]");
    return "success";
  }
}
