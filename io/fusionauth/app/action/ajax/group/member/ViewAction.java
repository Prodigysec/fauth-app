package io.fusionauth.app.action.ajax.group.member;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.GroupResponse;
import io.fusionauth.domain.api.UserResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "group_manager", "user_manager", "user_support_manager", "user_support_viewer"})
public class ViewAction extends BaseAJAXAction {
  public Group group;
  
  public UUID groupId;
  
  public GroupMember member;
  
  public User user;
  
  public UUID userId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.group = ((GroupResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveGroup(this.groupId))).group;
    this.user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(this.userId))).user;
    this.member = this.user.getMemberships().stream().filter(paramGroupMember -> paramGroupMember.groupId.equals(this.groupId)).findFirst().orElse(null);
    return "render";
  }
}
