package io.fusionauth.api.service.message;

import io.fusionauth.api.domain.message.BaseMessageResult;
import io.fusionauth.api.domain.message.ParsedMessageTemplate;
import io.fusionauth.domain.message.Message;
import java.util.Map;

public interface MessageRenderer {
  void render(ParsedMessageTemplate paramParsedMessageTemplate, Message paramMessage, Map<String, Object> paramMap, BaseMessageResult paramBaseMessageResult);
}
