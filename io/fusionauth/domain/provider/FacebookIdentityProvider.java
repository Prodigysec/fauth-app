package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;
import java.util.UUID;

public class FacebookIdentityProvider extends BaseIdentityProvider<FacebookApplicationConfiguration> implements Buildable<FacebookIdentityProvider>, SupportsPostBindings {
  @JSONColumn
  public String appId;
  
  @JSONColumn
  public String buttonText = "Login with Facebook";
  
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
    if (!(paramObject instanceof FacebookIdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    FacebookIdentityProvider facebookIdentityProvider = (FacebookIdentityProvider)paramObject;
    return (Objects.equals(this.appId, facebookIdentityProvider.appId) && 
      Objects.equals(this.buttonText, facebookIdentityProvider.buttonText) && 
      Objects.equals(this.client_secret, facebookIdentityProvider.client_secret) && 
      Objects.equals(this.fields, facebookIdentityProvider.fields) && this.loginMethod == facebookIdentityProvider.loginMethod && 
      
      Objects.equals(this.permissions, facebookIdentityProvider.permissions));
  }
  
  public IdentityProviderType getType() {
    return IdentityProviderType.Facebook;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.appId, this.buttonText, this.client_secret, this.fields, this.loginMethod, this.permissions });
  }
  
  public String lookupAppId(UUID paramUUID) {
    return (String)lookup(() -> this.appId, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupAppId(String paramString) {
    return (String)lookup(() -> this.appId, () -> (String)app(paramString, ()));
  }
  
  public String lookupButtonText(UUID paramUUID) {
    return (String)lookup(() -> this.buttonText, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupButtonText(String paramString) {
    return (String)lookup(() -> this.buttonText, () -> (String)app(paramString, ()));
  }
  
  public String lookupClientSecret(UUID paramUUID) {
    return (String)lookup(() -> this.client_secret, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupFields(UUID paramUUID) {
    return (String)lookup(() -> this.fields, () -> (String)app(paramUUID, ()));
  }
  
  public IdentityProviderLoginMethod lookupLoginMethod(String paramString) {
    return (IdentityProviderLoginMethod)lookup(() -> this.loginMethod, () -> (IdentityProviderLoginMethod)app(paramString, ()));
  }
  
  public String lookupPermissions(UUID paramUUID) {
    return (String)lookup(() -> this.permissions, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupPermissions(String paramString) {
    return (String)lookup(() -> this.permissions, () -> (String)app(paramString, ()));
  }
  
  public boolean postRequestEnabled() {
    return false;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
