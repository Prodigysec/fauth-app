package io.fusionauth.app.action.admin;

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.ReexecuteSavedRequest;

@Action
@ReexecuteSavedRequest(code = "success", uri = "/admin/")
public class SavedRequestAction {
  public String get() {
    return "success";
  }
}
