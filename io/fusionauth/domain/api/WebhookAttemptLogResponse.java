package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.WebhookAttemptLog;

public class WebhookAttemptLogResponse {
  public WebhookAttemptLog webhookAttemptLog;
  
  @JacksonConstructor
  public WebhookAttemptLogResponse() {}
  
  public WebhookAttemptLogResponse(WebhookAttemptLog paramWebhookAttemptLog) {
    this.webhookAttemptLog = paramWebhookAttemptLog;
  }
}
