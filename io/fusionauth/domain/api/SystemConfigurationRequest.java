package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.SystemConfiguration;

public class SystemConfigurationRequest {
  public SystemConfiguration systemConfiguration;
  
  @JacksonConstructor
  public SystemConfigurationRequest() {}
  
  public SystemConfigurationRequest(SystemConfiguration paramSystemConfiguration) {
    this.systemConfiguration = paramSystemConfiguration;
  }
}
