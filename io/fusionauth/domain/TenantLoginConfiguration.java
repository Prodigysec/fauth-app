package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class TenantLoginConfiguration implements Buildable<TenantLoginConfiguration> {
  public boolean requireAuthentication = true;
  
  public TenantLoginConfiguration(TenantLoginConfiguration paramTenantLoginConfiguration) {
    this.requireAuthentication = paramTenantLoginConfiguration.requireAuthentication;
  }
  
  @JacksonConstructor
  public TenantLoginConfiguration() {}
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    TenantLoginConfiguration tenantLoginConfiguration = (TenantLoginConfiguration)paramObject;
    return (this.requireAuthentication == tenantLoginConfiguration.requireAuthentication);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Boolean.valueOf(this.requireAuthentication) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
