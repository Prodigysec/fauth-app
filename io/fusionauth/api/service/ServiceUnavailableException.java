package io.fusionauth.api.service;

import org.primeframework.mvc.ErrorException;

public class ServiceUnavailableException extends ErrorException {
  public ServiceUnavailableException() {
    super("service-unavailable");
  }
}
