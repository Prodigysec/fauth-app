package io.fusionauth.api.service.system;

import org.primeframework.mvc.ErrorException;

public class ImportException extends ErrorException {
  public ImportException() {
    super("error");
  }
}
