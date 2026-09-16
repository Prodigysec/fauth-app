package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.search.WebhookSearchCriteria;

public class WebhookSearchRequest {
  public WebhookSearchCriteria search = new WebhookSearchCriteria();
  
  @JacksonConstructor
  public WebhookSearchRequest() {}
  
  public WebhookSearchRequest(WebhookSearchCriteria paramWebhookSearchCriteria) {
    this.search = paramWebhookSearchCriteria;
  }
}
