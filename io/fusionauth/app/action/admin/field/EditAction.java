package io.fusionauth.app.action.admin.field;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.CollectionTools;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.FormFieldRequest;
import io.fusionauth.domain.api.FormFieldResponse;
import io.fusionauth.domain.form.FormField;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{fieldId}", requiresAuthentication = true, constraints = {"admin", "form_manager"})
@List({@Redirect(code = "success", uri = "/admin/field/"), @Redirect(code = "api-error", uri = "/admin/field/")})
public class EditAction extends BaseFormAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.field = ((FormFieldResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveFormField(this.fieldId))).field;
    this.options = CollectionTools.collectionToString(this.field.options);
    return "input";
  }
  
  public String post() {
    FormField formField1 = ((FormFieldResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveFormField(this.fieldId))).field;
    this.field.data.clear();
    this.field.data.putAll(formField1.data);
    FormField formField2 = ((FormFieldResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateFormField(this.fieldId, new FormFieldRequest(this.field)))).field;
    writeAuditLogForUpdate("Updated the form field with Id [" + String.valueOf(formField2.id) + "] and name [" + formField2.name + "]", formField1, formField2);
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    validateFieldOptions();
  }
}
