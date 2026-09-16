package io.fusionauth.app.action.admin.userAction;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.api.UserActionRequest;
import io.fusionauth.domain.api.UserActionResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;

@Action(requiresAuthentication = true, constraints = {"admin", "user_action_manager"})
@Redirect(code = "success", uri = "/admin/user-action/")
public class AddAction extends BaseFormAction {
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    cleanOptionsAndLocalizationData();
    UserAction userAction = ((UserActionResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createUserAction(this.userAction.id, new UserActionRequest(this.userAction)))).userAction;
    writeAuditLog("Created user action with Id [" + String.valueOf(userAction.id) + "] and name [" + userAction.name + "]");
    return "success";
  }
}
