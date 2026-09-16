package io.fusionauth.domain.provider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.CORSConfiguration;
import io.fusionauth.domain.RequiresCORSConfiguration;
import io.fusionauth.domain.jwks.JSONWebKeyInfoProvider;
import io.fusionauth.domain.util.HTTPMethod;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class AppleIdentityProvider extends BaseIdentityProvider<AppleApplicationConfiguration> implements Buildable<AppleIdentityProvider>, JSONWebKeyInfoProvider, RequiresCORSConfiguration, SupportsPostBindings {
  public static final URI ISSUER = URI.create("https://appleid.apple.com");
  
  public static final URI JWKS_URI = URI.create("https://appleid.apple.com/auth/keys");
  
  @JSONColumn
  public String bundleId;
  
  @JSONColumn
  public String buttonText = "Sign in with Apple";
  
  public UUID keyId;
  
  @JSONColumn
  public String scope;
  
  @JSONColumn
  public String servicesId;
  
  @JSONColumn
  public String teamId;
  
  @JsonIgnore
  public CORSConfiguration corsConfiguration() {
    return (new CORSConfiguration()).with(paramCORSConfiguration -> paramCORSConfiguration.allowedMethods.add(HTTPMethod.POST))
      .with(paramCORSConfiguration -> paramCORSConfiguration.allowedOrigins.add(issuer()));
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof AppleIdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    AppleIdentityProvider appleIdentityProvider = (AppleIdentityProvider)paramObject;
    return (Objects.equals(this.bundleId, appleIdentityProvider.bundleId) && 
      Objects.equals(this.buttonText, appleIdentityProvider.buttonText) && 
      Objects.equals(this.keyId, appleIdentityProvider.keyId) && 
      Objects.equals(this.scope, appleIdentityProvider.scope) && 
      Objects.equals(this.servicesId, appleIdentityProvider.servicesId) && 
      Objects.equals(this.teamId, appleIdentityProvider.teamId));
  }
  
  public IdentityProviderType getType() {
    return IdentityProviderType.Apple;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.bundleId, this.buttonText, this.keyId, this.scope, this.servicesId, this.teamId });
  }
  
  public URI issuer() {
    return ISSUER;
  }
  
  public URI jwksURI() {
    return JWKS_URI;
  }
  
  public String lookupBundleId(String paramString) {
    return (String)lookup(() -> this.bundleId, () -> (String)app(paramString, ()));
  }
  
  public String lookupBundleId(UUID paramUUID) {
    return (String)lookup(() -> this.bundleId, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupButtonText(String paramString) {
    return (String)lookup(() -> this.buttonText, () -> (String)app(paramString, ()));
  }
  
  public String lookupButtonText(UUID paramUUID) {
    return (String)lookup(() -> this.buttonText, () -> (String)app(paramUUID, ()));
  }
  
  public UUID lookupKeyId(UUID paramUUID) {
    return (UUID)lookup(() -> this.keyId, () -> (UUID)app(paramUUID, ()));
  }
  
  public String lookupScope(String paramString) {
    return (String)lookup(() -> this.scope, () -> (String)app(paramString, ()));
  }
  
  public String lookupScope(UUID paramUUID) {
    return (String)lookup(() -> this.scope, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupServicesId(String paramString) {
    return (String)lookup(() -> this.servicesId, () -> (String)app(paramString, ()));
  }
  
  public String lookupServicesId(UUID paramUUID) {
    return (String)lookup(() -> this.servicesId, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupTeamId(String paramString) {
    return (String)lookup(() -> this.teamId, () -> (String)app(paramString, ()));
  }
  
  public String lookupTeamId(UUID paramUUID) {
    return (String)lookup(() -> this.teamId, () -> (String)app(paramUUID, ()));
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
