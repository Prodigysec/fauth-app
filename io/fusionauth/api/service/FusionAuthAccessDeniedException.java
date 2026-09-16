package io.fusionauth.api.service;

import org.primeframework.mvc.ErrorException;

public class FusionAuthAccessDeniedException extends ErrorException {
  public FusionAuthAccessDeniedException() {
    super("access-control-denied");
  }
}
