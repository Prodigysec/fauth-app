package io.fusionauth.app.action.tenantManager;

import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.ReexecuteSavedRequest;

@Action
@ReexecuteSavedRequest(uri = "/tenant-manager/?tenantId=${tenantId}")
public class SavedRequestAction {
  public UUID tenantId;
  
  public String get() {
    return "success";
  }
}
