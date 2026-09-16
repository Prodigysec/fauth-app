package io.fusionauth.app.action.admin.reactor;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.ReactorRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(requiresAuthentication = true, constraints = {"admin", "reactor_manager"})
@List({@Redirect(uri = "/admin/reactor/"), @Redirect(code = "input", uri = "/admin/reactor/")})
public class ActivateAction extends BaseAction {
  public String license;
  
  public String licenseId;
  
  @Inject
  public ActivateAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String post() {
    ClientResponse<Void, Errors> clientResponse = this.superClient.activateReactor(new ReactorRequest(this.licenseId, this.license));
    if (clientResponse.wasSuccessful()) {
      writeAuditLog("Activated FusionAuth Reactor.");
      this.frontEndSupport.addGeneralInfo("[SuccessfullyActivated]", new Object[0]);
    } else {
      this.frontEndSupport.transfer((Errors)clientResponse.errorResponse);
    } 
    return "success";
  }
}
