package io.fusionauth.app.action.api.email.template;

import com.google.inject.Inject;
import com.inversoft.error.Error;
import io.fusionauth.api.util.EmailTools;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.PreviewRequest;
import io.fusionauth.domain.api.PreviewResponse;
import io.fusionauth.domain.email.Email;
import io.fusionauth.domain.email.EmailAddress;
import java.util.List;
import org.primeframework.email.domain.BaseResult;
import org.primeframework.email.domain.PreviewResult;
import org.primeframework.email.domain.RawEmailTemplates;
import org.primeframework.email.service.EmailService;
import org.primeframework.email.service.PreviewEmailBuilder;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class PreviewAction extends BaseAPIAction {
  private final EmailService emailService;
  
  @JSONRequest
  public PreviewRequest request = new PreviewRequest();
  
  @JSONResponse
  public PreviewResponse response = new PreviewResponse();
  
  @Inject
  public PreviewAction(FrontEndSupport paramFrontEndSupport, EmailService paramEmailService) {
    super(paramFrontEndSupport);
    this.emailService = paramEmailService;
  }
  
  public String post() {
    RawEmailTemplates rawEmailTemplates = EmailTools.toRawEmailTemplates(this.request.emailTemplate, this.request.locale);
    PreviewResult previewResult = ((PreviewEmailBuilder)this.emailService.preview(null, rawEmailTemplates).withTemplateParameters(EmailTools.MOCK_PARAMETERS)).go();
    this.response.errors = EmailTools.translateErrors((BaseResult)previewResult);
    this.response.errors.fieldErrors.forEach((paramString, paramList) -> paramList.forEach(()));
    this.response.errors.generalErrors.forEach(paramError -> paramError.message = this.frontEndSupport.messageProvider.getMessage(paramError.code, paramError.values));
    if (previewResult.email != null)
      this.response.email = EmailTools.convert(previewResult.email); 
    fillInEmail(this.response.email, rawEmailTemplates);
    if (this.response.email.from == null)
      this.response.email.from = new EmailAddress(); 
    this.response.email.from.address = this.request.emailTemplate.fromEmail;
    return "render";
  }
  
  private void fillInEmail(Email paramEmail, RawEmailTemplates paramRawEmailTemplates) {
    if (paramEmail.from == null || paramEmail.from.display == null)
      paramEmail.from = new EmailAddress(this.request.emailTemplate.fromEmail, paramRawEmailTemplates.fromDisplay); 
    if (paramEmail.html == null)
      paramEmail.html = paramRawEmailTemplates.html; 
    if (paramEmail.subject == null)
      paramEmail.subject = paramRawEmailTemplates.subject; 
    if (paramEmail.text == null)
      paramEmail.text = paramRawEmailTemplates.text; 
  }
}
