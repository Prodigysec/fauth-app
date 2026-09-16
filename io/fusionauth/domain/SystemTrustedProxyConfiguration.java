package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.util.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SystemTrustedProxyConfiguration {
  public SystemTrustedProxyConfigurationPolicy trustPolicy = SystemTrustedProxyConfigurationPolicy.All;
  
  public List<String> trusted = new ArrayList<>();
  
  @JacksonConstructor
  public SystemTrustedProxyConfiguration() {}
  
  public SystemTrustedProxyConfiguration(SystemTrustedProxyConfiguration paramSystemTrustedProxyConfiguration) {
    this.trustPolicy = paramSystemTrustedProxyConfiguration.trustPolicy;
    this.trusted.addAll(paramSystemTrustedProxyConfiguration.trusted);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    SystemTrustedProxyConfiguration systemTrustedProxyConfiguration = (SystemTrustedProxyConfiguration)paramObject;
    return (this.trustPolicy == systemTrustedProxyConfiguration.trustPolicy && Objects.equals(this.trusted, systemTrustedProxyConfiguration.trusted));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.trustPolicy, this.trusted });
  }
  
  public void normalize() {
    Normalizer.removeEmpty(this.trusted);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
