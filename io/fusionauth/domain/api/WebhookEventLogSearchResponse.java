package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.WebhookEventLog;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

public class WebhookEventLogSearchResponse {
  public long total;
  
  public List<WebhookEventLog> webhookEventLogs;
  
  @JacksonConstructor
  public WebhookEventLogSearchResponse() {}
  
  public WebhookEventLogSearchResponse(SearchResults<WebhookEventLog> paramSearchResults) {
    this.webhookEventLogs = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
