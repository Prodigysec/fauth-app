package io.fusionauth.domain.tenantManager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Enableable;
import io.fusionauth.domain.provider.IdentityProviderLinkingStrategy;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class TenantManagerIdentityProviderTypeConfiguration extends Enableable implements Buildable<TenantManagerIdentityProviderTypeConfiguration>, JSONColumnable {
  @JsonIgnore
  public Map<String, Object> data = new LinkedHashMap<>();
  
  @JSONColumn
  public Map<String, String> defaultAttributeMappings = new HashMap<>();
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  @JSONColumn
  public IdentityProviderLinkingStrategy linkingStrategy;
  
  public IdentityProviderType type;
  
  @JacksonConstructor
  public TenantManagerIdentityProviderTypeConfiguration() {}
  
  public TenantManagerIdentityProviderTypeConfiguration(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration) {
    this.data.putAll(paramTenantManagerIdentityProviderTypeConfiguration.data);
    this.defaultAttributeMappings.putAll(paramTenantManagerIdentityProviderTypeConfiguration.defaultAttributeMappings);
    this.insertInstant = paramTenantManagerIdentityProviderTypeConfiguration.insertInstant;
    this.lastUpdateInstant = paramTenantManagerIdentityProviderTypeConfiguration.lastUpdateInstant;
    this.linkingStrategy = paramTenantManagerIdentityProviderTypeConfiguration.linkingStrategy;
    this.type = paramTenantManagerIdentityProviderTypeConfiguration.type;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    TenantManagerIdentityProviderTypeConfiguration tenantManagerIdentityProviderTypeConfiguration = (TenantManagerIdentityProviderTypeConfiguration)paramObject;
    return (Objects.equals(this.defaultAttributeMappings, tenantManagerIdentityProviderTypeConfiguration.defaultAttributeMappings) && 
      Objects.equals(this.insertInstant, tenantManagerIdentityProviderTypeConfiguration.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, tenantManagerIdentityProviderTypeConfiguration.lastUpdateInstant) && this.linkingStrategy == tenantManagerIdentityProviderTypeConfiguration.linkingStrategy && this.type == tenantManagerIdentityProviderTypeConfiguration.type);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.defaultAttributeMappings, this.insertInstant, this.lastUpdateInstant, this.linkingStrategy, this.type });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
