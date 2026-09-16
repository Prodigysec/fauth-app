package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.domain.Buildable;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class HYPRIdentityProvider extends BaseIdentityProvider<HYPRApplicationConfiguration> implements Buildable<HYPRIdentityProvider>, PasswordlessIdentityProvider {
  @JSONColumn
  public String relyingPartyApplicationId;
  
  @JSONColumn
  public URI relyingPartyURL;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof HYPRIdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    HYPRIdentityProvider hYPRIdentityProvider = (HYPRIdentityProvider)paramObject;
    return (Objects.equals(this.relyingPartyApplicationId, hYPRIdentityProvider.relyingPartyApplicationId) && 
      Objects.equals(this.relyingPartyURL, hYPRIdentityProvider.relyingPartyURL));
  }
  
  public IdentityProviderType getType() {
    return IdentityProviderType.HYPR;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.relyingPartyApplicationId, this.relyingPartyURL });
  }
  
  public String lookupRelyingPartyApplicationId(UUID paramUUID) {
    return (String)lookup(() -> this.relyingPartyApplicationId, () -> (String)app(paramUUID, ()));
  }
  
  public URI lookupRelyingPartyURL(UUID paramUUID) {
    return (URI)lookup(() -> this.relyingPartyURL, () -> (URI)app(paramUUID, ()));
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
