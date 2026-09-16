package io.fusionauth.api.domain;

import com.inversoft.json.ToString;
import java.util.Objects;
import java.util.UUID;

public class IntervalCount {
  public UUID applicationId;
  
  public int count;
  
  public int decrementedCount;
  
  public long id;
  
  public int period;
  
  public IntervalCount() {}
  
  public IntervalCount(UUID paramUUID, int paramInt1, int paramInt2, int paramInt3) {
    this.applicationId = paramUUID;
    this.count = paramInt1;
    this.decrementedCount = paramInt2;
    this.period = paramInt3;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    IntervalCount intervalCount = (IntervalCount)paramObject;
    return (this.count == intervalCount.count && this.decrementedCount == intervalCount.decrementedCount && this.id == intervalCount.id && this.period == intervalCount.period && Objects.equals(this.applicationId, intervalCount.applicationId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.applicationId, Integer.valueOf(this.count), Integer.valueOf(this.decrementedCount), Long.valueOf(this.id), Integer.valueOf(this.period) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
