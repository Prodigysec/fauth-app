package io.fusionauth.api.service.messenger;

import java.util.Map;
import org.primeframework.email.domain.BaseResult;
import org.primeframework.email.domain.Email;
import org.primeframework.email.domain.ParsedEmailTemplates;

public interface SMSRenderer {
  void render(ParsedEmailTemplates paramParsedEmailTemplates, Email paramEmail, Map<String, Object> paramMap, BaseResult paramBaseResult);
}
