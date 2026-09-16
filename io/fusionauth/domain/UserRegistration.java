package io.fusionauth.domain;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.util.Normalizer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class UserRegistration implements Buildable<UserRegistration>, JSONColumnable {
  public final Map<String, Object> data;
  
  @JSONColumn
  public final List<Locale> preferredLanguages = new ArrayList<>();
  
  @Deprecated
  @JSONColumn
  public final Map<String, String> tokens;
  
  public UUID applicationId;
  
  public String authenticationToken;
  
  public UUID cleanSpeakId;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastLoginInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  public SortedSet<String> roles = new TreeSet<>();
  
  public ZoneId timezone;
  
  public String username;
  
  public ContentStatus usernameStatus;
  
  public boolean verified;
  
  public ZonedDateTime verifiedInstant;
  
  public UserRegistration() {
    this.data = new LinkedHashMap<>();
    this.tokens = new LinkedHashMap<>();
  }
  
  public UserRegistration(Map<String, Object> paramMap) {
    this.data = paramMap;
    this.tokens = new LinkedHashMap<>();
  }
  
  public UserRegistration(UserRegistration paramUserRegistration) {
    this.applicationId = paramUserRegistration.applicationId;
    this.authenticationToken = paramUserRegistration.authenticationToken;
    this.cleanSpeakId = paramUserRegistration.cleanSpeakId;
    this.id = paramUserRegistration.id;
    this.insertInstant = paramUserRegistration.insertInstant;
    this.lastLoginInstant = paramUserRegistration.lastLoginInstant;
    this.lastUpdateInstant = paramUserRegistration.lastUpdateInstant;
    this.preferredLanguages.addAll(paramUserRegistration.preferredLanguages);
    this.roles.addAll(paramUserRegistration.roles);
    this.timezone = paramUserRegistration.timezone;
    this.username = paramUserRegistration.username;
    this.usernameStatus = paramUserRegistration.usernameStatus;
    this.verified = paramUserRegistration.verified;
    this.verifiedInstant = paramUserRegistration.verifiedInstant;
    this.data = new LinkedHashMap<>();
    if (paramUserRegistration.data != null)
      this.data.putAll(paramUserRegistration.data); 
    this.tokens = new LinkedHashMap<>();
    if (paramUserRegistration.tokens != null)
      this.tokens.putAll(paramUserRegistration.tokens); 
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof UserRegistration))
      return false; 
    UserRegistration userRegistration = (UserRegistration)paramObject;
    return (this.verified == userRegistration.verified && 
      Objects.equals(this.data, userRegistration.data) && 
      Objects.equals(this.preferredLanguages, userRegistration.preferredLanguages) && 
      Objects.equals(this.tokens, userRegistration.tokens) && 
      Objects.equals(this.applicationId, userRegistration.applicationId) && 
      Objects.equals(this.authenticationToken, userRegistration.authenticationToken) && 
      Objects.equals(this.cleanSpeakId, userRegistration.cleanSpeakId) && 
      Objects.equals(this.id, userRegistration.id) && 
      Objects.equals(this.insertInstant, userRegistration.insertInstant) && 
      Objects.equals(this.lastLoginInstant, userRegistration.lastLoginInstant) && 
      Objects.equals(this.lastUpdateInstant, userRegistration.lastUpdateInstant) && 
      Objects.equals(this.roles, userRegistration.roles) && 
      Objects.equals(this.timezone, userRegistration.timezone) && 
      Objects.equals(this.username, userRegistration.username) && this.usernameStatus == userRegistration.usernameStatus && 
      
      Objects.equals(this.verifiedInstant, userRegistration.verifiedInstant));
  }
  
  public boolean hasRegistrationData() {
    return !this.data.isEmpty();
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.data, this.preferredLanguages, this.tokens, this.applicationId, this.authenticationToken, this.cleanSpeakId, this.id, this.insertInstant, this.lastLoginInstant, this.lastUpdateInstant, 
          this.roles, this.timezone, this.username, this.usernameStatus, 












          
          Boolean.valueOf(this.verified), this.verifiedInstant });
  }
  
  public void normalize() {
    this.authenticationToken = Normalizer.trimToNull(this.authenticationToken);
    Normalizer.removeEmpty(this.preferredLanguages);
    Normalizer.deDuplicate(this.preferredLanguages);
    this.preferredLanguages.removeIf(paramLocale -> paramLocale.toString().equals(""));
    this.username = Normalizer.trim(this.username);
    Normalizer.removeEmpty(this.data);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
