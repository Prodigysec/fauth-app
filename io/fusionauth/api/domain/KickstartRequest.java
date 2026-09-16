package io.fusionauth.api.domain;

import com.fasterxml.jackson.databind.JsonNode;
import io.fusionauth.domain.util.HTTPMethod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class KickstartRequest<T> {
  public List<KickstartAPIKey> apiKeys = new ArrayList<>();
  
  public String license;
  
  public String licenseId;
  
  public List<T> requests = new ArrayList<>();
  
  public Settings settings = new Settings();
  
  public Map<String, String> variables = new HashMap<>();
  
  @Deprecated
  public void setSupportId(String paramString) {
    this.licenseId = paramString;
  }
  
  public static class APIKeyPermissions {
    public Map<String, Set<String>> endpoints = new HashMap<>();
  }
  
  public static class APIRequest {
    public JsonNode body;
    
    public String contentType;
    
    public HTTPMethod method;
    
    public UUID tenantId;
    
    public String url;
  }
  
  public static class KickstartAPIKey {
    public String description;
    
    public String id;
    
    public String ipAccessControlListId;
    
    public String key;
    
    public boolean keyManager;
    
    public KickstartRequest.APIKeyPermissions permissions;
    
    public String tenantId;
  }
  
  public static class Settings {
    public String connectTimeout = "5000";
    
    public String readTimeout = "5000";
  }
}
