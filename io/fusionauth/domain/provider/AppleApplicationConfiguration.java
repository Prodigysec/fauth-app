package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.domain.Buildable;
import java.util.Objects;
import java.util.UUID;

public class AppleApplicationConfiguration extends BaseIdentityProviderApplicationConfiguration implements Buildable<AppleApplicationConfiguration> {
  @JSONColumn
  public String bundleId;
  
  @JSONColumn
  public String buttonText;
  
  public UUID keyId;
  
  @JSONColumn
  public String scope;
  
  @JSONColumn
  public String servicesId;
  
  @JSONColumn
  public String teamId;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof AppleApplicationConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    AppleApplicationConfiguration appleApplicationConfiguration = (AppleApplicationConfiguration)paramObject;
    return (Objects.equals(this.bundleId, appleApplicationConfiguration.bundleId) && 
      Objects.equals(this.buttonText, appleApplicationConfiguration.buttonText) && 
      Objects.equals(this.keyId, appleApplicationConfiguration.keyId) && 
      Objects.equals(this.scope, appleApplicationConfiguration.scope) && 
      Objects.equals(this.servicesId, appleApplicationConfiguration.servicesId) && 
      Objects.equals(this.teamId, appleApplicationConfiguration.teamId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.bundleId, this.buttonText, this.keyId, this.scope, this.servicesId, this.teamId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
