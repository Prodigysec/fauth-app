package io.fusionauth.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.Entity;
import java.util.List;

public class EntitySearchResponse {
  public List<Entity> entities;
  
  public String nextResults;
  
  public long total;
  
  @JsonIgnore
  public boolean totalEqualToActual;
  
  public EntitySearchResponse() {}
  
  public EntitySearchResponse(List<Entity> paramList, long paramLong) {
    this.entities = paramList;
    this.total = paramLong;
  }
}
