package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.domain.Buildable;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class OpenIdConnectIdentityProvider extends BaseIdentityProvider<OpenIdConnectApplicationConfiguration> implements Buildable<OpenIdConnectIdentityProvider>, DomainBasedIdentityProvider, SupportsPostBindings {
  public final Set<String> domains = new LinkedHashSet<>();
  
  @JSONColumn
  public URI buttonImageURL;
  
  @JSONColumn
  public String buttonText = "Login with OpenID Connect";
  
  @JSONColumn
  public IdentityProviderOauth2Configuration oauth2 = new IdentityProviderOauth2Configuration();
  
  @JSONColumn
  public boolean postRequest;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof OpenIdConnectIdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    OpenIdConnectIdentityProvider openIdConnectIdentityProvider = (OpenIdConnectIdentityProvider)paramObject;
    return (this.postRequest == openIdConnectIdentityProvider.postRequest && 
      Objects.equals(this.domains, openIdConnectIdentityProvider.domains) && 
      Objects.equals(this.buttonImageURL, openIdConnectIdentityProvider.buttonImageURL) && 
      Objects.equals(this.buttonText, openIdConnectIdentityProvider.buttonText) && 
      Objects.equals(this.lambdaConfiguration, openIdConnectIdentityProvider.lambdaConfiguration) && 
      Objects.equals(this.oauth2, openIdConnectIdentityProvider.oauth2));
  }
  
  public Set<String> getDomains() {
    return this.domains;
  }
  
  public IdentityProviderType getType() {
    return IdentityProviderType.OpenIDConnect;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.domains, this.buttonImageURL, this.buttonText, this.oauth2, Boolean.valueOf(this.postRequest) });
  }
  
  public URI lookupButtonImageURL(String paramString) {
    return (URI)lookup(() -> this.buttonImageURL, () -> (URI)app(paramString, ()));
  }
  
  public URI lookupButtonImageURL(UUID paramUUID) {
    return (URI)lookup(() -> this.buttonImageURL, () -> (URI)app(paramUUID, ()));
  }
  
  public String lookupButtonText(String paramString) {
    return (String)lookup(() -> this.buttonText, () -> (String)app(paramString, ()));
  }
  
  public String lookupButtonText(UUID paramUUID) {
    return (String)lookup(() -> this.buttonText, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupClientId(String paramString) {
    return (String)lookup(() -> this.oauth2.client_id, () -> (String)app(paramString, ()));
  }
  
  public String lookupClientId(UUID paramUUID) {
    return (String)lookup(() -> this.oauth2.client_id, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupClientSecret(UUID paramUUID) {
    return (String)lookup(() -> this.oauth2.client_secret, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupScope(String paramString) {
    return (String)lookup(() -> this.oauth2.scope, () -> (String)app(paramString, ()));
  }
  
  public String lookupScope(UUID paramUUID) {
    return (String)lookup(() -> this.oauth2.scope, () -> (String)app(paramUUID, ()));
  }
  
  public void normalize() {
    super.normalize();
    normalizeDomains();
  }
  
  public boolean postRequestEnabled() {
    return this.postRequest;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
