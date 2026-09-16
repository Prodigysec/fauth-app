package io.fusionauth.app.action.ajax.message.template;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.messenger.MessengerService;
import io.fusionauth.api.util.PhoneMessageTools;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.MessageTemplateResponse;
import io.fusionauth.domain.message.Message;
import io.fusionauth.domain.message.MessageTemplate;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{messageTemplateId}", requiresAuthentication = true, constraints = {"admin", "message_template_manager"})
public class ViewAction extends BaseAJAXAction {
  private final MessengerService messengerService;
  
  public MessageTemplate messageTemplate;
  
  public UUID messageTemplateId;
  
  public Message renderedMessage;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport, MessengerService paramMessengerService) {
    super(paramFrontEndSupport);
    this.messengerService = paramMessengerService;
  }
  
  public String get() {
    this.messageTemplate = ((MessageTemplateResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveMessageTemplate(this.messageTemplateId))).messageTemplate;
    this.renderedMessage = (this.messengerService.preview(this.messageTemplate, null, PhoneMessageTools.MOCK_PARAMETERS)).message;
    return "render";
  }
}
