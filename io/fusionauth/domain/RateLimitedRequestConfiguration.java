package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class RateLimitedRequestConfiguration extends Enableable implements Buildable<RateLimitedRequestConfiguration> {
  public int limit;
  
  public int timePeriodInSeconds;
  
  @JacksonConstructor
  public RateLimitedRequestConfiguration() {}
  
  public RateLimitedRequestConfiguration(int paramInt1, int paramInt2) {
    this.limit = paramInt1;
    this.timePeriodInSeconds = paramInt2;
  }
  
  public RateLimitedRequestConfiguration(RateLimitedRequestConfiguration paramRateLimitedRequestConfiguration) {
    this.enabled = paramRateLimitedRequestConfiguration.enabled;
    this.limit = paramRateLimitedRequestConfiguration.limit;
    this.timePeriodInSeconds = paramRateLimitedRequestConfiguration.timePeriodInSeconds;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    RateLimitedRequestConfiguration rateLimitedRequestConfiguration = (RateLimitedRequestConfiguration)paramObject;
    return (this.limit == rateLimitedRequestConfiguration.limit && this.timePeriodInSeconds == rateLimitedRequestConfiguration.timePeriodInSeconds);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), Integer.valueOf(this.limit), Integer.valueOf(this.timePeriodInSeconds) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
