package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

public class EventLogSearchResponse {
  public List<EventLog> eventLogs;
  
  public long total;
  
  @JacksonConstructor
  public EventLogSearchResponse() {}
  
  public EventLogSearchResponse(SearchResults<EventLog> paramSearchResults) {
    this.eventLogs = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
