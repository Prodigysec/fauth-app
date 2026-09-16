package io.fusionauth.app.action.ajax.userAction;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.LocalizedStrings;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "user_action_manager"})
public class ValidateOptionAction extends BaseAJAXAction {
  public LocalizedStrings localizedNames = new LocalizedStrings();
  
  public String name;
  
  @Inject
  public ValidateOptionAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    Errors errors = (new Validator()).notBlank(this.name, "name", new Object[0]).done();
    if (errors.size() > 0) {
      this.frontEndSupport.transfer(errors);
      return "input";
    } 
    return "success";
  }
  
  @PostParameterMethod
  public void postParameterMethod() {
    this.localizedNames.normalize();
    this.localizedNames.removeEmpty();
  }
}
