package io.fusionauth.api.service.user;

import org.primeframework.mvc.ErrorException;

public class SearchByQueryUnsupportedException extends ErrorException {
  public SearchByQueryUnsupportedException() {
    super("input");
  }
}
