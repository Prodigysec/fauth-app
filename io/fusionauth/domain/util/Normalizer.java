package io.fusionauth.domain.util;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class Normalizer {
  public static <T> void deDuplicate(List<T> paramList) {
    if (paramList == null)
      return; 
    HashSet hashSet = new HashSet();
    paramList.removeIf(paramObject -> !paramSet.add(paramObject));
  }
  
  public static String lineReturns(String paramString) {
    if (paramString == null)
      return null; 
    return paramString.replaceAll("\\r\\n|\\r", "\n");
  }
  
  public static <T> void lineReturnsMap(Map<T, String> paramMap) {
    paramMap.forEach((paramObject, paramString) -> {
          if (paramString != null)
            paramMap.put(paramObject, paramString.replaceAll("\\r\\n|\\r", "\n")); 
        });
  }
  
  public static <T> void removeEmpty(List<T> paramList) {
    if (paramList == null)
      return; 
    paramList.removeIf(Objects::isNull);
  }
  
  public static <T, U> void removeEmpty(Map<T, U> paramMap) {
    if (paramMap == null)
      return; 
    paramMap.keySet().removeIf(paramObject -> (paramMap.get(paramObject) == null));
  }
  
  public static String removeLineReturns(String paramString) {
    if (paramString == null)
      return null; 
    return paramString.replaceAll("\\r\\n|\\r|\\n", "");
  }
  
  public static <T extends Collection<String>> void toLowerCase(Collection<String> paramCollection, Supplier<T> paramSupplier) {
    if (paramCollection == null || paramCollection.isEmpty())
      return; 
    Collection<? extends String> collection = (Collection)paramCollection.stream().map(String::toLowerCase).collect(Collectors.toCollection(paramSupplier));
    paramCollection.clear();
    paramCollection.addAll(collection);
  }
  
  public static String toLowerCase(String paramString) {
    if (paramString == null)
      return null; 
    return paramString.toLowerCase();
  }
  
  public static String toUpperCase(String paramString) {
    if (paramString == null)
      return null; 
    return paramString.toUpperCase();
  }
  
  public static String trim(String paramString) {
    if (paramString == null)
      return null; 
    return paramString.trim();
  }
  
  public static <T> void trimMap(Map<T, String> paramMap) {
    paramMap.forEach((paramObject, paramString) -> {
          if (paramString != null)
            paramMap.put(paramObject, paramString.trim()); 
        });
  }
  
  public static String trimToNull(String paramString) {
    if (paramString == null)
      return null; 
    paramString = paramString.trim();
    if (paramString.isEmpty())
      return null; 
    return paramString;
  }
  
  public static ZonedDateTime truncateToMilliseconds(ZonedDateTime paramZonedDateTime) {
    if (paramZonedDateTime == null)
      return null; 
    return paramZonedDateTime.truncatedTo(ChronoUnit.MILLIS);
  }
}
