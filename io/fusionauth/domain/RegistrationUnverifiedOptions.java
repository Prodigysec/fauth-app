package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class RegistrationUnverifiedOptions implements Buildable<RegistrationUnverifiedOptions> {
  public UnverifiedBehavior behavior = UnverifiedBehavior.Allow;
  
  @JacksonConstructor
  public RegistrationUnverifiedOptions() {}
  
  public RegistrationUnverifiedOptions(RegistrationUnverifiedOptions paramRegistrationUnverifiedOptions) {
    this.behavior = paramRegistrationUnverifiedOptions.behavior;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    RegistrationUnverifiedOptions registrationUnverifiedOptions = (RegistrationUnverifiedOptions)paramObject;
    return (this.behavior == registrationUnverifiedOptions.behavior);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.behavior });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
