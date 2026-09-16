package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Webhook;

public class WebhookRequest {
  public Webhook webhook;
  
  @JacksonConstructor
  public WebhookRequest() {}
  
  public WebhookRequest(Webhook paramWebhook) {
    this.webhook = paramWebhook;
  }
}
