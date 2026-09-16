package io.fusionauth.app.action.admin.webhook;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.util.ActionTools;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.api.WebhookRequest;
import io.fusionauth.domain.api.WebhookResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;

@Action(value = "{webhookId}", requiresAuthentication = true, constraints = {"admin", "webhook_manager"})
@Redirect(code = "success", uri = "/admin/webhook/")
public class EditAction extends BaseFormAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.webhook = ((WebhookResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveWebhook(this.webhookId))).webhook;
    return "input";
  }
  
  public String post() {
    this.webhook.headers.putAll(ActionTools.keyValueCollectionsToMap(this.headerNames, this.headerValues));
    Webhook webhook1 = ((WebhookResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveWebhook(this.webhookId))).webhook;
    this.webhook.data.clear();
    this.webhook.data.putAll(webhook1.data);
    Webhook webhook2 = ((WebhookResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateWebhook(this.webhookId, new WebhookRequest(this.webhook)))).webhook;
    writeAuditLogForUpdate("Updated webhook with Id [" + String.valueOf(this.webhookId) + "] and URL [" + String.valueOf(webhook2.url) + "]", webhook1, webhook2);
    return "success";
  }
}
