package io.fusionauth.domain;

import com.inversoft.mybatis.JSONColumnable;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class EntityGrant implements Buildable<EntityGrant>, JSONColumnable {
  public Map<String, Object> data = new LinkedHashMap<>();
  
  public Entity entity;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  public Set<String> permissions = new TreeSet<>();
  
  public UUID recipientEntityId;
  
  public UUID userId;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof EntityGrant))
      return false; 
    EntityGrant entityGrant = (EntityGrant)paramObject;
    return (Objects.equals(this.data, entityGrant.data) && 
      Objects.equals(this.id, entityGrant.id) && 
      Objects.equals(this.insertInstant, entityGrant.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, entityGrant.lastUpdateInstant) && 
      Objects.equals(this.permissions, entityGrant.permissions) && 
      Objects.equals(this.recipientEntityId, entityGrant.recipientEntityId) && 
      Objects.equals(this.userId, entityGrant.userId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.data, this.id, this.insertInstant, this.lastUpdateInstant, this.permissions, this.recipientEntityId, this.userId });
  }
}
