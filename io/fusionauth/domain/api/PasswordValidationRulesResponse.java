package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.PasswordValidationRules;

public class PasswordValidationRulesResponse {
  public PasswordValidationRules passwordValidationRules;
  
  public PasswordValidationRulesResponse(PasswordValidationRules paramPasswordValidationRules) {
    this.passwordValidationRules = paramPasswordValidationRules;
  }
  
  @JacksonConstructor
  public PasswordValidationRulesResponse() {}
}
