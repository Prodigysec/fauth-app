package io.fusionauth.domain.api;

import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.jwt.RefreshToken;
import java.util.UUID;

public class BaseLoginRequest extends BaseEventRequest {
  public UUID applicationId;
  
  public Double botDetectionScore;
  
  @Deprecated
  public String ipAddress;
  
  @Deprecated
  public RefreshToken.MetaData metaData;
  
  public boolean newDevice;
  
  public boolean noJWT;
  
  public BaseLoginRequest() {}
  
  public BaseLoginRequest(EventInfo paramEventInfo) {
    super(paramEventInfo);
  }
  
  public void normalize() {
    this.eventInfo = (this.eventInfo != null) ? this.eventInfo : new EventInfo();
    if (this.eventInfo.ipAddress == null)
      this.eventInfo.ipAddress = this.ipAddress; 
    if (this.metaData == null || this.metaData.device == null)
      return; 
    if (this.eventInfo.deviceDescription == null)
      this.eventInfo.deviceDescription = this.metaData.device.description; 
    if (this.eventInfo.deviceName == null)
      this.eventInfo.deviceName = this.metaData.device.name; 
    if (this.eventInfo.deviceType == null)
      this.eventInfo.deviceType = this.metaData.device.type; 
  }
}
