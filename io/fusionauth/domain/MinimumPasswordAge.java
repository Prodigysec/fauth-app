package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class MinimumPasswordAge extends Enableable {
  public int seconds = 30;
  
  @JacksonConstructor
  public MinimumPasswordAge() {}
  
  public MinimumPasswordAge(MinimumPasswordAge paramMinimumPasswordAge) {
    this.enabled = paramMinimumPasswordAge.enabled;
    this.seconds = paramMinimumPasswordAge.seconds;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    MinimumPasswordAge minimumPasswordAge = (MinimumPasswordAge)paramObject;
    return (super.equals(paramObject) && 
      Objects.equals(Integer.valueOf(this.seconds), Integer.valueOf(minimumPasswordAge.seconds)));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), Integer.valueOf(this.seconds) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
