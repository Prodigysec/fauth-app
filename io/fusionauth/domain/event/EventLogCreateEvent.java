package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventLog;
import java.util.Objects;

public class EventLogCreateEvent extends BaseEvent implements Buildable<EventLogCreateEvent>, InstanceEvent {
  public EventLog eventLog;
  
  @JacksonConstructor
  public EventLogCreateEvent() {}
  
  public EventLogCreateEvent(EventLog paramEventLog) {
    this.eventLog = paramEventLog;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    EventLogCreateEvent eventLogCreateEvent = (EventLogCreateEvent)paramObject;
    return Objects.equals(this.eventLog, eventLogCreateEvent.eventLog);
  }
  
  public EventType getType() {
    return EventType.EventLogCreate;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.eventLog });
  }
}
