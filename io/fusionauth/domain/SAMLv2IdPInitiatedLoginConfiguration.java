package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class SAMLv2IdPInitiatedLoginConfiguration extends Enableable implements Buildable<SAMLv2IdPInitiatedLoginConfiguration> {
  public String nameIdFormat = "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent";
  
  @JacksonConstructor
  public SAMLv2IdPInitiatedLoginConfiguration() {}
  
  public SAMLv2IdPInitiatedLoginConfiguration(SAMLv2IdPInitiatedLoginConfiguration paramSAMLv2IdPInitiatedLoginConfiguration) {
    this.enabled = paramSAMLv2IdPInitiatedLoginConfiguration.enabled;
    this.nameIdFormat = paramSAMLv2IdPInitiatedLoginConfiguration.nameIdFormat;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    SAMLv2IdPInitiatedLoginConfiguration sAMLv2IdPInitiatedLoginConfiguration = (SAMLv2IdPInitiatedLoginConfiguration)paramObject;
    return Objects.equals(this.nameIdFormat, sAMLv2IdPInitiatedLoginConfiguration.nameIdFormat);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.nameIdFormat });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
