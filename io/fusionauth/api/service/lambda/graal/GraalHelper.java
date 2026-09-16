package io.fusionauth.api.service.lambda.graal;

import org.graalvm.polyglot.Value;

public class GraalHelper {
  public static String stringify(Value paramValue) {
    try {
      return stringify(paramValue, false, false);
    } catch (Exception exception) {
      return paramValue.toString();
    } 
  }
  
  private static String stringify(Value paramValue, boolean paramBoolean1, boolean paramBoolean2) {
    if (paramValue.isString()) {
      if (paramBoolean2)
        return "\"" + paramValue.asString() + "\""; 
      return paramValue.asString();
    } 
    if (paramValue.isNumber()) {
      if (paramValue.toString().contains("."))
        return String.valueOf(paramValue.asDouble()); 
      return String.valueOf(paramValue.asLong());
    } 
    if (paramValue.hasArrayElements())
      return paramValue.toString(); 
    if (paramValue.hasMembers()) {
      StringBuilder stringBuilder = new StringBuilder("{");
      for (String str : paramValue.getMemberKeys()) {
        if (paramBoolean1) {
          stringBuilder.append("\"")
            .append(str)
            .append("\"");
        } else {
          stringBuilder.append(str);
        } 
        stringBuilder.append(": ")
          .append(stringify(paramValue.getMember(str), true, true))
          .append(", ");
      } 
      return stringBuilder.substring(0, stringBuilder.length() - 2) + "}";
    } 
    return paramValue.toString();
  }
}
