package io.fusionauth.app.action.admin;

import com.google.inject.Inject;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;

@Action
@Status(code = "success", status = 200)
public class SingleLogoutAction extends BaseAction {
  @Inject
  public SingleLogoutAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.frontEndSupport.userLoginSecurityContext.logout(this.frontEndSupport.buildEventInfo(null));
    return "success";
  }
}
