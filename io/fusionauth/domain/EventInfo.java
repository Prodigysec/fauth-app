package io.fusionauth.domain;

import com.inversoft.json.ToString;
import io.fusionauth.domain.jwt.DeviceInfo;
import io.fusionauth.domain.jwt.RefreshToken;
import java.util.Map;
import java.util.Objects;

public class EventInfo implements Buildable<EventInfo> {
  public Map<String, Object> data;
  
  public String deviceDescription;
  
  public String deviceName;
  
  public String deviceType;
  
  public String ipAddress;
  
  public Location location;
  
  public String os;
  
  public String userAgent;
  
  public EventInfo() {}
  
  public EventInfo(RefreshToken.MetaData paramMetaData) {
    this.deviceDescription = (paramMetaData != null && paramMetaData.device != null) ? paramMetaData.device.description : null;
    this.deviceName = (paramMetaData != null && paramMetaData.device != null) ? paramMetaData.device.name : null;
    this.deviceType = (paramMetaData != null && paramMetaData.device != null && paramMetaData.device.type != null) ? paramMetaData.device.type.toString() : null;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    EventInfo eventInfo = (EventInfo)paramObject;
    return (Objects.equals(this.deviceDescription, eventInfo.deviceDescription) && 
      Objects.equals(this.data, eventInfo.data) && 
      Objects.equals(this.deviceName, eventInfo.deviceName) && 
      Objects.equals(this.deviceType, eventInfo.deviceType) && 
      Objects.equals(this.ipAddress, eventInfo.ipAddress) && 
      Objects.equals(this.location, eventInfo.location) && 
      Objects.equals(this.os, eventInfo.os) && 
      Objects.equals(this.userAgent, eventInfo.userAgent));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.data, this.deviceDescription, this.deviceName, this.deviceType, this.ipAddress, this.location, this.os, this.userAgent });
  }
  
  public RefreshToken.MetaData toMetaData() {
    return (new RefreshToken.MetaData()).with(paramMetaData -> paramMetaData.device = new DeviceInfo())
      .with(paramMetaData -> paramMetaData.device.description = this.deviceDescription)
      .with(paramMetaData -> paramMetaData.device.name = this.deviceName)
      .with(paramMetaData -> paramMetaData.device.type = this.deviceType);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
