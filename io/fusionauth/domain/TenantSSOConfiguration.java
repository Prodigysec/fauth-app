package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class TenantSSOConfiguration {
  public boolean allowAccessTokenBootstrap = false;
  
  public int deviceTrustTimeToLiveInSeconds = 31536000;
  
  public TenantSSOConfiguration(TenantSSOConfiguration paramTenantSSOConfiguration) {
    this.allowAccessTokenBootstrap = paramTenantSSOConfiguration.allowAccessTokenBootstrap;
    this.deviceTrustTimeToLiveInSeconds = paramTenantSSOConfiguration.deviceTrustTimeToLiveInSeconds;
  }
  
  @JacksonConstructor
  public TenantSSOConfiguration() {}
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof TenantSSOConfiguration))
      return false; 
    TenantSSOConfiguration tenantSSOConfiguration = (TenantSSOConfiguration)paramObject;
    return (this.allowAccessTokenBootstrap == tenantSSOConfiguration.allowAccessTokenBootstrap && this.deviceTrustTimeToLiveInSeconds == tenantSSOConfiguration.deviceTrustTimeToLiveInSeconds);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Boolean.valueOf(this.allowAccessTokenBootstrap), Integer.valueOf(this.deviceTrustTimeToLiveInSeconds) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
