package io.fusionauth.app.action.ajax.message.template;

import com.google.inject.Inject;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "message_template_manager"})
public class DeleteLocalizationAction extends BaseAJAXAction {
  @Inject
  public DeleteLocalizationAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String post() {
    return "success";
  }
}
