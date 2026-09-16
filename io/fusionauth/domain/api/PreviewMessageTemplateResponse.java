package io.fusionauth.domain.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inversoft.error.Errors;
import io.fusionauth.client.json.PreviewMessageTemplateResponseDeserializer;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.message.sms.SMSMessage;

@JsonDeserialize(using = PreviewMessageTemplateResponseDeserializer.class)
public class PreviewMessageTemplateResponse implements Buildable<PreviewMessageTemplateResponse> {
  public Errors errors;
  
  @Deprecated
  public SMSMessage message;
  
  public String previewMessage;
}
