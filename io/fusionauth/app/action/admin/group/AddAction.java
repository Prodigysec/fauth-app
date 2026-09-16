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
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"group_manager", "admin"})
@List({@Redirect(code = "success", uri = "/admin/group/"), @Redirect(code = "api-error", uri = "/admin/group/")})
public class AddAction extends BaseFormAction {
  public Group group;
  
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    if (this.groupId != null) {
      this.group = ((GroupResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveGroup(this.groupId))).group;
      this.roleIds = (List<UUID>)this.group.roles.values().stream().flatMap(Collection::stream).map(paramApplicationRole -> paramApplicationRole.id).collect(Collectors.toList());
      this.group.id = null;
      this.groupId = null;
      this.group.name += " - copy";
    } 
    return "input";
  }
  
  public String post() {
    Group group = ((GroupResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.createGroup(this.groupId, new GroupRequest(this.group, this.roleIds)))).group;
    writeAuditLog("Created the group with Id [" + String.valueOf(group.id) + "] and name [" + this.group.name + "]");
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    if (this.tenantId == null)
      this.frontEndSupport.addFieldError("tenantId", "[blank]tenantId", new Object[0]); 
  }
}
