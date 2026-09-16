package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;
import java.util.UUID;

public class NintendoIdentityProvider extends BaseIdentityProvider<NintendoApplicationConfiguration> implements Buildable<NintendoIdentityProvider>, SupportsPostBindings {
  @JSONColumn
  public String buttonText = "Login with Nintendo";
  
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
  
  public NintendoIdentityProvider() {
    this.linkingStrategy = IdentityProviderLinkingStrategy.CreatePendingLink;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof NintendoIdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    NintendoIdentityProvider nintendoIdentityProvider = (NintendoIdentityProvider)paramObject;
    return (Objects.equals(this.buttonText, nintendoIdentityProvider.buttonText) && 
      Objects.equals(this.client_id, nintendoIdentityProvider.client_id) && 
      Objects.equals(this.client_secret, nintendoIdentityProvider.client_secret) && 
      Objects.equals(this.emailClaim, nintendoIdentityProvider.emailClaim) && 
      Objects.equals(this.scope, nintendoIdentityProvider.scope) && 
      Objects.equals(this.uniqueIdClaim, nintendoIdentityProvider.uniqueIdClaim) && 
      Objects.equals(this.usernameClaim, nintendoIdentityProvider.usernameClaim));
  }
  
  public IdentityProviderType getType() {
    return IdentityProviderType.Nintendo;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.buttonText, this.client_id, this.client_secret, this.emailClaim, this.scope, this.uniqueIdClaim, this.usernameClaim });
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
  
  public String lookupEmailClaim(UUID paramUUID) {
    return (String)lookup(() -> this.emailClaim, () -> (String)app(this.emailClaim, ()));
  }
  
  public String lookupScope(String paramString) {
    return (String)lookup(() -> this.scope, () -> (String)app(paramString, ()));
  }
  
  public String lookupUniqueIdClaim(UUID paramUUID) {
    return (String)lookup(() -> this.uniqueIdClaim, () -> (String)app(this.uniqueIdClaim, ()));
  }
  
  public String lookupUsernameClaim(UUID paramUUID) {
    return (String)lookup(() -> this.usernameClaim, () -> (String)app(this.usernameClaim, ()));
  }
  
  public boolean postRequestEnabled() {
    return false;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
