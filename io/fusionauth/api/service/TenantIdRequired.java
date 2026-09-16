package io.fusionauth.api.service;

import org.primeframework.mvc.ErrorException;

public class TenantIdRequired extends ErrorException {
  public TenantIdRequired() {
    super("input");
  }
}
