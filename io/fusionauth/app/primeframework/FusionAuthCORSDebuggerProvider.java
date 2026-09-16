package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.fusionauth.api.service.cache.CORSConfigurationCache;
import io.fusionauth.api.service.system.eventLog.Debugger;
import org.primeframework.mvc.cors.CORSDebugger;

public class FusionAuthCORSDebuggerProvider implements Provider<CORSDebugger> {
  private final CORSConfigurationCache corsConfigurationCache;
  
  @Inject
  public FusionAuthCORSDebuggerProvider(CORSConfigurationCache paramCORSConfigurationCache) {
    this.corsConfigurationCache = paramCORSConfigurationCache;
  }
  
  public CORSDebugger get() {
    return new Debugger((this.corsConfigurationCache.get()).debug, "CORS Debugger");
  }
}
