package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.time.ZonedDateTime;
import java.util.Objects;

public class TimeBasedDeletePolicy extends Enableable implements Buildable<TimeBasedDeletePolicy> {
  public ZonedDateTime enabledInstant;
  
  public int numberOfDaysToRetain = 120;
  
  @JacksonConstructor
  public TimeBasedDeletePolicy() {}
  
  public TimeBasedDeletePolicy(TimeBasedDeletePolicy paramTimeBasedDeletePolicy) {
    this.enabledInstant = paramTimeBasedDeletePolicy.enabledInstant;
    this.enabled = paramTimeBasedDeletePolicy.enabled;
    this.numberOfDaysToRetain = paramTimeBasedDeletePolicy.numberOfDaysToRetain;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof TimeBasedDeletePolicy))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    TimeBasedDeletePolicy timeBasedDeletePolicy = (TimeBasedDeletePolicy)paramObject;
    return (this.numberOfDaysToRetain == timeBasedDeletePolicy.numberOfDaysToRetain && Objects.equals(this.enabledInstant, timeBasedDeletePolicy.enabledInstant));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.enabledInstant, Integer.valueOf(this.numberOfDaysToRetain) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
