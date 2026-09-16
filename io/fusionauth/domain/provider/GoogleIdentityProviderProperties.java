package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;

public class GoogleIdentityProviderProperties implements Buildable<GoogleIdentityProviderProperties> {
  public String api;
  
  public String button;
  
  public GoogleIdentityProviderProperties() {}
  
  public GoogleIdentityProviderProperties(String paramString1, String paramString2) {
    this.api = paramString1;
    this.button = paramString2;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    GoogleIdentityProviderProperties googleIdentityProviderProperties = (GoogleIdentityProviderProperties)paramObject;
    return (Objects.equals(this.api, googleIdentityProviderProperties.api) && Objects.equals(this.button, googleIdentityProviderProperties.button));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.api, this.button });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
