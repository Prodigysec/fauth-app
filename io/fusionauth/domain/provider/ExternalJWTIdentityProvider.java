package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.util.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class ExternalJWTIdentityProvider extends BaseIdentityProvider<ExternalJWTApplicationConfiguration> implements Buildable<ExternalJWTIdentityProvider>, DomainBasedIdentityProvider {
  @JSONColumn
  public final Map<String, String> claimMap = new LinkedHashMap<>();
  
  public final Set<String> domains = new LinkedHashSet<>();
  
  @Deprecated
  public UUID defaultKeyId;
  
  public List<UUID> verificationKeyIds = new ArrayList<>();
  
  @JSONColumn
  public String headerKeyParameter = "kid";
  
  @JSONColumn
  public IdentityProviderOauth2Configuration oauth2 = new IdentityProviderOauth2Configuration();
  
  @Deprecated
  @JSONColumn
  public String uniqueIdentityClaim;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof ExternalJWTIdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    ExternalJWTIdentityProvider externalJWTIdentityProvider = (ExternalJWTIdentityProvider)paramObject;
    return (Objects.equals(this.claimMap, externalJWTIdentityProvider.claimMap) && 
      Objects.equals(this.defaultKeyId, externalJWTIdentityProvider.defaultKeyId) && 
      Objects.equals(this.headerKeyParameter, externalJWTIdentityProvider.headerKeyParameter) && 
      Objects.equals(this.oauth2, externalJWTIdentityProvider.oauth2) && 
      Objects.equals(this.uniqueIdentityClaim, externalJWTIdentityProvider.uniqueIdentityClaim) && 
      Objects.equals(this.domains, externalJWTIdentityProvider.domains) && 
      Objects.equals(this.verificationKeyIds, externalJWTIdentityProvider.verificationKeyIds));
  }
  
  public Set<String> getDomains() {
    return this.domains;
  }
  
  public IdentityProviderType getType() {
    return IdentityProviderType.ExternalJWT;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.claimMap, this.defaultKeyId, this.headerKeyParameter, this.oauth2, this.uniqueIdentityClaim, this.domains, this.verificationKeyIds });
  }
  
  public void normalize() {
    super.normalize();
    Normalizer.toLowerCase(this.domains, java.util.HashSet::new);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
