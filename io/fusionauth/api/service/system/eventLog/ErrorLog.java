package io.fusionauth.api.service.system.eventLog;

import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;

public class ErrorLog extends Debugger {
  public ErrorLog(String paramString) {
    this(true, paramString);
  }
  
  private ErrorLog(boolean paramBoolean, String paramString) {
    super(paramBoolean, paramString);
  }
  
  public void done() {
    EventLogHelper.create(new EventLog(EventLogType.Error, this.sb.toString()));
  }
  
  public Debugger log(String paramString) {
    this.sb.append(paramString)
      .append("\n");
    return this;
  }
}
