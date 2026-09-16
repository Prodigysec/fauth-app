package io.fusionauth.api.domain.api.hypr;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.inversoft.json.ToString;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class HYPRDevice {
  public String deviceId;
  
  public String modelNumber;
  
  @JsonAnySetter
  public Map<String, Object> other = new LinkedHashMap<>();
  
  public boolean equals(Object paramObject) {
    HYPRDevice hYPRDevice;
    if (this == paramObject)
      return true; 
    if (paramObject instanceof HYPRDevice) {
      hYPRDevice = (HYPRDevice)paramObject;
    } else {
      return false;
    } 
    return (Objects.equals(this.deviceId, hYPRDevice.deviceId) && 
      Objects.equals(this.modelNumber, hYPRDevice.modelNumber) && 
      Objects.equals(this.other, hYPRDevice.other));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.deviceId, this.modelNumber, this.other });
  }
  
  @JsonAnySetter
  public void setOther(String paramString, Object paramObject) {
    this.other.put(paramString, paramObject);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
