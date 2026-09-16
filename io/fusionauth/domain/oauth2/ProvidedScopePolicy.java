package io.fusionauth.domain.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Requirable;
import java.util.Objects;

public class ProvidedScopePolicy implements Buildable<ProvidedScopePolicy> {
  public Requirable address = new Requirable(true, false);
  
  public Requirable email = new Requirable(true, false);
  
  public Requirable phone = new Requirable(true, false);
  
  public Requirable profile = new Requirable(true, false);
  
  @JacksonConstructor
  public ProvidedScopePolicy() {}
  
  public ProvidedScopePolicy(ProvidedScopePolicy paramProvidedScopePolicy) {
    this.address = new Requirable(paramProvidedScopePolicy.address);
    this.email = new Requirable(paramProvidedScopePolicy.email);
    this.phone = new Requirable(paramProvidedScopePolicy.phone);
    this.profile = new Requirable(paramProvidedScopePolicy.profile);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof ProvidedScopePolicy))
      return false; 
    ProvidedScopePolicy providedScopePolicy = (ProvidedScopePolicy)paramObject;
    return (Objects.equals(this.address, providedScopePolicy.address) && 
      Objects.equals(this.email, providedScopePolicy.email) && 
      Objects.equals(this.phone, providedScopePolicy.phone) && 
      Objects.equals(this.profile, providedScopePolicy.profile));
  }
  
  @JsonIgnore
  public Requirable getScopePolicy(String paramString) {
    switch (paramString) {
      case "address":
        return this.address;
      case "email":
        return this.email;
      case "phone":
        return this.phone;
      case "profile":
        return this.profile;
    } 
    return null;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.address, this.email, this.phone, this.profile });
  }
}
