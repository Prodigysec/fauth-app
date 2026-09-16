package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class MaximumPasswordAge extends Enableable {
  public int days = 180;
  
  @JacksonConstructor
  public MaximumPasswordAge() {}
  
  public MaximumPasswordAge(MaximumPasswordAge paramMaximumPasswordAge) {
    this.days = paramMaximumPasswordAge.days;
    this.enabled = paramMaximumPasswordAge.enabled;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    MaximumPasswordAge maximumPasswordAge = (MaximumPasswordAge)paramObject;
    return (super.equals(paramObject) && 
      Objects.equals(Integer.valueOf(this.days), Integer.valueOf(maximumPasswordAge.days)));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), Integer.valueOf(this.days) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
