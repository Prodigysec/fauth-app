package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.ExcludeFromJSONColumn;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EntityType implements Buildable<EntityType>, JSONColumnable {
  public final Map<String, Object> data = new LinkedHashMap<>();
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  @JSONColumn
  public EntityJWTConfiguration jwtConfiguration = new EntityJWTConfiguration();
  
  public ZonedDateTime lastUpdateInstant;
  
  public String name;
  
  public List<EntityTypePermission> permissions = new ArrayList<>();
  
  public EntityType(EntityType paramEntityType) {
    this.data.putAll(paramEntityType.data);
    this.id = paramEntityType.id;
    this.insertInstant = paramEntityType.insertInstant;
    this.jwtConfiguration = new EntityJWTConfiguration(paramEntityType.jwtConfiguration);
    this.lastUpdateInstant = paramEntityType.lastUpdateInstant;
    this.name = paramEntityType.name;
    paramEntityType.permissions.forEach(paramEntityTypePermission -> this.permissions.add(new EntityTypePermission(paramEntityTypePermission)));
  }
  
  public EntityType(String paramString) {
    this.name = paramString;
  }
  
  public EntityType(UUID paramUUID, String paramString) {
    this.id = paramUUID;
    this.name = paramString;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof EntityType))
      return false; 
    EntityType entityType = (EntityType)paramObject;
    return (Objects.equals(this.data, entityType.data) && 
      Objects.equals(this.id, entityType.id) && 
      Objects.equals(this.insertInstant, entityType.insertInstant) && 
      Objects.equals(this.jwtConfiguration, entityType.jwtConfiguration) && 
      Objects.equals(this.lastUpdateInstant, entityType.lastUpdateInstant) && 
      Objects.equals(this.name, entityType.name) && 
      Objects.equals(this.permissions, entityType.permissions));
  }
  
  public EntityTypePermission getPermission(String paramString) {
    for (EntityTypePermission entityTypePermission : this.permissions) {
      if (entityTypePermission.name.equals(paramString))
        return entityTypePermission; 
    } 
    return null;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.data, this.id, this.insertInstant, this.jwtConfiguration, this.lastUpdateInstant, this.name, this.permissions });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  @JacksonConstructor
  public EntityType() {}
  
  public static class EntityJWTConfiguration extends Enableable implements Buildable<EntityJWTConfiguration> {
    @ExcludeFromJSONColumn
    public UUID accessTokenKeyId;
    
    @ExcludeFromJSONColumn
    public List<UUID> accessTokenVerificationKeyIds = new ArrayList<>();
    
    public int timeToLiveInSeconds;
    
    @JacksonConstructor
    public EntityJWTConfiguration() {}
    
    public EntityJWTConfiguration(EntityJWTConfiguration param1EntityJWTConfiguration) {
      this.enabled = param1EntityJWTConfiguration.enabled;
      this.accessTokenKeyId = param1EntityJWTConfiguration.accessTokenKeyId;
      this.accessTokenVerificationKeyIds.addAll(param1EntityJWTConfiguration.accessTokenVerificationKeyIds);
      this.timeToLiveInSeconds = param1EntityJWTConfiguration.timeToLiveInSeconds;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (!(param1Object instanceof EntityJWTConfiguration))
        return false; 
      EntityJWTConfiguration entityJWTConfiguration = (EntityJWTConfiguration)param1Object;
      return (this.enabled == entityJWTConfiguration.enabled && this.timeToLiveInSeconds == entityJWTConfiguration.timeToLiveInSeconds && 
        
        Objects.equals(this.accessTokenKeyId, entityJWTConfiguration.accessTokenKeyId) && 
        Objects.equals(this.accessTokenVerificationKeyIds, entityJWTConfiguration.accessTokenVerificationKeyIds));
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { Boolean.valueOf(this.enabled), this.accessTokenKeyId, this.accessTokenVerificationKeyIds, Integer.valueOf(this.timeToLiveInSeconds) });
    }
  }
}
