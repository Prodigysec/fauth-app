package io.fusionauth.domain.provider;

import com.inversoft.mybatis.JSONColumn;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseSAMLv2IdentityProvider<D extends BaseIdentityProviderApplicationConfiguration> extends BaseIdentityProvider<D> {
  @JSONColumn
  public SAMLv2AssertionDecryptionConfiguration assertionDecryptionConfiguration = new SAMLv2AssertionDecryptionConfiguration();
  
  @JSONColumn
  public String emailClaim;
  
  @Deprecated
  public UUID keyId;
  
  public List<UUID> verificationKeyIds = new ArrayList<>();
  
  @JSONColumn
  public String uniqueIdClaim;
  
  @JSONColumn
  public boolean useNameIdForEmail;
  
  @JSONColumn
  public String usernameClaim;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof BaseSAMLv2IdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    BaseSAMLv2IdentityProvider baseSAMLv2IdentityProvider = (BaseSAMLv2IdentityProvider)paramObject;
    return (Objects.equals(this.assertionDecryptionConfiguration, baseSAMLv2IdentityProvider.assertionDecryptionConfiguration) && 
      Objects.equals(this.emailClaim, baseSAMLv2IdentityProvider.emailClaim) && 
      Objects.equals(this.keyId, baseSAMLv2IdentityProvider.keyId) && 
      Objects.equals(this.uniqueIdClaim, baseSAMLv2IdentityProvider.uniqueIdClaim) && this.useNameIdForEmail == baseSAMLv2IdentityProvider.useNameIdForEmail && 
      
      Objects.equals(this.usernameClaim, baseSAMLv2IdentityProvider.usernameClaim) && 
      Objects.equals(this.verificationKeyIds, baseSAMLv2IdentityProvider.verificationKeyIds));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.assertionDecryptionConfiguration, this.emailClaim, this.keyId, this.uniqueIdClaim, Boolean.valueOf(this.useNameIdForEmail), this.usernameClaim, this.verificationKeyIds });
  }
}
