package io.fusionauth.app.freemarker;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.List;

public class PhoneNumberFormatter implements TemplateMethodModelEx {
  private static final String ERROR_MESSAGE = "You must pass an object like this:\n\n  phone_format(object)";
  
  public Object exec(List<Object> paramList) throws TemplateModelException {
    if (paramList.size() != 1)
      throw new TemplateModelException("You must pass an object like this:\n\n  phone_format(object)"); 
    Object object = paramList.get(0);
    if (object == null)
      return null; 
    String str = object.toString();
    if (str.length() == 0)
      return str; 
    try {
      PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
      Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(str, "US");
      return phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    } catch (NumberParseException numberParseException) {
      return str;
    } 
  }
}
