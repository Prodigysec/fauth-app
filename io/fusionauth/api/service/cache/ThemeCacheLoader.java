package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.inversoft.cache.Cache;
import com.inversoft.cache.CacheLoader;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.api.domain.ThemeMapper;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.ThemeService;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.ThemeType;
import io.fusionauth.http.server.HTTPContext;
import java.util.HashMap;
import java.util.UUID;

public class ThemeCacheLoader implements CacheLoader, Runnable {
  private final Cache<UUID, CachedTheme> cache;
  
  private final HTTPContext context;
  
  private final ThemeMapper themeMapper;
  
  @Inject
  public ThemeCacheLoader(Cache<UUID, CachedTheme> paramCache, HTTPContext paramHTTPContext, ThemeMapper paramThemeMapper) {
    this.cache = paramCache;
    this.context = paramHTTPContext;
    this.themeMapper = paramThemeMapper;
  }
  
  public void load() {
    HashMap<Object, Object> hashMap = new HashMap<>();
    for (Theme theme : this.themeMapper.retrieveAll()) {
      try {
        if (theme.id.equals(Theme.FUSIONAUTH_THEME_ID))
          ThemeService.populateFusionAuthTheme(theme, this.context.resolve("/")); 
        if (theme.type.equals(ThemeType.simple))
          ThemeService.populateSimpleTheme(theme, this.context.resolve("/")); 
        hashMap.put(theme.id, new CachedTheme(theme));
      } catch (Exception exception) {
        EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to load theme [" + theme.name + "] into the cache.", exception));
      } 
    } 
    this.cache.replace(hashMap);
  }
  
  public void run() {
    load();
  }
}
