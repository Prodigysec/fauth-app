package io.fusionauth.api.domain;

import com.inversoft.json.ToString;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class KickstartFile {
  public UUID id;
  
  public byte[] kickstart;
  
  public String name;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    KickstartFile kickstartFile = (KickstartFile)paramObject;
    return (Objects.equals(this.id, kickstartFile.id) && Arrays.equals(this.kickstart, kickstartFile.kickstart) && Objects.equals(this.name, kickstartFile.name));
  }
  
  public int hashCode() {
    int i = Objects.hash(new Object[] { this.id, this.name });
    i = 31 * i + Arrays.hashCode(this.kickstart);
    return i;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
