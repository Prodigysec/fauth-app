package io.fusionauth.api.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.RateLimitedRequestType;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class RequestFrequencyRecord {
  public int count;
  
  public ZonedDateTime lastUpdateInstant;
  
  public String requestId;
  
  public UUID tenantId;
  
  public RateLimitedRequestType type;
  
  @JacksonConstructor
  public RequestFrequencyRecord() {}
  
  public RequestFrequencyRecord(int paramInt, UUID paramUUID, ZonedDateTime paramZonedDateTime, String paramString, RateLimitedRequestType paramRateLimitedRequestType) {
    this.count = paramInt;
    this.requestId = paramString;
    this.lastUpdateInstant = paramZonedDateTime;
    this.tenantId = paramUUID;
    this.type = paramRateLimitedRequestType;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    RequestFrequencyRecord requestFrequencyRecord = (RequestFrequencyRecord)paramObject;
    return (this.count == requestFrequencyRecord.count && 
      Objects.equals(this.lastUpdateInstant, requestFrequencyRecord.lastUpdateInstant) && 
      Objects.equals(this.requestId, requestFrequencyRecord.requestId) && 
      Objects.equals(this.tenantId, requestFrequencyRecord.tenantId) && this.type == requestFrequencyRecord.type);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(this.count), this.lastUpdateInstant, this.requestId, this.tenantId, this.type });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
