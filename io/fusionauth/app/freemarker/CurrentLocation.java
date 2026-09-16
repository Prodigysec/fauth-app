package io.fusionauth.app.freemarker;

import com.google.inject.Inject;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.service.cache.SystemConfigurationCache;
import io.fusionauth.api.service.ip.LocationService;
import io.fusionauth.api.util.NetworkTools;
import io.fusionauth.domain.Location;
import io.fusionauth.http.server.HTTPRequest;
import java.util.List;
import java.util.Optional;

public class CurrentLocation implements TemplateMethodModelEx {
  private static final String CurrentLocation = "currentLocation";
  
  private final FusionAuthConfiguration configuration;
  
  private final HTTPRequest httpRequest;
  
  private final LocationService locationService;
  
  private final SystemConfigurationCache systemConfigurationCache;
  
  @Inject
  public CurrentLocation(FusionAuthConfiguration paramFusionAuthConfiguration, HTTPRequest paramHTTPRequest, LocationService paramLocationService, SystemConfigurationCache paramSystemConfigurationCache) {
    this.configuration = paramFusionAuthConfiguration;
    this.httpRequest = paramHTTPRequest;
    this.locationService = paramLocationService;
    this.systemConfigurationCache = paramSystemConfigurationCache;
  }
  
  public Object exec(List paramList) throws TemplateModelException {
    Optional<Location> optional = (Optional)this.httpRequest.getAttribute("currentLocation");
    if (optional == null) {
      String str = NetworkTools.getTrustedClientIPAddress(this.httpRequest, this.configuration, this.systemConfigurationCache.get());
      optional = Optional.ofNullable(this.locationService.ipToLocation(str));
      this.httpRequest.setAttribute("currentLocation", optional);
    } 
    return optional.orElse(null);
  }
}
