package io.fusionauth.domain.lambda.parameters.mfa;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Trust {
  public final UUID applicationId;
  
  public final Map<String, String> attributes = new LinkedHashMap<>();
  
  public final ZonedDateTime expirationInstant;
  
  public final String id;
  
  public final ZonedDateTime insertInstant;
  
  public final StartInstant startInstants;
  
  public final Map<String, Object> state;
  
  public final UUID tenantId;
  
  public final UUID userId;
  
  public Trust(UUID paramUUID1, ZonedDateTime paramZonedDateTime1, String paramString, ZonedDateTime paramZonedDateTime2, StartInstant paramStartInstant, Map<String, Object> paramMap, UUID paramUUID2, UUID paramUUID3) {
    this.applicationId = paramUUID1;
    this.expirationInstant = paramZonedDateTime1;
    this.id = paramString;
    this.insertInstant = paramZonedDateTime2;
    this.startInstants = paramStartInstant;
    this.state = paramMap;
    this.tenantId = paramUUID2;
    this.userId = paramUUID3;
  }
  
  public static class StartInstant {
    public final Map<UUID, ZonedDateTime> applications;
    
    public final ZonedDateTime tenant;
    
    public StartInstant(Map<UUID, ZonedDateTime> param1Map, ZonedDateTime param1ZonedDateTime) {
      this.applications = param1Map;
      this.tenant = param1ZonedDateTime;
    }
  }
}
