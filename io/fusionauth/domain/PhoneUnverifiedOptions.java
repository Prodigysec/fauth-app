package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class PhoneUnverifiedOptions implements Buildable<PhoneUnverifiedOptions> {
  public boolean allowPhoneNumberChangeWhenGated;
  
  public UnverifiedBehavior behavior = UnverifiedBehavior.Allow;
  
  @JacksonConstructor
  public PhoneUnverifiedOptions() {}
  
  public PhoneUnverifiedOptions(PhoneUnverifiedOptions paramPhoneUnverifiedOptions) {
    this.allowPhoneNumberChangeWhenGated = paramPhoneUnverifiedOptions.allowPhoneNumberChangeWhenGated;
    this.behavior = paramPhoneUnverifiedOptions.behavior;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    PhoneUnverifiedOptions phoneUnverifiedOptions = (PhoneUnverifiedOptions)paramObject;
    return (this.allowPhoneNumberChangeWhenGated == phoneUnverifiedOptions.allowPhoneNumberChangeWhenGated && this.behavior == phoneUnverifiedOptions.behavior);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Boolean.valueOf(this.allowPhoneNumberChangeWhenGated), this.behavior });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
