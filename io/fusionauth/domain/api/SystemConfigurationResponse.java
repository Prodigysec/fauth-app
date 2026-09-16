package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.SystemConfiguration;

public class SystemConfigurationResponse {
  public SystemConfiguration systemConfiguration;
  
  @JacksonConstructor
  public SystemConfigurationResponse() {}
  
  public SystemConfigurationResponse(SystemConfiguration paramSystemConfiguration) {
    this.systemConfiguration = paramSystemConfiguration;
  }
}
