package io.fusionauth.domain.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.client.json.MessageTemplateResponseDeserializer;
import io.fusionauth.domain.message.MessageTemplate;
import java.util.List;

@JsonDeserialize(using = MessageTemplateResponseDeserializer.class)
public class MessageTemplateResponse {
  public MessageTemplate messageTemplate;
  
  public List<MessageTemplate> messageTemplates;
  
  @JacksonConstructor
  public MessageTemplateResponse() {}
  
  public MessageTemplateResponse(MessageTemplate paramMessageTemplate) {
    this.messageTemplate = paramMessageTemplate;
  }
  
  public MessageTemplateResponse(List<MessageTemplate> paramList) {
    this.messageTemplates = paramList;
  }
}
