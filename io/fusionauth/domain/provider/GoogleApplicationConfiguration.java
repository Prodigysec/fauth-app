package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;

public class GoogleApplicationConfiguration extends BaseIdentityProviderApplicationConfiguration implements Buildable<GoogleApplicationConfiguration> {
  @JSONColumn
  public String buttonText;
  
  @JSONColumn
  public String client_id;
  
  @MaskString
  @JSONColumn
  public String client_secret;
  
  @JSONColumn
  public IdentityProviderLoginMethod loginMethod;
  
  @JSONColumn
  public GoogleIdentityProviderProperties properties = new GoogleIdentityProviderProperties();
  
  @JSONColumn
  public String scope;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof GoogleApplicationConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    GoogleApplicationConfiguration googleApplicationConfiguration = (GoogleApplicationConfiguration)paramObject;
    return (Objects.equals(this.buttonText, googleApplicationConfiguration.buttonText) && 
      Objects.equals(this.client_id, googleApplicationConfiguration.client_id) && 
      Objects.equals(this.client_secret, googleApplicationConfiguration.client_secret) && this.loginMethod == googleApplicationConfiguration.loginMethod && 
      
      Objects.equals(this.properties, googleApplicationConfiguration.properties) && 
      Objects.equals(this.scope, googleApplicationConfiguration.scope));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.buttonText, this.client_id, this.client_secret, this.loginMethod, this.properties, this.scope });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
