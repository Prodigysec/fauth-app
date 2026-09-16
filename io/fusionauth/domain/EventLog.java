package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.Objects;

public class EventLog implements Buildable<EventLog> {
  public Long id;
  
  public ZonedDateTime insertInstant;
  
  public String message;
  
  public EventLogType type;
  
  public EventLog(String paramString) {
    this.message = paramString;
  }
  
  public EventLog(EventLogType paramEventLogType, String paramString) {
    this.message = paramString;
    this.type = paramEventLogType;
  }
  
  public EventLog(EventLogType paramEventLogType, String paramString, Throwable paramThrowable) {
    this.message = paramString;
    this.type = paramEventLogType;
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    paramThrowable.printStackTrace(printWriter);
    this.message = this.message + "\n\nException:\n" + this.message;
  }
  
  @JacksonConstructor
  public EventLog() {}
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof EventLog))
      return false; 
    EventLog eventLog = (EventLog)paramObject;
    return (Objects.equals(this.id, eventLog.id) && 
      Objects.equals(this.insertInstant, eventLog.insertInstant) && 
      Objects.equals(this.message, eventLog.message) && 
      Objects.equals(this.type, eventLog.type));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.id, this.insertInstant, this.message, this.type });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
