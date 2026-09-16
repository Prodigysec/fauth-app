package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;
import java.util.UUID;

public class XboxIdentityProvider extends BaseIdentityProvider<XboxApplicationConfiguration> implements Buildable<XboxIdentityProvider>, SupportsPostBindings {
  @JSONColumn
  public String buttonText = "Login with Xbox";
  
  @JSONColumn
  public String client_id;
  
  @MaskString
  @JSONColumn
  public String client_secret;
  
  @JSONColumn
  public String scope;
  
  public XboxIdentityProvider() {
    this.linkingStrategy = IdentityProviderLinkingStrategy.CreatePendingLink;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof XboxIdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    XboxIdentityProvider xboxIdentityProvider = (XboxIdentityProvider)paramObject;
    return (Objects.equals(this.buttonText, xboxIdentityProvider.buttonText) && 
      Objects.equals(this.client_id, xboxIdentityProvider.client_id) && 
      Objects.equals(this.client_secret, xboxIdentityProvider.client_secret) && 
      Objects.equals(this.scope, xboxIdentityProvider.scope));
  }
  
  public IdentityProviderType getType() {
    return IdentityProviderType.Xbox;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.buttonText, this.client_id, this.client_secret, this.scope });
  }
  
  public String lookupButtonText(String paramString) {
    return (String)lookup(() -> this.buttonText, () -> (String)app(paramString, ()));
  }
  
  public String lookupClientId(UUID paramUUID) {
    return (String)lookup(() -> this.client_id, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupClientId(String paramString) {
    return (String)lookup(() -> this.client_id, () -> (String)app(paramString, ()));
  }
  
  public String lookupClientSecret(UUID paramUUID) {
    return (String)lookup(() -> this.client_secret, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupScope(String paramString) {
    return (String)lookup(() -> this.scope, () -> (String)app(paramString, ()));
  }
  
  public boolean postRequestEnabled() {
    return false;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
