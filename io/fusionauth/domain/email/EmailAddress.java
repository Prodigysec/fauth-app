package io.fusionauth.domain.email;

import com.inversoft.json.JacksonConstructor;
import java.util.Objects;

public class EmailAddress {
  public String address;
  
  public String display;
  
  @JacksonConstructor
  public EmailAddress() {}
  
  public EmailAddress(String paramString) {
    this.address = paramString;
    this.display = null;
  }
  
  public EmailAddress(String paramString1, String paramString2) {
    this.address = paramString1;
    this.display = paramString2;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof EmailAddress))
      return false; 
    EmailAddress emailAddress = (EmailAddress)paramObject;
    return (Objects.equals(this.address, emailAddress.address) && 
      Objects.equals(this.display, emailAddress.display));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.address, this.display });
  }
}
