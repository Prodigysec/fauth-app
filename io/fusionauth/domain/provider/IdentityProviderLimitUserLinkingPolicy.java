package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Enableable;
import java.util.Objects;

public class IdentityProviderLimitUserLinkingPolicy extends Enableable implements Buildable<IdentityProviderLimitUserLinkingPolicy> {
  public int maximumLinks = 42;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    IdentityProviderLimitUserLinkingPolicy identityProviderLimitUserLinkingPolicy = (IdentityProviderLimitUserLinkingPolicy)paramObject;
    return (this.maximumLinks == identityProviderLimitUserLinkingPolicy.maximumLinks);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), Integer.valueOf(this.maximumLinks) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
