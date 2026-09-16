package io.fusionauth.app.action.ajax.field;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.FormFieldResponse;
import io.fusionauth.domain.form.FormField;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{fieldId}", requiresAuthentication = true, constraints = {"admin", "form_manager"})
public class ViewAction extends BaseAJAXAction {
  public FormField field;
  
  public UUID fieldId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.field = ((FormFieldResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveFormField(this.fieldId))).field;
    return "render";
  }
}
