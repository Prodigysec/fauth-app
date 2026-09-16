package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import io.fusionauth.domain.Enableable;
import java.util.Objects;
import java.util.UUID;

public class SAMLv2AssertionDecryptionConfiguration extends Enableable {
  public UUID keyTransportDecryptionKeyId;
  
  public SAMLv2AssertionDecryptionConfiguration() {}
  
  public SAMLv2AssertionDecryptionConfiguration(SAMLv2AssertionDecryptionConfiguration paramSAMLv2AssertionDecryptionConfiguration) {
    this.enabled = paramSAMLv2AssertionDecryptionConfiguration.enabled;
    this.keyTransportDecryptionKeyId = paramSAMLv2AssertionDecryptionConfiguration.keyTransportDecryptionKeyId;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof SAMLv2AssertionDecryptionConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    SAMLv2AssertionDecryptionConfiguration sAMLv2AssertionDecryptionConfiguration = (SAMLv2AssertionDecryptionConfiguration)paramObject;
    return Objects.equals(this.keyTransportDecryptionKeyId, sAMLv2AssertionDecryptionConfiguration.keyTransportDecryptionKeyId);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.keyTransportDecryptionKeyId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
