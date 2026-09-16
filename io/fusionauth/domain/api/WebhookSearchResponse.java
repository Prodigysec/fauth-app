package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

public class WebhookSearchResponse {
  public long total;
  
  public List<Webhook> webhooks;
  
  @JacksonConstructor
  public WebhookSearchResponse() {}
  
  public WebhookSearchResponse(SearchResults<Webhook> paramSearchResults) {
    this.webhooks = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
