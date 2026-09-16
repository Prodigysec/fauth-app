package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;
import java.util.UUID;

public class LinkedInIdentityProvider extends BaseIdentityProvider<LinkedInApplicationConfiguration> implements Buildable<LinkedInIdentityProvider>, SupportsPostBindings {
  @JSONColumn
  public String buttonText = "Sign in with LinkedIn";
  
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
    if (!(paramObject instanceof LinkedInIdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    LinkedInIdentityProvider linkedInIdentityProvider = (LinkedInIdentityProvider)paramObject;
    return (Objects.equals(this.buttonText, linkedInIdentityProvider.buttonText) && 
      Objects.equals(this.client_id, linkedInIdentityProvider.client_id) && 
      Objects.equals(this.client_secret, linkedInIdentityProvider.client_secret) && 
      Objects.equals(this.scope, linkedInIdentityProvider.scope));
  }
  
  public IdentityProviderType getType() {
    return IdentityProviderType.LinkedIn;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.buttonText, this.client_id, this.client_secret, this.scope });
  }
  
  public String lookupButtonText(String paramString) {
    return (String)lookup(() -> this.buttonText, () -> (String)app(paramString, ()));
  }
  
  public String lookupButtonText(UUID paramUUID) {
    return (String)lookup(() -> this.buttonText, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupClientId(String paramString) {
    return (String)lookup(() -> this.client_id, () -> (String)app(paramString, ()));
  }
  
  public String lookupClientId(UUID paramUUID) {
    return (String)lookup(() -> this.client_id, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupClientSecret(UUID paramUUID) {
    return (String)lookup(() -> this.client_secret, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupScope(String paramString) {
    return (String)lookup(() -> this.scope, () -> (String)app(paramString, ()));
  }
  
  public String lookupScope(UUID paramUUID) {
    return (String)lookup(() -> this.scope, () -> (String)app(paramUUID, ()));
  }
  
  public void normalize() {
    super.normalize();
  }
  
  public boolean postRequestEnabled() {
    return false;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
