package io.fusionauth.app.primeframework.exceptions;

import org.primeframework.mvc.ErrorException;

public class InvalidCSRFTokenException extends ErrorException {
  public InvalidCSRFTokenException() {
    super("invalid-csrf-token");
  }
}
