package io.fusionauth.api.domain.api.hypr;

import com.inversoft.json.ToString;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeviceAuthenticationStatusResponse {
  public HYPRDevice device;
  
  public List<HYPRLink> links = new ArrayList<>();
  
  public String machine;
  
  public String namedUser;
  
  public String requestId;
  
  public HYPRStateList state = new HYPRStateList();
  
  public boolean equals(Object paramObject) {
    DeviceAuthenticationStatusResponse deviceAuthenticationStatusResponse;
    if (this == paramObject)
      return true; 
    if (paramObject instanceof DeviceAuthenticationStatusResponse) {
      deviceAuthenticationStatusResponse = (DeviceAuthenticationStatusResponse)paramObject;
    } else {
      return false;
    } 
    return (Objects.equals(this.device, deviceAuthenticationStatusResponse.device) && 
      Objects.equals(this.links, deviceAuthenticationStatusResponse.links) && 
      Objects.equals(this.machine, deviceAuthenticationStatusResponse.machine) && 
      Objects.equals(this.namedUser, deviceAuthenticationStatusResponse.namedUser) && 
      Objects.equals(this.requestId, deviceAuthenticationStatusResponse.requestId) && 
      Objects.equals(this.state, deviceAuthenticationStatusResponse.state));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.device, this.links, this.machine, this.namedUser, this.requestId, this.state });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
