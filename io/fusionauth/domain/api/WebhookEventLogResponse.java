package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.WebhookEventLog;

public class WebhookEventLogResponse implements Buildable<WebhookEventLogResponse> {
  public WebhookEventLog webhookEventLog;
  
  @JacksonConstructor
  public WebhookEventLogResponse() {}
  
  public WebhookEventLogResponse(WebhookEventLog paramWebhookEventLog) {
    this.webhookEventLog = paramWebhookEventLog;
  }
}
