package io.fusionauth.domain.reactor;

import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;

public class MFATenantMetric implements Buildable<MFATenantMetric> {
  public long challengeCount;
  
  public long failedAttemptCount;
  
  public long successCount;
  
  public MFATenantMetric() {}
  
  public MFATenantMetric(MFATenantMetric paramMFATenantMetric) {
    this.challengeCount = paramMFATenantMetric.challengeCount;
    this.failedAttemptCount = paramMFATenantMetric.failedAttemptCount;
    this.successCount = paramMFATenantMetric.successCount;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof MFATenantMetric))
      return false; 
    MFATenantMetric mFATenantMetric = (MFATenantMetric)paramObject;
    return (this.challengeCount == mFATenantMetric.challengeCount && this.failedAttemptCount == mFATenantMetric.failedAttemptCount && this.successCount == mFATenantMetric.successCount);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Long.valueOf(this.challengeCount), Long.valueOf(this.failedAttemptCount), Long.valueOf(this.successCount) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
