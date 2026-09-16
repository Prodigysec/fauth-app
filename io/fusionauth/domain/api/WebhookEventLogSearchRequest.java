package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.search.WebhookEventLogSearchCriteria;

public class WebhookEventLogSearchRequest {
  public WebhookEventLogSearchCriteria search = new WebhookEventLogSearchCriteria();
  
  @JacksonConstructor
  public WebhookEventLogSearchRequest() {}
  
  public WebhookEventLogSearchRequest(WebhookEventLogSearchCriteria paramWebhookEventLogSearchCriteria) {
    this.search = paramWebhookEventLogSearchCriteria;
  }
}
