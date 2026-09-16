package io.fusionauth.app.action.admin.userAction.reason;

import com.google.inject.Inject;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.UserActionReason;
import io.fusionauth.domain.api.UserActionReasonResponse;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "user_action_manager"})
public class IndexAction extends BaseAction {
  public List<UserActionReason> reasons;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.reasons = ((UserActionReasonResponse)superDelegate().execute(FusionAuthClient::retrieveUserActionReasons)).userActionReasons;
    return "input";
  }
}
