package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;

public class FacebookApplicationConfiguration extends BaseIdentityProviderApplicationConfiguration implements Buildable<FacebookApplicationConfiguration> {
  @JSONColumn
  public String appId;
  
  @JSONColumn
  public String buttonText;
  
  @MaskString
  @JSONColumn
  public String client_secret;
  
  @JSONColumn
  public String fields;
  
  @JSONColumn
  public IdentityProviderLoginMethod loginMethod;
  
  @JSONColumn
  public String permissions;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof FacebookApplicationConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    FacebookApplicationConfiguration facebookApplicationConfiguration = (FacebookApplicationConfiguration)paramObject;
    return (Objects.equals(this.appId, facebookApplicationConfiguration.appId) && 
      Objects.equals(this.buttonText, facebookApplicationConfiguration.buttonText) && 
      Objects.equals(this.client_secret, facebookApplicationConfiguration.client_secret) && 
      Objects.equals(this.fields, facebookApplicationConfiguration.fields) && this.loginMethod == facebookApplicationConfiguration.loginMethod && 
      
      Objects.equals(this.permissions, facebookApplicationConfiguration.permissions));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.appId, this.buttonText, this.client_secret, this.fields, this.loginMethod, this.permissions });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
