package io.fusionauth.api.domain;

import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.search.AuditLogSearchCriteria;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AuditLogMapper {
  public static final String SELECT_AUDIT_LOGS = "SELECT id, tenants_id, insert_instant, insert_user, message, data FROM audit_logs %s ORDER BY insert_instant DESC";
  
  @Insert({"INSERT INTO audit_logs (data, insert_instant, insert_user, message, tenants_id) VALUES (#{dataToDatabase}, #{insertInstant}, #{insertUser}, #{message}, #{tenantId})"})
  @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
  int create(AuditLog paramAuditLog);
  
  @Delete({"DELETE FROM audit_logs WHERE insert_instant < #{cutoff}"})
  int deleteOlderThan(ZonedDateTime paramZonedDateTime);
  
  List<AuditLog> retrieveByCriteria(AuditLogSearchCriteria paramAuditLogSearchCriteria);
  
  AuditLog retrieveById(@Nullable @Param("tenantId") UUID paramUUID, @Param("id") int paramInt);
  
  int retrieveCountByCriteria(AuditLogSearchCriteria paramAuditLogSearchCriteria);
  
  @Select({"SELECT insert_instant FROM audit_logs WHERE insert_instant < #{cutoff} ORDER BY insert_instant LIMIT 1 OFFSET #{offset}"})
  ZonedDateTime retrieveEndOffsetTime(@Param("offset") int paramInt, @Param("cutoff") ZonedDateTime paramZonedDateTime);
}
