package io.fusionauth.api.service.authentication;

import org.primeframework.mvc.ErrorException;

public class UserLockedException extends ErrorException {
  public UserLockedException() {
    super("user-locked");
  }
}
