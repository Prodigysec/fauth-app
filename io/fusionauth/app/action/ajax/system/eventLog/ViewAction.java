package io.fusionauth.app.action.ajax.system.eventLog;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.api.EventLogResponse;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, value = "{id}", constraints = {"admin", "event_log_viewer"})
public class ViewAction extends BaseAJAXAction {
  public EventLog eventLog;
  
  public Integer id;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.eventLog = ((EventLogResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEventLog(this.id))).eventLog;
    return "render";
  }
}
