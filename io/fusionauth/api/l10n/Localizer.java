package io.fusionauth.api.l10n;

import com.inversoft.time.DurationFormatter;
import io.fusionauth.domain.LocalizedStrings;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class Localizer {
  public static String localize(List<Locale> paramList, Duration paramDuration) {
    if (paramDuration == null)
      return null; 
    if (paramList.isEmpty())
      return DurationFormatter.format(paramDuration, Locale.ENGLISH); 
    return DurationFormatter.format(paramDuration, paramList.get(0));
  }
  
  public static String localize(List<Locale> paramList, LocalizedStrings paramLocalizedStrings, String paramString) {
    if (paramLocalizedStrings == null || paramList.isEmpty())
      return paramString; 
    for (Locale locale : paramList) {
      String str = paramLocalizedStrings.get(locale);
      if (str != null)
        return str; 
      if (locale.getCountry() != null) {
        str = paramLocalizedStrings.get(new Locale(locale.getLanguage()));
        if (str != null)
          return str; 
      } 
    } 
    return paramString;
  }
}
