package io.fusionauth.api.admin;

import io.fusionauth.jwt.domain.JWT;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JWTManager {
  private static final ScheduledThreadPoolExecutor executorService;
  
  private static final Map<UUID, ZonedDateTime> revokedByApplication = new ConcurrentHashMap<>();
  
  private static final Map<UUID, Map<UUID, ZonedDateTime>> revokedByUser = new ConcurrentHashMap<>();
  
  private static final Map<UUID, ZonedDateTime> revokedRefreshTokens = new ConcurrentHashMap<>();
  
  public static boolean isValid(JWT paramJWT) {
    try {
      String str = paramJWT.getString("sid");
      if (str != null) {
        ZonedDateTime zonedDateTime = revokedRefreshTokens.get(UUID.fromString(str));
        if (zonedDateTime != null) {
          boolean bool = zonedDateTime.isBefore(paramJWT.expiration);
          if (!bool)
            return false; 
        } 
      } 
    } catch (Exception exception) {}
    try {
      String str = paramJWT.getString("applicationId");
      if (str != null) {
        ZonedDateTime zonedDateTime = revokedByApplication.get(UUID.fromString(str));
        if (zonedDateTime != null) {
          boolean bool = zonedDateTime.isBefore(paramJWT.expiration);
          if (!bool)
            return false; 
        } 
        String str1 = paramJWT.subject;
        if (str1 != null) {
          Map map = revokedByUser.get(UUID.fromString(str1));
          if (map != null) {
            zonedDateTime = (ZonedDateTime)map.get(UUID.fromString(str));
            if (zonedDateTime != null) {
              boolean bool = zonedDateTime.isBefore(paramJWT.expiration);
              if (!bool)
                return false; 
            } 
          } 
        } 
      } 
    } catch (Exception exception) {}
    return true;
  }
  
  public static void reset() {
    revokedByApplication.clear();
    revokedByUser.clear();
    revokedRefreshTokens.clear();
  }
  
  public static void revokeByApplication(UUID paramUUID, int paramInt) {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(paramInt);
    revokedByApplication.put(paramUUID, zonedDateTime);
  }
  
  public static void revokeByRefreshToken(UUID paramUUID, int paramInt) {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(paramInt);
    revokedRefreshTokens.put(paramUUID, zonedDateTime);
  }
  
  public static void revokedByUser(UUID paramUUID, Map<UUID, Integer> paramMap) {
    Map<UUID, ZonedDateTime> map = revokedByUser.computeIfAbsent(paramUUID, paramUUID -> new HashMap<>());
    for (UUID uUID : paramMap.keySet()) {
      ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(((Integer)paramMap.get(uUID)).intValue());
      map.put(uUID, zonedDateTime);
    } 
  }
  
  static {
    executorService = new ScheduledThreadPoolExecutor(1, paramRunnable -> {
          Thread thread = new Thread(paramRunnable);
          thread.setName("JWTManager Thread");
          thread.setDaemon(true);
          return thread;
        });
    executorService.schedule(() -> {
          ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
          revokedByUser.values().forEach(());
          revokedByApplication.entrySet().removeIf(());
          revokedRefreshTokens.entrySet().removeIf(());
        }7L, TimeUnit.SECONDS);
  }
}
