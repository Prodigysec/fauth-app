package io.fusionauth.app.action.admin.system.webhookEventLog;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.WebhookEventLog;
import io.fusionauth.domain.api.WebhookEventLogResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(requiresAuthentication = true, value = "{id}", constraints = {"admin", "webhook_event_log_viewer"})
@List({@Redirect(code = "api-error", uri = "/admin/system/webhook-event-log/"), @Redirect(code = "missing", uri = "/admin/system/webhook-event-log/")})
public class ManageAction extends BaseAction {
  public UUID id;
  
  public WebhookEventLog webhookEventLog;
  
  @Inject
  public ManageAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.webhookEventLog = ((WebhookEventLogResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveWebhookEventLog(this.id))).webhookEventLog;
    return "render";
  }
}
