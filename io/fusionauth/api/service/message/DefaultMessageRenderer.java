package io.fusionauth.api.service.message;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.fusionauth.api.domain.message.BaseMessageResult;
import io.fusionauth.api.domain.message.ParsedMessageTemplate;
import io.fusionauth.domain.message.Message;
import io.fusionauth.domain.message.sms.SMSMessage;
import io.fusionauth.domain.message.voice.VoiceMessage;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;

public class DefaultMessageRenderer implements MessageRenderer {
  public void render(ParsedMessageTemplate paramParsedMessageTemplate, Message paramMessage, Map<String, Object> paramMap, BaseMessageResult paramBaseMessageResult) {
    if (paramMessage instanceof SMSMessage) {
      SMSMessage sMSMessage = (SMSMessage)paramMessage;
      sMSMessage.textMessage = callTemplate(paramParsedMessageTemplate.message, paramMap, "message", paramBaseMessageResult);
      sMSMessage.code = (String)paramMap.get("code");
      sMSMessage.userId = (UUID)paramMap.get("userId");
    } else if (paramMessage instanceof VoiceMessage) {
      VoiceMessage voiceMessage = (VoiceMessage)paramMessage;
      voiceMessage.message = callTemplate(paramParsedMessageTemplate.message, paramMap, "message", paramBaseMessageResult);
      voiceMessage.code = (String)paramMap.get("code");
      voiceMessage.userId = (UUID)paramMap.get("userId");
    } 
  }
  
  protected String callTemplate(Template paramTemplate, Map<String, Object> paramMap, String paramString, BaseMessageResult paramBaseMessageResult) {
    if (paramTemplate == null)
      return null; 
    StringWriter stringWriter = new StringWriter();
    try {
      paramTemplate.process(paramMap, stringWriter);
    } catch (TemplateException templateException) {
      paramBaseMessageResult.renderErrors.put(paramString, templateException);
    } catch (IOException iOException) {
      throw new IllegalStateException(iOException);
    } 
    return stringWriter.toString();
  }
}
