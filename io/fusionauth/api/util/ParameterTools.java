package io.fusionauth.api.util;

import com.inversoft.util.StringTools;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ParameterTools {
  public static Set<String> splitSpaceSeparated(String paramString) {
    if (StringTools.isBlank(paramString))
      return new LinkedHashSet<>(0); 
    LinkedHashSet<String> linkedHashSet = new LinkedHashSet(0);
    linkedHashSet.addAll(Arrays.asList(paramString.split(" ")));
    linkedHashSet.removeIf(String::isEmpty);
    return linkedHashSet;
  }
}
