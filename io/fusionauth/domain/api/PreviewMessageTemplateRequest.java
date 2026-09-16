package io.fusionauth.domain.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.client.json.PreviewMessageTemplateRequestDeserializer;
import io.fusionauth.domain.message.MessageTemplate;
import java.util.Locale;

@JsonDeserialize(using = PreviewMessageTemplateRequestDeserializer.class)
public class PreviewMessageTemplateRequest {
  public Locale locale;
  
  public MessageTemplate messageTemplate;
  
  @JacksonConstructor
  public PreviewMessageTemplateRequest() {}
  
  public PreviewMessageTemplateRequest(MessageTemplate paramMessageTemplate, Locale paramLocale) {
    this.messageTemplate = paramMessageTemplate;
    this.locale = paramLocale;
  }
}
