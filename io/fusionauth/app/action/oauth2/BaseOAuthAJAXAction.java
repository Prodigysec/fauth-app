package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.JSON.List;
import org.primeframework.mvc.action.result.annotation.Status;

@List({@JSON(code = "render-json", status = 200), @JSON(code = "render-input-json", status = 400), @JSON(code = "input", status = 400), @JSON(code = "api-error", status = 400), @JSON(code = "render-error-json", status = 500)})
@Status(code = "disabled", status = 403)
public abstract class BaseOAuthAJAXAction extends BaseOAuthAction {
  @Inject
  public BaseOAuthAJAXAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
  }
}
