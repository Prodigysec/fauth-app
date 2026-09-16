package io.fusionauth.api.service.authentication;

import org.primeframework.mvc.ErrorException;

public class UserAuthenticationFailedException extends ErrorException {
  public UserAuthenticationFailedException() {
    super("user-authentication-failed");
  }
}
