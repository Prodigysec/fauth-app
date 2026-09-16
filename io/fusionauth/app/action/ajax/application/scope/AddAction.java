package io.fusionauth.app.action.ajax.application.scope;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.application.BaseApplicationAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.ApplicationOAuthScope;
import io.fusionauth.domain.api.ApplicationOAuthScopeRequest;
import io.fusionauth.domain.api.ApplicationOAuthScopeResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.message.scope.MessageScope;

@Action(requiresAuthentication = true, constraints = {"admin", "application_manager"})
public class AddAction extends BaseApplicationAJAXAction {
  @Inject
  protected AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    this.scope = new ApplicationOAuthScope();
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    ClientResponse<ApplicationOAuthScopeResponse, Errors> clientResponse = this.client.createOAuthScope(this.applicationId, null, new ApplicationOAuthScopeRequest(this.scope));
    if (!clientResponse.wasSuccessful()) {
      Errors errors = (Errors)clientResponse.getErrorResponse();
      if (errors.containsError("[notLicensed]")) {
        this.frontEndSupport.addGeneralError(MessageScope.FLASH, "[notLicensed]", new Object[0]);
        return "success";
      } 
      if (errors.containsError("[notLicensedFor]")) {
        this.frontEndSupport.addGeneralError(MessageScope.FLASH, "[notLicensedFor]", new Object[0]);
        return "success";
      } 
      this.frontEndSupport.frontEndErrorHandling(clientResponse);
    } 
    ApplicationOAuthScope applicationOAuthScope = ((ApplicationOAuthScopeResponse)clientResponse.getSuccessResponse()).scope;
    writeAuditLog("Added OAuth scope with Id [" + String.valueOf(applicationOAuthScope.id) + "] and name [" + this.scope.name + "] to application with Id [" + String.valueOf(this.applicationId) + "]");
    return "success";
  }
}
