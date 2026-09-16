package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class RefreshTokenOneTimeUseConfiguration implements Buildable<RefreshTokenOneTimeUseConfiguration> {
  public int gracePeriodInSeconds;
  
  @JacksonConstructor
  public RefreshTokenOneTimeUseConfiguration() {}
  
  public RefreshTokenOneTimeUseConfiguration(RefreshTokenOneTimeUseConfiguration paramRefreshTokenOneTimeUseConfiguration) {
    this.gracePeriodInSeconds = paramRefreshTokenOneTimeUseConfiguration.gracePeriodInSeconds;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    RefreshTokenOneTimeUseConfiguration refreshTokenOneTimeUseConfiguration = (RefreshTokenOneTimeUseConfiguration)paramObject;
    return (this.gracePeriodInSeconds == refreshTokenOneTimeUseConfiguration.gracePeriodInSeconds);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(this.gracePeriodInSeconds) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
