package io.fusionauth.app.freemarker;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.List;

public class PhoneNumberParser implements TemplateMethodModelEx {
  private static final String ERROR_MESSAGE = "You must pass an object like this:\n\n  phone_number(object)";
  
  public Object exec(List<SimpleScalar> paramList) throws TemplateModelException {
    if (paramList.size() != 1)
      throw new TemplateModelException("You must pass an object like this:\n\n  phone_number(object)"); 
    SimpleScalar simpleScalar = paramList.get(0);
    String str = simpleScalar.getAsString();
    if (str.length() == 0)
      return str; 
    try {
      PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
      Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(str, "US");
      return phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
    } catch (NumberParseException numberParseException) {
      return str;
    } 
  }
}
