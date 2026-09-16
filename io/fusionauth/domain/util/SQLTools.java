package io.fusionauth.domain.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SQLTools {
  private static final Pattern EmptySpace = Pattern.compile("\\s{2,}");
  
  private static final Pattern ValidOrderBy = Pattern.compile("(([a-z0-9_]+|`[a-z0-9_]+`)(\\s+(asc|desc))?\\s*(,\\s*(?=[a-z0-9_`])|$))+", 2);
  
  public static String normalizeOrderBy(String paramString, Map<String, String> paramMap) {
    return normalizeOrderBy(paramString, paramMap, Collections.emptySet());
  }
  
  public static String normalizeOrderBy(String paramString, Map<String, String> paramMap, Set<String> paramSet) {
    List list = (List)Arrays.<String>stream(paramString.split(",")).map(paramString -> (List)Arrays.<String>stream(paramString.trim().split("\\s+")).collect(Collectors.toList())).collect(Collectors.toList());
    ArrayList<String> arrayList = new ArrayList();
    for (List<String> list1 : (Iterable<List<String>>)list) {
      String str = list1.get(0);
      if (list1.size() <= 2 && paramSet.contains(str))
        if (((String)list1.get(list1.size() - 1)).equalsIgnoreCase("desc")) {
          arrayList.add(str + " IS NULL");
        } else {
          arrayList.add(str + " IS NOT NULL");
        }  
      arrayList.add(String.join(" ", (Iterable)list1));
    } 
    paramString = String.join(", ", (Iterable)arrayList);
    for (String str : paramMap.keySet()) {
      paramString = paramString.replaceAll(String.format("\\b%s\\b", new Object[] { str }), paramMap.get(str));
    } 
    return paramString;
  }
  
  public static String sanitizeOrderBy(String paramString) {
    if (paramString == null)
      return null; 
    String str = EmptySpace.matcher(paramString).replaceAll(" ").trim();
    if (ValidOrderBy.matcher(str).matches())
      return paramString; 
    return null;
  }
  
  public static String toSearchString(String paramString) {
    return toSearchString(paramString, false);
  }
  
  public static String toSearchString(String paramString, boolean paramBoolean) {
    if (paramString == null)
      return null; 
    if (paramBoolean)
      return paramString.toLowerCase(); 
    StringBuilder stringBuilder = new StringBuilder();
    int i = 0;
    while (i < paramString.length()) {
      if (paramString.charAt(i) == '%' || paramString.charAt(i) == '_') {
        stringBuilder.append('\\')
          .append(paramString.charAt(i));
      } else {
        stringBuilder.append(paramString.charAt(i));
      } 
      i++;
    } 
    if (stringBuilder.length() > paramString.length())
      paramString = stringBuilder.toString(); 
    i = paramString.indexOf('*');
    if (i == -1)
      return "%" + paramString.trim().toLowerCase() + "%"; 
    stringBuilder = new StringBuilder();
    i = 0;
    paramString = paramString.trim().toLowerCase();
    while (i < paramString.length()) {
      if (paramString.charAt(i) == '*') {
        if (i < paramString.length() - 1 && paramString.charAt(i + 1) == '*') {
          stringBuilder.append(paramString.charAt(i));
          i++;
        } else {
          stringBuilder.append('%');
        } 
      } else {
        stringBuilder.append(paramString.charAt(i));
      } 
      i++;
    } 
    return stringBuilder.toString();
  }
}
