package io.fusionauth.domain.provider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.Enableable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@JsonIgnoreProperties(value = {"type"}, allowGetters = true, allowSetters = false)
public abstract class BaseIdentityProvider<D extends BaseIdentityProviderApplicationConfiguration> extends Enableable implements JSONColumnable {
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public final Map<String, Object> data = new HashMap<>();
  
  public Map<UUID, D> applicationConfiguration = new HashMap<>();
  
  @JSONColumn
  public Map<String, String> attributeMappings = new HashMap<>();
  
  @JSONColumn
  public boolean debug;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public LambdaConfiguration lambdaConfiguration = new LambdaConfiguration();
  
  public ZonedDateTime lastUpdateInstant;
  
  @JSONColumn
  public IdentityProviderLinkingStrategy linkingStrategy = IdentityProviderLinkingStrategy.LinkByEmail;
  
  public String name;
  
  public String source = "System";
  
  public Map<UUID, IdentityProviderTenantConfiguration> tenantConfiguration = new HashMap<>();
  
  public UUID tenantId;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof BaseIdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    BaseIdentityProvider baseIdentityProvider = (BaseIdentityProvider)paramObject;
    return (this.debug == baseIdentityProvider.debug && 
      Objects.equals(this.data, baseIdentityProvider.data) && 
      Objects.equals(this.applicationConfiguration, baseIdentityProvider.applicationConfiguration) && 
      Objects.equals(this.attributeMappings, baseIdentityProvider.attributeMappings) && 
      Objects.equals(this.id, baseIdentityProvider.id) && 
      Objects.equals(this.insertInstant, baseIdentityProvider.insertInstant) && 
      Objects.equals(this.lambdaConfiguration, baseIdentityProvider.lambdaConfiguration) && 
      Objects.equals(this.lastUpdateInstant, baseIdentityProvider.lastUpdateInstant) && this.linkingStrategy == baseIdentityProvider.linkingStrategy && 
      
      Objects.equals(this.name, baseIdentityProvider.name) && 
      Objects.equals(this.source, baseIdentityProvider.source) && 
      Objects.equals(this.tenantId, baseIdentityProvider.tenantId) && 
      Objects.equals(this.tenantConfiguration, baseIdentityProvider.tenantConfiguration));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          Integer.valueOf(super.hashCode()), this.data, this.applicationConfiguration, this.attributeMappings, Boolean.valueOf(this.debug), this.id, this.insertInstant, this.lambdaConfiguration, this.lastUpdateInstant, this.linkingStrategy, 
          this.name, this.source, this.tenantId, this.tenantConfiguration });
  }
  
  @JsonIgnore
  public boolean inUse() {
    return (this.enabled && this.applicationConfiguration.values().stream().anyMatch(paramBaseIdentityProviderApplicationConfiguration -> paramBaseIdentityProviderApplicationConfiguration.enabled));
  }
  
  public boolean isEnabledForApplicationId(UUID paramUUID) {
    BaseIdentityProviderApplicationConfiguration baseIdentityProviderApplicationConfiguration = (BaseIdentityProviderApplicationConfiguration)this.applicationConfiguration.get(paramUUID);
    return (baseIdentityProviderApplicationConfiguration != null && baseIdentityProviderApplicationConfiguration.enabled);
  }
  
  public void normalize() {}
  
  protected <R> R app(UUID paramUUID, Function<D, R> paramFunction) {
    BaseIdentityProviderApplicationConfiguration baseIdentityProviderApplicationConfiguration = (BaseIdentityProviderApplicationConfiguration)this.applicationConfiguration.get(paramUUID);
    return (baseIdentityProviderApplicationConfiguration == null) ? null : paramFunction.apply((D)baseIdentityProviderApplicationConfiguration);
  }
  
  protected <R> R app(String paramString, Function<D, R> paramFunction) {
    return app(parseUUID(paramString), paramFunction);
  }
  
  protected <R> R lookup(Supplier<R> paramSupplier1, Supplier<R> paramSupplier2) {
    R r = paramSupplier2.get();
    return (r != null) ? r : paramSupplier1.get();
  }
  
  protected UUID parseUUID(String paramString) {
    try {
      return UUID.fromString(paramString);
    } catch (IllegalArgumentException|NullPointerException illegalArgumentException) {
      return null;
    } 
  }
  
  public abstract IdentityProviderType getType();
  
  public static class LambdaConfiguration {
    public UUID reconcileId;
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (!(param1Object instanceof LambdaConfiguration))
        return false; 
      LambdaConfiguration lambdaConfiguration = (LambdaConfiguration)param1Object;
      return Objects.equals(this.reconcileId, lambdaConfiguration.reconcileId);
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { this.reconcileId });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
}
