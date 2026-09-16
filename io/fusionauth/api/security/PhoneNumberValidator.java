package io.fusionauth.api.security;

import com.google.i18n.phonenumbers.NumberParseException;
import io.fusionauth.api.util.PhoneNumberTools;

public class PhoneNumberValidator {
  public static boolean areEqual(String paramString1, String paramString2) {
    try {
      String str1 = PhoneNumberTools.toE164format(paramString1);
      String str2 = PhoneNumberTools.toE164format(paramString2);
      return str1.equals(str2);
    } catch (NumberParseException numberParseException) {
      return false;
    } 
  }
  
  public static boolean validateE164format(String paramString) {
    try {
      PhoneNumberTools.toE164format(paramString);
      return true;
    } catch (NumberParseException numberParseException) {
      return false;
    } 
  }
}
