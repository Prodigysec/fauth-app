package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import java.util.Objects;

public class Requirable extends Enableable {
  public boolean required;
  
  @JacksonConstructor
  public Requirable() {}
  
  public Requirable(boolean paramBoolean1, boolean paramBoolean2) {
    this.enabled = paramBoolean1;
    this.required = paramBoolean2;
  }
  
  public Requirable(Requirable paramRequirable) {
    this.enabled = paramRequirable.enabled;
    this.required = paramRequirable.required;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof Requirable))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    Requirable requirable = (Requirable)paramObject;
    return (this.required == requirable.required);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), Boolean.valueOf(this.required) });
  }
}
