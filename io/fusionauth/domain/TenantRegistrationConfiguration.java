package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class TenantRegistrationConfiguration implements Buildable<TenantRegistrationConfiguration> {
  public Set<String> blockedDomains = new LinkedHashSet<>();
  
  @JacksonConstructor
  public TenantRegistrationConfiguration() {}
  
  public TenantRegistrationConfiguration(TenantRegistrationConfiguration paramTenantRegistrationConfiguration) {
    this.blockedDomains.addAll(paramTenantRegistrationConfiguration.blockedDomains);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    TenantRegistrationConfiguration tenantRegistrationConfiguration = (TenantRegistrationConfiguration)paramObject;
    return Objects.equals(this.blockedDomains, tenantRegistrationConfiguration.blockedDomains);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.blockedDomains });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
