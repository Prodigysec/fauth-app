package io.fusionauth.app.primeframework.exceptions;

import org.primeframework.mvc.ErrorException;

public class InvalidRefererException extends ErrorException {
  public final String requestedRelativeURI;
  
  public InvalidRefererException(String paramString) {
    super("invalid-referer-header");
    this.requestedRelativeURI = paramString;
  }
}
