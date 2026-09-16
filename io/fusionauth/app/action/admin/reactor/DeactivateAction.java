package io.fusionauth.app.action.admin.reactor;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "reactor_manager"})
@Redirect(uri = "/admin/reactor/")
public class DeactivateAction extends BaseAction {
  public String confirm;
  
  @Inject
  public DeactivateAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    ClientResponse<Void, Void> clientResponse = this.superClient.deactivateReactor();
    if (clientResponse.wasSuccessful()) {
      writeAuditLog("Deactivated FusionAuth Reactor.");
      this.frontEndSupport.addGeneralInfo("[SuccessfullyDeactivated]", new Object[0]);
    } 
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    if (this.confirm == null) {
      this.frontEndSupport.addFieldError("confirm", "[missing]confirm", new Object[0]);
    } else if (!this.confirm.equals("DECOMMISSION")) {
      this.frontEndSupport.addFieldError("confirm", "[invalid]confirm", new Object[0]);
    } 
  }
}
