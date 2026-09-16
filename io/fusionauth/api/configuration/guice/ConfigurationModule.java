package io.fusionauth.api.configuration.guice;

import com.google.inject.AbstractModule;
import com.inversoft.configuration.InversoftConfiguration;
import io.fusionauth.api.configuration.DefaultFusionAuthConfiguration;
import io.fusionauth.api.configuration.FusionAuthConfiguration;

public class ConfigurationModule extends AbstractModule {
  protected void configure() {
    bind(InversoftConfiguration.class).to(DefaultFusionAuthConfiguration.class).asEagerSingleton();
    bind(FusionAuthConfiguration.class).to(DefaultFusionAuthConfiguration.class).asEagerSingleton();
  }
}
