package io.fusionauth.app.action.ajax;

import com.google.inject.Inject;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.api.GroupResponse;
import java.util.Comparator;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "group_manager", "user_manager", "user_support_manager", "user_support_viewer"})
public class GroupAction extends BaseAJAXAction {
  public List<Group> groups;
  
  @Inject
  public GroupAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.groups = ((GroupResponse)this.delegate.execute(FusionAuthClient::retrieveGroups)).groups;
    if (this.groups != null)
      this.groups.sort(Comparator.comparing(paramGroup -> paramGroup.name)); 
    return "render";
  }
}
