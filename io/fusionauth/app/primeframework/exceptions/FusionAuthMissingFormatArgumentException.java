package io.fusionauth.app.primeframework.exceptions;

import java.util.MissingFormatArgumentException;

public class FusionAuthMissingFormatArgumentException extends MissingFormatArgumentException {
  private final String message;
  
  public FusionAuthMissingFormatArgumentException(String paramString, Throwable paramThrowable) {
    super(paramString);
    this.message = paramString + " Cause: " + paramString;
    initCause(paramThrowable);
  }
  
  public String getMessage() {
    return this.message;
  }
}
