package io.fusionauth.app.action.account;

import com.google.inject.Inject;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.util.QueryStringBuilder;

@Action
@Redirect(uri = "${logoutRedirectURI}")
public class LogoutAction extends BaseAction {
  public String client_id;
  
  @FTLVariable
  public String logoutRedirectURI;
  
  @Inject
  public LogoutAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this


      
      .logoutRedirectURI = QueryStringBuilder.builder("/oauth2/logout").with("client_id", this.client_id).with("tenantId", this.tenantId).build();
    this.frontEndSupport.userLoginSecurityContext.logout();
    return "success";
  }
}
