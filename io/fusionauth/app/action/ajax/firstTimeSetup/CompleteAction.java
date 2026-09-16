package io.fusionauth.app.action.ajax.firstTimeSetup;

import io.fusionauth.api.service.system.InstanceService;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import javax.inject.Inject;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin"})
public class CompleteAction extends BaseAJAXAction {
  private final InstanceService instanceService;
  
  @Inject
  public CompleteAction(FrontEndSupport paramFrontEndSupport, InstanceService paramInstanceService) {
    super(paramFrontEndSupport);
    this.instanceService = paramInstanceService;
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    this.instanceService.firstTimeSetupComplete();
    writeAuditLog("Completed the first time setup wizard.");
    return "success";
  }
}
