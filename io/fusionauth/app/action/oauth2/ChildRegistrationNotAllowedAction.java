package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.BaseThemedAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.api.FamilyEmailRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;

@Action
@Redirect(uri = "/oauth2/child-registration-not-allowed-complete?client_id=${client_id}&tenantId=${tenantId}")
public class ChildRegistrationNotAllowedAction extends BaseThemedAction {
  public String parentEmail;
  
  @Inject
  public ChildRegistrationNotAllowedAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    this.client.sendFamilyRequestEmail(new FamilyEmailRequest(this.parentEmail));
    return "success";
  }
}
