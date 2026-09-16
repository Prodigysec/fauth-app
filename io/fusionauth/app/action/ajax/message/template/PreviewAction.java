package io.fusionauth.app.action.ajax.message.template;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.PreviewMessageTemplateRequest;
import io.fusionauth.domain.api.PreviewMessageTemplateResponse;
import io.fusionauth.domain.message.MessageTemplate;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.message.sms.SMSMessageTemplate;
import io.fusionauth.domain.message.voice.VoiceMessageTemplate;
import java.util.Locale;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;

@Action(requiresAuthentication = true, value = "{type}", constraints = {"admin", "message_template_manager"})
public class PreviewAction extends BaseAJAXAction {
  public Errors errors;
  
  public MessageTemplate messageTemplate;
  
  public UUID messageTemplateId;
  
  public Locale previewLocale;
  
  public String renderedMessage;
  
  @PreParameter
  public MessageType type;
  
  @Inject
  public PreviewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String post() {
    PreviewMessageTemplateResponse previewMessageTemplateResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveMessageTemplatePreview(new PreviewMessageTemplateRequest(this.messageTemplate, this.previewLocale)));
    this.errors = previewMessageTemplateResponse.errors;
    this.renderedMessage = previewMessageTemplateResponse.previewMessage;
    return "render";
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
