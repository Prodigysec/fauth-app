package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class TenantUnverifiedConfiguration implements Buildable<TenantUnverifiedConfiguration> {
  public UnverifiedBehavior email;
  
  public RegistrationUnverifiedOptions whenGated = new RegistrationUnverifiedOptions();
  
  public TenantUnverifiedConfiguration(TenantUnverifiedConfiguration paramTenantUnverifiedConfiguration) {
    this.email = paramTenantUnverifiedConfiguration.email;
    this.whenGated = new RegistrationUnverifiedOptions(paramTenantUnverifiedConfiguration.whenGated);
  }
  
  @JacksonConstructor
  public TenantUnverifiedConfiguration() {}
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    TenantUnverifiedConfiguration tenantUnverifiedConfiguration = (TenantUnverifiedConfiguration)paramObject;
    return (this.email == tenantUnverifiedConfiguration.email && 
      Objects.equals(this.whenGated, tenantUnverifiedConfiguration.whenGated));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.email, this.whenGated });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
