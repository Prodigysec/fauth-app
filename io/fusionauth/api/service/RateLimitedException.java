package io.fusionauth.api.service;

import org.primeframework.mvc.ErrorException;

public class RateLimitedException extends ErrorException {
  public RateLimitedException() {
    super("rate-limited");
  }
}
