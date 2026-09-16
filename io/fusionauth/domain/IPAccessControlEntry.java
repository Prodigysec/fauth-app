package io.fusionauth.domain;

import com.inversoft.json.ToString;
import java.util.Objects;

public class IPAccessControlEntry implements Buildable<IPAccessControlEntry> {
  public IPAccessControlEntryAction action = IPAccessControlEntryAction.Block;
  
  public String endIPAddress;
  
  public String startIPAddress;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    IPAccessControlEntry iPAccessControlEntry = (IPAccessControlEntry)paramObject;
    return (this.action == iPAccessControlEntry.action && 
      Objects.equals(this.endIPAddress, iPAccessControlEntry.endIPAddress) && 
      Objects.equals(this.startIPAddress, iPAccessControlEntry.startIPAddress));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.action, this.endIPAddress, this.startIPAddress });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
