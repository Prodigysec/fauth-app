package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class TenantUserDeletePolicy implements Buildable<TenantUserDeletePolicy> {
  public TimeBasedDeletePolicy unverified = new TimeBasedDeletePolicy();
  
  @JacksonConstructor
  public TenantUserDeletePolicy() {}
  
  public TenantUserDeletePolicy(TenantUserDeletePolicy paramTenantUserDeletePolicy) {
    this.unverified = new TimeBasedDeletePolicy(paramTenantUserDeletePolicy.unverified);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof TenantUserDeletePolicy))
      return false; 
    TenantUserDeletePolicy tenantUserDeletePolicy = (TenantUserDeletePolicy)paramObject;
    return Objects.equals(this.unverified, tenantUserDeletePolicy.unverified);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.unverified });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
