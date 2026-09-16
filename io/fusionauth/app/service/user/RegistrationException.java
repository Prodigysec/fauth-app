package io.fusionauth.app.service.user;

import com.inversoft.error.Errors;
import org.primeframework.mvc.ErrorException;

public class RegistrationException extends ErrorException {
  public final Errors errors;
  
  public RegistrationException(Errors paramErrors) {
    super(new Object[0]);
    this.errors = paramErrors;
  }
}
