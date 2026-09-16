package io.fusionauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.util.Normalizer;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EntityTypePermission implements Buildable<EntityTypePermission>, JSONColumnable {
  public final Map<String, Object> data = new LinkedHashMap<>();
  
  public String description;
  
  @JsonIgnore
  public UUID entityTypeId;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public boolean isDefault;
  
  public ZonedDateTime lastUpdateInstant;
  
  public String name;
  
  @JacksonConstructor
  public EntityTypePermission() {}
  
  public EntityTypePermission(EntityTypePermission paramEntityTypePermission) {
    this.data.putAll(paramEntityTypePermission.data);
    this.description = paramEntityTypePermission.description;
    this.entityTypeId = paramEntityTypePermission.entityTypeId;
    this.id = paramEntityTypePermission.id;
    this.insertInstant = paramEntityTypePermission.insertInstant;
    this.isDefault = paramEntityTypePermission.isDefault;
    this.lastUpdateInstant = paramEntityTypePermission.lastUpdateInstant;
    this.name = paramEntityTypePermission.name;
  }
  
  public EntityTypePermission(String paramString) {
    this.name = paramString;
  }
  
  public EntityTypePermission(UUID paramUUID, String paramString) {
    this.id = paramUUID;
    this.name = paramString;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof EntityTypePermission))
      return false; 
    EntityTypePermission entityTypePermission = (EntityTypePermission)paramObject;
    return (Objects.equals(this.data, entityTypePermission.data) && 
      Objects.equals(this.description, entityTypePermission.description) && 
      Objects.equals(this.entityTypeId, entityTypePermission.entityTypeId) && 
      Objects.equals(this.id, entityTypePermission.id) && 
      Objects.equals(Boolean.valueOf(this.isDefault), Boolean.valueOf(entityTypePermission.isDefault)) && 
      Objects.equals(this.name, entityTypePermission.name) && 
      Objects.equals(this.insertInstant, entityTypePermission.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, entityTypePermission.lastUpdateInstant));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.data, this.description, this.entityTypeId, this.id, Boolean.valueOf(this.isDefault), this.name, this.insertInstant, this.lastUpdateInstant });
  }
  
  public void normalize() {
    this.description = Normalizer.trim(this.description);
    this.name = Normalizer.trim(this.name);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
