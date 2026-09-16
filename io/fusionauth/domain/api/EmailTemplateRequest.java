package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.email.EmailTemplate;

public class EmailTemplateRequest {
  public EmailTemplate emailTemplate;
  
  @JacksonConstructor
  public EmailTemplateRequest() {}
  
  public EmailTemplateRequest(EmailTemplate paramEmailTemplate) {
    this.emailTemplate = paramEmailTemplate;
  }
}
