package io.fusionauth.app.action.app;

import com.google.inject.Inject;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;

@Action("{client_id}")
public class RegisterAction extends BaseAppRedirectAction {
  @Inject
  public RegisterAction(FrontEndSupport paramFrontEndSupport, OAuthService paramOAuthService) {
    super("/oauth2/register", paramFrontEndSupport, paramOAuthService);
  }
}
