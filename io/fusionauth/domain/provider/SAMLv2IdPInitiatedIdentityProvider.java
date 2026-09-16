package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.domain.Buildable;
import java.util.Objects;

public class SAMLv2IdPInitiatedIdentityProvider extends BaseSAMLv2IdentityProvider<SAMLv2IdPInitiatedApplicationConfiguration> implements Buildable<SAMLv2IdPInitiatedIdentityProvider> {
  @JSONColumn
  public String issuer;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof SAMLv2IdPInitiatedIdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    SAMLv2IdPInitiatedIdentityProvider sAMLv2IdPInitiatedIdentityProvider = (SAMLv2IdPInitiatedIdentityProvider)paramObject;
    return Objects.equals(this.issuer, sAMLv2IdPInitiatedIdentityProvider.issuer);
  }
  
  public IdentityProviderType getType() {
    return IdentityProviderType.SAMLv2IdPInitiated;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.issuer });
  }
  
  public void normalize() {
    super.normalize();
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
