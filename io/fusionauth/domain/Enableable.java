package io.fusionauth.domain;

import java.util.Objects;

public class Enableable {
  public boolean enabled;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof Enableable))
      return false; 
    Enableable enableable = (Enableable)paramObject;
    return (this.enabled == enableable.enabled);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Boolean.valueOf(this.enabled) });
  }
}
