package io.fusionauth.api.service.system.eventLog;

import com.inversoft.json.ToString;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.primeframework.mvc.cors.CORSDebugger;

public class Debugger implements CORSDebugger {
  final StringBuilder sb = new StringBuilder();
  
  private final boolean debugEnabled;
  
  private String defaultValue = "-";
  
  private boolean done;
  
  private boolean timeStamp = true;
  
  public Debugger(boolean paramBoolean, String paramString) {
    this.debugEnabled = paramBoolean;
    if (paramBoolean)
      this.sb.append(paramString)
        .append("\n\n"); 
  }
  
  public static String buildMessageFromResponse(String paramString, ClientResponse<?, ?> paramClientResponse) {
    StringBuilder stringBuilder = (new StringBuilder()).append(paramString).append("\nStatus code [").append(paramClientResponse.status).append("]\n");
    if (paramClientResponse.exception != null) {
      stringBuilder.append("\nException encountered.\n\n")
        .append(paramClientResponse.exception.getClass().getCanonicalName())
        .append(" : ")
        .append("Message: ").append(paramClientResponse.exception.getMessage());
      Throwable throwable = paramClientResponse.exception.getCause();
      if (throwable != null)
        stringBuilder.append("\nCause: ")
          .append(throwable.getClass().getCanonicalName())
          .append("\nMessage: ").append(throwable.getMessage()); 
    } 
    if (paramClientResponse.errorResponse != null)
      try {
        stringBuilder.append("\nError response is \n").append(ToString.toString(paramClientResponse.errorResponse));
      } catch (Exception exception) {
        stringBuilder.append("\nError response is \n").append(paramClientResponse.errorResponse);
      }  
    return stringBuilder.toString();
  }
  
  public Debugger disableTimestamp() {
    this.timeStamp = false;
    return this;
  }
  
  public void done() {
    if (this.done)
      return; 
    this.done = true;
    if (this.debugEnabled)
      EventLogHelper.create(new EventLog(EventLogType.Debug, this.sb.toString())); 
  }
  
  public void handleError(ClientResponse<?, ?> paramClientResponse, String paramString) {
    log("The response was not successful, see the error event log.")
      .done();
    EventLogHelper.create(new EventLog(EventLogType.Error, buildMessageFromResponse(paramString, paramClientResponse)));
  }
  
  public Debugger log(String paramString) {
    if (this.debugEnabled) {
      if (this.timeStamp)
        this.sb.append(DateTimeFormatter.ofPattern("M/d/yyyy hh:mm:ss a z").format(ZonedDateTime.now(ZoneOffset.UTC)))
          .append(" "); 
      this.sb.append(paramString)
        .append("\n");
    } 
    return this;
  }
  
  public CORSDebugger log(String paramString, Object... paramVarArgs) {
    Object[] arrayOfObject = new Object[paramVarArgs.length];
    for (byte b = 0; b < paramVarArgs.length; b++)
      arrayOfObject[b] = (paramVarArgs[b] != null) ? paramVarArgs[b] : this.defaultValue; 
    return log(String.format(paramString, arrayOfObject));
  }
  
  public Debugger log(Exception paramException) {
    if (paramException == null)
      return this; 
    if (this.debugEnabled)
      this.sb.append("\nException encountered.\n\n")
        .append(paramException.getClass().getCanonicalName())
        .append(" : ")
        .append("Message: ").append(paramException.getMessage())
        .append("\n"); 
    return this;
  }
  
  public Debugger logObjectToJSON(Object paramObject) {
    try {
      return log(ToString.toString(paramObject));
    } catch (Exception exception) {
      return log(paramObject.toString());
    } 
  }
  
  public Debugger logObjectToJSON(String paramString, Object paramObject) {
    if (paramObject == null)
      return this; 
    try {
      return log(paramString + paramString);
    } catch (Exception exception) {
      return log(paramString + paramString);
    } 
  }
  
  public String toString() {
    return this.sb.toString();
  }
  
  public Debugger withDefaultValue(String paramString) {
    this.defaultValue = paramString;
    return this;
  }
}
