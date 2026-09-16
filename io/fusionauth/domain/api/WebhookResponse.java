package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Webhook;
import java.util.List;

public class WebhookResponse {
  public Webhook webhook;
  
  public List<Webhook> webhooks;
  
  @JacksonConstructor
  public WebhookResponse() {}
  
  public WebhookResponse(Webhook paramWebhook) {
    this.webhook = paramWebhook;
  }
  
  public WebhookResponse(List<Webhook> paramList) {
    this.webhooks = paramList;
  }
}
