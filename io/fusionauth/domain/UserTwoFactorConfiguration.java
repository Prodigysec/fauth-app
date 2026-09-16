package io.fusionauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.api.domain.annotation.InternalUse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(value = {"enabled"}, allowGetters = true, allowSetters = false)
public class UserTwoFactorConfiguration implements Buildable<UserTwoFactorConfiguration> {
  public final List<TwoFactorMethod> methods = new ArrayList<>();
  
  public final List<String> recoveryCodes = new ArrayList<>();
  
  @InternalUse
  public String recoveryCodeEncryptionScheme;
  
  @InternalUse
  public Integer recoveryCodeWorkFactor;
  
  public UserTwoFactorConfiguration(UserTwoFactorConfiguration paramUserTwoFactorConfiguration) {
    paramUserTwoFactorConfiguration.methods.forEach(paramTwoFactorMethod -> this.methods.add(new TwoFactorMethod(paramTwoFactorMethod)));
    this.recoveryCodes.addAll(paramUserTwoFactorConfiguration.recoveryCodes);
    this.recoveryCodeEncryptionScheme = paramUserTwoFactorConfiguration.recoveryCodeEncryptionScheme;
    this.recoveryCodeWorkFactor = paramUserTwoFactorConfiguration.recoveryCodeWorkFactor;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    UserTwoFactorConfiguration userTwoFactorConfiguration = (UserTwoFactorConfiguration)paramObject;
    return (Objects.equals(this.methods, userTwoFactorConfiguration.methods) && 
      Objects.equals(this.recoveryCodes, userTwoFactorConfiguration.recoveryCodes) && 
      Objects.equals(this.recoveryCodeEncryptionScheme, userTwoFactorConfiguration.recoveryCodeEncryptionScheme) && 
      Objects.equals(this.recoveryCodeWorkFactor, userTwoFactorConfiguration.recoveryCodeWorkFactor));
  }
  
  @JsonIgnore
  public TwoFactorMethod getLastUsedMethod() {
    if (this.methods.size() == 1)
      return this.methods.get(0); 
    return this.methods.stream().filter(paramTwoFactorMethod -> (paramTwoFactorMethod.lastUsed != null && paramTwoFactorMethod.lastUsed.booleanValue())).findFirst().orElse(null);
  }
  
  @JsonIgnore
  public TwoFactorMethod getMethodById(String paramString) {
    return this.methods.stream()
      .filter(paramTwoFactorMethod -> (paramTwoFactorMethod.id != null))
      .filter(paramTwoFactorMethod -> paramTwoFactorMethod.id.equals(paramString))
      .findFirst()
      .orElse(null);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.methods, this.recoveryCodes, this.recoveryCodeEncryptionScheme, this.recoveryCodeWorkFactor });
  }
  
  public UserTwoFactorConfiguration secure() {
    this.recoveryCodes.clear();
    this.recoveryCodeWorkFactor = null;
    this.recoveryCodeEncryptionScheme = null;
    this.methods.forEach(TwoFactorMethod::secure);
    return this;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  @JacksonConstructor
  public UserTwoFactorConfiguration() {}
}
