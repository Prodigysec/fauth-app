package io.fusionauth.app.action.ajax.form;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.FormResponse;
import io.fusionauth.domain.form.Form;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{formId}", requiresAuthentication = true, constraints = {"admin", "form_manager"})
public class ViewAction extends BaseAJAXAction {
  public Form form;
  
  public UUID formId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.form = ((FormResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveForm(this.formId))).form;
    return "render";
  }
}
