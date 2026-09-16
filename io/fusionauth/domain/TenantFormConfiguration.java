package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.ExcludeFromJSONColumn;
import java.util.Objects;
import java.util.UUID;

public class TenantFormConfiguration implements Buildable<TenantFormConfiguration> {
  @ExcludeFromJSONColumn
  public UUID adminUserFormId;
  
  @JacksonConstructor
  public TenantFormConfiguration() {}
  
  public TenantFormConfiguration(TenantFormConfiguration paramTenantFormConfiguration) {
    this.adminUserFormId = paramTenantFormConfiguration.adminUserFormId;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    TenantFormConfiguration tenantFormConfiguration = (TenantFormConfiguration)paramObject;
    return Objects.equals(this.adminUserFormId, tenantFormConfiguration.adminUserFormId);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.adminUserFormId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
