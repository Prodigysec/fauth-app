package io.fusionauth.api.util;

import com.inversoft.util.StringTools;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.ExpiryUnit;
import io.fusionauth.http.server.HTTPRequest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ActionTools {
  public static String bearer = "bearer ";
  
  public static Optional<String> extractBearerTokenFromAuthorizationHeader(String paramString) {
    if (StringTools.isNotBlank(paramString) && paramString.toLowerCase().startsWith(bearer)) {
      String str = paramString.substring(bearer.length());
      return StringTools.isNotBlank(str) ? Optional.<String>of(str) : Optional.<String>empty();
    } 
    return Optional.empty();
  }
  
  public static ZonedDateTime getExpirationFrom(long paramLong, ExpiryUnit paramExpiryUnit) {
    Objects.requireNonNull(paramExpiryUnit);
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    switch (paramExpiryUnit) {
      default:
        throw new MatchException(null, null);
      case MINUTES:
      
      case HOURS:
      
      case DAYS:
      
      case WEEKS:
      
      case MONTHS:
      
      case YEARS:
        break;
    } 
    return 




      
      zonedDateTime.plusYears(paramLong);
  }
  
  public static Map<String, String> keyValueCollectionsToMap(List<String> paramList1, List<String> paramList2) {
    LinkedHashMap<Object, Object> linkedHashMap = new LinkedHashMap<>();
    for (byte b = 0; b < paramList1.size(); b++) {
      String str1 = paramList1.get(b);
      String str2 = (b < paramList2.size()) ? paramList2.get(b) : null;
      if (str1 != null)
        linkedHashMap.put(str1, (str2 != null) ? str2 : ""); 
    } 
    return (Map)linkedHashMap;
  }
  
  public static Optional<UUID> resolveTenantIdFromHeader(HTTPRequest paramHTTPRequest) {
    return Optional.<String>ofNullable(paramHTTPRequest.getHeader(FusionAuthClient.TENANT_ID_HEADER))
      .map(StringTools::parseUUID);
  }
}
