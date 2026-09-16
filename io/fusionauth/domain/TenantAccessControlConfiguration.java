package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.ExcludeFromJSONColumn;
import java.util.Objects;
import java.util.UUID;

public class TenantAccessControlConfiguration implements Buildable<TenantAccessControlConfiguration> {
  @ExcludeFromJSONColumn
  public UUID uiIPAccessControlListId;
  
  @JacksonConstructor
  public TenantAccessControlConfiguration() {}
  
  public TenantAccessControlConfiguration(TenantAccessControlConfiguration paramTenantAccessControlConfiguration) {
    this.uiIPAccessControlListId = paramTenantAccessControlConfiguration.uiIPAccessControlListId;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    TenantAccessControlConfiguration tenantAccessControlConfiguration = (TenantAccessControlConfiguration)paramObject;
    return Objects.equals(this.uiIPAccessControlListId, tenantAccessControlConfiguration.uiIPAccessControlListId);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.uiIPAccessControlListId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
