package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.email.EmailTemplate;
import java.util.List;

public class EmailTemplateResponse {
  public EmailTemplate emailTemplate;
  
  public List<EmailTemplate> emailTemplates;
  
  @JacksonConstructor
  public EmailTemplateResponse() {}
  
  public EmailTemplateResponse(EmailTemplate paramEmailTemplate) {
    this.emailTemplate = paramEmailTemplate;
  }
  
  public EmailTemplateResponse(List<EmailTemplate> paramList) {
    this.emailTemplates = paramList;
  }
}
