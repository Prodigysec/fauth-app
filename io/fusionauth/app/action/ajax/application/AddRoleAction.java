package io.fusionauth.app.action.ajax.application;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.api.ApplicationRequest;
import io.fusionauth.domain.api.ApplicationResponse;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "application_manager"})
public class AddRoleAction extends BaseApplicationAJAXAction {
  @Inject
  public AddRoleAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    this.role = new ApplicationRole();
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    retrieveApplication();
    ApplicationRole applicationRole = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.createApplicationRole(this.applicationId, null, new ApplicationRequest(this.frontEndSupport.buildEventInfo(null), this.role)))).role;
    writeAuditLog("Added role with Id [" + String.valueOf(applicationRole.id) + "] and name [" + this.role.name + "] to application with Id [" + String.valueOf(this.applicationId) + "] and name [" + this.application.name + "]");
    return "success";
  }
}
