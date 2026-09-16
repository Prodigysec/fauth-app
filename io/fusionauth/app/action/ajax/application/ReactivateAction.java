package io.fusionauth.app.action.ajax.application;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, value = "{applicationId}", constraints = {"admin", "application_manager"})
public class ReactivateAction extends BaseApplicationAJAXAction {
  @Inject
  public ReactivateAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    retrieveApplication();
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.reactivateApplication(this.applicationId));
    writeAuditLog("Reactivated application with Id [" + String.valueOf(this.applicationId) + "] and name [" + this.application.name + "]");
    return "success";
  }
}
