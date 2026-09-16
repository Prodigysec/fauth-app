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
public class ViewAction extends BaseAJAXAction {
  public UserAction userAction;
  
  public UUID userActionId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.userAction = ((UserActionResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserAction(this.userActionId))).userAction;
    return "render";
  }
}
