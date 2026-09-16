package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.api.domain.json.annotation.MaskString;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Entity implements Buildable<Entity>, JSONColumnable, Tenantable {
  public final Map<String, Object> data = new LinkedHashMap<>();
  
  public String clientId;
  
  @MaskString
  public String clientSecret;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  public String name;
  
  public UUID parentId;
  
  public UUID tenantId;
  
  public EntityType type;
  
  public Entity(Entity paramEntity) {
    this.data.putAll(paramEntity.data);
    this.clientId = paramEntity.clientId;
    this.clientSecret = paramEntity.clientSecret;
    this.id = paramEntity.id;
    this.insertInstant = paramEntity.insertInstant;
    this.lastUpdateInstant = paramEntity.lastUpdateInstant;
    this.name = paramEntity.name;
    this.parentId = paramEntity.parentId;
    this.tenantId = paramEntity.tenantId;
    this.type = (paramEntity.type == null) ? null : new EntityType(paramEntity.type);
  }
  
  public Entity(String paramString) {
    this.name = paramString;
  }
  
  public Entity(UUID paramUUID, String paramString) {
    this.id = paramUUID;
    this.name = paramString;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof Entity))
      return false; 
    Entity entity = (Entity)paramObject;
    return (Objects.equals(this.clientSecret, entity.clientSecret) && 
      Objects.equals(this.data, entity.data) && 
      Objects.equals(this.id, entity.id) && 
      Objects.equals(this.name, entity.name) && 
      Objects.equals(this.parentId, entity.parentId) && 
      Objects.equals(this.tenantId, entity.tenantId) && 
      Objects.equals(this.type, entity.type) && 
      Objects.equals(this.insertInstant, entity.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, entity.lastUpdateInstant));
  }
  
  public UUID getTenantId() {
    return this.tenantId;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.clientSecret, this.data, this.id, this.name, this.parentId, this.tenantId, this.type, this.insertInstant, this.lastUpdateInstant });
  }
  
  public Entity secure() {
    this.clientSecret = null;
    return this;
  }
  
  public Entity sort() {
    if (this.type != null && this.type.permissions != null)
      this.type.permissions.sort(Comparator.comparing(paramEntityTypePermission -> paramEntityTypePermission.name)); 
    return this;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  @JacksonConstructor
  public Entity() {}
}
