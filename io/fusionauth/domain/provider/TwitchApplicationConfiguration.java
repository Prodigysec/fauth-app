package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;

public class TwitchApplicationConfiguration extends BaseIdentityProviderApplicationConfiguration implements Buildable<TwitchApplicationConfiguration> {
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
    if (!(paramObject instanceof TwitchApplicationConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    TwitchApplicationConfiguration twitchApplicationConfiguration = (TwitchApplicationConfiguration)paramObject;
    return (Objects.equals(this.buttonText, twitchApplicationConfiguration.buttonText) && 
      Objects.equals(this.client_id, twitchApplicationConfiguration.client_id) && 
      Objects.equals(this.client_secret, twitchApplicationConfiguration.client_secret) && 
      Objects.equals(this.scope, twitchApplicationConfiguration.scope));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.buttonText, this.client_id, this.client_secret, this.scope });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
