package io.fusionauth.api.migration.guice;

import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.migration.Migration;
import io.fusionauth.api.domain.ThemeMapper;
import io.fusionauth.api.service.system.ThemeService;
import io.fusionauth.domain.Theme;
import io.fusionauth.http.server.HTTPContext;

public class Migration_1_10_0 implements Migration {
  private final CacheNotifier cacheNotifier;
  
  private final HTTPContext context;
  
  private final ThemeMapper themeMapper;
  
  @Inject
  public Migration_1_10_0(CacheNotifier paramCacheNotifier, HTTPContext paramHTTPContext, ThemeMapper paramThemeMapper) {
    this.cacheNotifier = paramCacheNotifier;
    this.context = paramHTTPContext;
    this.themeMapper = paramThemeMapper;
  }
  
  public void cleanup() {}
  
  public void runOnce() {
    Theme.Templates templates = ThemeService.templatesFromFilesystem(this.context.resolve("/"));
    this.themeMapper.retrieveAll()
      .stream()
      .filter(paramTheme -> !paramTheme.id.equals(Theme.FUSIONAUTH_THEME_ID))
      .forEach(paramTheme -> this.themeMapper.update(paramTheme.with(()).with(())));
    this.cacheNotifier.reload("Themes");
  }
}
