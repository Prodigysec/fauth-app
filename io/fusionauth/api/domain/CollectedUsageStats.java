package io.fusionauth.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.usagestats.shared.domain.UsageStats;
import java.util.Objects;

public class CollectedUsageStats extends UsageStats implements Buildable<CollectedUsageStats> {
  @JsonIgnore
  public boolean sent;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    CollectedUsageStats collectedUsageStats = (CollectedUsageStats)paramObject;
    return (this.sent == collectedUsageStats.sent);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), Boolean.valueOf(this.sent) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
