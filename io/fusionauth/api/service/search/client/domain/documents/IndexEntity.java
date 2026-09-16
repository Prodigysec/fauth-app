package io.fusionauth.api.service.search.client.domain.documents;

import com.inversoft.search.client.domain.ElasticRequest;
import io.fusionauth.api.annotation.NoDoc;
import io.fusionauth.api.annotation.PublicDoc;
import io.fusionauth.domain.Entity;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class IndexEntity implements ElasticRequest {
  @PublicDoc(description = "The entity's data object. This is arbitrary JSON and all fields in this object are indexed.")
  public final Map<String, Object> data = new LinkedHashMap<>();
  
  @PublicDoc(description = "The client Id of this entity.")
  public String clientId;
  
  @PublicDoc(description = "The Id of this entity.")
  public String id;
  
  @PublicDoc(description = "The date the entity was inserted, as an Instant.")
  public ZonedDateTime insertInstant;
  
  @PublicDoc(description = "The date the entity was most recently updated, as an Instant.")
  public ZonedDateTime lastUpdateInstant;
  
  @PublicDoc(description = "The name of this entity.")
  public String name;
  
  @NoDoc
  public UUID parentId;
  
  @PublicDoc(description = "The tenant Id of this entity.")
  public UUID tenantId;
  
  @PublicDoc(description = "The Id of the entity type of this entity.")
  public UUID typeId;
  
  public IndexEntity(Entity paramEntity) {
    this.clientId = paramEntity.clientId;
    this.data.putAll(paramEntity.data);
    this.id = paramEntity.id.toString();
    this.insertInstant = paramEntity.insertInstant;
    this.lastUpdateInstant = paramEntity.lastUpdateInstant;
    this.name = paramEntity.name;
    this.parentId = paramEntity.parentId;
    this.tenantId = paramEntity.tenantId;
    this.typeId = paramEntity.type.id;
  }
}
