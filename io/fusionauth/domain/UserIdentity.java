package io.fusionauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.util.Normalizer;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class UserIdentity implements Buildable<UserIdentity> {
  public String displayValue;
  
  @JsonIgnore
  public Long id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastLoginInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  public ContentStatus moderationStatus;
  
  public boolean primary = true;
  
  @JsonIgnore
  public UUID tenantId;
  
  public IdentityType type;
  
  @JsonIgnore
  public UUID userId;
  
  public String value;
  
  public boolean verified;
  
  public ZonedDateTime verifiedInstant;
  
  public IdentityVerifiedReason verifiedReason;
  
  public UserIdentity(UserIdentity paramUserIdentity) {
    this.id = paramUserIdentity.id;
    this.insertInstant = paramUserIdentity.insertInstant;
    this.lastLoginInstant = paramUserIdentity.lastLoginInstant;
    this.lastUpdateInstant = paramUserIdentity.lastUpdateInstant;
    this.moderationStatus = paramUserIdentity.moderationStatus;
    this.primary = paramUserIdentity.primary;
    this.type = new IdentityType(paramUserIdentity.type);
    this.tenantId = paramUserIdentity.tenantId;
    this.displayValue = paramUserIdentity.displayValue;
    this.userId = paramUserIdentity.userId;
    this.value = paramUserIdentity.value;
    this.verified = paramUserIdentity.verified;
    this.verifiedReason = paramUserIdentity.verifiedReason;
    this.verifiedInstant = paramUserIdentity.verifiedInstant;
  }
  
  @JacksonConstructor
  public UserIdentity() {}
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof UserIdentity))
      return false; 
    UserIdentity userIdentity = (UserIdentity)paramObject;
    return (Objects.equals(this.id, userIdentity.id) && this.primary == userIdentity.primary && this.verified == userIdentity.verified && this.verifiedReason == userIdentity.verifiedReason && 


      
      Objects.equals(this.insertInstant, userIdentity.insertInstant) && 
      Objects.equals(this.lastLoginInstant, userIdentity.lastLoginInstant) && 
      Objects.equals(this.lastUpdateInstant, userIdentity.lastUpdateInstant) && this.moderationStatus == userIdentity.moderationStatus && 
      
      Objects.equals(this.tenantId, userIdentity.tenantId) && 
      Objects.equals(this.type, userIdentity.type) && 
      Objects.equals(this.displayValue, userIdentity.displayValue) && 
      Objects.equals(this.userId, userIdentity.userId) && 
      Objects.equals(this.value, userIdentity.value) && 
      Objects.equals(this.verifiedInstant, userIdentity.verifiedInstant));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.id, this.insertInstant, this.lastLoginInstant, this.lastUpdateInstant, this.moderationStatus, 



          
          Boolean.valueOf(this.primary), this.tenantId, this.type, this.displayValue, this.userId, 
          this.value, 




          
          Boolean.valueOf(this.verified), this.verifiedReason, this.verifiedInstant });
  }
  
  public void normalize() {
    this.value = Normalizer.trim(this.value);
    if (this.value != null && this.value.length() == 0)
      this.value = null; 
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  public boolean verificationRequired() {
    return (!this.verified && !IdentityVerifiedReason.Disabled.equals(this.verifiedReason) && 
      !IdentityVerifiedReason.Import.equals(this.verifiedReason) && 
      !IdentityVerifiedReason.Trusted.equals(this.verifiedReason) && 
      !IdentityVerifiedReason.Skipped.equals(this.verifiedReason) && 
      !IdentityVerifiedReason.Unverifiable.equals(this.verifiedReason));
  }
}
