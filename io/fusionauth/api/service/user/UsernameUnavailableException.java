package io.fusionauth.api.service.user;

import org.primeframework.mvc.ErrorException;

public class UsernameUnavailableException extends ErrorException {
  public UsernameUnavailableException() {
    super("username-unavailable");
  }
}
