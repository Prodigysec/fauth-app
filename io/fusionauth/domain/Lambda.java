package io.fusionauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class Lambda implements Buildable<Lambda> {
  public String body;
  
  public boolean debug;
  
  @Deprecated
  @JsonIgnore
  public boolean enabled;
  
  public LambdaEngineType engineType = LambdaEngineType.GraalJS;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  public String name;
  
  public LambdaType type;
  
  @JacksonConstructor
  public Lambda() {}
  
  public Lambda(Lambda paramLambda) {
    this.body = paramLambda.body;
    this.name = paramLambda.name;
    this.debug = paramLambda.debug;
    this.enabled = paramLambda.enabled;
    this.engineType = paramLambda.engineType;
    this.id = paramLambda.id;
    this.insertInstant = paramLambda.insertInstant;
    this.lastUpdateInstant = paramLambda.lastUpdateInstant;
    this.type = paramLambda.type;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    Lambda lambda = (Lambda)paramObject;
    return (this.debug == lambda.debug && this.enabled == lambda.enabled && 
      
      Objects.equals(this.body, lambda.body) && this.engineType == lambda.engineType && 
      
      Objects.equals(this.id, lambda.id) && 
      Objects.equals(this.insertInstant, lambda.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, lambda.lastUpdateInstant) && 
      Objects.equals(this.name, lambda.name) && this.type == lambda.type);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.body, Boolean.valueOf(this.debug), Boolean.valueOf(this.enabled), this.engineType, this.id, this.insertInstant, this.lastUpdateInstant, this.name, this.type });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
