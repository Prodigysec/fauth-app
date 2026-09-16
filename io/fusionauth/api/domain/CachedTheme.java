package io.fusionauth.api.domain;

import io.fusionauth.api.util.PropertiesTools;
import io.fusionauth.domain.Theme;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class CachedTheme extends Theme {
  public final Properties defaultProperties;
  
  public final Map<Locale, Properties> localizedProperties = new HashMap<>();
  
  public CachedTheme(Theme paramTheme) {
    super(paramTheme);
    this.defaultProperties = loadProperties(paramTheme.defaultMessages);
    paramTheme.localizedMessages.forEach((paramLocale, paramString) -> this.localizedProperties.put(paramLocale, loadProperties(paramString)));
  }
  
  public CachedTheme(CachedTheme paramCachedTheme) {
    super(paramCachedTheme);
    this.defaultProperties = paramCachedTheme.defaultProperties;
    this.localizedProperties.putAll(paramCachedTheme.localizedProperties);
  }
  
  private Properties loadProperties(String paramString) {
    try {
      return PropertiesTools.loadProperties(paramString);
    } catch (Exception exception) {
      throw new IllegalStateException("Invalid message value for theme. Raw messages string:\n" + paramString, exception);
    } 
  }
}
