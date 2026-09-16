package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import io.fusionauth.api.service.cache.CORSConfigurationCache;
import io.fusionauth.domain.CORSConfiguration;
import io.fusionauth.domain.util.HTTPMethod;
import io.fusionauth.http.HTTPMethod;
import java.util.regex.Pattern;
import org.primeframework.mvc.cors.CORSConfiguration;
import org.primeframework.mvc.cors.CORSConfigurationProvider;

public class FusionAuthCORSConfigurationProvider implements CORSConfigurationProvider {
  private static final Pattern EXCLUDED_URI_PATTERN = Pattern.compile("^/account.*|^/admin.*|^/support.*|^/ajax.*|^/css/.*|^/fonts/.*|^/images/.*|^/js/.*");
  
  private final CORSConfigurationCache corsConfigurationCache;
  
  @Inject
  public FusionAuthCORSConfigurationProvider(CORSConfigurationCache paramCORSConfigurationCache) {
    this.corsConfigurationCache = paramCORSConfigurationCache;
  }
  
  public CORSConfiguration get() {
    CORSConfiguration cORSConfiguration = this.corsConfigurationCache.get();
    if (cORSConfiguration != null && cORSConfiguration.enabled)
      return (CORSConfiguration)((CORSConfiguration)((CORSConfiguration)((CORSConfiguration)((CORSConfiguration)((CORSConfiguration)((CORSConfiguration)((CORSConfiguration)(new CORSConfiguration())
        .with(paramCORSConfiguration1 -> paramCORSConfiguration1.allowCredentials = paramCORSConfiguration.allowCredentials))
        .with(paramCORSConfiguration1 -> paramCORSConfiguration1.allowedHeaders.addAll(paramCORSConfiguration.allowedHeaders)))
        .with(paramCORSConfiguration1 -> paramCORSConfiguration1.allowedMethods.addAll(paramCORSConfiguration.allowedMethods.stream().map(()).toList())))
        .with(paramCORSConfiguration1 -> paramCORSConfiguration1.allowedOrigins.addAll(paramCORSConfiguration.allowedOrigins)))
        .with(paramCORSConfiguration1 -> paramCORSConfiguration1.debug = paramCORSConfiguration.debug))
        .with(paramCORSConfiguration -> paramCORSConfiguration.excludedPathPattern = EXCLUDED_URI_PATTERN))
        .with(paramCORSConfiguration1 -> paramCORSConfiguration1.exposedHeaders.addAll(paramCORSConfiguration.exposedHeaders)))
        .with(paramCORSConfiguration1 -> paramCORSConfiguration1.preflightMaxAgeInSeconds = paramCORSConfiguration.preflightMaxAgeInSeconds); 
    return null;
  }
}
