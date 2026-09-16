package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.EntityGrant;
import io.fusionauth.domain.EntityTypePermission;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EntityGrantResponse {
  public final List<EntityGrant> grants = new ArrayList<>();
  
  public EntityGrant grant;
  
  public EntityGrantResponse(EntityGrant paramEntityGrant) {
    this.grant = paramEntityGrant;
    this.grant.entity.type.permissions.sort(Comparator.comparing(paramEntityTypePermission -> paramEntityTypePermission.name));
  }
  
  public EntityGrantResponse(List<EntityGrant> paramList) {
    this.grants.addAll(paramList);
    this.grants.forEach(paramEntityGrant -> paramEntityGrant.entity.type.permissions.sort(Comparator.comparing(())));
  }
  
  @JacksonConstructor
  public EntityGrantResponse() {}
}
