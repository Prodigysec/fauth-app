package io.fusionauth.app.action.admin.message.template;

import com.google.inject.Inject;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.message.MessageTemplate;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.message.sms.SMSMessageTemplate;
import io.fusionauth.domain.message.voice.VoiceMessageTemplate;
import java.util.UUID;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;

public abstract class BaseFormAction extends BaseAction {
  public MessageTemplate messageTemplate;
  
  public UUID messageTemplateId;
  
  @PreParameter
  public MessageType type;
  
  public MessageType[] types = MessageType.values();
  
  @Inject
  public BaseFormAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @PreParameterMethod
  public void setupTemplate() {
    if (this.type == null)
      this.type = MessageType.SMS; 
    switch (this.type) {
      case SMS:
        this.messageTemplate = new SMSMessageTemplate();
        break;
      case Voice:
        this.messageTemplate = new VoiceMessageTemplate();
        break;
    } 
  }
}
