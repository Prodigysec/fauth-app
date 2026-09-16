package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class RefreshTokenSlidingWindowConfiguration implements Buildable<RefreshTokenSlidingWindowConfiguration> {
  public int maximumTimeToLiveInMinutes = 43200;
  
  @JacksonConstructor
  public RefreshTokenSlidingWindowConfiguration() {}
  
  public RefreshTokenSlidingWindowConfiguration(RefreshTokenSlidingWindowConfiguration paramRefreshTokenSlidingWindowConfiguration) {
    this.maximumTimeToLiveInMinutes = paramRefreshTokenSlidingWindowConfiguration.maximumTimeToLiveInMinutes;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    RefreshTokenSlidingWindowConfiguration refreshTokenSlidingWindowConfiguration = (RefreshTokenSlidingWindowConfiguration)paramObject;
    return (this.maximumTimeToLiveInMinutes == refreshTokenSlidingWindowConfiguration.maximumTimeToLiveInMinutes);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(this.maximumTimeToLiveInMinutes) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
