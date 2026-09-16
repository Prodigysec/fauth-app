package io.fusionauth.domain.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumnable;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@JsonIgnoreProperties(value = {"type"}, allowGetters = true, allowSetters = false)
public abstract class MessageTemplate implements JSONColumnable {
  public Map<String, Object> data = new LinkedHashMap<>();
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  public String name;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    MessageTemplate messageTemplate = (MessageTemplate)paramObject;
    return (this.id.equals(messageTemplate.id) && this.insertInstant
      .equals(messageTemplate.insertInstant) && this.lastUpdateInstant
      .equals(messageTemplate.lastUpdateInstant) && this.name
      .equals(messageTemplate.name) && this.data
      .equals(messageTemplate.data));
  }
  
  public abstract MessageType getType();
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.id, this.insertInstant, this.lastUpdateInstant, this.name, this.data });
  }
  
  public abstract void normalize();
  
  public String toString() {
    return ToString.toString(this);
  }
}
