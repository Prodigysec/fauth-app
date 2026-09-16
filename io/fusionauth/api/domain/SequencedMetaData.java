package io.fusionauth.api.domain;

import com.inversoft.json.JacksonConstructor;
import java.time.ZonedDateTime;
import java.util.Objects;

public class SequencedMetaData {
  public String digest;
  
  public ZonedDateTime lastModified;
  
  @JacksonConstructor
  public SequencedMetaData() {}
  
  public SequencedMetaData(String paramString, ZonedDateTime paramZonedDateTime) {
    this.digest = paramString;
    this.lastModified = paramZonedDateTime;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    SequencedMetaData sequencedMetaData = (SequencedMetaData)paramObject;
    return (Objects.equals(this.digest, sequencedMetaData.digest) && Objects.equals(this.lastModified, sequencedMetaData.lastModified));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.digest, this.lastModified });
  }
}
