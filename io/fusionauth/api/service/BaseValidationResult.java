package io.fusionauth.api.service;

import com.inversoft.error.Errors;

public abstract class BaseValidationResult {
  public Errors errors = new Errors();
}
