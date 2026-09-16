package io.fusionauth.domain.provider;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Enableable;
import java.util.Objects;

public class SAMLv2IdpInitiatedConfiguration extends Enableable implements Buildable<SAMLv2IdpInitiatedConfiguration> {
  public String issuer;
  
  @JacksonConstructor
  public SAMLv2IdpInitiatedConfiguration() {}
  
  public SAMLv2IdpInitiatedConfiguration(boolean paramBoolean) {
    this.enabled = paramBoolean;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    SAMLv2IdpInitiatedConfiguration sAMLv2IdpInitiatedConfiguration = (SAMLv2IdpInitiatedConfiguration)paramObject;
    return Objects.equals(this.issuer, sAMLv2IdpInitiatedConfiguration.issuer);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.issuer });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
