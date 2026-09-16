package io.fusionauth.api.util;

import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.jwt.domain.JWT;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;

public class ClaimTools {
  public static boolean audienceContainsClientId(Object paramObject, String paramString) {
    if (paramObject instanceof List) {
      List list = (List)paramObject;
      return list.contains(paramString);
    } 
    return paramString.equals(paramObject);
  }
  
  public static Optional<UUID> getAppIdFromAudClaim(@Nonnull JWT paramJWT) {
    return Optional.ofNullable(resolveApplicationId(paramJWT));
  }
  
  public static Optional<UUID> getAppIdFromKidClaim(@Nonnull JWT paramJWT) {
    V v = (paramJWT.header == null) ? null : (V)paramJWT.header.properties.get("kid");
    String str = (String)v;
    return (v instanceof String) ? Optional.<UUID>ofNullable(UUIDTools.fromString(str)) : Optional.<UUID>empty();
  }
  
  public static String getAudience(JWT paramJWT) {
    Object object = paramJWT.audience;
    if (object instanceof String)
      return (String)object; 
    if (object instanceof List) {
      List list = (List)object;
      return (String)list.getFirst();
    } 
    return null;
  }
  
  public static GrantType getPrimaryGrantType(JWT paramJWT) {
    Object object = paramJWT.getObject("gty");
    if (object instanceof List) {
      List list = (List)object;
      if (list.size() == 1)
        return GrantType.forValue((String)list.getFirst()); 
    } 
    return null;
  }
  
  public static Optional<UUID> getTenantIdFromTidClaim(@Nonnull JWT paramJWT) {
    return Optional.ofNullable(resolveTenantId(paramJWT));
  }
  
  public static UUID resolveApplicationId(JWT paramJWT) {
    UUID uUID = null;
    Object object = paramJWT.getObject("aud");
    if (object instanceof String) {
      String str = (String)object;
      uUID = UUIDTools.fromString(str);
    } else if (object instanceof List) {
      List list = (List)object;
      if (!list.isEmpty()) {
        Object object1 = list.getFirst();
        if (object1 instanceof String) {
          String str = (String)object1;
          uUID = UUIDTools.fromString(str);
        } 
      } 
    } 
    return uUID;
  }
  
  public static UUID resolveTenantId(JWT paramJWT) {
    Object object = paramJWT.getObject("tid");
    String str = (String)object;
    return (object instanceof String) ? UUIDTools.fromString(str) : null;
  }
  
  public static UUID resolveUserId(JWT paramJWT) {
    String str = paramJWT.getString("fa_uid");
    if (str != null) {
      UUID uUID = UUIDTools.fromString(str);
      if (uUID != null)
        return uUID; 
    } 
    return UUIDTools.fromString(paramJWT.subject);
  }
}
