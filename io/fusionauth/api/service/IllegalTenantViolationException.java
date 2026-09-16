package io.fusionauth.api.service;

import org.primeframework.mvc.ErrorException;

public class IllegalTenantViolationException extends ErrorException {
  public IllegalTenantViolationException() {
    super("input");
  }
}
