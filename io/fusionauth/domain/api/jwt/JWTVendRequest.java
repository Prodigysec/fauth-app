package io.fusionauth.domain.api.jwt;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class JWTVendRequest {
  public Map<String, Object> claims = new LinkedHashMap<>();
  
  public UUID keyId;
  
  public int timeToLiveInSeconds;
}
