package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.EntityTypePermission;

public class EntityTypeRequest implements Buildable<EntityTypeRequest> {
  public EntityType entityType;
  
  public EntityTypePermission permission;
  
  @JacksonConstructor
  public EntityTypeRequest() {}
  
  public EntityTypeRequest(EntityType paramEntityType) {
    this.entityType = paramEntityType;
  }
  
  public EntityTypeRequest(EntityTypePermission paramEntityTypePermission) {
    this.permission = paramEntityTypePermission;
  }
}
