package io.fusionauth.api.migration.guice;

import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.migration.Migration;
import io.fusionauth.api.domain.SystemConfigurationMapper;
import io.fusionauth.api.domain.ThemeMapper;
import io.fusionauth.api.service.system.ThemeService;
import io.fusionauth.domain.CORSConfiguration;
import io.fusionauth.domain.SystemConfiguration;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.util.HTTPMethod;
import io.fusionauth.http.server.HTTPContext;
import java.util.Arrays;

public class Migration_1_8_0 implements Migration {
  private final CacheNotifier cacheNotifier;
  
  private final HTTPContext context;
  
  private final SystemConfigurationMapper systemConfigurationMapper;
  
  private final ThemeMapper themeMapper;
  
  @Inject
  public Migration_1_8_0(CacheNotifier paramCacheNotifier, HTTPContext paramHTTPContext, SystemConfigurationMapper paramSystemConfigurationMapper, ThemeMapper paramThemeMapper) {
    this.cacheNotifier = paramCacheNotifier;
    this.context = paramHTTPContext;
    this.systemConfigurationMapper = paramSystemConfigurationMapper;
    this.themeMapper = paramThemeMapper;
  }
  
  public void cleanup() {}
  
  public void runOnce() {
    this.systemConfigurationMapper.update(this.systemConfigurationMapper
        .retrieve()
        .with(paramSystemConfiguration -> paramSystemConfiguration.corsConfiguration = (new CORSConfiguration()).with(()).with(()).with(()).with(()).with(()).with(())));
    Theme.Templates templates = ThemeService.templatesFromFilesystem(this.context.resolve("/"));
    this.themeMapper.retrieveAll()
      .stream()
      .filter(paramTheme -> !paramTheme.id.equals(Theme.FUSIONAUTH_THEME_ID))
      .forEach(paramTheme -> this.themeMapper.update(paramTheme.with(()).with(())));
    this.cacheNotifier.reload("Themes");
  }
}
