package io.fusionauth.api.domain.ip.maxmind;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;

public class IPLocation implements Buildable<IPLocation> {
  public byte[] data;
  
  public ZonedDateTime lastModified;
  
  public int seq;
  
  @JacksonConstructor
  public IPLocation() {}
  
  public IPLocation(byte[] paramArrayOfbyte, ZonedDateTime paramZonedDateTime, Integer paramInteger) {
    this.data = paramArrayOfbyte;
    this.lastModified = paramZonedDateTime;
    this.seq = paramInteger.intValue();
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    IPLocation iPLocation = (IPLocation)paramObject;
    return (this.seq == iPLocation.seq && Arrays.equals(this.data, iPLocation.data) && Objects.equals(this.lastModified, iPLocation.lastModified));
  }
  
  public int hashCode() {
    int i = Objects.hash(new Object[] { this.lastModified, Integer.valueOf(this.seq) });
    i = 31 * i + Arrays.hashCode(this.data);
    return i;
  }
}
