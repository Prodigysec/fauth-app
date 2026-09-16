package io.fusionauth.app.action.ajax.group;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.GroupResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{groupId}", requiresAuthentication = true, constraints = {"admin", "group_manager"})
public class ViewAction extends BaseAJAXAction {
  public Map<UUID, Application> applications = new HashMap<>();
  
  public Group group;
  
  public UUID groupId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.group = ((GroupResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveGroup(this.groupId))).group;
    ((ApplicationResponse)this.delegate.execute(FusionAuthClient::retrieveApplications)).applications.forEach(paramApplication -> this.applications.put(paramApplication.id, paramApplication));
    return "render";
  }
}
