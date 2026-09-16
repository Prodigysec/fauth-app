package io.fusionauth.app.action.ajax.email.template;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.util.EmailTools;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.EmailTemplateResponse;
import io.fusionauth.domain.email.Email;
import io.fusionauth.domain.email.EmailTemplate;
import java.util.UUID;
import org.primeframework.email.domain.PreviewResult;
import org.primeframework.email.domain.RawEmailTemplates;
import org.primeframework.email.service.EmailService;
import org.primeframework.email.service.PreviewEmailBuilder;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{emailTemplateId}", requiresAuthentication = true, constraints = {"admin", "email_template_manager"})
public class ViewAction extends BaseAJAXAction {
  private final EmailService emailService;
  
  public EmailTemplate emailTemplate;
  
  public UUID emailTemplateId;
  
  public Email renderedEmail;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport, EmailService paramEmailService) {
    super(paramFrontEndSupport);
    this.emailService = paramEmailService;
  }
  
  public String get() {
    this.emailTemplate = ((EmailTemplateResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEmailTemplate(this.emailTemplateId))).emailTemplate;
    RawEmailTemplates rawEmailTemplates = EmailTools.toRawEmailTemplates(this.emailTemplate, null);
    PreviewResult previewResult = ((PreviewEmailBuilder)this.emailService.preview(null, rawEmailTemplates).withTemplateParameters(EmailTools.MOCK_PARAMETERS)).go();
    this.renderedEmail = EmailTools.convert(previewResult.email);
    return "render";
  }
}
