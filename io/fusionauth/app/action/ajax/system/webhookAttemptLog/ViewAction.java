package io.fusionauth.app.action.ajax.system.webhookAttemptLog;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.WebhookAttemptLog;
import io.fusionauth.domain.api.WebhookAttemptLogResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, value = "{id}", constraints = {"admin", "webhook_event_log_viewer"})
public class ViewAction extends BaseAJAXAction {
  public UUID id;
  
  public WebhookAttemptLog webhookAttemptLog;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.webhookAttemptLog = ((WebhookAttemptLogResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveWebhookAttemptLog(this.id))).webhookAttemptLog;
    return "render";
  }
}
