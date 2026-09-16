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
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{groupId}", requiresAuthentication = true, constraints = {"admin", "group_deleter"})
@List({@Redirect(code = "api-error", uri = "/admin/group/"), @Redirect(code = "success", uri = "/admin/group/"), @Redirect(code = "missing", uri = "/admin/group/")})
public class DeleteAction extends BaseAction {
  public String confirm;
  
  public Group group;
  
  public UUID groupId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.deleteGroup(this.groupId));
    writeAuditLog("Deleted the group with Id [" + String.valueOf(this.groupId) + "] and name [" + this.group.name + "]");
    return "success";
  }
  
  @PostParameterMethod
  public void retrieveGroup() {
    this.group = ((GroupResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveGroup(this.groupId))).group;
  }
  
  @ValidationMethod
  public void validate() {
    if (this.confirm == null) {
      this.frontEndSupport.addFieldError("confirm", "[missing]confirm", new Object[0]);
    } else if (!this.confirm.equals("DELETE")) {
      this.frontEndSupport.addFieldError("confirm", "[invalid]confirm", new Object[0]);
    } 
  }
}
