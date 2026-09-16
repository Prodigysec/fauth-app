package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class PasswordEncryptionConfiguration implements Buildable<PasswordEncryptionConfiguration> {
  public String encryptionScheme;
  
  public int encryptionSchemeFactor;
  
  public boolean modifyEncryptionSchemeOnLogin;
  
  @JacksonConstructor
  public PasswordEncryptionConfiguration() {}
  
  public PasswordEncryptionConfiguration(PasswordEncryptionConfiguration paramPasswordEncryptionConfiguration) {
    this.encryptionScheme = paramPasswordEncryptionConfiguration.encryptionScheme;
    this.encryptionSchemeFactor = paramPasswordEncryptionConfiguration.encryptionSchemeFactor;
    this.modifyEncryptionSchemeOnLogin = paramPasswordEncryptionConfiguration.modifyEncryptionSchemeOnLogin;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    PasswordEncryptionConfiguration passwordEncryptionConfiguration = (PasswordEncryptionConfiguration)paramObject;
    return (this.modifyEncryptionSchemeOnLogin == passwordEncryptionConfiguration.modifyEncryptionSchemeOnLogin && 
      Objects.equals(this.encryptionScheme, passwordEncryptionConfiguration.encryptionScheme) && 
      Objects.equals(Integer.valueOf(this.encryptionSchemeFactor), Integer.valueOf(passwordEncryptionConfiguration.encryptionSchemeFactor)));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.encryptionScheme, Integer.valueOf(this.encryptionSchemeFactor), Boolean.valueOf(this.modifyEncryptionSchemeOnLogin) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
