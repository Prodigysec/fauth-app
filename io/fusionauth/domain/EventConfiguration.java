package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.event.EventType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EventConfiguration implements Buildable<EventConfiguration> {
  public Map<EventType, EventConfigurationData> events = new HashMap<>();
  
  public EventConfiguration(EventConfiguration paramEventConfiguration) {
    for (Map.Entry<EventType, EventConfigurationData> entry : paramEventConfiguration.events.entrySet())
      this.events.put((EventType)entry.getKey(), new EventConfigurationData((EventConfigurationData)entry.getValue())); 
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    EventConfiguration eventConfiguration = (EventConfiguration)paramObject;
    return Objects.equals(this.events, eventConfiguration.events);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.events });
  }
  
  public EventConfiguration normalize() {
    this.events.forEach((paramEventType, paramEventConfigurationData) -> paramEventConfigurationData.transactionType = paramEventType.isTransactionalEvent() ? paramEventConfigurationData.transactionType : TransactionType.None);
    this.events.keySet().removeIf(EventType::isInstanceEvent);
    return this;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  @JacksonConstructor
  public EventConfiguration() {}
  
  public static class EventConfigurationData extends Enableable {
    public TransactionType transactionType = TransactionType.None;
    
    @JacksonConstructor
    public EventConfigurationData() {}
    
    public EventConfigurationData(boolean param1Boolean, TransactionType param1TransactionType) {
      this.enabled = param1Boolean;
      this.transactionType = param1TransactionType;
    }
    
    public EventConfigurationData(EventConfigurationData param1EventConfigurationData) {
      this.enabled = param1EventConfigurationData.enabled;
      this.transactionType = param1EventConfigurationData.transactionType;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      EventConfigurationData eventConfigurationData = (EventConfigurationData)param1Object;
      return (super.equals(param1Object) && 
        Objects.equals(this.transactionType, eventConfigurationData.transactionType));
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.transactionType });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
}
