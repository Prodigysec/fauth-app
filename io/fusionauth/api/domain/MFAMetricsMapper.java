package io.fusionauth.api.domain;

import io.fusionauth.api.domain.mybatis._MFATenantMetric;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface MFAMetricsMapper {
  @Delete({"DELETE FROM mfa_metrics WHERE tenants_id = #{tenantId}"})
  void deleteMetricsByTenantId(UUID paramUUID);
  
  void incrementChallengeCount(@Param("tenantId") UUID paramUUID);
  
  void incrementFailedAttemptCount(@Param("tenantId") UUID paramUUID);
  
  void incrementSuccessCount(@Param("tenantId") UUID paramUUID);
  
  @Select({"SELECT * FROM mfa_metrics"})
  @ResultMap({"MfaTenantMetric"})
  List<_MFATenantMetric> retrieveAllMfaMetrics();
}
