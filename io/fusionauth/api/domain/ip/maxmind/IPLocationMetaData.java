package io.fusionauth.api.domain.ip.maxmind;

import com.inversoft.json.JacksonConstructor;
import java.time.ZonedDateTime;
import java.util.Objects;

public class IPLocationMetaData {
  public String digest;
  
  public ZonedDateTime lastModified;
  
  @JacksonConstructor
  public IPLocationMetaData() {}
  
  public IPLocationMetaData(String paramString, ZonedDateTime paramZonedDateTime) {
    this.digest = paramString;
    this.lastModified = paramZonedDateTime;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    IPLocationMetaData iPLocationMetaData = (IPLocationMetaData)paramObject;
    return (Objects.equals(this.digest, iPLocationMetaData.digest) && Objects.equals(this.lastModified, iPLocationMetaData.lastModified));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.digest, this.lastModified });
  }
}
