package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Entity;

public class EntityRequest implements Buildable<EntityRequest> {
  public Entity entity;
  
  @JacksonConstructor
  public EntityRequest() {}
  
  public EntityRequest(Entity paramEntity) {
    this.entity = paramEntity;
  }
}
