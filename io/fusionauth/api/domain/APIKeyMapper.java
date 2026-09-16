package io.fusionauth.api.domain;

import com.inversoft.authentication.api.domain.AuthenticationKey;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;

public interface APIKeyMapper {
  List<AuthenticationKey> retrieveByCriteria(@Param("nameOrDescription") String paramString1, @Param("key") String paramString2, @Param("tenantId") UUID paramUUID, @Param("keyManager") Boolean paramBoolean, @Param("expirationStart") ZonedDateTime paramZonedDateTime1, @Param("expirationEnd") ZonedDateTime paramZonedDateTime2, @Param("orderBy") String paramString3, @Param("numberOfResults") int paramInt1, @Param("startRow") int paramInt2);
  
  int retrieveCountByCriteria(@Param("nameOrDescription") String paramString1, @Param("key") String paramString2, @Param("tenantId") UUID paramUUID, @Param("keyManager") Boolean paramBoolean, @Param("expirationStart") ZonedDateTime paramZonedDateTime1, @Param("expirationEnd") ZonedDateTime paramZonedDateTime2);
}
