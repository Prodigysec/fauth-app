package io.fusionauth.domain.reactor;

import com.inversoft.json.ToString;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ReactorMetrics {
  public Map<UUID, BreachedPasswordTenantMetric> breachedPasswordMetrics = new HashMap<>();
  
  public Map<UUID, MFATenantMetric> mfaMetrics = new HashMap<>();
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    ReactorMetrics reactorMetrics = (ReactorMetrics)paramObject;
    return (Objects.equals(this.breachedPasswordMetrics, reactorMetrics.breachedPasswordMetrics) && 
      Objects.equals(this.mfaMetrics, reactorMetrics.mfaMetrics));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.breachedPasswordMetrics, this.mfaMetrics });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
