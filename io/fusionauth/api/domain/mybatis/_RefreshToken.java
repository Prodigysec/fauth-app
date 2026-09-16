package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.jwt.RefreshToken;

public class _RefreshToken extends RefreshToken {
  public static final String LastRotated = "lastRotated";
  
  public static final String Version = "v";
  
  @JsonIgnore
  public boolean hashN1;
  
  @JsonIgnore
  public String requestedToken;
  
  @JsonIgnore
  public boolean seedOnly;
  
  @JsonIgnore
  public String tokenHash;
  
  @JsonIgnore
  public String tokenHashN1;
  
  @JsonIgnore
  public String tokenSeed;
  
  public _RefreshToken() {}
  
  public _RefreshToken(RefreshToken paramRefreshToken) {
    super(paramRefreshToken);
  }
  
  public void generateSyntheticFields(String paramString1, String paramString2) {
    boolean bool1 = (this.tokenHash != null && paramString1.equals(this.tokenHash.trim())) ? true : false;
    boolean bool2 = (this.tokenHashN1 != null && paramString1.equals(this.tokenHashN1.trim())) ? true : false;
    this.hashN1 = bool2;
    this.seedOnly = (!bool1 && !bool2);
    this.requestedToken = paramString2;
  }
  
  @JsonIgnore
  public boolean isVersion(int paramInt) {
    Object object = this.data.get("v");
    if (object instanceof Number) {
      Number number = (Number)object;
      return (number.intValue() == paramInt);
    } 
    return false;
  }
  
  @JsonIgnore
  public void setVersion(int paramInt) {
    this.data.put("v", Integer.valueOf(paramInt));
  }
}
