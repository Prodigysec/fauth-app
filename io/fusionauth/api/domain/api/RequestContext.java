package io.fusionauth.api.domain.api;

import java.util.Objects;

public class RequestContext {
  private static final ThreadLocal<RequestContext> INSTANCE = new ThreadLocal<>();
  
  public final String path;
  
  public RequestContext(String paramString) {
    this.path = Objects.<String>requireNonNull(paramString);
  }
  
  public static void clear() {
    INSTANCE.remove();
  }
  
  public static RequestContext get() {
    return INSTANCE.get();
  }
  
  public static void set(RequestContext paramRequestContext) {
    INSTANCE.set(paramRequestContext);
  }
}
