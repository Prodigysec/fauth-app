package io.fusionauth.api.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.User;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class PreviousPassword implements Buildable<PreviousPassword> {
  public String encryptionScheme;
  
  public Integer factor;
  
  public ZonedDateTime insertInstant;
  
  public String password;
  
  public String salt;
  
  public UUID userId;
  
  @JacksonConstructor
  public PreviousPassword() {}
  
  public PreviousPassword(User paramUser) {
    this.encryptionScheme = paramUser.encryptionScheme;
    this.insertInstant = paramUser.passwordLastUpdateInstant;
    this.factor = paramUser.factor;
    this.password = paramUser.password;
    this.salt = paramUser.salt;
    this.userId = paramUser.id;
  }
  
  public PreviousPassword(ZonedDateTime paramZonedDateTime, String paramString1, Integer paramInteger, String paramString2, String paramString3, UUID paramUUID) {
    this.encryptionScheme = paramString1;
    this.insertInstant = paramZonedDateTime;
    this.factor = paramInteger;
    this.password = paramString2;
    this.salt = paramString3;
    this.userId = paramUUID;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    PreviousPassword previousPassword = (PreviousPassword)paramObject;
    return (Objects.equals(this.encryptionScheme, previousPassword.encryptionScheme) && 
      Objects.equals(this.factor, previousPassword.factor) && 
      Objects.equals(this.insertInstant, previousPassword.insertInstant) && 
      Objects.equals(this.password, previousPassword.password) && 
      Objects.equals(this.salt, previousPassword.salt) && 
      Objects.equals(this.userId, previousPassword.userId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.encryptionScheme, this.factor, this.insertInstant, this.password, this.salt, this.userId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
