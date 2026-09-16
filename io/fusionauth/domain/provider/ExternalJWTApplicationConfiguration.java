package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;

public class ExternalJWTApplicationConfiguration extends BaseIdentityProviderApplicationConfiguration implements Buildable<ExternalJWTApplicationConfiguration> {
  public boolean equals(Object paramObject) {
    return super.equals(paramObject);
  }
  
  public int hashCode() {
    return super.hashCode();
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
