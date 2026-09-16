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
public class ViewAction extends BaseAJAXAction {
  public UserActionReason userActionReason;
  
  public UUID userActionReasonId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.userActionReason = ((UserActionReasonResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserActionReason(this.userActionReasonId))).userActionReason;
    return "render";
  }
}
