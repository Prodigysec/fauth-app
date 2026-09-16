package io.fusionauth.app.action.ajax.application;

import com.google.inject.Inject;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.UserRegistration;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{applicationId}", requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager", "user_support_viewer"})
public class RolesAction extends BaseApplicationAJAXAction {
  public UserRegistration registration = new UserRegistration();
  
  public String render = "checkboxList";
  
  @Inject
  public RolesAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    retrieveApplication();
    this.application.roles.sort((paramApplicationRole1, paramApplicationRole2) -> (paramApplicationRole1.isSuperRole == paramApplicationRole2.isSuperRole) ? paramApplicationRole1.getDisplay().compareTo(paramApplicationRole2.getDisplay()) : (paramApplicationRole1.isSuperRole ? 0 : 1));
    return "render";
  }
}
