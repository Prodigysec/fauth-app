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
import org.primeframework.mvc.validation.annotation.PostValidationMethod;

@Action(value = "{formId}", requiresAuthentication = true, constraints = {"admin", "form_deleter"})
public class DeleteAction extends BaseAJAXAction {
  public Form form;
  
  public UUID formId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteForm(this.formId));
    writeAuditLog("Deleted the form with Id [" + String.valueOf(this.formId) + "] and name [" + this.form.name + "]");
    return "success";
  }
  
  @PostValidationMethod
  public void postValidate() {
    this.form = ((FormResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveForm(this.formId))).form;
  }
}
