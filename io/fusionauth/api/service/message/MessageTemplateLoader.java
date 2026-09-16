package io.fusionauth.api.service.message;

import io.fusionauth.api.domain.message.BaseMessageResult;
import io.fusionauth.api.domain.message.ParsedMessageTemplate;
import io.fusionauth.domain.message.MessageTemplate;
import java.util.Locale;
import java.util.UUID;

public interface MessageTemplateLoader {
  ParsedMessageTemplate load(UUID paramUUID, Locale paramLocale, BaseMessageResult paramBaseMessageResult);
  
  ParsedMessageTemplate load(MessageTemplate paramMessageTemplate, Locale paramLocale, BaseMessageResult paramBaseMessageResult);
}
