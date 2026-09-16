package io.fusionauth.app.action.admin.group;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.api.GroupResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{groupId}", requiresAuthentication = true, constraints = {"group_manager", "admin"})
public class MembersAction extends BaseAction {
  public Group group;
  
  public UUID groupId;
  
  @Inject
  public MembersAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.group = ((GroupResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveGroup(this.groupId))).group;
    return "input";
  }
}
