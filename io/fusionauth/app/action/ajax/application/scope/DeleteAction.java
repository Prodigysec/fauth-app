package io.fusionauth.app.action.ajax.application.scope;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.application.BaseApplicationAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "application_manager"})
public class DeleteAction extends BaseApplicationAJAXAction {
  @Inject
  protected DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    retrieveScope();
    return "render";
  }
  
  public String post() {
    retrieveScope();
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.deleteOAuthScope(this.applicationId, this.scopeId));
    writeAuditLog("Deleted OAuth scope with Id [" + String.valueOf(this.scopeId) + "] and name [" + this.scope.name + "] from application with Id [" + String.valueOf(this.applicationId) + "]");
    return "success";
  }
}
