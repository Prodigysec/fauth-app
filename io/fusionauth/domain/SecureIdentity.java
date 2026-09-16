package io.fusionauth.domain;

import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SecureIdentity {
  public final List<UserIdentity> identities = new ArrayList<>();
  
  public ZonedDateTime breachedPasswordLastCheckedInstant;
  
  public BreachedPasswordStatus breachedPasswordStatus;
  
  public UUID connectorId = BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID;
  
  public String encryptionScheme;
  
  public Integer factor;
  
  public UUID id;
  
  public ZonedDateTime lastLoginInstant;
  
  @MaskString
  public String password;
  
  public ChangePasswordReason passwordChangeReason;
  
  public boolean passwordChangeRequired;
  
  public ZonedDateTime passwordLastUpdateInstant;
  
  @MaskString
  public String salt;
  
  public String uniqueUsername;
  
  public String username;
  
  public ContentStatus usernameStatus = ContentStatus.ACTIVE;
  
  @Deprecated
  public boolean verified;
  
  public ZonedDateTime verifiedInstant;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    SecureIdentity secureIdentity = (SecureIdentity)paramObject;
    return (this.passwordChangeRequired == secureIdentity.passwordChangeRequired && this.verified == secureIdentity.verified && 
      
      Objects.equals(this.breachedPasswordLastCheckedInstant, secureIdentity.breachedPasswordLastCheckedInstant) && this.breachedPasswordStatus == secureIdentity.breachedPasswordStatus && 
      
      Objects.equals(this.connectorId, secureIdentity.connectorId) && 
      Objects.equals(this.encryptionScheme, secureIdentity.encryptionScheme) && 
      Objects.equals(this.factor, secureIdentity.factor) && 
      Objects.equals(this.id, secureIdentity.id) && 
      Objects.equals(this.identities, secureIdentity.identities) && 
      Objects.equals(this.lastLoginInstant, secureIdentity.lastLoginInstant) && 
      Objects.equals(this.password, secureIdentity.password) && this.passwordChangeReason == secureIdentity.passwordChangeReason && 
      
      Objects.equals(this.passwordLastUpdateInstant, secureIdentity.passwordLastUpdateInstant) && 
      Objects.equals(this.salt, secureIdentity.salt) && 
      Objects.equals(this.uniqueUsername, secureIdentity.uniqueUsername) && 
      Objects.equals(this.username, secureIdentity.username) && this.usernameStatus == secureIdentity.usernameStatus && 
      
      Objects.equals(this.verifiedInstant, secureIdentity.verifiedInstant));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.breachedPasswordLastCheckedInstant, this.breachedPasswordStatus, this.connectorId, this.encryptionScheme, this.factor, this.id, this.identities, this.lastLoginInstant, this.password, this.passwordChangeReason, 
          Boolean.valueOf(this.passwordChangeRequired), this.passwordLastUpdateInstant, this.salt, this.uniqueUsername, this.username, this.usernameStatus, 




          
          Boolean.valueOf(this.verified), this.verifiedInstant });
  }
}
