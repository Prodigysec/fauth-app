package io.fusionauth.api.service.authentication;

import org.primeframework.mvc.ErrorException;

public class UserAuthenticationPendingException extends ErrorException {
  public UserAuthenticationPendingException() {
    super("user-authentication-pending");
  }
}
