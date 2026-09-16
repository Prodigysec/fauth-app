package io.fusionauth.app.action.admin.group;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.api.GroupRequest;
import io.fusionauth.domain.api.GroupResponse;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@List({@Redirect(code = "success", uri = "/admin/group/"), @Redirect(code = "api-error", uri = "/admin/group/"), @Redirect(code = "missing", uri = "/admin/group/")})
@Action(value = "{groupId}", requiresAuthentication = true, constraints = {"admin", "group_manager"})
public class EditAction extends BaseFormAction {
  public Group group;
  
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.group = ((GroupResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveGroup(this.groupId))).group;
    this.roleIds = (List<UUID>)this.group.roles.values().stream().flatMap(Collection::stream).map(paramApplicationRole -> paramApplicationRole.id).collect(Collectors.toList());
    return "input";
  }
  
  public String post() {
    Group group1 = ((GroupResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveGroup(this.groupId))).group;
    this.group.data.clear();
    this.group.data.putAll(group1.data);
    Group group2 = ((GroupResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateGroup(this.groupId, new GroupRequest(this.group, this.roleIds)))).group;
    writeAuditLogForUpdate("Updated the group with Id [" + String.valueOf(this.groupId) + "] and name [" + group2.name + "]", group1, group2);
    return "success";
  }
}
