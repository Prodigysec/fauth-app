package io.fusionauth.domain.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.client.json.MessageTemplateRequestDeserializer;
import io.fusionauth.domain.message.MessageTemplate;

@JsonDeserialize(using = MessageTemplateRequestDeserializer.class)
public class MessageTemplateRequest {
  public MessageTemplate messageTemplate;
  
  @JacksonConstructor
  public MessageTemplateRequest() {}
  
  public MessageTemplateRequest(MessageTemplate paramMessageTemplate) {
    this.messageTemplate = paramMessageTemplate;
  }
}
