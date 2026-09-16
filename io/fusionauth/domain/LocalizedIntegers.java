package io.fusionauth.domain;

import io.fusionauth.domain.util.Normalizer;
import java.util.Locale;
import java.util.TreeMap;

public class LocalizedIntegers extends TreeMap<Locale, Integer> {
  public LocalizedIntegers() {
    super((paramLocale1, paramLocale2) -> (paramLocale1 == null) ? -1 : ((paramLocale2 == null) ? 1 : paramLocale1.toString().compareTo(paramLocale2.toString())));
  }
  
  public LocalizedIntegers(Locale paramLocale, Integer paramInteger) {
    super((paramLocale1, paramLocale2) -> (paramLocale1 == null) ? -1 : ((paramLocale2 == null) ? 1 : paramLocale1.toString().compareTo(paramLocale2.toString())));
    put(paramLocale, paramInteger);
  }
  
  public LocalizedIntegers(Locale paramLocale1, Integer paramInteger1, Locale paramLocale2, Integer paramInteger2) {
    super((paramLocale1, paramLocale2) -> (paramLocale1 == null) ? -1 : ((paramLocale2 == null) ? 1 : paramLocale1.toString().compareTo(paramLocale2.toString())));
    put(paramLocale1, paramInteger1);
    put(paramLocale2, paramInteger2);
  }
  
  public void removeEmpty() {
    Normalizer.removeEmpty(this);
  }
}
