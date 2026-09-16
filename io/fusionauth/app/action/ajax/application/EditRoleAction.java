package io.fusionauth.app.action.ajax.application;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.api.ApplicationRequest;
import io.fusionauth.domain.api.ApplicationResponse;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "application_manager"})
public class EditRoleAction extends BaseApplicationAJAXAction {
  @Inject
  public EditRoleAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    this.role = new ApplicationRole();
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
    ApplicationRole applicationRole1 = this.application.getRole(this.role.name);
    ApplicationRole applicationRole2 = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateApplicationRole(this.applicationId, this.roleId, new ApplicationRequest(this.frontEndSupport.buildEventInfo(null), this.role)))).role;
    writeAuditLogForUpdate("Updated role with Id [" + String.valueOf(this.roleId) + "] and name [" + applicationRole2.name + "] in application with Id [" + String.valueOf(this.applicationId) + "] and name [" + this.application.name + "]", applicationRole1, applicationRole2);
    return "success";
  }
}
