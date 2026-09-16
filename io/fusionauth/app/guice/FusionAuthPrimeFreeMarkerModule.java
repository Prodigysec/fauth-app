package io.fusionauth.app.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import io.fusionauth.app.freemarker.FusionAuthFreeMarkerMap;
import io.fusionauth.app.freemarker.FusionAuthFreemarkerService;
import io.fusionauth.app.primeframework.FusionAuthTemplateLoader;
import org.primeframework.mvc.freemarker.FreeMarkerMap;
import org.primeframework.mvc.freemarker.FreeMarkerService;

public class FusionAuthPrimeFreeMarkerModule extends AbstractModule {
  protected void configure() {
    bind(Configuration.class).toProvider(FusionAuthFreeMarkerConfigurationProvider.class).in(Singleton.class);
    bind(FreeMarkerMap.class).to(FusionAuthFreeMarkerMap.class);
    bind(TemplateLoader.class).to(FusionAuthTemplateLoader.class);
    bind(TemplateExceptionHandler.class).toProvider(FusionAuthFreeMarkerTemplateExceptionHandlerProvider.class).in(Singleton.class);
    bind(FreeMarkerService.class).to(FusionAuthFreemarkerService.class).in(Singleton.class);
  }
}
