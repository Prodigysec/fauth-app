package io.fusionauth.domain;

import com.inversoft.json.ToString;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class RawLogin {
  public UUID applicationId;
  
  public ZonedDateTime instant;
  
  public String ipAddress;
  
  public UUID userId;
  
  public RawLogin() {}
  
  public RawLogin(UUID paramUUID1, ZonedDateTime paramZonedDateTime, String paramString, UUID paramUUID2) {
    this.applicationId = paramUUID1;
    this.instant = paramZonedDateTime;
    this.ipAddress = paramString;
    this.userId = paramUUID2;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof RawLogin))
      return false; 
    RawLogin rawLogin = (RawLogin)paramObject;
    return (Objects.equals(this.applicationId, rawLogin.applicationId) && 
      Objects.equals(this.instant, rawLogin.instant) && 
      Objects.equals(this.ipAddress, rawLogin.ipAddress) && 
      Objects.equals(this.userId, rawLogin.userId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.applicationId, this.instant, this.ipAddress, this.userId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
