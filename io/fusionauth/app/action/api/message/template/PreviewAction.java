package io.fusionauth.app.action.api.message.template;

import com.google.inject.Inject;
import com.inversoft.error.Error;
import io.fusionauth.api.domain.message.MessageTemplateResult;
import io.fusionauth.api.service.messenger.MessengerService;
import io.fusionauth.api.util.PhoneMessageTools;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.PreviewMessageTemplateRequest;
import io.fusionauth.domain.api.PreviewMessageTemplateResponse;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.message.sms.SMSMessage;
import io.fusionauth.domain.message.voice.VoiceMessage;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class PreviewAction extends BaseAPIAction {
  @JSONRequest
  public final PreviewMessageTemplateRequest request = new PreviewMessageTemplateRequest();
  
  private final MessengerService messengerService;
  
  @JSONResponse
  public PreviewMessageTemplateResponse response = new PreviewMessageTemplateResponse();
  
  @Inject
  public PreviewAction(FrontEndSupport paramFrontEndSupport, MessengerService paramMessengerService) {
    super(paramFrontEndSupport);
    this.messengerService = paramMessengerService;
  }
  
  public String post() {
    SMSMessage sMSMessage;
    MessageTemplateResult messageTemplateResult = this.messengerService.preview(this.request.messageTemplate, this.request.locale, PhoneMessageTools.MOCK_PARAMETERS);
    this.response.errors = PhoneMessageTools.translateErrors(messageTemplateResult);
    this.response.errors.fieldErrors.forEach((paramString, paramList) -> paramList.forEach(()));
    this.response.errors.generalErrors.forEach(paramError -> paramError.message = this.frontEndSupport.messageProvider.getMessage(paramError.code, paramError.values));
    switch (messageTemplateResult.message.getType()) {
      default:
        throw new MatchException(null, null);
      case SMS:
        sMSMessage = (SMSMessage)messageTemplateResult.message;
      case Voice:
        break;
    } 
    this.response = this.response.with(paramPreviewMessageTemplateResponse -> paramPreviewMessageTemplateResponse.previewMessage = ((VoiceMessage)paramMessageTemplateResult.message).message);
    return "render";
  }
}
