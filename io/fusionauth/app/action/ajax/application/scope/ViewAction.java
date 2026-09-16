package io.fusionauth.app.action.ajax.application.scope;

import com.google.inject.Inject;
import io.fusionauth.app.action.ajax.application.BaseApplicationAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "application_manager"})
public class ViewAction extends BaseApplicationAJAXAction {
  @Inject
  protected ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    retrieveScope();
    return "render";
  }
}
