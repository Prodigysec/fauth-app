package io.fusionauth.app.action.ajax.field;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.application.BaseApplicationAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.FormFieldResponse;
import io.fusionauth.domain.form.FormField;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

@Action(value = "{fieldId}", requiresAuthentication = true, constraints = {"admin", "form_deleter"})
public class DeleteAction extends BaseApplicationAJAXAction {
  public FormField field;
  
  public UUID fieldId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteFormField(this.fieldId));
    writeAuditLog("Deleted the form field with Id [" + String.valueOf(this.fieldId) + "] and name [" + this.field.name + "]");
    return "success";
  }
  
  @PostParameterMethod
  public void retrieveFormField() {
    this.field = ((FormFieldResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveFormField(this.fieldId))).field;
  }
}
