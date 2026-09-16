package io.fusionauth.api.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;
import java.util.UUID;

public class IntervalUser {
  public UUID applicationId;
  
  public int period;
  
  public UUID userId;
  
  @JacksonConstructor
  public IntervalUser() {}
  
  public IntervalUser(UUID paramUUID1, int paramInt, UUID paramUUID2) {
    this.applicationId = paramUUID1;
    this.period = paramInt;
    this.userId = paramUUID2;
  }
  
  public boolean equals(Object paramObject) {
    IntervalUser intervalUser;
    if (this == paramObject)
      return true; 
    if (paramObject instanceof IntervalUser) {
      intervalUser = (IntervalUser)paramObject;
    } else {
      return false;
    } 
    return (Objects.equals(this.applicationId, intervalUser.applicationId) && 
      Objects.equals(Integer.valueOf(this.period), Integer.valueOf(intervalUser.period)) && 
      Objects.equals(this.userId, intervalUser.userId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.applicationId, Integer.valueOf(this.period), this.userId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
