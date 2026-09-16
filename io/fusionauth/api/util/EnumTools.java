package io.fusionauth.api.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumTools {
  public static <E extends Enum<E>> String join(Class<E> paramClass, CharSequence paramCharSequence) {
    return Arrays.<Enum>stream((Enum[])paramClass.getEnumConstants()).map(Enum::name).collect(Collectors.joining(paramCharSequence));
  }
  
  public static <E extends Enum<E>> String sortedJoin(Class<E> paramClass, CharSequence paramCharSequence) {
    return Arrays.<Enum>stream((Enum[])paramClass.getEnumConstants()).map(Enum::name).sorted().collect(Collectors.joining(paramCharSequence));
  }
}
