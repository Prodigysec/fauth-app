package io.fusionauth.api.service.authentication;

import org.primeframework.mvc.ErrorException;

public class UserExpiredException extends ErrorException {
  public UserExpiredException() {
    super("expired");
  }
}
