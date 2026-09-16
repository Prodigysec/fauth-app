package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.SAMLv2DestinationAssertionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SAMLv2DestinationAssertionConfiguration implements Buildable<SAMLv2DestinationAssertionConfiguration> {
  public List<String> alternates = new ArrayList<>();
  
  public SAMLv2DestinationAssertionPolicy policy = SAMLv2DestinationAssertionPolicy.Enabled;
  
  public SAMLv2DestinationAssertionConfiguration() {}
  
  public SAMLv2DestinationAssertionConfiguration(SAMLv2DestinationAssertionConfiguration paramSAMLv2DestinationAssertionConfiguration) {
    this.alternates.addAll(paramSAMLv2DestinationAssertionConfiguration.alternates);
    this.policy = paramSAMLv2DestinationAssertionConfiguration.policy;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    SAMLv2DestinationAssertionConfiguration sAMLv2DestinationAssertionConfiguration = (SAMLv2DestinationAssertionConfiguration)paramObject;
    return (Objects.equals(this.alternates, sAMLv2DestinationAssertionConfiguration.alternates) && this.policy == sAMLv2DestinationAssertionConfiguration.policy);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.alternates, this.policy });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
