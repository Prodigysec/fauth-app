package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;

public class NintendoApplicationConfiguration extends BaseIdentityProviderApplicationConfiguration implements Buildable<NintendoApplicationConfiguration> {
  @JSONColumn
  public String buttonText;
  
  @JSONColumn
  public String client_id;
  
  @MaskString
  @JSONColumn
  public String client_secret;
  
  @JSONColumn
  public String emailClaim = "email";
  
  @JSONColumn
  public String scope;
  
  @JSONColumn
  public String uniqueIdClaim = "id";
  
  @JSONColumn
  public String usernameClaim = "preferred_username";
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof NintendoApplicationConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    NintendoApplicationConfiguration nintendoApplicationConfiguration = (NintendoApplicationConfiguration)paramObject;
    return (Objects.equals(this.buttonText, nintendoApplicationConfiguration.buttonText) && 
      Objects.equals(this.client_id, nintendoApplicationConfiguration.client_id) && 
      Objects.equals(this.client_secret, nintendoApplicationConfiguration.client_secret) && 
      Objects.equals(this.scope, nintendoApplicationConfiguration.scope) && 
      Objects.equals(this.uniqueIdClaim, nintendoApplicationConfiguration.uniqueIdClaim) && 
      Objects.equals(this.emailClaim, nintendoApplicationConfiguration.emailClaim) && 
      Objects.equals(this.usernameClaim, nintendoApplicationConfiguration.usernameClaim));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.buttonText, this.client_id, this.client_secret, this.scope, this.uniqueIdClaim, this.emailClaim, this.usernameClaim });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
