package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EntityTypePermission;
import java.util.Comparator;

public class EntityResponse {
  public Entity entity;
  
  @JacksonConstructor
  public EntityResponse() {}
  
  public EntityResponse(Entity paramEntity) {
    this.entity = paramEntity;
    this.entity.type.permissions.sort(Comparator.comparing(paramEntityTypePermission -> paramEntityTypePermission.name));
  }
}
