package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.EntityTypePermission;
import java.util.Comparator;
import java.util.List;

public class EntityTypeResponse {
  public EntityType entityType;
  
  public List<EntityType> entityTypes;
  
  public EntityTypePermission permission;
  
  @JacksonConstructor
  public EntityTypeResponse() {}
  
  public EntityTypeResponse(EntityType paramEntityType) {
    this.entityType = paramEntityType;
    this.entityType.permissions.sort(Comparator.comparing(paramEntityTypePermission -> paramEntityTypePermission.name));
  }
  
  public EntityTypeResponse(List<EntityType> paramList) {
    this.entityTypes = paramList;
    this.entityTypes.sort(Comparator.comparing(paramEntityType -> paramEntityType.name));
    this.entityTypes.forEach(paramEntityType -> paramEntityType.permissions.sort(Comparator.comparing(())));
  }
  
  public EntityTypeResponse(EntityTypePermission paramEntityTypePermission) {
    this.permission = paramEntityTypePermission;
  }
}
