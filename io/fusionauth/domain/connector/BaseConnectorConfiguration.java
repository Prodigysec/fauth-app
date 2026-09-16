package io.fusionauth.domain.connector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@JsonIgnoreProperties(value = {"type"}, allowGetters = true, allowSetters = false)
public abstract class BaseConnectorConfiguration implements JSONColumnable {
  public static final UUID FUSIONAUTH_CONNECTOR_ID = UUID.fromString("e3306678-a53a-4964-9040-1c96f36dda72");
  
  public Map<String, Object> data = new LinkedHashMap<>();
  
  @JSONColumn
  public boolean debug;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  public String name;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof BaseConnectorConfiguration))
      return false; 
    BaseConnectorConfiguration baseConnectorConfiguration = (BaseConnectorConfiguration)paramObject;
    return (this.debug == baseConnectorConfiguration.debug && 
      Objects.equals(this.data, baseConnectorConfiguration.data) && 
      Objects.equals(this.id, baseConnectorConfiguration.id) && 
      Objects.equals(this.insertInstant, baseConnectorConfiguration.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, baseConnectorConfiguration.lastUpdateInstant) && 
      Objects.equals(this.name, baseConnectorConfiguration.name));
  }
  
  public abstract ConnectorType getType();
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.data, Boolean.valueOf(this.debug), this.id, this.insertInstant, this.lastUpdateInstant, this.name });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
