package io.fusionauth.app.action.admin;

import com.google.inject.Inject;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;

@Action
@Redirect(uri = "/oauth2/logout?client_id=${fusionAuthId}&post_logout_redirect_uri=${postLogoutRedirectURI}")
public class LogoutAction extends BaseAction {
  public String postLogoutRedirectURI;
  
  @Inject
  public LogoutAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    this.postLogoutRedirectURI = "/admin/";
  }
  
  public String get() {
    this.frontEndSupport.userLoginSecurityContext.logout(this.frontEndSupport.buildEventInfo(null));
    return "success";
  }
}
