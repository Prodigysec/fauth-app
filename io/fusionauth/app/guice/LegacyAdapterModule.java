package io.fusionauth.app.guice;

import com.google.inject.AbstractModule;
import io.fusionauth.app.service.legacy.DefaultLegacyTokenSigner;
import io.fusionauth.app.service.legacy.LegacyTokenSigner;

public class LegacyAdapterModule extends AbstractModule {
  protected void configure() {
    bind(LegacyTokenSigner.class).to(DefaultLegacyTokenSigner.class);
  }
}
