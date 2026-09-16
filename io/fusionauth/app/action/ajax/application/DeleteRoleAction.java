package io.fusionauth.app.action.ajax.application;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "application_manager"})
public class DeleteRoleAction extends BaseApplicationAJAXAction {
  @Inject
  public DeleteRoleAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    retrieveApplication();
    retrieveRole();
    if (this.role.id == null)
      throw new NotFoundException(); 
    return "render";
  }
  
  public String post() {
    retrieveApplication();
    retrieveRole();
    if (this.role.id == null)
      throw new NotFoundException(); 
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.deleteApplicationRole(this.applicationId, this.roleId));
    writeAuditLog("Deleted role with Id [" + String.valueOf(this.roleId) + "] and name [" + this.role.name + "] from application with Id [" + String.valueOf(this.applicationId) + "] and name [" + this.application.name + "]");
    return "success";
  }
}
