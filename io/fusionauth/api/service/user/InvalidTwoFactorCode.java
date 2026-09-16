package io.fusionauth.api.service.user;

import org.primeframework.mvc.ErrorException;

public class InvalidTwoFactorCode extends ErrorException {
  public InvalidTwoFactorCode() {
    super("invalid-two-factor-code");
  }
}
