package io.fusionauth.domain;

import java.util.Objects;

public class DisplayableRawLogin extends RawLogin implements Buildable<DisplayableRawLogin> {
  public String applicationName;
  
  public Location location;
  
  public String loginId;
  
  public IdentityType loginIdType;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    DisplayableRawLogin displayableRawLogin = (DisplayableRawLogin)paramObject;
    return (Objects.equals(this.applicationName, displayableRawLogin.applicationName) && Objects.equals(this.location, displayableRawLogin.location) && Objects.equals(this.loginId, displayableRawLogin.loginId) && Objects.equals(this.loginIdType, displayableRawLogin.loginIdType));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationName, this.location, this.loginId, this.loginIdType });
  }
}
