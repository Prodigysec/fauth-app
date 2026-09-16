package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class RefreshTokenRevocationPolicy implements Buildable<RefreshTokenRevocationPolicy> {
  public boolean onLoginPrevented;
  
  public boolean onMultiFactorEnable;
  
  public boolean onOneTimeTokenReuse;
  
  public boolean onPasswordChanged;
  
  @JacksonConstructor
  public RefreshTokenRevocationPolicy() {}
  
  public RefreshTokenRevocationPolicy(RefreshTokenRevocationPolicy paramRefreshTokenRevocationPolicy) {
    this.onLoginPrevented = paramRefreshTokenRevocationPolicy.onLoginPrevented;
    this.onMultiFactorEnable = paramRefreshTokenRevocationPolicy.onMultiFactorEnable;
    this.onPasswordChanged = paramRefreshTokenRevocationPolicy.onPasswordChanged;
    this.onOneTimeTokenReuse = paramRefreshTokenRevocationPolicy.onOneTimeTokenReuse;
  }
  
  public RefreshTokenRevocationPolicy(boolean paramBoolean1, boolean paramBoolean2) {
    this.onLoginPrevented = paramBoolean1;
    this.onPasswordChanged = paramBoolean2;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    RefreshTokenRevocationPolicy refreshTokenRevocationPolicy = (RefreshTokenRevocationPolicy)paramObject;
    return (this.onLoginPrevented == refreshTokenRevocationPolicy.onLoginPrevented && this.onMultiFactorEnable == refreshTokenRevocationPolicy.onMultiFactorEnable && this.onPasswordChanged == refreshTokenRevocationPolicy.onPasswordChanged && this.onOneTimeTokenReuse == refreshTokenRevocationPolicy.onOneTimeTokenReuse);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Boolean.valueOf(this.onLoginPrevented), Boolean.valueOf(this.onMultiFactorEnable), Boolean.valueOf(this.onPasswordChanged), Boolean.valueOf(this.onOneTimeTokenReuse) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
