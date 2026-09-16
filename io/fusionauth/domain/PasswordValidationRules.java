package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class PasswordValidationRules implements Buildable<PasswordValidationRules> {
  public PasswordBreachDetection breachDetection = new PasswordBreachDetection();
  
  public boolean disallowUserLoginId;
  
  public int maxLength = 256;
  
  public int minLength;
  
  public RememberPreviousPasswords rememberPreviousPasswords = new RememberPreviousPasswords();
  
  public boolean requireMixedCase;
  
  public boolean requireNonAlpha;
  
  public boolean requireNumber;
  
  public boolean validateOnLogin;
  
  @JacksonConstructor
  public PasswordValidationRules() {
    this.minLength = FIPS.minimumPasswordLength();
  }
  
  public PasswordValidationRules(PasswordValidationRules paramPasswordValidationRules) {
    this.disallowUserLoginId = paramPasswordValidationRules.disallowUserLoginId;
    this.breachDetection = new PasswordBreachDetection(paramPasswordValidationRules.breachDetection);
    this.maxLength = paramPasswordValidationRules.maxLength;
    this.minLength = paramPasswordValidationRules.minLength;
    this.rememberPreviousPasswords = new RememberPreviousPasswords(paramPasswordValidationRules.rememberPreviousPasswords);
    this.requireMixedCase = paramPasswordValidationRules.requireMixedCase;
    this.requireNonAlpha = paramPasswordValidationRules.requireNonAlpha;
    this.requireNumber = paramPasswordValidationRules.requireNumber;
    this.validateOnLogin = paramPasswordValidationRules.validateOnLogin;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof PasswordValidationRules))
      return false; 
    PasswordValidationRules passwordValidationRules = (PasswordValidationRules)paramObject;
    return (this.disallowUserLoginId == passwordValidationRules.disallowUserLoginId && this.maxLength == passwordValidationRules.maxLength && this.minLength == passwordValidationRules.minLength && this.requireMixedCase == passwordValidationRules.requireMixedCase && this.requireNonAlpha == passwordValidationRules.requireNonAlpha && this.requireNumber == passwordValidationRules.requireNumber && 




      
      Objects.equals(this.breachDetection, passwordValidationRules.breachDetection) && 
      Objects.equals(this.rememberPreviousPasswords, passwordValidationRules.rememberPreviousPasswords));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Boolean.valueOf(this.disallowUserLoginId), this.breachDetection, Integer.valueOf(this.maxLength), Integer.valueOf(this.minLength), this.rememberPreviousPasswords, Boolean.valueOf(this.requireMixedCase), Boolean.valueOf(this.requireNonAlpha), Boolean.valueOf(this.requireNumber), Boolean.valueOf(this.validateOnLogin) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
