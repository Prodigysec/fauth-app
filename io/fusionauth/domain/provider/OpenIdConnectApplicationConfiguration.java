package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.domain.Buildable;
import java.net.URI;
import java.util.Objects;

public class OpenIdConnectApplicationConfiguration extends BaseIdentityProviderApplicationConfiguration implements Buildable<OpenIdConnectApplicationConfiguration> {
  @JSONColumn
  public URI buttonImageURL;
  
  @JSONColumn
  public String buttonText;
  
  @JSONColumn
  public IdentityProviderOauth2Configuration oauth2 = new IdentityProviderOauth2Configuration();
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof OpenIdConnectApplicationConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    OpenIdConnectApplicationConfiguration openIdConnectApplicationConfiguration = (OpenIdConnectApplicationConfiguration)paramObject;
    return (Objects.equals(this.buttonImageURL, openIdConnectApplicationConfiguration.buttonImageURL) && 
      Objects.equals(this.buttonText, openIdConnectApplicationConfiguration.buttonText) && 
      Objects.equals(this.oauth2, openIdConnectApplicationConfiguration.oauth2));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.buttonImageURL, this.buttonText, this.oauth2 });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
