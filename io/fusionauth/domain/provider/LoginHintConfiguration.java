package io.fusionauth.domain.provider;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Enableable;
import java.util.Objects;

public class LoginHintConfiguration extends Enableable implements Buildable<LoginHintConfiguration> {
  public String parameterName = "login_hint";
  
  @JacksonConstructor
  public LoginHintConfiguration() {}
  
  public LoginHintConfiguration(boolean paramBoolean) {
    this.enabled = paramBoolean;
  }
  
  public LoginHintConfiguration(LoginHintConfiguration paramLoginHintConfiguration) {
    this.enabled = paramLoginHintConfiguration.enabled;
    this.parameterName = paramLoginHintConfiguration.parameterName;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    LoginHintConfiguration loginHintConfiguration = (LoginHintConfiguration)paramObject;
    return Objects.equals(this.parameterName, loginHintConfiguration.parameterName);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.parameterName });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
