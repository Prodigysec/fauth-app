package io.fusionauth.app.action.tenantManager;

import com.google.inject.name.Named;
import javax.inject.Inject;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.security.UserLoginSecurityContext;

@Action
@Status(code = "success", status = 200)
public class SingleLogoutAction {
  private final UserLoginSecurityContext securityContext;
  
  @Inject
  public SingleLogoutAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext) {
    this.securityContext = paramUserLoginSecurityContext;
  }
  
  public String get() {
    this.securityContext.logout();
    return "success";
  }
}
