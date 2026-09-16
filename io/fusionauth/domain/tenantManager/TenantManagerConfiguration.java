package io.fusionauth.domain.tenantManager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.ExcludeFromJSONColumn;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.Buildable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TenantManagerConfiguration implements Buildable<TenantManagerConfiguration>, JSONColumnable {
  @ExcludeFromJSONColumn
  public List<TenantManagerApplicationConfiguration> applicationConfigurations = new ArrayList<>();
  
  public UUID attributeFormId;
  
  @JSONColumn
  public String brandName;
  
  @JsonIgnore
  public Map<String, Object> data = new HashMap<>();
  
  @ExcludeFromJSONColumn
  public Map<String, TenantManagerIdentityProviderTypeConfiguration> identityProviderTypeConfigurations = new HashMap<>();
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  public TenantManagerConfiguration(TenantManagerConfiguration paramTenantManagerConfiguration) {
    this.applicationConfigurations.addAll((Collection<? extends TenantManagerApplicationConfiguration>)paramTenantManagerConfiguration.applicationConfigurations.stream().map(TenantManagerApplicationConfiguration::new).collect(Collectors.toList()));
    this.attributeFormId = paramTenantManagerConfiguration.attributeFormId;
    this.brandName = paramTenantManagerConfiguration.brandName;
    if (paramTenantManagerConfiguration.data != null)
      this.data.putAll(paramTenantManagerConfiguration.data); 
    paramTenantManagerConfiguration.identityProviderTypeConfigurations.forEach((paramString, paramTenantManagerIdentityProviderTypeConfiguration) -> this.identityProviderTypeConfigurations.put(paramString, new TenantManagerIdentityProviderTypeConfiguration(paramTenantManagerIdentityProviderTypeConfiguration)));
    this.insertInstant = paramTenantManagerConfiguration.insertInstant;
    this.lastUpdateInstant = paramTenantManagerConfiguration.lastUpdateInstant;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof TenantManagerConfiguration))
      return false; 
    TenantManagerConfiguration tenantManagerConfiguration = (TenantManagerConfiguration)paramObject;
    return (Objects.equals(this.applicationConfigurations, tenantManagerConfiguration.applicationConfigurations) && 
      Objects.equals(this.attributeFormId, tenantManagerConfiguration.attributeFormId) && 
      Objects.equals(this.brandName, tenantManagerConfiguration.brandName) && 
      Objects.equals(this.data, tenantManagerConfiguration.data) && 
      Objects.equals(this.identityProviderTypeConfigurations, tenantManagerConfiguration.identityProviderTypeConfigurations) && 
      Objects.equals(this.insertInstant, tenantManagerConfiguration.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, tenantManagerConfiguration.lastUpdateInstant));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.applicationConfigurations, this.attributeFormId, this.brandName, this.data, this.identityProviderTypeConfigurations, this.insertInstant, this.lastUpdateInstant });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  @JacksonConstructor
  public TenantManagerConfiguration() {}
}
