package io.fusionauth.app.action.admin.userAction;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.api.UserActionResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, value = "{userActionId}", constraints = {"admin", "user_action_deleter"})
@List({@Redirect(code = "missing", uri = "/admin/user-action/"), @Redirect(code = "success", uri = "/admin/user-action/")})
public class DeleteAction extends BaseAction {
  public String confirm;
  
  public UserAction userAction;
  
  public UUID userActionId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteUserAction(this.userActionId));
    writeAuditLog("Deleted user action with Id [" + String.valueOf(this.userActionId) + "]");
    return "success";
  }
  
  @PostParameterMethod
  public void retrieveUserAction() {
    this.userAction = ((UserActionResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserAction(this.userActionId))).userAction;
  }
  
  @ValidationMethod
  public void validate() {
    if (this.confirm == null) {
      this.frontEndSupport.addFieldError("confirm", "[missing]confirm", new Object[0]);
    } else if (!this.confirm.equals("DELETE")) {
      this.frontEndSupport.addFieldError("confirm", "[invalid]confirm", new Object[0]);
    } 
  }
}
