package io.fusionauth.domain.api;

import io.fusionauth.domain.EventInfo;

public abstract class BaseEventRequest {
  public EventInfo eventInfo;
  
  public BaseEventRequest() {}
  
  public BaseEventRequest(EventInfo paramEventInfo) {
    this.eventInfo = paramEventInfo;
  }
}
