package io.fusionauth.api.domain.api.hypr;

import com.inversoft.json.ToString;
import java.util.Objects;

public class HYPRStatusResponse {
  public int responseCode;
  
  public String responseMessage;
  
  public boolean equals(Object paramObject) {
    HYPRStatusResponse hYPRStatusResponse;
    if (this == paramObject)
      return true; 
    if (paramObject instanceof HYPRStatusResponse) {
      hYPRStatusResponse = (HYPRStatusResponse)paramObject;
    } else {
      return false;
    } 
    return (this.responseCode == hYPRStatusResponse.responseCode && 
      Objects.equals(this.responseMessage, hYPRStatusResponse.responseMessage));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(this.responseCode), this.responseMessage });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
