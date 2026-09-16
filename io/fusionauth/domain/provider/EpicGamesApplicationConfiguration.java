package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;

public class EpicGamesApplicationConfiguration extends BaseIdentityProviderApplicationConfiguration implements Buildable<EpicGamesApplicationConfiguration> {
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
    if (!(paramObject instanceof EpicGamesApplicationConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    EpicGamesApplicationConfiguration epicGamesApplicationConfiguration = (EpicGamesApplicationConfiguration)paramObject;
    return (Objects.equals(this.buttonText, epicGamesApplicationConfiguration.buttonText) && 
      Objects.equals(this.client_id, epicGamesApplicationConfiguration.client_id) && 
      Objects.equals(this.client_secret, epicGamesApplicationConfiguration.client_secret) && 
      Objects.equals(this.scope, epicGamesApplicationConfiguration.scope));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.buttonText, this.client_id, this.client_secret, this.scope });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
