package io.fusionauth.app.guice;

import com.google.inject.Inject;
import freemarker.cache.TemplateLoader;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.RuntimeMode;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.freemarker.guice.FreeMarkerConfigurationProvider;

public class FusionAuthFreeMarkerConfigurationProvider extends FreeMarkerConfigurationProvider {
  private final FusionAuthConfiguration configuration;
  
  private final TemplateExceptionHandler templateExceptionHandler;
  
  @Inject
  public FusionAuthFreeMarkerConfigurationProvider(FusionAuthConfiguration paramFusionAuthConfiguration, MVCConfiguration paramMVCConfiguration, TemplateExceptionHandler paramTemplateExceptionHandler, TemplateLoader paramTemplateLoader) {
    super(paramMVCConfiguration, paramTemplateLoader);
    this.configuration = paramFusionAuthConfiguration;
    this.templateExceptionHandler = paramTemplateExceptionHandler;
  }
  
  public Configuration get() {
    Configuration configuration = super.get();
    RuntimeMode runtimeMode = this.configuration.runtimeMode();
    configuration.setLogTemplateExceptions((runtimeMode != RuntimeMode.Production));
    configuration.setTemplateExceptionHandler(this.templateExceptionHandler);
    configuration.setObjectWrapper(rebuildBeanWrapperWithMemberAccessPolicy());
    return configuration;
  }
  
  protected Version incompatibleImprovementsVersion() {
    return Configuration.VERSION_2_3_34;
  }
  
  private ObjectWrapper rebuildBeanWrapperWithMemberAccessPolicy() {
    Version version = incompatibleImprovementsVersion();
    BeansWrapperBuilder beansWrapperBuilder = new BeansWrapperBuilder(version);
    beansWrapperBuilder.setExposeFields(true);
    beansWrapperBuilder.setSimpleMapWrapper(true);
    beansWrapperBuilder.setMemberAccessPolicy(FusionAuthMemberAccessPolicy.getInstance(version));
    return (ObjectWrapper)beansWrapperBuilder.build();
  }
}
