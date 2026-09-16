package io.fusionauth.api.service.email;

import java.util.UUID;
import org.primeframework.mvc.ErrorException;

public class EmailTemplateNotFoundException extends ErrorException {
  public EmailTemplateNotFoundException(UUID paramUUID) {
    super("render-missing", new Object[] { paramUUID });
  }
}
