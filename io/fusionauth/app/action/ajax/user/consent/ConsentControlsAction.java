package io.fusionauth.app.action.ajax.user.consent;

import com.google.inject.Inject;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
public class ConsentControlsAction extends AddAction {
  @Inject
  public ConsentControlsAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    prepareForm();
    return "render";
  }
}
