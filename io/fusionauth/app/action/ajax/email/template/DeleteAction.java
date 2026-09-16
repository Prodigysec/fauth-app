package io.fusionauth.app.action.ajax.email.template;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.EmailTemplateResponse;
import io.fusionauth.domain.email.EmailTemplate;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;

@Action(value = "{emailTemplateId}", requiresAuthentication = true, constraints = {"admin", "email_template_manager"})
public class DeleteAction extends BaseAJAXAction {
  public EmailTemplate emailTemplate;
  
  public UUID emailTemplateId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteEmailTemplate(this.emailTemplateId));
    writeAuditLog("Deleted the email template with id[ [" + String.valueOf(this.emailTemplateId) + "] and name [" + this.emailTemplate.name + "]");
    return "success";
  }
  
  @PostValidationMethod
  public void postValidate() {
    this.emailTemplate = ((EmailTemplateResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEmailTemplate(this.emailTemplateId))).emailTemplate;
  }
}
