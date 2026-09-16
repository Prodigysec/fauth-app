package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.search.EventLogSearchCriteria;

public class EventLogSearchRequest {
  public EventLogSearchCriteria search = new EventLogSearchCriteria();
  
  @JacksonConstructor
  public EventLogSearchRequest() {}
  
  public EventLogSearchRequest(EventLogSearchCriteria paramEventLogSearchCriteria) {
    this.search = paramEventLogSearchCriteria;
  }
}
