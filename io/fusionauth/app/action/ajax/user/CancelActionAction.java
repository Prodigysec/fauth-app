package io.fusionauth.app.action.ajax.user;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.UserActionLog;
import io.fusionauth.domain.api.UserActionResponse;
import io.fusionauth.domain.api.user.ActionRequest;
import io.fusionauth.domain.api.user.ActionResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

@Action(requiresAuthentication = true, value = "{actionId}", constraints = {"admin", "user_manager", "user_support_manager"})
public class CancelActionAction extends BaseUserActionAJAXAction {
  public UserAction userAction;
  
  public UserActionLog userActionLog;
  
  @Inject
  protected CancelActionAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.action.notifyUser = this.userActionLog.notifyUserOnEnd;
    this.action.emailUser = this.userActionLog.emailUserOnEnd;
    return "render";
  }
  
  @PostParameterMethod
  public void loadData() {
    this.userActionLog = findAction();
    this.userAction = ((UserActionResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserAction(this.userActionLog.userActionId))).userAction;
  }
  
  public String post() {
    this.action.actionerUserId = this.codeCurrentUser.id;
    UserActionLog userActionLog = ((ActionResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.cancelAction(this.actionId, new ActionRequest(this.frontEndSupport.buildEventInfo(null), this.action, true)))).action;
    writeAuditLog("Cancelled action [" + userActionLog.name + "] with Id [" + String.valueOf(userActionLog.id) + "] for user [" + String.valueOf(this.action.actioneeUserId) + "]");
    return "success";
  }
}
