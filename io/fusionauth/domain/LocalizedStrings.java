package io.fusionauth.domain;

import io.fusionauth.domain.util.Normalizer;
import java.util.Locale;
import java.util.TreeMap;

public class LocalizedStrings extends TreeMap<Locale, String> {
  public LocalizedStrings() {
    super((paramLocale1, paramLocale2) -> (paramLocale1 == null) ? -1 : ((paramLocale2 == null) ? 1 : paramLocale1.toString().compareTo(paramLocale2.toString())));
  }
  
  public LocalizedStrings(Locale paramLocale, String paramString) {
    super((paramLocale1, paramLocale2) -> (paramLocale1 == null) ? -1 : ((paramLocale2 == null) ? 1 : paramLocale1.toString().compareTo(paramLocale2.toString())));
    put(paramLocale, paramString);
  }
  
  public LocalizedStrings(Locale paramLocale1, String paramString1, Locale paramLocale2, String paramString2) {
    super((paramLocale1, paramLocale2) -> (paramLocale1 == null) ? -1 : ((paramLocale2 == null) ? 1 : paramLocale1.toString().compareTo(paramLocale2.toString())));
    put(paramLocale1, paramString1);
    put(paramLocale2, paramString2);
  }
  
  public void normalize() {
    Normalizer.trimMap(this);
    Normalizer.lineReturnsMap(this);
  }
  
  public void removeEmpty() {
    Normalizer.removeEmpty(this);
  }
}
