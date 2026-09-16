package io.fusionauth.app.action;

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;

@Action
@Forward(code = "missing", page = "/errors/404.ftl", status = 404)
public class UnauthorizedAction {
  public String get() {
    return "missing";
  }
}
