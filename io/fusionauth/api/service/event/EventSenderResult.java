package io.fusionauth.api.service.event;

import com.inversoft.json.ToString;
import java.util.Objects;

public class EventSenderResult {
  public Exception exception;
  
  public String message;
  
  public EventSender sender;
  
  public int status;
  
  public boolean success;
  
  public EventSenderResult() {}
  
  public EventSenderResult(Exception paramException, String paramString, EventSender paramEventSender, int paramInt, boolean paramBoolean) {
    this.exception = paramException;
    this.message = paramString;
    this.sender = paramEventSender;
    this.status = paramInt;
    this.success = paramBoolean;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    EventSenderResult eventSenderResult = (EventSenderResult)paramObject;
    return (this.status == eventSenderResult.status && this.success == eventSenderResult.success && Objects.equals(this.exception, eventSenderResult.exception) && Objects.equals(this.message, eventSenderResult.message) && Objects.equals(this.sender, eventSenderResult.sender));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.exception, this.message, this.sender, Integer.valueOf(this.status), Boolean.valueOf(this.success) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
