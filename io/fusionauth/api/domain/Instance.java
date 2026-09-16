package io.fusionauth.api.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.api.service.system.SetupService;
import io.fusionauth.domain.Buildable;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Instance implements Buildable<Instance>, JSONColumnable {
  public ZonedDateTime activateInstant;
  
  public Map<String, Object> data = new LinkedHashMap<>();
  
  public Boolean fipsEnabled;
  
  @JSONColumn
  public SetupService.FirstTimeSetup firstTimeSetup = new SetupService.FirstTimeSetup();
  
  public UUID id;
  
  public ReactorHealthChecks reactorHealthChecks = new ReactorHealthChecks();
  
  public boolean setupComplete;
  
  @Deprecated(since = "1.65.0")
  @JSONColumn
  public JsonNode status;
  
  public Instance() {}
  
  public Instance(Instance paramInstance) {
    this.activateInstant = paramInstance.activateInstant;
    this.data.putAll(paramInstance.data);
    this.firstTimeSetup = new SetupService.FirstTimeSetup(paramInstance.firstTimeSetup);
    this.fipsEnabled = paramInstance.fipsEnabled;
    this.id = paramInstance.id;
    this.setupComplete = paramInstance.setupComplete;
    this.reactorHealthChecks = new ReactorHealthChecks(paramInstance.reactorHealthChecks);
    this.status = paramInstance.status;
  }
  
  public boolean equals(Object paramObject) {
    Instance instance;
    if (this == paramObject)
      return true; 
    if (paramObject instanceof Instance) {
      instance = (Instance)paramObject;
    } else {
      return false;
    } 
    return (this.fipsEnabled == instance.fipsEnabled && this.setupComplete == instance.setupComplete && 
      
      Objects.equals(this.activateInstant, instance.activateInstant) && 
      Objects.equals(this.data, instance.data) && 
      Objects.equals(this.firstTimeSetup, instance.firstTimeSetup) && 
      Objects.equals(this.id, instance.id) && 
      Objects.equals(this.reactorHealthChecks, instance.reactorHealthChecks));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.activateInstant, this.data, this.firstTimeSetup, this.fipsEnabled, this.id, Boolean.valueOf(this.setupComplete), this.reactorHealthChecks });
  }
  
  public Instance secure() {
    this.activateInstant = null;
    return this;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
