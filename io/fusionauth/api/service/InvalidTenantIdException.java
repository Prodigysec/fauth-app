package io.fusionauth.api.service;

import java.util.UUID;
import org.primeframework.mvc.ErrorException;

public class InvalidTenantIdException extends ErrorException {
  public InvalidTenantIdException(UUID paramUUID) {
    super("input", new Object[] { paramUUID });
  }
}
