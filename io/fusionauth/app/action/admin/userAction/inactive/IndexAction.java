package io.fusionauth.app.action.admin.userAction.inactive;

import com.google.inject.Inject;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.api.UserActionResponse;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "user_action_manager"})
public class IndexAction extends BaseAction {
  public List<UserAction> userActions;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.userActions = ((UserActionResponse)superDelegate().execute(FusionAuthClient::retrieveInactiveUserActions)).userActions;
    return "input";
  }
}
