package io.fusionauth.api.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class PhoneNumberTools {
  public static String safeToE164format(String paramString) {
    try {
      return toE164format(paramString);
    } catch (NumberParseException numberParseException) {
      return null;
    } 
  }
  
  public static String toE164format(String paramString) throws NumberParseException {
    if (paramString != null && paramString.contains("@"))
      throw new NumberParseException(NumberParseException.ErrorType.NOT_A_NUMBER, "Phone number cannot contain '@'"); 
    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(paramString, "US");
    return phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
  }
}
