package io.fusionauth.api.service.message;

import com.google.inject.Inject;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.fusionauth.api.domain.MessageTemplateMapper;
import io.fusionauth.api.domain.message.BaseMessageResult;
import io.fusionauth.api.domain.message.ParsedMessageTemplate;
import io.fusionauth.domain.message.MessageTemplate;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.message.sms.SMSMessageTemplate;
import io.fusionauth.domain.message.voice.VoiceMessageTemplate;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import org.primeframework.email.guice.Email;

public class DatabaseMessageTemplateLoader implements MessageTemplateLoader {
  protected final Configuration freeMarkerConfiguration;
  
  private final MessageTemplateMapper messageTemplateMapper;
  
  @Inject
  public DatabaseMessageTemplateLoader(MessageTemplateMapper paramMessageTemplateMapper, @Email Configuration paramConfiguration) {
    this.messageTemplateMapper = paramMessageTemplateMapper;
    this.freeMarkerConfiguration = paramConfiguration;
  }
  
  public ParsedMessageTemplate load(UUID paramUUID, Locale paramLocale, BaseMessageResult paramBaseMessageResult) {
    MessageTemplate messageTemplate = this.messageTemplateMapper.retrieveById(paramUUID);
    if (messageTemplate == null)
      throw new IllegalStateException("Illegal state, a template does not exist with Id [" + String.valueOf(paramUUID) + "]."); 
    return load(messageTemplate, paramLocale, paramBaseMessageResult);
  }
  
  public ParsedMessageTemplate load(MessageTemplate paramMessageTemplate, Locale paramLocale, BaseMessageResult paramBaseMessageResult) {
    ParsedMessageTemplate parsedMessageTemplate = new ParsedMessageTemplate();
    if (paramMessageTemplate instanceof SMSMessageTemplate) {
      SMSMessageTemplate sMSMessageTemplate = (SMSMessageTemplate)paramMessageTemplate;
      parsedMessageTemplate.type = MessageType.SMS;
      if (paramLocale != null && sMSMessageTemplate.localizedTemplates.containsKey(paramLocale))
        sMSMessageTemplate.defaultTemplate = sMSMessageTemplate.localizedTemplates.get(paramLocale); 
      parsedMessageTemplate.message = parseTemplate(sMSMessageTemplate.defaultTemplate, "message", paramBaseMessageResult);
    } else if (paramMessageTemplate instanceof VoiceMessageTemplate) {
      VoiceMessageTemplate voiceMessageTemplate = (VoiceMessageTemplate)paramMessageTemplate;
      parsedMessageTemplate.type = MessageType.Voice;
      if (paramLocale != null && voiceMessageTemplate.localizedTemplates.containsKey(paramLocale))
        voiceMessageTemplate.defaultTemplate = voiceMessageTemplate.localizedTemplates.get(paramLocale); 
      parsedMessageTemplate.message = parseTemplate(voiceMessageTemplate.defaultTemplate, "message", paramBaseMessageResult);
    } else {
      throw new IllegalStateException("Unexpected template type [" + String.valueOf(paramMessageTemplate.getClass()) + "]");
    } 
    return parsedMessageTemplate;
  }
  
  protected Template parseTemplate(String paramString1, String paramString2, BaseMessageResult paramBaseMessageResult) {
    if (paramString1 == null)
      return null; 
    try {
      return new Template(null, paramString1, this.freeMarkerConfiguration);
    } catch (ParseException parseException) {
      paramBaseMessageResult.parseErrors.put(paramString2, parseException);
    } catch (IOException iOException) {}
    return null;
  }
}
