package io.fusionauth.api.service.messenger;

import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;

public class MessageTemplateException extends ErrorException {
  public final Map<String, ?> renderOrParseErrors;
  
  public final UUID templateId;
  
  public MessageTemplateException(UUID paramUUID, Map<String, ?> paramMap) {
    super("error");
    this.renderOrParseErrors = paramMap;
    this.templateId = paramUUID;
  }
}
