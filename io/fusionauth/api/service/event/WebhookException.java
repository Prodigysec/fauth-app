package io.fusionauth.api.service.event;

import io.fusionauth.api.service.system.LocalizableErrorException;

public class WebhookException extends LocalizableErrorException {
  public WebhookException() {
    super(new Object[0]);
  }
  
  public WebhookException(String paramString) {
    super(paramString);
  }
  
  public WebhookException(Throwable paramThrowable, Object... paramVarArgs) {
    super(paramThrowable, paramVarArgs);
  }
}
