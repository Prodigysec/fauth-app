package io.fusionauth.domain.tenantManager;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;
import java.util.UUID;

public class TenantManagerApplicationConfiguration implements Buildable<TenantManagerApplicationConfiguration> {
  public UUID applicationId;
  
  @JacksonConstructor
  public TenantManagerApplicationConfiguration() {}
  
  public TenantManagerApplicationConfiguration(UUID paramUUID) {
    this.applicationId = paramUUID;
  }
  
  public TenantManagerApplicationConfiguration(TenantManagerApplicationConfiguration paramTenantManagerApplicationConfiguration) {
    this.applicationId = paramTenantManagerApplicationConfiguration.applicationId;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof TenantManagerApplicationConfiguration))
      return false; 
    TenantManagerApplicationConfiguration tenantManagerApplicationConfiguration = (TenantManagerApplicationConfiguration)paramObject;
    return Objects.equals(this.applicationId, tenantManagerApplicationConfiguration.applicationId);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.applicationId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
