package io.fusionauth.domain;

import com.inversoft.error.Errors;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;
import java.util.UUID;

public class UserLoginFailedReason implements Buildable<UserLoginFailedReason> {
  public String code;
  
  public UUID lambdaId;
  
  public Errors lambdaResult;
  
  @JacksonConstructor
  public UserLoginFailedReason() {}
  
  public UserLoginFailedReason(String paramString) {
    this.code = paramString;
  }
  
  public UserLoginFailedReason(String paramString, UUID paramUUID, Errors paramErrors) {
    this.code = paramString;
    this.lambdaId = paramUUID;
    this.lambdaResult = paramErrors;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    UserLoginFailedReason userLoginFailedReason = (UserLoginFailedReason)paramObject;
    return (Objects.equals(this.code, userLoginFailedReason.code) && Objects.equals(this.lambdaId, userLoginFailedReason.lambdaId) && Objects.equals(this.lambdaResult, userLoginFailedReason.lambdaResult));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.code, this.lambdaId, this.lambdaResult });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
