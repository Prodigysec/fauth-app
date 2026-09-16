package io.fusionauth.app.action.app;

import com.google.inject.Inject;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.http.Cookie;
import java.net.URI;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.util.QueryStringBuilder;
import org.primeframework.mvc.validation.ValidationMethod;

@Action("{client_id}")
@Redirect(code = "redirect-to-logout", uri = "${redirectToLogoutURI}")
public class LogoutAction extends BaseAppAction {
  public URI post_logout_redirect_uri;
  
  public String redirectToLogoutURI;
  
  @Inject
  public LogoutAction(FrontEndSupport paramFrontEndSupport, OAuthService paramOAuthService) {
    super(paramFrontEndSupport, paramOAuthService);
  }
  
  public String get() {
    deleteCookies(new String[] { "app.at", "app.rt", "app.idt", "app.at_exp" });
    Cookie cookie = this.frontEndSupport.getCookie("app.idt");
    String str = (cookie != null) ? cookie.value : null;
    this



      
      .redirectToLogoutURI = QueryStringBuilder.builder("/oauth2/logout").with("client_id", this.client_id).with("id_token_hint", str).with("post_logout_redirect_uri", this.post_logout_redirect_uri).with("tenantId", (this.tenantId != null) ? this.tenantId : ((this.codeTenant != null) ? this.codeTenant.id : null)).build();
    return "redirect-to-logout";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    validateOrigin();
    OAuthService.OAuthValidationResult oAuthValidationResult = this.oauthService.validateAppLogoutRequest(this.client_id);
    this.codeApplication = oAuthValidationResult.application;
    this.codeTenant = oAuthValidationResult.tenant;
  }
}
