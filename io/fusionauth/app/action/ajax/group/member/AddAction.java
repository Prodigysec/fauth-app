package io.fusionauth.app.action.ajax.group.member;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.util.UserTools;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.MemberRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "group_manager", "user_manager", "user_support_manager"})
public class AddAction extends BaseMemberAJAXAction {
  public Map<UUID, List<GroupMember>> members = new HashMap<>();
  
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    if (this.request != null)
      return "render"; 
    this.members.put(this.groupId, (List<GroupMember>)this.userId.stream().map(paramUUID -> (new GroupMember()).with(())).collect(Collectors.toList()));
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.createGroupMembers(new MemberRequest(this.members)));
    audit();
    return "success";
  }
  
  private void audit() {
    if (this.users == null || this.users.isEmpty() || this.group == null)
      return; 
    List list = this.users.stream().filter(paramUser -> !UserTools.isMemberOfGroup(paramUser, this.groupId)).toList();
    if (list.isEmpty())
      return; 
    String str = list.stream().map(paramUser -> paramUser.id.toString()).collect(Collectors.joining(", "));
    if (list.size() == 1) {
      writeAuditLog("Added user with Id [" + str + "] and login [" + ((User)list.getFirst()).getLogin() + "] to group with Id [" + String.valueOf(this.groupId) + "] and name [" + this.group.name + "]");
    } else {
      writeAuditLog("Added multiple users with Ids [" + str + "] to group with Id [" + String.valueOf(this.groupId) + "] and name [" + this.group.name + "]");
    } 
  }
}
