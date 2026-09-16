package io.fusionauth.api.domain;

import com.inversoft.json.ToString;
import java.security.Key;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;

public class SystemEncryptionKey {
  public String encryptionKey;
  
  public SystemEncryptionKey(String paramString) {
    this.encryptionKey = paramString;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    SystemEncryptionKey systemEncryptionKey = (SystemEncryptionKey)paramObject;
    return Objects.equals(this.encryptionKey, systemEncryptionKey.encryptionKey);
  }
  
  public Key getKey() {
    return new SecretKeySpec(Base64.getDecoder().decode(this.encryptionKey), "AES");
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.encryptionKey });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
