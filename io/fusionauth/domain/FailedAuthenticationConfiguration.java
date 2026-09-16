package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.ExcludeFromJSONColumn;
import java.util.Objects;
import java.util.UUID;

public class FailedAuthenticationConfiguration implements Buildable<FailedAuthenticationConfiguration> {
  public FailedAuthenticationActionCancelPolicy actionCancelPolicy = new FailedAuthenticationActionCancelPolicy(false);
  
  public long actionDuration = 3L;
  
  public ExpiryUnit actionDurationUnit = ExpiryUnit.MINUTES;
  
  public boolean emailUser;
  
  public int resetCountInSeconds = 60;
  
  public int tooManyAttempts = 5;
  
  @ExcludeFromJSONColumn
  public UUID userActionId;
  
  @JacksonConstructor
  public FailedAuthenticationConfiguration() {}
  
  public FailedAuthenticationConfiguration(FailedAuthenticationConfiguration paramFailedAuthenticationConfiguration) {
    this.actionCancelPolicy = new FailedAuthenticationActionCancelPolicy(paramFailedAuthenticationConfiguration.actionCancelPolicy);
    this.actionDuration = paramFailedAuthenticationConfiguration.actionDuration;
    this.actionDurationUnit = paramFailedAuthenticationConfiguration.actionDurationUnit;
    this.emailUser = paramFailedAuthenticationConfiguration.emailUser;
    this.resetCountInSeconds = paramFailedAuthenticationConfiguration.resetCountInSeconds;
    this.tooManyAttempts = paramFailedAuthenticationConfiguration.tooManyAttempts;
    this.userActionId = paramFailedAuthenticationConfiguration.userActionId;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    FailedAuthenticationConfiguration failedAuthenticationConfiguration = (FailedAuthenticationConfiguration)paramObject;
    return (this.actionDuration == failedAuthenticationConfiguration.actionDuration && this.emailUser == failedAuthenticationConfiguration.emailUser && this.resetCountInSeconds == failedAuthenticationConfiguration.resetCountInSeconds && this.tooManyAttempts == failedAuthenticationConfiguration.tooManyAttempts && Objects.equals(this.actionCancelPolicy, failedAuthenticationConfiguration.actionCancelPolicy) && this.actionDurationUnit == failedAuthenticationConfiguration.actionDurationUnit && Objects.equals(this.userActionId, failedAuthenticationConfiguration.userActionId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.actionCancelPolicy, Long.valueOf(this.actionDuration), this.actionDurationUnit, Boolean.valueOf(this.emailUser), Integer.valueOf(this.resetCountInSeconds), Integer.valueOf(this.tooManyAttempts), this.userActionId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
