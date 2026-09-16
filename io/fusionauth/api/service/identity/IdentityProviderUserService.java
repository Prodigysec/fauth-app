package io.fusionauth.api.service.identity;

import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import java.util.List;
import java.util.UUID;

public interface IdentityProviderUserService {
  IdentityProviderLink link(EventInfo paramEventInfo, Tenant paramTenant, User paramUser, BaseIdentityProvider<?> paramBaseIdentityProvider, String paramString1, String paramString2, String paramString3, String paramString4);
  
  IdentityProviderLink retrieveIdentityProviderUser(Tenant paramTenant, BaseIdentityProvider<?> paramBaseIdentityProvider, String paramString, User paramUser);
  
  List<IdentityProviderLink> retrieveIdentityProviderUsersByUser(Tenant paramTenant, BaseIdentityProvider<?> paramBaseIdentityProvider, UUID paramUUID);
  
  void unlink(EventInfo paramEventInfo, Tenant paramTenant, BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLink paramIdentityProviderLink, User paramUser);
  
  ValidationResult validateLink(Tenant paramTenant, UUID paramUUID1, String paramString1, String paramString2, UUID paramUUID2);
  
  ValidationResult validatePendingLinkRetrieve(Tenant paramTenant, String paramString, UUID paramUUID);
  
  ValidationResult validateRetrieve(Tenant paramTenant, UUID paramUUID1, String paramString, UUID paramUUID2);
  
  ValidationResult validateUnlink(Tenant paramTenant, UUID paramUUID1, String paramString, UUID paramUUID2);
  
  public static class ValidationResult extends BaseValidationResult {
    public ExternalIdentifier externalIdentifier;
    
    public BaseIdentityProvider<?> identityProvider;
    
    public String identityProviderUserId;
    
    public IdentityProviderLink link;
    
    public Tenant tenant;
    
    public User user;
    
    public List<IdentityProviderLink> userLinks;
  }
}
