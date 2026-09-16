package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;

public class SAMLv2AssertionConfiguration implements Buildable<SAMLv2AssertionConfiguration> {
  public SAMLv2DestinationAssertionConfiguration destination = new SAMLv2DestinationAssertionConfiguration();
  
  public SAMLv2AssertionConfiguration() {}
  
  public SAMLv2AssertionConfiguration(SAMLv2AssertionConfiguration paramSAMLv2AssertionConfiguration) {
    this.destination = new SAMLv2DestinationAssertionConfiguration(paramSAMLv2AssertionConfiguration.destination);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    SAMLv2AssertionConfiguration sAMLv2AssertionConfiguration = (SAMLv2AssertionConfiguration)paramObject;
    return Objects.equals(this.destination, sAMLv2AssertionConfiguration.destination);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.destination });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
