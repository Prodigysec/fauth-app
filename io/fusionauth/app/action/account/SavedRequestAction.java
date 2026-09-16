package io.fusionauth.app.action.account;

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.ReexecuteSavedRequest;

@Action
@ReexecuteSavedRequest(code = "success", uri = "/account/?client_id=${client_id}&tenantId=${tenantId}")
public class SavedRequestAction {
  public String client_id;
  
  public String tenantId;
  
  public String get() {
    return "success";
  }
}
