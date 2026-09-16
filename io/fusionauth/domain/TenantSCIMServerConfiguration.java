package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.ExcludeFromJSONColumn;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class TenantSCIMServerConfiguration extends Enableable implements Buildable<TenantSCIMServerConfiguration> {
  @ExcludeFromJSONColumn
  public UUID clientEntityTypeId;
  
  public Map<String, Object> schemas;
  
  @ExcludeFromJSONColumn
  public UUID serverEntityTypeId;
  
  @JacksonConstructor
  public TenantSCIMServerConfiguration() {}
  
  public TenantSCIMServerConfiguration(TenantSCIMServerConfiguration paramTenantSCIMServerConfiguration) {
    this.clientEntityTypeId = paramTenantSCIMServerConfiguration.clientEntityTypeId;
    this.enabled = paramTenantSCIMServerConfiguration.enabled;
    this.schemas = (paramTenantSCIMServerConfiguration.schemas != null) ? new LinkedHashMap<>(paramTenantSCIMServerConfiguration.schemas) : null;
    this.serverEntityTypeId = paramTenantSCIMServerConfiguration.serverEntityTypeId;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    TenantSCIMServerConfiguration tenantSCIMServerConfiguration = (TenantSCIMServerConfiguration)paramObject;
    return (Objects.equals(this.clientEntityTypeId, tenantSCIMServerConfiguration.clientEntityTypeId) && 
      Objects.equals(this.schemas, tenantSCIMServerConfiguration.schemas) && 
      Objects.equals(this.serverEntityTypeId, tenantSCIMServerConfiguration.serverEntityTypeId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.clientEntityTypeId, this.schemas, this.serverEntityTypeId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
