package io.fusionauth.app.action.ajax.system.auditLog;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.api.AuditLogResponse;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, value = "{id}", constraints = {"admin", "audit_log_viewer"})
public class ViewAction extends BaseAJAXAction {
  public AuditLog auditLog;
  
  public Integer id;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.auditLog = ((AuditLogResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveAuditLog(this.id))).auditLog;
    return "render";
  }
}
