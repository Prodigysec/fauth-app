package io.fusionauth.domain.messenger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.OptBoolean;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.message.MessageType;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@JsonIgnoreProperties(value = {"type"}, allowGetters = true, allowSetters = false)
public abstract class BaseMessengerConfiguration implements JSONColumnable {
  public final Map<String, Object> data = new HashMap<>();
  
  @JSONColumn
  public boolean debug;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  @JSONColumn
  @JsonMerge(OptBoolean.FALSE)
  public Set<MessageType> messageTypes = new LinkedHashSet<>(
      
      Collections.singletonList(MessageType.SMS));
  
  public String name;
  
  @JSONColumn
  @Deprecated
  public String transport = MessengerTransport.SMS;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof BaseMessengerConfiguration))
      return false; 
    BaseMessengerConfiguration baseMessengerConfiguration = (BaseMessengerConfiguration)paramObject;
    return (this.debug == baseMessengerConfiguration.debug && 
      Objects.equals(this.data, baseMessengerConfiguration.data) && 
      Objects.equals(this.id, baseMessengerConfiguration.id) && 
      Objects.equals(this.insertInstant, baseMessengerConfiguration.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, baseMessengerConfiguration.lastUpdateInstant) && 
      Objects.equals(this.messageTypes, baseMessengerConfiguration.messageTypes) && 
      Objects.equals(this.name, baseMessengerConfiguration.name) && 
      Objects.equals(this.transport, baseMessengerConfiguration.transport));
  }
  
  public abstract MessengerType getType();
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.data, Boolean.valueOf(this.debug), this.id, this.insertInstant, this.lastUpdateInstant, this.messageTypes, this.name, this.transport });
  }
  
  public void normalize() {}
  
  public String toString() {
    return ToString.toString(this);
  }
}
