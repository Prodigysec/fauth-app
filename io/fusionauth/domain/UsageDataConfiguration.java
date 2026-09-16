package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import java.util.Objects;

public class UsageDataConfiguration extends Enableable {
  public int numberOfDaysToRetain = 366;
  
  @JacksonConstructor
  public UsageDataConfiguration() {}
  
  public UsageDataConfiguration(UsageDataConfiguration paramUsageDataConfiguration) {
    this.enabled = paramUsageDataConfiguration.enabled;
    this.numberOfDaysToRetain = paramUsageDataConfiguration.numberOfDaysToRetain;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    UsageDataConfiguration usageDataConfiguration = (UsageDataConfiguration)paramObject;
    return (this.numberOfDaysToRetain == usageDataConfiguration.numberOfDaysToRetain);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), Integer.valueOf(this.numberOfDaysToRetain) });
  }
}
