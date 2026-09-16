package io.fusionauth.api.security;

import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.user.IdentityHelper;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.PasswordValidationRules;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import java.util.Objects;
import javax.annotation.Nonnull;

public class PasswordValidator {
  private final PasswordValidationRules rules;
  
  private final User user;
  
  public PasswordValidator(@Nonnull PasswordValidationRules paramPasswordValidationRules, @Nonnull User paramUser) {
    this.rules = Objects.<PasswordValidationRules>requireNonNull(paramPasswordValidationRules);
    this.user = Objects.<User>requireNonNull(paramUser);
  }
  
  public Errors validate(String paramString1, String paramString2) {
    Errors errors = new Errors();
    if (paramString1 == null || paramString1.trim().length() == 0)
      return errors; 
    Objects.requireNonNull(errors);
    (new Validator()).maxLength(paramString1, this.rules.maxLength, paramString2, new Object[] { Integer.valueOf(this.rules.maxLength) }).minLength(paramString1, this.rules.minLength, paramString2, new Object[] { Integer.valueOf(this.rules.minLength) }).done(errors::add);
    char[] arrayOfChar = paramString1.toCharArray();
    if (this.rules.requireMixedCase && (!containsLowerCase(arrayOfChar) || !containsUpperCase(arrayOfChar)))
      return errors.addFieldError(paramString2, "[singleCase]" + paramString2, null, new Object[0]); 
    if (this.rules.requireNonAlpha && !containsNonAlphanumeric(arrayOfChar))
      return errors.addFieldError(paramString2, "[onlyAlpha]" + paramString2, null, new Object[0]); 
    if (this.rules.requireNumber && !containsNumber(arrayOfChar))
      return errors.addFieldError(paramString2, "[requireNumber]" + paramString2, null, new Object[0]); 
    if (this.rules.disallowUserLoginId) {
      String str = checkContainsLoginId(paramString1);
      if (str != null)
        return errors.addFieldError(paramString2, str + str, null, new Object[0]); 
    } 
    return errors;
  }
  
  private boolean containsLowerCase(char[] paramArrayOfchar) {
    for (char c : paramArrayOfchar) {
      if (Character.isLowerCase(c))
        return true; 
    } 
    return false;
  }
  
  private boolean containsNonAlphanumeric(char[] paramArrayOfchar) {
    for (char c : paramArrayOfchar) {
      if (!Character.isAlphabetic(c) && !Character.isDigit(c))
        return true; 
    } 
    return false;
  }
  
  private boolean containsNumber(char[] paramArrayOfchar) {
    for (char c : paramArrayOfchar) {
      if (Character.isDigit(c))
        return true; 
    } 
    return false;
  }
  
  private boolean containsUpperCase(char[] paramArrayOfchar) {
    for (char c : paramArrayOfchar) {
      if (Character.isUpperCase(c))
        return true; 
    } 
    return false;
  }
  
  private String checkContainsLoginId(String paramString) {
    IdentityType identityType = null;
    for (UserIdentity userIdentity : this.user.identities) {
      if (userIdentity.value == null)
        continue; 
      String str1 = IdentityHelper.canonicalizeValue(userIdentity).toLowerCase();
      String str2 = IdentityHelper.canonicalizeValue(paramString, userIdentity.type).toLowerCase();
      if (str2.contains(str1)) {
        identityType = userIdentity.type;
        break;
      } 
    } 
    if (identityType == null)
      return null; 
    if (identityType.is(IdentityType.email))
      return "[containsEmail]"; 
    if (identityType.is(IdentityType.username))
      return "[containsUsername]"; 
    if (identityType.is(IdentityType.phoneNumber))
      return "[containsPhoneNumber]"; 
    return "[invalid]";
  }
}
