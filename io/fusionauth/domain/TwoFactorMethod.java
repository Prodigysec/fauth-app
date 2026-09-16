package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.api.domain.json.annotation.MaskString;
import java.util.Objects;

public class TwoFactorMethod implements Buildable<TwoFactorMethod> {
  public static final String Authenticator = "authenticator";
  
  public static final String Email = "email";
  
  public static final int MaximumNameLength = 256;
  
  public static final String RecoveryCode = "recoveryCode";
  
  public static final String SMS = "sms";
  
  public AuthenticatorConfiguration authenticator;
  
  public String email;
  
  public String id;
  
  public Boolean lastUsed;
  
  public String method;
  
  public String mobilePhone;
  
  public String name;
  
  @MaskString
  public String secret;
  
  @JacksonConstructor
  public TwoFactorMethod() {}
  
  public TwoFactorMethod(String paramString) {
    this.method = paramString;
  }
  
  public TwoFactorMethod(TwoFactorMethod paramTwoFactorMethod) {
    if (paramTwoFactorMethod.authenticator != null)
      this.authenticator = new AuthenticatorConfiguration(paramTwoFactorMethod.authenticator); 
    this.email = paramTwoFactorMethod.email;
    this.id = paramTwoFactorMethod.id;
    this.lastUsed = paramTwoFactorMethod.lastUsed;
    this.method = paramTwoFactorMethod.method;
    this.mobilePhone = paramTwoFactorMethod.mobilePhone;
    this.name = paramTwoFactorMethod.name;
    this.secret = paramTwoFactorMethod.secret;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    TwoFactorMethod twoFactorMethod = (TwoFactorMethod)paramObject;
    return (Objects.equals(this.authenticator, twoFactorMethod.authenticator) && Objects.equals(this.email, twoFactorMethod.email) && Objects.equals(this.id, twoFactorMethod.id) && Objects.equals(this.lastUsed, twoFactorMethod.lastUsed) && Objects.equals(this.method, twoFactorMethod.method) && Objects.equals(this.mobilePhone, twoFactorMethod.mobilePhone) && Objects.equals(this.name, twoFactorMethod.name) && Objects.equals(this.secret, twoFactorMethod.secret));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.authenticator, this.email, this.id, this.lastUsed, this.method, this.mobilePhone, this.name, this.secret });
  }
  
  public void normalize() {
    if (this.method != null)
      switch (this.method) {
        case "authenticator":
          this.email = null;
          this.mobilePhone = null;
          break;
        case "email":
          this.mobilePhone = null;
          this.secret = null;
          break;
        case "sms":
          this.email = null;
          this.secret = null;
          break;
      }  
  }
  
  public TwoFactorMethod secure() {
    this.secret = null;
    return this;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
