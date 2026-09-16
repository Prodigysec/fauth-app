package io.fusionauth.api.service.messaging;

import org.primeframework.mvc.ErrorException;

public class KafkaProducerException extends ErrorException {
  public String message;
  
  public KafkaProducerException(Throwable paramThrowable, Object... paramVarArgs) {
    super(paramThrowable, paramVarArgs);
  }
  
  public KafkaProducerException(String paramString) {
    super(new Object[0]);
    this.message = paramString;
  }
  
  public String getMessage() {
    StringBuilder stringBuilder = new StringBuilder();
    if (super.getMessage() != null)
      stringBuilder.append(super.getMessage()); 
    if (stringBuilder.length() > 0) {
      Throwable throwable = getCause();
      while (throwable != null) {
        String str = throwable.getMessage();
        if (str != null)
          stringBuilder.append("\n  - ").append(str); 
        throwable = throwable.getCause();
      } 
    } 
    return ((this.message == null) ? "" : (this.message + "\n ")) + ((this.message == null) ? "" : (this.message + "\n "));
  }
}
