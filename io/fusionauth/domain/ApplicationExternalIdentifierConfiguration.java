package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class ApplicationExternalIdentifierConfiguration implements Buildable<ApplicationExternalIdentifierConfiguration> {
  public Integer twoFactorTrustIdTimeToLiveInSeconds;
  
  @JacksonConstructor
  public ApplicationExternalIdentifierConfiguration() {}
  
  public ApplicationExternalIdentifierConfiguration(ApplicationExternalIdentifierConfiguration paramApplicationExternalIdentifierConfiguration) {
    this.twoFactorTrustIdTimeToLiveInSeconds = paramApplicationExternalIdentifierConfiguration.twoFactorTrustIdTimeToLiveInSeconds;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    ApplicationExternalIdentifierConfiguration applicationExternalIdentifierConfiguration = (ApplicationExternalIdentifierConfiguration)paramObject;
    return Objects.equals(this.twoFactorTrustIdTimeToLiveInSeconds, applicationExternalIdentifierConfiguration.twoFactorTrustIdTimeToLiveInSeconds);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.twoFactorTrustIdTimeToLiveInSeconds });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
