package io.fusionauth.domain.provider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.User;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PendingIdPLink implements Buildable<PendingIdPLink> {
  public String displayName;
  
  public String email;
  
  public UUID identityProviderId;
  
  public List<IdentityProviderLink> identityProviderLinks;
  
  public String identityProviderName;
  
  public IdentityProviderTenantConfiguration identityProviderTenantConfiguration;
  
  public IdentityProviderType identityProviderType;
  
  public String identityProviderUserId;
  
  public User user;
  
  public String username;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    PendingIdPLink pendingIdPLink = (PendingIdPLink)paramObject;
    return (Objects.equals(this.displayName, pendingIdPLink.displayName) && Objects.equals(this.email, pendingIdPLink.email) && Objects.equals(this.identityProviderId, pendingIdPLink.identityProviderId) && Objects.equals(this.identityProviderLinks, pendingIdPLink.identityProviderLinks) && Objects.equals(this.identityProviderName, pendingIdPLink.identityProviderName) && this.identityProviderType == pendingIdPLink.identityProviderType && Objects.equals(this.identityProviderUserId, pendingIdPLink.identityProviderUserId) && Objects.equals(this.user, pendingIdPLink.user) && Objects.equals(this.username, pendingIdPLink.username));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.displayName, this.email, this.identityProviderId, this.identityProviderLinks, this.identityProviderName, this.identityProviderType, this.identityProviderUserId, this.user, this.username });
  }
  
  @JsonIgnore
  public boolean isLinkLimitExceeded() {
    if (this.identityProviderTenantConfiguration == null || this.identityProviderLinks == null)
      return false; 
    if (this.identityProviderLinks.stream().anyMatch(paramIdentityProviderLink -> paramIdentityProviderLink.identityProviderUserId.equals(this.identityProviderUserId)))
      return false; 
    return (this.identityProviderLinks.size() >= this.identityProviderTenantConfiguration.limitUserLinkCount.maximumLinks);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
