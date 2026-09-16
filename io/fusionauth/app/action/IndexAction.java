package io.fusionauth.app.action;

import com.google.inject.Inject;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;

@Action
@Redirect(code = "setup-wizard", uri = "/admin/setup-wizard")
public class IndexAction extends BaseThemedAction {
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    if (!(this.frontEndSupport.getInstance()).setupComplete)
      return "setup-wizard"; 
    return "input";
  }
}
