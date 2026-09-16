package io.fusionauth.app.action.admin.system;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.SearchEngineType;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.ReindexRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
@List({@Redirect(code = "exit", uri = "/"), @Redirect(code = "success", uri = "/admin/system/reindex"), @Redirect(code = "busy", uri = "/admin/system/reindex")})
public class ReindexAction extends BaseAction {
  public String confirm;
  
  public String index;
  
  public boolean running;
  
  @Inject
  public ReindexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    if (this.searchEngineType == SearchEngineType.database)
      return "exit"; 
    ClientResponse<Void, Errors> clientResponse = this.superClient.retrieveReindexStatus();
    if (clientResponse.status == 202) {
      this.frontEndSupport.addGeneralInfo("reindex-in-progress", new Object[0]);
      this.running = true;
    } 
    return "input";
  }
  
  public String post() {
    if (this.searchEngineType == SearchEngineType.database)
      return "exit"; 
    ClientResponse<Void, Errors> clientResponse = this.superClient.retrieveReindexStatus();
    if (clientResponse.status == 202)
      return "busy"; 
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.reindex(new ReindexRequest(this.index)));
    writeAuditLog("Started a re-index [" + this.index + "] operation");
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    if (this.index == null) {
      this.frontEndSupport.addFieldError("index", "[missing]index", new Object[0]);
    } else if (!this.index.equals("fusionauth_user") && !this.index.equals("fusionauth_entity")) {
      this.frontEndSupport.addFieldError("index", "[invalid]index", new Object[0]);
    } 
    if (this.confirm == null) {
      this.frontEndSupport.addFieldError("confirm", "[missing]confirm", new Object[0]);
    } else if (!this.confirm.equals("REINDEX")) {
      this.frontEndSupport.addFieldError("confirm", "[invalid]confirm", new Object[0]);
    } 
  }
}
