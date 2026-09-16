package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class FailedAuthenticationActionCancelPolicy {
  public boolean onPasswordReset;
  
  @JacksonConstructor
  public FailedAuthenticationActionCancelPolicy() {}
  
  public FailedAuthenticationActionCancelPolicy(FailedAuthenticationActionCancelPolicy paramFailedAuthenticationActionCancelPolicy) {
    this.onPasswordReset = paramFailedAuthenticationActionCancelPolicy.onPasswordReset;
  }
  
  public FailedAuthenticationActionCancelPolicy(boolean paramBoolean) {
    this.onPasswordReset = paramBoolean;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    FailedAuthenticationActionCancelPolicy failedAuthenticationActionCancelPolicy = (FailedAuthenticationActionCancelPolicy)paramObject;
    return (this.onPasswordReset == failedAuthenticationActionCancelPolicy.onPasswordReset);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Boolean.valueOf(this.onPasswordReset) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
