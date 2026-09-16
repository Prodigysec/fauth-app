package io.fusionauth.api.service.messaging;

import org.primeframework.mvc.ErrorException;

public class InvalidMessageLength extends ErrorException {
  public InvalidMessageLength() {
    super(new Object[0]);
  }
}
