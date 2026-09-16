package io.fusionauth.api.domain.api.hypr;

import com.inversoft.json.ToString;
import java.util.Objects;

public class HYPRRequestState {
  public String message;
  
  public long timestamp;
  
  public HYPRState value;
  
  public boolean equals(Object paramObject) {
    HYPRRequestState hYPRRequestState;
    if (this == paramObject)
      return true; 
    if (paramObject instanceof HYPRRequestState) {
      hYPRRequestState = (HYPRRequestState)paramObject;
    } else {
      return false;
    } 
    return (this.timestamp == hYPRRequestState.timestamp && 
      Objects.equals(this.message, hYPRRequestState.message) && 
      Objects.equals(this.value, hYPRRequestState.value));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.message, Long.valueOf(this.timestamp), this.value });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
