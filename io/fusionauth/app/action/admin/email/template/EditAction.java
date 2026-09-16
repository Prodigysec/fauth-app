package io.fusionauth.app.action.admin.email.template;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.EmailTemplateRequest;
import io.fusionauth.domain.api.EmailTemplateResponse;
import io.fusionauth.domain.email.EmailTemplate;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(requiresAuthentication = true, value = "{emailTemplateId}", constraints = {"admin", "email_template_manager"})
@List({@Redirect(code = "missing", uri = "/admin/email/template/"), @Redirect(code = "success", uri = "/admin/email/template/")})
public class EditAction extends FormAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.emailTemplate = ((EmailTemplateResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEmailTemplate(this.emailTemplateId))).emailTemplate;
    return "input";
  }
  
  public String post() {
    EmailTemplate emailTemplate1 = ((EmailTemplateResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEmailTemplate(this.emailTemplateId))).emailTemplate;
    EmailTemplate emailTemplate2 = ((EmailTemplateResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateEmailTemplate(this.emailTemplateId, new EmailTemplateRequest(this.emailTemplate)))).emailTemplate;
    writeAuditLogForUpdate("Update the email template with Id [" + String.valueOf(this.emailTemplateId) + "] and name [" + emailTemplate2.name + "]", emailTemplate1, emailTemplate2);
    return "success";
  }
}
