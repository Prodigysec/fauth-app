package io.fusionauth.api.domain.api.hypr;

import com.inversoft.json.ToString;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeviceAuthenticationResponse {
  public Response response;
  
  public HYPRStatusResponse status;
  
  public boolean equals(Object paramObject) {
    DeviceAuthenticationResponse deviceAuthenticationResponse;
    if (this == paramObject)
      return true; 
    if (paramObject instanceof DeviceAuthenticationResponse) {
      deviceAuthenticationResponse = (DeviceAuthenticationResponse)paramObject;
    } else {
      return false;
    } 
    return (Objects.equals(this.response, deviceAuthenticationResponse.response) && 
      Objects.equals(this.status, deviceAuthenticationResponse.status));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.response, this.status });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  public static class Response {
    public List<HYPRLink> links = new ArrayList<>();
    
    public String requestId;
  }
}
