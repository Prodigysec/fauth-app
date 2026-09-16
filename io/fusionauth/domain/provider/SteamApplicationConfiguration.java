package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;

public class SteamApplicationConfiguration extends BaseIdentityProviderApplicationConfiguration implements Buildable<SteamApplicationConfiguration> {
  @JSONColumn
  public SteamAPIMode apiMode;
  
  @JSONColumn
  public String buttonText;
  
  @JSONColumn
  public String client_id;
  
  @JSONColumn
  public String scope;
  
  @MaskString
  @JSONColumn
  public String webAPIKey;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof SteamApplicationConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    SteamApplicationConfiguration steamApplicationConfiguration = (SteamApplicationConfiguration)paramObject;
    return (Objects.equals(this.apiMode, steamApplicationConfiguration.apiMode) && 
      Objects.equals(this.buttonText, steamApplicationConfiguration.buttonText) && 
      Objects.equals(this.client_id, steamApplicationConfiguration.client_id) && 
      Objects.equals(this.webAPIKey, steamApplicationConfiguration.webAPIKey) && 
      Objects.equals(this.scope, steamApplicationConfiguration.scope));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.apiMode, this.buttonText, this.client_id, this.webAPIKey, this.scope });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
