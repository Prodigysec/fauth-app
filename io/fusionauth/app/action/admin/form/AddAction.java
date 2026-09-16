package io.fusionauth.app.action.admin.form;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.FormRequest;
import io.fusionauth.domain.api.FormResponse;
import io.fusionauth.domain.form.Form;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(requiresAuthentication = true, constraints = {"admin", "form_manager"})
@List({@Redirect(code = "success", uri = "/admin/form/"), @Redirect(code = "api-error", uri = "/admin/form/")})
public class AddAction extends BaseFormAction {
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    if (this.formId != null) {
      this.form = ((FormResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveForm(this.formId))).form;
      this.form.id = null;
      this.formId = null;
      this.form.name += " - copy";
    } 
    return "input";
  }
  
  public String post() {
    Form form = ((FormResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createForm(this.formId, new FormRequest(this.form)))).form;
    writeAuditLog("Created the form with Id [" + String.valueOf(form.id) + "] and name [" + this.form.name + "]");
    return "success";
  }
}
