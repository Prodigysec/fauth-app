package io.fusionauth.app.action.ajax.webhook;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.api.WebhookResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{webhookId}", requiresAuthentication = true, constraints = {"admin", "webhook_manager"})
public class DeleteAction extends BaseAJAXAction {
  public UUID webhookId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    Webhook webhook = ((WebhookResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveWebhook(this.webhookId))).webhook;
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteWebhook(this.webhookId));
    writeAuditLog("Deleted the webhook with Id [" + String.valueOf(this.webhookId) + "] and URL [" + String.valueOf(webhook.url) + "]");
    return "success";
  }
}
