package io.fusionauth.api.service.form;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.function.Function;
import java.util.regex.Pattern;

public class FormTools {
  private static final Pattern ValidKey = Pattern.compile("(?>\\.\\w+(?>\\[\\d+]|\\['\\w+'])*)+");
  
  public static Boolean convertToBoolean(Object paramObject) {
    if (paramObject == null)
      return null; 
    try {
      String str = paramObject.toString().toLowerCase();
      switch (str) {
        case "true":
        
        case "false":
        
      } 
      return 

        
        null;
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public static Object convertToBooleans(Object paramObject) {
    return convert(paramObject, Boolean.class, FormTools::convertToBoolean);
  }
  
  public static LocalDate convertToDate(Object paramObject) {
    try {
      return LocalDate.parse(paramObject.toString());
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public static Object convertToDates(Object paramObject) {
    return convert(paramObject, LocalDate.class, FormTools::convertToDate);
  }
  
  public static Number convertToNumber(Object paramObject) {
    if (paramObject == null)
      return null; 
    String str = paramObject.toString();
    try {
      return new BigInteger(str);
    } catch (Exception exception) {
      try {
        return new BigDecimal(str);
      } catch (Exception exception1) {
        try {
          if (str.indexOf('-') != -1)
            return null; 
          return NumberFormat.getNumberInstance().parse(str);
        } catch (Exception exception2) {
          return null;
        } 
      } 
    } 
  }
  
  public static Object convertToNumbers(Object paramObject) {
    return convert(paramObject, Number.class, FormTools::convertToNumber);
  }
  
  public static boolean isNullOrEmptyString(Object paramObject) {
    return (paramObject == null || (paramObject instanceof String && paramObject.toString().equals("")));
  }
  
  public static boolean validateFieldKey(String paramString) {
    return ValidKey.matcher(paramString).matches();
  }
  
  private static <T> Object convert(Object paramObject, Class<?> paramClass, Function<Object, T> paramFunction) {
    if (paramObject == null)
      return null; 
    if (paramObject instanceof Object[]) {
      Object[] arrayOfObject1 = (Object[])paramObject;
      Object[] arrayOfObject2 = (Object[])Array.newInstance(paramClass, arrayOfObject1.length);
      for (byte b = 0; b < arrayOfObject1.length; b++) {
        arrayOfObject2[b] = paramFunction.apply(arrayOfObject1[b]);
        if (arrayOfObject2[b] == null)
          return null; 
      } 
      return arrayOfObject2;
    } 
    return paramFunction.apply(paramObject);
  }
}
