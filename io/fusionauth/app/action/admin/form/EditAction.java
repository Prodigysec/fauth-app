package io.fusionauth.app.action.admin.form;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.FormRequest;
import io.fusionauth.domain.api.FormResponse;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormStep;
import io.fusionauth.domain.form.FormStepType;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(value = "{formId}", requiresAuthentication = true, constraints = {"admin", "form_manager"})
@List({@Redirect(code = "success", uri = "/admin/form/"), @Redirect(code = "api-error", uri = "/admin/form/")})
public class EditAction extends BaseFormAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    paramFrontEndSupport.customPreTransferErrorConsumer = (paramErrors -> paramFrontEndSupport.moveFieldErrorsToGeneral(paramErrors, new String[] { "form.id" }));
  }
  
  public String get() {
    this.form = ((FormResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveForm(this.formId))).form;
    this.verifyEmailIncluded = this.form.steps.stream().anyMatch(paramFormStep -> (paramFormStep.type == FormStepType.verifyEmail));
    this.verifyPhoneIncluded = this.form.steps.stream().anyMatch(paramFormStep -> (paramFormStep.type == FormStepType.verifyPhoneNumber));
    return "input";
  }
  
  public String post() {
    Form form1 = ((FormResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveForm(this.formId))).form;
    this.form.data.clear();
    this.form.data.putAll(form1.data);
    Form form2 = ((FormResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateForm(this.formId, new FormRequest(this.form)))).form;
    writeAuditLogForUpdate("Updated the form with Id [" + String.valueOf(this.formId) + "] and name [" + form2.name + "]", form1, form2);
    return "success";
  }
}
