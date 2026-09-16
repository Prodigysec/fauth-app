package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class EmailUnverifiedOptions implements Buildable<EmailUnverifiedOptions> {
  public boolean allowEmailChangeWhenGated;
  
  public UnverifiedBehavior behavior = UnverifiedBehavior.Allow;
  
  @JacksonConstructor
  public EmailUnverifiedOptions() {}
  
  public EmailUnverifiedOptions(EmailUnverifiedOptions paramEmailUnverifiedOptions) {
    this.allowEmailChangeWhenGated = paramEmailUnverifiedOptions.allowEmailChangeWhenGated;
    this.behavior = paramEmailUnverifiedOptions.behavior;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    EmailUnverifiedOptions emailUnverifiedOptions = (EmailUnverifiedOptions)paramObject;
    return (this.allowEmailChangeWhenGated == emailUnverifiedOptions.allowEmailChangeWhenGated && this.behavior == emailUnverifiedOptions.behavior);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Boolean.valueOf(this.allowEmailChangeWhenGated), this.behavior });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
