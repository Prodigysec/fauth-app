package io.fusionauth.api.domain.guice;

import com.google.inject.AbstractModule;
import io.fusionauth.client.FusionAuthClient;
import java.util.UUID;

public class FusionAuthClientModule extends AbstractModule {
  protected void configure() {
    bind(FusionAuthClient.class).toProvider(FusionAuthClientProvider.class);
    bind(UUID.class).annotatedWith(FusionAuthTenantId.class).toProvider(FusionAuthTenantIdProvider.class);
    bind(String.class).annotatedWith(FusionAuthLocalClientURL.class).toProvider(FusionAuthLocalURLProvider.class).asEagerSingleton();
  }
}
