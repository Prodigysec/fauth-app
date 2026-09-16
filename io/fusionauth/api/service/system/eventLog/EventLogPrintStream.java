package io.fusionauth.api.service.system.eventLog;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class EventLogPrintStream extends PrintStream {
  public boolean done;
  
  public EventLogPrintStream() {
    super(new ByteArrayOutputStream(), true);
  }
  
  public void println(String paramString) {
    super.println(paramString);
    if (this.done) {
      ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream)this.out;
      (new Debugger(true, "Email debug information")).log(byteArrayOutputStream.toString()).done();
      byteArrayOutputStream.reset();
    } else if (paramString.equals("QUIT")) {
      this.done = true;
    } 
  }
}
