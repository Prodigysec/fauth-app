package io.fusionauth.app.action.admin.webhook;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.util.ActionTools;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.api.WebhookRequest;
import io.fusionauth.domain.api.WebhookResponse;
import io.fusionauth.domain.event.EventType;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;

@Action(requiresAuthentication = true, constraints = {"admin", "webhook_manager"})
@Redirect(code = "success", uri = "/admin/webhook/")
public class AddAction extends BaseFormAction {
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    for (EventType eventType : EventType.values())
      this.webhook.eventsEnabled.put(eventType, Boolean.valueOf(true)); 
    this.webhook.global = true;
    return "input";
  }
  
  public String post() {
    this.webhook.headers.putAll(ActionTools.keyValueCollectionsToMap(this.headerNames, this.headerValues));
    Webhook webhook = ((WebhookResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createWebhook(this.webhookId, new WebhookRequest(this.webhook)))).webhook;
    writeAuditLog("Created webhook with Id [" + String.valueOf(webhook.id) + "] and URL [" + String.valueOf(this.webhook.url) + "]");
    return "success";
  }
}
