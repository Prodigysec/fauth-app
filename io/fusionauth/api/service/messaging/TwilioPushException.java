package io.fusionauth.api.service.messaging;

import org.primeframework.mvc.ErrorException;

public class TwilioPushException extends ErrorException {
  public String responseBody;
  
  public int statusCode;
  
  public TwilioPushException(Throwable paramThrowable, Object... paramVarArgs) {
    super(paramThrowable, paramVarArgs);
  }
  
  public TwilioPushException(String paramString, int paramInt) {
    super(new Object[0]);
    this.responseBody = paramString;
    this.statusCode = paramInt;
  }
}
