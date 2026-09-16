package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class APIKey implements Buildable<APIKey> {
  public ZonedDateTime expirationInstant;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public UUID ipAccessControlListId;
  
  public String key;
  
  public boolean keyManager;
  
  public ZonedDateTime lastUpdateInstant;
  
  public APIKeyMetaData metaData;
  
  public String name;
  
  public APIKeyPermissions permissions;
  
  public boolean retrievable = true;
  
  public UUID tenantId;
  
  public APIKey(String paramString) {
    this.key = paramString;
  }
  
  public APIKey(APIKey paramAPIKey) {
    this.expirationInstant = paramAPIKey.expirationInstant;
    this.id = paramAPIKey.id;
    this.insertInstant = paramAPIKey.insertInstant;
    this.ipAccessControlListId = paramAPIKey.ipAccessControlListId;
    this.key = paramAPIKey.key;
    this.keyManager = paramAPIKey.keyManager;
    this.lastUpdateInstant = paramAPIKey.lastUpdateInstant;
    if (paramAPIKey.metaData != null)
      this.metaData = new APIKeyMetaData(paramAPIKey.metaData.attributes); 
    this.name = paramAPIKey.name;
    if (paramAPIKey.permissions != null)
      this.permissions = new APIKeyPermissions(paramAPIKey.permissions.endpoints); 
    this.retrievable = paramAPIKey.retrievable;
    this.tenantId = paramAPIKey.tenantId;
  }
  
  @JacksonConstructor
  public APIKey() {}
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    APIKey aPIKey = (APIKey)paramObject;
    return (this.keyManager == aPIKey.keyManager && this.retrievable == aPIKey.retrievable && 
      
      Objects.equals(this.expirationInstant, aPIKey.expirationInstant) && 
      Objects.equals(this.id, aPIKey.id) && 
      Objects.equals(this.insertInstant, aPIKey.insertInstant) && 
      Objects.equals(this.ipAccessControlListId, aPIKey.ipAccessControlListId) && 
      Objects.equals(this.key, aPIKey.key) && 
      Objects.equals(this.lastUpdateInstant, aPIKey.lastUpdateInstant) && 
      Objects.equals(this.metaData, aPIKey.metaData) && 
      Objects.equals(this.name, aPIKey.name) && 
      Objects.equals(this.permissions, aPIKey.permissions) && 
      Objects.equals(this.tenantId, aPIKey.tenantId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.expirationInstant, this.id, this.insertInstant, this.ipAccessControlListId, this.key, Boolean.valueOf(this.keyManager), this.lastUpdateInstant, this.metaData, this.name, this.permissions, 
          Boolean.valueOf(this.retrievable), this.tenantId });
  }
  
  public void normalize() {
    if (this.permissions != null)
      this.permissions.endpoints.remove("/api/api-key"); 
  }
  
  public APIKey secure() {
    this.key = null;
    return this;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  public static class APIKeyMetaData {
    public final Map<String, String> attributes = new HashMap<>();
    
    public APIKeyMetaData(Map<String, String> param1Map) {
      this.attributes.putAll(param1Map);
    }
    
    public APIKeyMetaData() {}
    
    public APIKeyMetaData(String param1String1, String param1String2) {
      this.attributes.put(param1String1, param1String2);
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      APIKeyMetaData aPIKeyMetaData = (APIKeyMetaData)param1Object;
      return Objects.equals(this.attributes, aPIKeyMetaData.attributes);
    }
    
    public int hashCode() {
      return this.attributes.hashCode();
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
  
  public static class APIKeyPermissions {
    public final Map<String, Set<String>> endpoints = new HashMap<>();
    
    public APIKeyPermissions(Map<String, Set<String>> param1Map) {
      param1Map.entrySet().stream().forEach(param1Entry -> this.endpoints.put((String)param1Entry.getKey(), new HashSet<>((Collection<? extends String>)param1Entry.getValue())));
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      APIKeyPermissions aPIKeyPermissions = (APIKeyPermissions)param1Object;
      return Objects.equals(this.endpoints, aPIKeyPermissions.endpoints);
    }
    
    public int hashCode() {
      return this.endpoints.hashCode();
    }
    
    public String toString() {
      return ToString.toString(this);
    }
    
    public APIKeyPermissions() {}
  }
}
