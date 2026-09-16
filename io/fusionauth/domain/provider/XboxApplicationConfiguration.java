package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;

public class XboxApplicationConfiguration extends BaseIdentityProviderApplicationConfiguration implements Buildable<XboxApplicationConfiguration> {
  @JSONColumn
  public String buttonText;
  
  @JSONColumn
  public String client_id;
  
  @MaskString
  @JSONColumn
  public String client_secret;
  
  @JSONColumn
  public String scope;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof XboxApplicationConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    XboxApplicationConfiguration xboxApplicationConfiguration = (XboxApplicationConfiguration)paramObject;
    return (Objects.equals(this.buttonText, xboxApplicationConfiguration.buttonText) && 
      Objects.equals(this.client_id, xboxApplicationConfiguration.client_id) && 
      Objects.equals(this.client_secret, xboxApplicationConfiguration.client_secret) && 
      Objects.equals(this.scope, xboxApplicationConfiguration.scope));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.buttonText, this.client_id, this.client_secret, this.scope });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
