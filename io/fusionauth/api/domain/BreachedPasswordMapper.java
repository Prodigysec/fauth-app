package io.fusionauth.api.domain;

import io.fusionauth.api.domain.mybatis._BreachedPasswordTenantMetric;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface BreachedPasswordMapper {
  void createCommonPassword(String paramString);
  
  void deleteAllCommonPasswords();
  
  @Delete({"DELETE FROM breached_password_metrics WHERE tenants_id = #{tenantId}"})
  void deleteMetricsByTenantId(UUID paramUUID);
  
  void incrementBreachedCommonPasswordMatch(UUID paramUUID);
  
  void incrementBreachedExactMatch(@Param("tenantId") UUID paramUUID);
  
  void incrementBreachedNoMatch(@Param("tenantId") UUID paramUUID);
  
  void incrementBreachedPasswordMatch(UUID paramUUID);
  
  void incrementBreachedSubAddressMatch(UUID paramUUID);
  
  @Select({"SELECT * FROM breached_password_metrics"})
  @ResultMap({"BreachedPasswordTenantMetric"})
  List<_BreachedPasswordTenantMetric> retrieveAllBreachedMetrics();
  
  List<String> retrieveAllCommonPasswords();
  
  String retrieveCommonPassword(String paramString);
  
  void upsertCommonPasswords(@Param("passwords") List<String> paramList);
}
