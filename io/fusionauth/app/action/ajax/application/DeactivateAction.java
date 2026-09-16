package io.fusionauth.app.action.ajax.application;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, value = "{applicationId}", constraints = {"admin", "application_manager"})
public class DeactivateAction extends BaseApplicationAJAXAction {
  @Inject
  public DeactivateAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    retrieveApplication();
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.deactivateApplication(this.applicationId));
    writeAuditLog("Deactivated application with Id [" + String.valueOf(this.applicationId) + "] and name [" + this.application.name + "]");
    return "success";
  }
}
