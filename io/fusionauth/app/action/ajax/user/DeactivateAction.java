package io.fusionauth.app.action.ajax.user;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.UserDeleteSingleRequest;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, value = "{userId}", constraints = {"admin", "user_manager", "user_support_manager"})
public class DeactivateAction extends BaseAJAXAction {
  public UUID userId;
  
  @Inject
  public DeactivateAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.deleteUserWithRequest(this.userId, new UserDeleteSingleRequest(this.frontEndSupport.buildEventInfo(null), false)));
    writeAuditLog("Deactivated user with Id [" + String.valueOf(this.userId) + "]");
    return "success";
  }
}
