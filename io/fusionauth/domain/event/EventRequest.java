package io.fusionauth.domain.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.client.json.WebhookEventDeserializer;
import java.util.Objects;

public class EventRequest {
  @JsonDeserialize(using = WebhookEventDeserializer.class)
  public BaseEvent event;
  
  @JacksonConstructor
  public EventRequest() {}
  
  public EventRequest(BaseEvent paramBaseEvent) {
    this.event = paramBaseEvent;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    EventRequest eventRequest = (EventRequest)paramObject;
    return Objects.equals(this.event, eventRequest.event);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.event });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
