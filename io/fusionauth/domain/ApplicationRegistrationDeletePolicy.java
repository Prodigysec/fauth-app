package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class ApplicationRegistrationDeletePolicy implements Buildable<ApplicationRegistrationDeletePolicy> {
  public TimeBasedDeletePolicy unverified = new TimeBasedDeletePolicy();
  
  @JacksonConstructor
  public ApplicationRegistrationDeletePolicy() {}
  
  public ApplicationRegistrationDeletePolicy(ApplicationRegistrationDeletePolicy paramApplicationRegistrationDeletePolicy) {
    this.unverified = new TimeBasedDeletePolicy(paramApplicationRegistrationDeletePolicy.unverified);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof ApplicationRegistrationDeletePolicy))
      return false; 
    ApplicationRegistrationDeletePolicy applicationRegistrationDeletePolicy = (ApplicationRegistrationDeletePolicy)paramObject;
    return Objects.equals(this.unverified, applicationRegistrationDeletePolicy.unverified);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.unverified });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
