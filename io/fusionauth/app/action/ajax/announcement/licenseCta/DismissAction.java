package io.fusionauth.app.action.ajax.announcement.licenseCta;

import com.google.inject.Inject;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.user.UserPreferencesFrontendService;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin"})
public class DismissAction extends BaseAJAXAction {
  private final UserPreferencesFrontendService userPreferencesFrontendService;
  
  @Inject
  public DismissAction(FrontEndSupport paramFrontEndSupport, UserPreferencesFrontendService paramUserPreferencesFrontendService) {
    super(paramFrontEndSupport);
    this.userPreferencesFrontendService = paramUserPreferencesFrontendService;
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    this.userPreferencesFrontendService.dismissLicenseCTA(this.codeCurrentUser);
    return "success";
  }
}
