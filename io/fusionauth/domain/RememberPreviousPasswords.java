package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class RememberPreviousPasswords extends Enableable {
  public int count = 1;
  
  @JacksonConstructor
  public RememberPreviousPasswords() {}
  
  public RememberPreviousPasswords(RememberPreviousPasswords paramRememberPreviousPasswords) {
    this.count = paramRememberPreviousPasswords.count;
    this.enabled = paramRememberPreviousPasswords.enabled;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    RememberPreviousPasswords rememberPreviousPasswords = (RememberPreviousPasswords)paramObject;
    return (super.equals(paramObject) && 
      Objects.equals(Integer.valueOf(this.count), Integer.valueOf(rememberPreviousPasswords.count)));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), Integer.valueOf(this.count) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
