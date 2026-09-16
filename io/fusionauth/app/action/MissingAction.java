package io.fusionauth.app.action;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Status;

@Action
@Forward(code = "missing", status = 404, page = "/errors/404.ftl")
@Status(code = "status", status = 404)
public class MissingAction {
  private final HTTPRequest request;
  
  @Inject
  public MissingAction(HTTPRequest paramHTTPRequest) {
    this.request = paramHTTPRequest;
  }
  
  public String get() {
    if (this.request.getPath().startsWith("/api"))
      return "status"; 
    return "missing";
  }
}
