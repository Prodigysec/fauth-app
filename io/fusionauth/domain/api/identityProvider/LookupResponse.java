package io.fusionauth.domain.api.identityProvider;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.BaseIdentityProviderApplicationConfiguration;
import io.fusionauth.domain.provider.ExternalJWTIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderOauth2Configuration;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.OpenIdConnectIdentityProvider;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LookupResponse {
  public IdentityProviderDetails identityProvider;
  
  @JacksonConstructor
  public LookupResponse() {}
  
  public LookupResponse(BaseIdentityProvider<?> paramBaseIdentityProvider) {
    this.identityProvider = new IdentityProviderDetails();
    this.identityProvider.id = paramBaseIdentityProvider.id;
    this.identityProvider.tenantId = paramBaseIdentityProvider.tenantId;
    this.identityProvider.name = paramBaseIdentityProvider.name;
    this.identityProvider.type = paramBaseIdentityProvider.getType();
    paramBaseIdentityProvider.applicationConfiguration.entrySet().stream()
      .filter(paramEntry -> ((BaseIdentityProviderApplicationConfiguration)paramEntry.getValue()).enabled)
      .forEach(paramEntry -> this.identityProvider.applicationIds.add((UUID)paramEntry.getKey()));
    if (paramBaseIdentityProvider instanceof ExternalJWTIdentityProvider) {
      this.identityProvider.oauth2 = new IdentityProviderOauth2Configuration(((ExternalJWTIdentityProvider)paramBaseIdentityProvider).oauth2);
      this.identityProvider.oauth2.clientAuthenticationMethod = null;
    } 
    if (paramBaseIdentityProvider instanceof OpenIdConnectIdentityProvider)
      this.identityProvider.oauth2 = new IdentityProviderOauth2Configuration(((OpenIdConnectIdentityProvider)paramBaseIdentityProvider).oauth2); 
    if (this.identityProvider.oauth2 != null) {
      this.identityProvider.oauth2.client_secret = null;
      this.identityProvider.oauth2.emailClaim = null;
      this.identityProvider.oauth2.emailVerifiedClaim = null;
      this.identityProvider.oauth2.uniqueIdClaim = null;
      this.identityProvider.oauth2.usernameClaim = null;
    } 
    if (paramBaseIdentityProvider instanceof SAMLv2IdentityProvider)
      this.identityProvider.idpEndpoint = ((SAMLv2IdentityProvider)paramBaseIdentityProvider).idpEndpoint; 
  }
  
  public static class IdentityProviderDetails {
    public List<UUID> applicationIds = new ArrayList<>();
    
    public UUID id;
    
    public URI idpEndpoint;
    
    public String name;
    
    public IdentityProviderOauth2Configuration oauth2;
    
    public UUID tenantId;
    
    public IdentityProviderType type;
  }
}
