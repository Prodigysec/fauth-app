package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.EventLog;

public class EventLogResponse {
  public EventLog eventLog;
  
  @JacksonConstructor
  public EventLogResponse() {}
  
  public EventLogResponse(EventLog paramEventLog) {
    this.eventLog = paramEventLog;
  }
}
