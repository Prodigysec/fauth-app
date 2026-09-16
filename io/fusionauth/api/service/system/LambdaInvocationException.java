package io.fusionauth.api.service.system;

import org.primeframework.mvc.ErrorException;

public class LambdaInvocationException extends ErrorException {
  public LambdaInvocationException(Throwable paramThrowable) {
    super("lambda-invocation-error", paramThrowable, new Object[0]);
  }
}
