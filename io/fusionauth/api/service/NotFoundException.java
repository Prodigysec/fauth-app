package io.fusionauth.api.service;

import org.primeframework.mvc.ErrorException;

public class NotFoundException extends ErrorException {
  public NotFoundException() {
    super("missing");
  }
}
