package io.fusionauth.api.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.event.BaseEvent;
import io.fusionauth.domain.event.EventType;
import io.fusionauth.domain.event.NonTransactionalEvent;
import java.util.Objects;

public class TenantAuditLogCreateEvent extends BaseEvent implements Buildable<TenantAuditLogCreateEvent>, NonTransactionalEvent {
  public AuditLog auditLog;
  
  @JacksonConstructor
  public TenantAuditLogCreateEvent() {}
  
  public TenantAuditLogCreateEvent(EventInfo paramEventInfo, AuditLog paramAuditLog) {
    super(paramEventInfo);
    this.auditLog = paramAuditLog;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    TenantAuditLogCreateEvent tenantAuditLogCreateEvent = (TenantAuditLogCreateEvent)paramObject;
    return Objects.equals(this.auditLog, tenantAuditLogCreateEvent.auditLog);
  }
  
  public EventType getType() {
    return EventType.AuditLogCreate;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.auditLog });
  }
}
