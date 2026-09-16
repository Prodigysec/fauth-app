package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.email.EmailTemplate;
import java.util.Locale;

public class PreviewRequest {
  public EmailTemplate emailTemplate;
  
  public Locale locale;
  
  @JacksonConstructor
  public PreviewRequest() {}
  
  public PreviewRequest(EmailTemplate paramEmailTemplate, Locale paramLocale) {
    this.emailTemplate = paramEmailTemplate;
    this.locale = paramLocale;
  }
}
