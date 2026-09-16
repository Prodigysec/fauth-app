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

@Action(requiresAuthentication = true, constraints = {"admin", "email_template_manager"})
@List({@Redirect(code = "success", uri = "/admin/email/template/"), @Redirect(code = "api-error", uri = "/admin/email/template/")})
public class AddAction extends FormAction {
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    if (this.emailTemplateId != null) {
      this.emailTemplate = ((EmailTemplateResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEmailTemplate(this.emailTemplateId))).emailTemplate;
      this.emailTemplate.id = null;
      this.emailTemplateId = null;
      this.emailTemplate.name += " - copy";
    } 
    return "input";
  }
  
  public String post() {
    EmailTemplate emailTemplate = ((EmailTemplateResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createEmailTemplate(this.emailTemplateId, new EmailTemplateRequest(this.emailTemplate)))).emailTemplate;
    writeAuditLog("Created the email template with Id [" + String.valueOf(emailTemplate.id) + "] and name [" + this.emailTemplate.name + "]");
    return "success";
  }
}
