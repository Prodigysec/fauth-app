package io.fusionauth.domain.api;

import io.fusionauth.domain.EntityGrant;

public class EntityGrantRequest {
  public EntityGrant grant;
  
  public EntityGrantRequest() {}
  
  public EntityGrantRequest(EntityGrant paramEntityGrant) {
    this.grant = paramEntityGrant;
  }
}
