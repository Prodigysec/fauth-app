package io.fusionauth.api.service.lambda.graal.domain;

import io.fusionauth.api.domain.LambdaConsoleAdapter;
import io.fusionauth.api.service.lambda.graal.GraalHelper;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

public class LambdaConsole extends AbstractProxyObject implements LambdaConsoleAdapter {
  public final StringBuilder debug = new StringBuilder();
  
  public final StringBuilder error = new StringBuilder();
  
  public final StringBuilder info = new StringBuilder();
  
  private final boolean debugEnabled;
  
  public LambdaConsole(boolean paramBoolean) {
    this.debugEnabled = paramBoolean;
    this.members.put("info", new ConsoleWriter(this.info));
    this.members.put("log", this.members.get("info"));
    this.members.put("debug", new ConsoleWriter(this.debug));
    this.members.put("error", new ConsoleWriter(this.error));
  }
  
  public String cutDebugLog() {
    return this.debug.toString();
  }
  
  public String cutErrorLog() {
    return this.error.toString();
  }
  
  public String cutInfoLog() {
    return this.info.toString();
  }
  
  public boolean isDebugEnabled() {
    return this.debugEnabled;
  }
  
  public static class ConsoleWriter implements ProxyExecutable {
    private final StringBuilder builder;
    
    public ConsoleWriter(StringBuilder param1StringBuilder) {
      this.builder = param1StringBuilder;
    }
    
    public Object execute(Value... param1VarArgs) {
      for (Value value : param1VarArgs)
        this.builder.append(GraalHelper.stringify(value)); 
      if (param1VarArgs.length > 0)
        this.builder.append("\n"); 
      return null;
    }
  }
}
