package io.fusionauth.api.service.authentication;

import org.primeframework.mvc.ErrorException;

public class UserAuthenticationCanceledException extends ErrorException {
  public UserAuthenticationCanceledException() {
    super("user-authentication-canceled");
  }
}
