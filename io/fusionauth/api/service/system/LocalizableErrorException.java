package io.fusionauth.api.service.system;

import org.primeframework.mvc.ErrorException;

public class LocalizableErrorException extends ErrorException {
  public LocalizableErrorException(Throwable paramThrowable, Object... paramVarArgs) {
    super(paramThrowable, paramVarArgs);
  }
  
  public LocalizableErrorException(Object... paramVarArgs) {
    super(paramVarArgs);
  }
  
  public LocalizableErrorException(String paramString) {
    super(paramString);
  }
  
  public LocalizableErrorException(String paramString, boolean paramBoolean) {
    super(paramString, paramBoolean);
  }
  
  public LocalizableErrorException(String paramString, Object... paramVarArgs) {
    super(paramString, paramVarArgs);
  }
  
  public LocalizableErrorException(String paramString, Throwable paramThrowable, Object... paramVarArgs) {
    super(paramString, paramThrowable, paramVarArgs);
  }
  
  public LocalizableErrorException(String paramString, boolean paramBoolean, Throwable paramThrowable, Object... paramVarArgs) {
    super(paramString, paramBoolean, paramThrowable, paramVarArgs);
  }
}
