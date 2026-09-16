package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import java.util.Objects;

public class UniversalApplicationConfiguration {
  public boolean universal;
  
  @JacksonConstructor
  public UniversalApplicationConfiguration() {}
  
  public UniversalApplicationConfiguration(boolean paramBoolean) {
    this.universal = paramBoolean;
  }
  
  public UniversalApplicationConfiguration(UniversalApplicationConfiguration paramUniversalApplicationConfiguration) {
    this.universal = paramUniversalApplicationConfiguration.universal;
  }
  
  public boolean equals(Object paramObject) {
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    UniversalApplicationConfiguration universalApplicationConfiguration = (UniversalApplicationConfiguration)paramObject;
    return (this.universal == universalApplicationConfiguration.universal);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Boolean.valueOf(this.universal) });
  }
}
