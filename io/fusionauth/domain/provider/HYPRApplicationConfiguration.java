package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.domain.Buildable;
import java.net.URI;
import java.util.Objects;

public class HYPRApplicationConfiguration extends BaseIdentityProviderApplicationConfiguration implements Buildable<HYPRApplicationConfiguration> {
  @JSONColumn
  public String relyingPartyApplicationId;
  
  @JSONColumn
  public URI relyingPartyURL;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof HYPRApplicationConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    HYPRApplicationConfiguration hYPRApplicationConfiguration = (HYPRApplicationConfiguration)paramObject;
    return (Objects.equals(this.relyingPartyApplicationId, hYPRApplicationConfiguration.relyingPartyApplicationId) && 
      Objects.equals(this.relyingPartyURL, hYPRApplicationConfiguration.relyingPartyURL));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.relyingPartyApplicationId, this.relyingPartyURL });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
