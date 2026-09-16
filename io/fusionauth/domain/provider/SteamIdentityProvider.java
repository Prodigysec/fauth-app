package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;
import java.util.UUID;

public class SteamIdentityProvider extends BaseIdentityProvider<SteamApplicationConfiguration> implements Buildable<SteamIdentityProvider>, SupportsPostBindings {
  @JSONColumn
  public SteamAPIMode apiMode = SteamAPIMode.Public;
  
  @JSONColumn
  public String buttonText = "Login with Steam";
  
  @JSONColumn
  public String client_id;
  
  @JSONColumn
  public String scope;
  
  @MaskString
  @JSONColumn
  public String webAPIKey;
  
  public SteamIdentityProvider() {
    this.linkingStrategy = IdentityProviderLinkingStrategy.CreatePendingLink;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof SteamIdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    SteamIdentityProvider steamIdentityProvider = (SteamIdentityProvider)paramObject;
    return (Objects.equals(this.apiMode, steamIdentityProvider.apiMode) && 
      Objects.equals(this.buttonText, steamIdentityProvider.buttonText) && 
      Objects.equals(this.client_id, steamIdentityProvider.client_id) && 
      Objects.equals(this.webAPIKey, steamIdentityProvider.webAPIKey) && 
      Objects.equals(this.scope, steamIdentityProvider.scope));
  }
  
  public IdentityProviderType getType() {
    return IdentityProviderType.Steam;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.apiMode, this.buttonText, this.client_id, this.webAPIKey, this.scope });
  }
  
  public SteamAPIMode lookupAPIMode(UUID paramUUID) {
    return (SteamAPIMode)lookup(() -> this.apiMode, () -> (SteamAPIMode)app(paramUUID, ()));
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
  
  public String lookupScope(String paramString) {
    return (String)lookup(() -> this.scope, () -> (String)app(paramString, ()));
  }
  
  public String lookupWebAPIKey(UUID paramUUID) {
    return (String)lookup(() -> this.webAPIKey, () -> (String)app(paramUUID, ()));
  }
  
  public boolean postRequestEnabled() {
    return false;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
