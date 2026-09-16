package io.fusionauth.api.service.messenger;

import com.google.inject.Inject;
import io.fusionauth.api.domain.MessengerConfigurationMapper;
import io.fusionauth.api.domain.message.BaseMessageResult;
import io.fusionauth.api.domain.message.MessageTemplateResult;
import io.fusionauth.api.domain.message.ParsedMessageTemplate;
import io.fusionauth.api.service.message.MessageRenderer;
import io.fusionauth.api.service.message.MessageTemplateLoader;
import io.fusionauth.domain.message.MessageTemplate;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.message.sms.SMSMessage;
import io.fusionauth.domain.message.voice.VoiceMessage;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultMessengerService implements MessengerService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultMessengerService.class);
  
  private final MessageRenderer messageRenderer;
  
  private final MessengerConfigurationMapper messengerConfigurationMapper;
  
  private final MessengerProvider messengerProvider;
  
  private final MessageTemplateLoader templateLoader;
  
  @Inject
  public DefaultMessengerService(MessageRenderer paramMessageRenderer, MessengerConfigurationMapper paramMessengerConfigurationMapper, MessengerProvider paramMessengerProvider, MessageTemplateLoader paramMessageTemplateLoader) {
    this.messageRenderer = paramMessageRenderer;
    this.messengerConfigurationMapper = paramMessengerConfigurationMapper;
    this.messengerProvider = paramMessengerProvider;
    this.templateLoader = paramMessageTemplateLoader;
  }
  
  public MessageTemplateResult preview(MessageTemplate paramMessageTemplate, Locale paramLocale, Map<String, Object> paramMap) {
    MessageTemplateResult messageTemplateResult = new MessageTemplateResult();
    ParsedMessageTemplate parsedMessageTemplate = this.templateLoader.load(paramMessageTemplate, paramLocale, messageTemplateResult);
    switch (parsedMessageTemplate.type) {
      default:
        throw new MatchException(null, null);
      case SMS:
      
      case Voice:
        break;
    } 
    VoiceMessage voiceMessage = new VoiceMessage();
    voiceMessage.locale = paramLocale;
  }
  
  public void send(UUID paramUUID1, UUID paramUUID2, Locale paramLocale, Map<String, Object> paramMap) {
    SMSMessage sMSMessage;
    VoiceMessage voiceMessage2;
    Objects.requireNonNull(paramUUID1);
    MessageTemplateResult messageTemplateResult = new MessageTemplateResult();
    ParsedMessageTemplate parsedMessageTemplate = this.templateLoader.load(paramUUID1, paramLocale, messageTemplateResult);
    switch (parsedMessageTemplate.type) {
      default:
        throw new MatchException(null, null);
      case SMS:
        sMSMessage = new SMSMessage();
        sMSMessage.phoneNumber = (String)paramMap.get("phoneNumber");
      case Voice:
        voiceMessage2 = new VoiceMessage();
        voiceMessage2.locale = paramLocale;
        voiceMessage2.phoneNumber = (String)paramMap.get("phoneNumber");
    } 
    VoiceMessage voiceMessage1 = voiceMessage2;
    this.messageRenderer.render(parsedMessageTemplate, voiceMessage1, paramMap, messageTemplateResult);
    if (!messageTemplateResult.parseErrors.isEmpty() || !messageTemplateResult.renderErrors.isEmpty()) {
      if (!messageTemplateResult.parseErrors.isEmpty()) {
        logger.error("For templateId [{}] - errors parsing message template: {}", paramUUID1, messageTemplateResult.parseErrors);
        throw new MessageTemplateException(paramUUID1, messageTemplateResult.parseErrors);
      } 
      logger.error("For templateId [{}] - errors rendering message template: {}", paramUUID1, messageTemplateResult.renderErrors);
      throw new MessageTemplateException(paramUUID1, messageTemplateResult.renderErrors);
    } 
    BaseMessengerConfiguration baseMessengerConfiguration = this.messengerConfigurationMapper.retrieveById(paramUUID2);
    this.messengerProvider.get(baseMessengerConfiguration.getType()).send(voiceMessage1, baseMessengerConfiguration);
  }
  
  public ValidationResult validate(MessageTemplate paramMessageTemplate, Locale paramLocale, Map<String, Object> paramMap) {
    ValidationResult validationResult = new ValidationResult();
    ParsedMessageTemplate parsedMessageTemplate = this.templateLoader.load(paramMessageTemplate, paramLocale, validationResult);
    switch (parsedMessageTemplate.type) {
      default:
        throw new MatchException(null, null);
      case SMS:
      
      case Voice:
        break;
    } 
    VoiceMessage voiceMessage = new VoiceMessage();
    voiceMessage.locale = paramLocale;
  }
  
  public static class ValidationResult extends BaseMessageResult {}
}
