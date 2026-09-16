package io.fusionauth.api.service.messenger;

import io.fusionauth.api.domain.message.MessageTemplateResult;
import io.fusionauth.domain.message.MessageTemplate;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public interface MessengerService {
  MessageTemplateResult preview(MessageTemplate paramMessageTemplate, Locale paramLocale, Map<String, Object> paramMap);
  
  void send(UUID paramUUID1, UUID paramUUID2, Locale paramLocale, Map<String, Object> paramMap);
  
  DefaultMessengerService.ValidationResult validate(MessageTemplate paramMessageTemplate, Locale paramLocale, Map<String, Object> paramMap);
}
