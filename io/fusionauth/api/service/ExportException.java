package io.fusionauth.api.service;

import org.primeframework.mvc.ErrorException;

public class ExportException extends ErrorException {
  public ExportException(Exception paramException) {
    super("error", paramException, new Object[] { paramException.getMessage() });
  }
}
