package io.fusionauth.api.network;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import io.fusionauth.api.configuration.FusionAuthConfiguration;

public class FusionAuthProxyProvider implements Provider<ProxyInfoSupplier> {
  private final FusionAuthConfiguration configuration;
  
  @Inject
  public FusionAuthProxyProvider(FusionAuthConfiguration paramFusionAuthConfiguration) {
    this.configuration = paramFusionAuthConfiguration;
  }
  
  public ProxyInfoSupplier get() {
    return () -> new ProxyInfo(this.configuration.proxyHost(), this.configuration.proxyPort(), this.configuration.proxyUsername(), this.configuration.proxyPassword());
  }
}
