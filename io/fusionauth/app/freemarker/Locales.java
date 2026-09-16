package io.fusionauth.app.freemarker;

import com.google.inject.Inject;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import org.primeframework.mvc.locale.LocaleProvider;

public class Locales implements TemplateMethodModelEx {
  private final LocaleProvider localeProvider;
  
  @Inject
  public Locales(LocaleProvider paramLocaleProvider) {
    this.localeProvider = paramLocaleProvider;
  }
  
  public Object exec(List paramList) throws TemplateModelException {
    LinkedHashMap<Object, Object> linkedHashMap = new LinkedHashMap<>();
    ArrayList<? super Locale> arrayList = new ArrayList();
    Collections.addAll(arrayList, Locale.getAvailableLocales());
    arrayList.removeIf(paramLocale -> (paramLocale.getLanguage().isEmpty() || paramLocale.hasExtensions() || !paramLocale.getScript().isEmpty() || !paramLocale.getVariant().isEmpty()));
    arrayList.sort(Comparator.comparing(paramLocale -> paramLocale.getDisplayName((Locale)this.localeProvider.get())));
    for (Locale locale : arrayList) {
      if (!linkedHashMap.containsKey(locale))
        linkedHashMap.put(locale, locale.getDisplayName((Locale)this.localeProvider.get())); 
    } 
    return linkedHashMap;
  }
}
