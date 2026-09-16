package io.fusionauth.app.action.email;

import com.google.inject.Inject;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.BaseSentAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import org.primeframework.mvc.action.annotation.Action;

@Action
public class SentAction extends BaseSentAction {
  public final boolean emailSent = true;
  
  public String email;
  
  @Inject
  public SentAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    return checkLoginId(this.email);
  }
  
  protected String restartURL() {
    return "/email/verify";
  }
}
