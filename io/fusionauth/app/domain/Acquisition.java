package io.fusionauth.app.domain;

import io.fusionauth.domain.Buildable;
import java.util.Objects;

public class Acquisition implements Buildable<Acquisition> {
  public String channel;
  
  public String other;
  
  public boolean equals(Object paramObject) {
    Acquisition acquisition;
    if (this == paramObject)
      return true; 
    if (paramObject instanceof Acquisition) {
      acquisition = (Acquisition)paramObject;
    } else {
      return false;
    } 
    return (Objects.equals(this.channel, acquisition.channel) && Objects.equals(this.other, acquisition.other));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.channel, this.other });
  }
}
