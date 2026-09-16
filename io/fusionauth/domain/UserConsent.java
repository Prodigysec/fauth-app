package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class UserConsent implements Buildable<UserConsent>, JSONColumnable {
  public final Map<String, Object> data = new LinkedHashMap<>();
  
  public Consent consent;
  
  public UUID consentId;
  
  public UUID giverUserId;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  @JSONColumn
  public ConsentStatus status;
  
  public UUID userId;
  
  @JSONColumn
  public List<String> values = new ArrayList<>();
  
  public UserConsent(UserConsent paramUserConsent) {
    this.data.putAll(paramUserConsent.data);
    this.consent = paramUserConsent.consent;
    this.consentId = paramUserConsent.consentId;
    this.giverUserId = paramUserConsent.giverUserId;
    this.id = paramUserConsent.id;
    this.insertInstant = paramUserConsent.insertInstant;
    this.lastUpdateInstant = paramUserConsent.lastUpdateInstant;
    this.status = paramUserConsent.status;
    this.userId = paramUserConsent.userId;
    if (paramUserConsent.values != null)
      this.values = paramUserConsent.values; 
  }
  
  @JacksonConstructor
  public UserConsent() {}
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof UserConsent))
      return false; 
    UserConsent userConsent = (UserConsent)paramObject;
    return (Objects.equals(this.consent, userConsent.consent) && 
      Objects.equals(this.consentId, userConsent.consentId) && 
      Objects.equals(this.data, userConsent.data) && 
      Objects.equals(this.giverUserId, userConsent.giverUserId) && 
      Objects.equals(this.id, userConsent.id) && 
      Objects.equals(this.insertInstant, userConsent.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, userConsent.lastUpdateInstant) && this.status == userConsent.status && 
      
      Objects.equals(this.userId, userConsent.userId) && 
      Objects.equals(this.values, userConsent.values));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.consent, this.consentId, this.data, this.giverUserId, this.id, this.insertInstant, this.lastUpdateInstant, this.status, this.userId, this.values });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
