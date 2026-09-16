package io.fusionauth.api.domain;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;

public class SequencedData implements Buildable<SequencedData> {
  public byte[] data;
  
  public ZonedDateTime lastModified;
  
  public int seq;
  
  @JacksonConstructor
  public SequencedData() {}
  
  public SequencedData(byte[] paramArrayOfbyte, ZonedDateTime paramZonedDateTime, Integer paramInteger) {
    this.data = paramArrayOfbyte;
    this.lastModified = paramZonedDateTime;
    this.seq = paramInteger.intValue();
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    SequencedData sequencedData = (SequencedData)paramObject;
    return (this.seq == sequencedData.seq && Arrays.equals(this.data, sequencedData.data) && Objects.equals(this.lastModified, sequencedData.lastModified));
  }
  
  public int hashCode() {
    int i = Objects.hash(new Object[] { this.lastModified, Integer.valueOf(this.seq) });
    i = 31 * i + Arrays.hashCode(this.data);
    return i;
  }
}
