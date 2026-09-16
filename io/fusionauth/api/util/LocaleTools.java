package io.fusionauth.api.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class LocaleTools {
  public static List<Locale> availableLocales() {
    ArrayList<? super Locale> arrayList = new ArrayList();
    Collections.addAll(arrayList, Locale.getAvailableLocales());
    arrayList.removeIf(paramLocale -> (paramLocale.getLanguage().isEmpty() || paramLocale.hasExtensions() || !paramLocale.getScript().isEmpty()));
    arrayList.sort(Comparator.comparing(Locale::getDisplayName));
    return (List)arrayList;
  }
  
  public static boolean validate(Locale paramLocale) {
    String str = paramLocale.toString();
    return (!str.equals("") && str.length() < 24);
  }
}
