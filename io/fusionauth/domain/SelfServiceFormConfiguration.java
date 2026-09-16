package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class SelfServiceFormConfiguration implements Buildable<SelfServiceFormConfiguration> {
  public boolean requireCurrentPasswordOnPasswordChange;
  
  @JacksonConstructor
  public SelfServiceFormConfiguration() {}
  
  public SelfServiceFormConfiguration(SelfServiceFormConfiguration paramSelfServiceFormConfiguration) {
    this.requireCurrentPasswordOnPasswordChange = paramSelfServiceFormConfiguration.requireCurrentPasswordOnPasswordChange;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    SelfServiceFormConfiguration selfServiceFormConfiguration = (SelfServiceFormConfiguration)paramObject;
    return (this.requireCurrentPasswordOnPasswordChange == selfServiceFormConfiguration.requireCurrentPasswordOnPasswordChange);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Boolean.valueOf(this.requireCurrentPasswordOnPasswordChange) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
