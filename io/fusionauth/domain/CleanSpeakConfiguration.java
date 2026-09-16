package io.fusionauth.domain;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.OptBoolean;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.util.Normalizer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CleanSpeakConfiguration extends Enableable implements Buildable<CleanSpeakConfiguration>, Integration {
  @MaskString
  public String apiKey;
  
  @JsonMerge(OptBoolean.FALSE)
  public List<UUID> applicationIds = new ArrayList<>();
  
  public URI url;
  
  public UsernameModeration usernameModeration = new UsernameModeration();
  
  @JacksonConstructor
  public CleanSpeakConfiguration() {}
  
  public CleanSpeakConfiguration(CleanSpeakConfiguration paramCleanSpeakConfiguration) {
    this.apiKey = paramCleanSpeakConfiguration.apiKey;
    this.applicationIds.addAll(paramCleanSpeakConfiguration.applicationIds);
    this.enabled = paramCleanSpeakConfiguration.enabled;
    this.url = paramCleanSpeakConfiguration.url;
    this.usernameModeration = new UsernameModeration(paramCleanSpeakConfiguration.usernameModeration);
  }
  
  public CleanSpeakConfiguration(String paramString, URI paramURI, UsernameModeration paramUsernameModeration, UUID... paramVarArgs) {
    this.apiKey = paramString;
    this.url = paramURI;
    this.usernameModeration = paramUsernameModeration;
    Collections.addAll(this.applicationIds, paramVarArgs);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    CleanSpeakConfiguration cleanSpeakConfiguration = (CleanSpeakConfiguration)paramObject;
    return (Objects.equals(this.apiKey, cleanSpeakConfiguration.apiKey) && Objects.equals(this.applicationIds, cleanSpeakConfiguration.applicationIds) && Objects.equals(this.url, cleanSpeakConfiguration.url) && Objects.equals(this.usernameModeration, cleanSpeakConfiguration.usernameModeration));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.apiKey, this.applicationIds, this.url, this.usernameModeration });
  }
  
  public void normalize() {
    this.apiKey = Normalizer.trim(this.apiKey);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  public static class UsernameModeration extends Enableable implements Buildable<UsernameModeration> {
    public UUID applicationId;
    
    @JacksonConstructor
    public UsernameModeration() {}
    
    public UsernameModeration(UsernameModeration param1UsernameModeration) {
      this.applicationId = param1UsernameModeration.applicationId;
      this.enabled = param1UsernameModeration.enabled;
    }
    
    public UsernameModeration(UUID param1UUID, boolean param1Boolean) {
      this.applicationId = param1UUID;
      this.enabled = param1Boolean;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      UsernameModeration usernameModeration = (UsernameModeration)param1Object;
      return (super.equals(param1Object) && 
        Objects.equals(this.applicationId, usernameModeration.applicationId));
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationId });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
}
