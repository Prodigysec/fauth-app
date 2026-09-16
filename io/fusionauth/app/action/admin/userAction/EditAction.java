package io.fusionauth.app.action.admin.userAction;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.api.UserActionRequest;
import io.fusionauth.domain.api.UserActionResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(requiresAuthentication = true, value = "{userActionId}", constraints = {"admin", "user_action_manager"})
@List({@Redirect(code = "missing", uri = "/admin/user-action/"), @Redirect(code = "success", uri = "/admin/user-action/")})
public class EditAction extends BaseFormAction {
  public UUID userActionId;
  
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.userAction = ((UserActionResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserAction(this.userActionId))).userAction;
    if (!this.userAction.active) {
      this.frontEndSupport.addGeneralError("[inactive]", new Object[0]);
      return "success";
    } 
    return "input";
  }
  
  public String post() {
    cleanOptionsAndLocalizationData();
    UserAction userAction1 = ((UserActionResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserAction(this.userActionId))).userAction;
    UserAction userAction2 = ((UserActionResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateUserAction(this.userActionId, new UserActionRequest(this.userAction)))).userAction;
    writeAuditLogForUpdate("Updated user action with Id [" + String.valueOf(this.userActionId) + "] and name [" + userAction2.name + "]", userAction1, userAction2);
    return "success";
  }
}
