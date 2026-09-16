package io.fusionauth.app.action.tenantManager;

import io.fusionauth.app.guice.TenantManagerApplicationId;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.util.QueryStringBuilder;

@Action
@Redirect(uri = "${redirectToLogoutURI}")
public class LogoutAction {
  private final UUID client_id;
  
  private final UserLoginSecurityContext userLoginSecurityContext;
  
  public String post_logout_redirect_uri;
  
  public String redirectToLogoutURI;
  
  public UUID tenantId;
  
  @Inject
  public LogoutAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, @TenantManagerApplicationId UUID paramUUID) {
    this.client_id = paramUUID;
    this.userLoginSecurityContext = paramUserLoginSecurityContext;
  }
  
  public String get() {
    this.userLoginSecurityContext.logout();
    this


      
      .redirectToLogoutURI = QueryStringBuilder.builder("/oauth2/logout").with("client_id", this.client_id).with("post_logout_redirect_uri", "/tenant-manager/?tenantId=" + String.valueOf(this.tenantId)).with("tenantId", this.tenantId).build();
    return "success";
  }
}
