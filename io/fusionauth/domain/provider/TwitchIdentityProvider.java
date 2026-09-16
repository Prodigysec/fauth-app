package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;
import java.util.UUID;

public class TwitchIdentityProvider extends BaseIdentityProvider<TwitchApplicationConfiguration> implements Buildable<TwitchIdentityProvider>, SupportsPostBindings {
  @JSONColumn
  public String buttonText = "Login with Twitch";
  
  @JSONColumn
  public String client_id;
  
  @MaskString
  @JSONColumn
  public String client_secret;
  
  @JSONColumn
  public String scope = "openid user:read:email";
  
  public TwitchIdentityProvider() {
    this.linkingStrategy = IdentityProviderLinkingStrategy.CreatePendingLink;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof TwitchIdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    TwitchIdentityProvider twitchIdentityProvider = (TwitchIdentityProvider)paramObject;
    return (Objects.equals(this.buttonText, twitchIdentityProvider.buttonText) && 
      Objects.equals(this.client_id, twitchIdentityProvider.client_id) && 
      Objects.equals(this.client_secret, twitchIdentityProvider.client_secret) && 
      Objects.equals(this.scope, twitchIdentityProvider.scope));
  }
  
  public IdentityProviderType getType() {
    return IdentityProviderType.Twitch;
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
