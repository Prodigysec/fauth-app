package io.fusionauth.app.action.admin.field;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.CollectionTools;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.FormFieldRequest;
import io.fusionauth.domain.api.FormFieldResponse;
import io.fusionauth.domain.form.FormField;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "form_manager"})
@List({@Redirect(code = "success", uri = "/admin/field/"), @Redirect(code = "api-error", uri = "/admin/field/")})
public class AddAction extends BaseFormAction {
  @FTLVariable
  public String customKey;
  
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    if (this.fieldId != null) {
      this.field = ((FormFieldResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveFormField(this.fieldId))).field;
      this.field.id = null;
      this.fieldId = null;
      this.field.name += " - copy";
      this.options = CollectionTools.collectionToString(this.field.options);
    } 
    return "input";
  }
  
  @PostParameterMethod
  public void normalizeKeys() {
    if (this.frontEndSupport.isPOST() && 
      this.field.key != null && this.customKey != null)
      this.field.key += this.field.key; 
  }
  
  public String post() {
    FormField formField = ((FormFieldResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createFormField(this.fieldId, new FormFieldRequest(this.field)))).field;
    writeAuditLog("Created the form field with Id [" + String.valueOf(formField.id) + "] and name [" + this.field.name + "]");
    return "success";
  }
  
  @FormPrepareMethod
  public void prepare() {
    if (this.field.key != null)
      if (this.field.key.startsWith("user.data.")) {
        this.customKey = this.field.key.substring(10);
        this.field.key = "user.data.";
      } else if (this.field.key.startsWith("registration.data.")) {
        this.customKey = this.field.key.substring(18);
        this.field.key = "registration.data.";
      }  
  }
  
  @ValidationMethod
  public void validate() {
    if (this.field.key != null && (
      this.field.key.equals("user.data.") || this.field.key.equals("registration.data.")))
      this.frontEndSupport.addFieldError("customKey", "[blank]customKey", new Object[0]); 
    validateFieldOptions();
  }
}
