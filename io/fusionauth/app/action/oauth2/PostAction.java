package io.fusionauth.app.action.oauth2;

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;

@Action
@Forward(code = "missing", page = "/errors/404.ftl", status = 404)
public class PostAction {
  public String get() {
    return "missing";
  }
}
