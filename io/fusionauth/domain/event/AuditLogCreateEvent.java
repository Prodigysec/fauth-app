package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import java.util.Objects;

public class AuditLogCreateEvent extends BaseEvent implements Buildable<AuditLogCreateEvent>, InstanceEvent {
  public AuditLog auditLog;
  
  @JacksonConstructor
  public AuditLogCreateEvent() {}
  
  public AuditLogCreateEvent(EventInfo paramEventInfo, AuditLog paramAuditLog) {
    super(paramEventInfo);
    this.auditLog = paramAuditLog;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    AuditLogCreateEvent auditLogCreateEvent = (AuditLogCreateEvent)paramObject;
    return Objects.equals(this.auditLog, auditLogCreateEvent.auditLog);
  }
  
  public EventType getType() {
    return EventType.AuditLogCreate;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.auditLog });
  }
}
