package io.fusionauth.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class IdentityProviderLink implements Buildable<IdentityProviderLink>, JSONColumnable, Tenantable {
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Map<String, Object> data = new LinkedHashMap<>();
  
  @JSONColumn
  public String displayName;
  
  public UUID identityProviderId;
  
  public String identityProviderName;
  
  public IdentityProviderType identityProviderType;
  
  public String identityProviderUserId;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastLoginInstant;
  
  public UUID tenantId;
  
  @JSONColumn
  public String token;
  
  public UUID userId;
  
  @JacksonConstructor
  public IdentityProviderLink() {}
  
  public IdentityProviderLink(UUID paramUUID1, String paramString, UUID paramUUID2) {
    this.identityProviderId = paramUUID1;
    this.identityProviderUserId = paramString;
    this.userId = paramUUID2;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    IdentityProviderLink identityProviderLink = (IdentityProviderLink)paramObject;
    return (Objects.equals(this.data, identityProviderLink.data) && 
      Objects.equals(this.displayName, identityProviderLink.displayName) && 
      Objects.equals(this.identityProviderId, identityProviderLink.identityProviderId) && 
      Objects.equals(this.identityProviderName, identityProviderLink.identityProviderName) && 
      Objects.equals(this.identityProviderType, identityProviderLink.identityProviderType) && 
      Objects.equals(this.identityProviderUserId, identityProviderLink.identityProviderUserId) && 
      Objects.equals(this.insertInstant, identityProviderLink.insertInstant) && 
      Objects.equals(this.lastLoginInstant, identityProviderLink.lastLoginInstant) && 
      Objects.equals(this.tenantId, identityProviderLink.tenantId) && 
      Objects.equals(this.token, identityProviderLink.token) && 
      Objects.equals(this.userId, identityProviderLink.userId));
  }
  
  public UUID getTenantId() {
    return this.tenantId;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.data, this.displayName, this.identityProviderId, this.identityProviderName, this.identityProviderType, this.identityProviderUserId, this.insertInstant, this.lastLoginInstant, this.tenantId, this.token, 
          this.userId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
