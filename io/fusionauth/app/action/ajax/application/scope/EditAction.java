package io.fusionauth.app.action.ajax.application.scope;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.application.BaseApplicationAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.ApplicationOAuthScope;
import io.fusionauth.domain.api.ApplicationOAuthScopeRequest;
import io.fusionauth.domain.api.ApplicationOAuthScopeResponse;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "application_manager"})
public class EditAction extends BaseApplicationAJAXAction {
  @Inject
  protected EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    retrieveScope();
    return "render";
  }
  
  public String post() {
    ApplicationOAuthScope applicationOAuthScope = ((ApplicationOAuthScopeResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveOAuthScope(this.applicationId, this.scopeId))).scope;
    this.scope = ((ApplicationOAuthScopeResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateOAuthScope(this.applicationId, this.scopeId, new ApplicationOAuthScopeRequest(this.scope)))).scope;
    writeAuditLogForUpdate("Updated OAuth scope with Id [" + String.valueOf(this.scopeId) + "] and name [" + this.scope.name + "] in application with Id [" + String.valueOf(this.applicationId) + "]", applicationOAuthScope, this.scope);
    return "success";
  }
}
