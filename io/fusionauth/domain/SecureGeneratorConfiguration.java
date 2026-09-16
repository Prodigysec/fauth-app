package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class SecureGeneratorConfiguration {
  public int length;
  
  public SecureGeneratorType type;
  
  @JacksonConstructor
  public SecureGeneratorConfiguration() {}
  
  public SecureGeneratorConfiguration(SecureGeneratorConfiguration paramSecureGeneratorConfiguration) {
    this.length = paramSecureGeneratorConfiguration.length;
    this.type = paramSecureGeneratorConfiguration.type;
  }
  
  public SecureGeneratorConfiguration(int paramInt, SecureGeneratorType paramSecureGeneratorType) {
    this.length = paramInt;
    this.type = paramSecureGeneratorType;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof SecureGeneratorConfiguration))
      return false; 
    SecureGeneratorConfiguration secureGeneratorConfiguration = (SecureGeneratorConfiguration)paramObject;
    return (this.length == secureGeneratorConfiguration.length && this.type == secureGeneratorConfiguration.type);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(this.length), this.type });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
