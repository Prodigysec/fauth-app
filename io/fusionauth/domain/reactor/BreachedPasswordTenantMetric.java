package io.fusionauth.domain.reactor;

import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;

public class BreachedPasswordTenantMetric implements Buildable<BreachedPasswordTenantMetric> {
  public int actionRequired;
  
  public int matchedCommonPasswordCount;
  
  public int matchedExactCount;
  
  public int matchedPasswordCount;
  
  public int matchedSubAddressCount;
  
  public int passwordsCheckedCount;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof BreachedPasswordTenantMetric))
      return false; 
    BreachedPasswordTenantMetric breachedPasswordTenantMetric = (BreachedPasswordTenantMetric)paramObject;
    return (this.actionRequired == breachedPasswordTenantMetric.actionRequired && this.matchedCommonPasswordCount == breachedPasswordTenantMetric.matchedCommonPasswordCount && this.matchedExactCount == breachedPasswordTenantMetric.matchedExactCount && this.matchedPasswordCount == breachedPasswordTenantMetric.matchedPasswordCount && this.matchedSubAddressCount == breachedPasswordTenantMetric.matchedSubAddressCount && this.passwordsCheckedCount == breachedPasswordTenantMetric.passwordsCheckedCount);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(this.actionRequired), 
          Integer.valueOf(this.matchedCommonPasswordCount), 
          Integer.valueOf(this.matchedExactCount), 
          Integer.valueOf(this.matchedPasswordCount), 
          Integer.valueOf(this.matchedSubAddressCount), 
          Integer.valueOf(this.passwordsCheckedCount) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  public int totalBreached() {
    return this.matchedCommonPasswordCount + this.matchedExactCount + this.matchedPasswordCount + this.matchedSubAddressCount;
  }
}
