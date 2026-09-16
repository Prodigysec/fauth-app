package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class EmailHeader implements Buildable<EmailHeader> {
  public String name;
  
  public String value;
  
  @JacksonConstructor
  public EmailHeader() {}
  
  public EmailHeader(String paramString1, String paramString2) {
    this.name = paramString1;
    this.value = paramString2;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    EmailHeader emailHeader = (EmailHeader)paramObject;
    return (Objects.equals(this.name, emailHeader.name) && Objects.equals(this.value, emailHeader.value));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.name, this.value });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
