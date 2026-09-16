package io.fusionauth.app.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import freemarker.template.TemplateExceptionHandler;
import io.fusionauth.api.configuration.FusionAuthConfiguration;

public class FusionAuthFreeMarkerTemplateExceptionHandlerProvider implements Provider<TemplateExceptionHandler> {
  private final FusionAuthConfiguration configuration;
  
  @Inject
  public FusionAuthFreeMarkerTemplateExceptionHandlerProvider(FusionAuthConfiguration paramFusionAuthConfiguration) {
    this.configuration = paramFusionAuthConfiguration;
  }
  
  public FusionAuthFreeMarkerTemplateExceptionHandler get() {
    return new FusionAuthFreeMarkerTemplateExceptionHandler(this.configuration);
  }
}
