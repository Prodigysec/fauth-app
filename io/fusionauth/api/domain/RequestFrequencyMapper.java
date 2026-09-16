package io.fusionauth.api.domain;

import io.fusionauth.domain.RateLimitedRequestType;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface RequestFrequencyMapper {
  void createRequestFrequencyRecord(@Param("record") RequestFrequencyRecord paramRequestFrequencyRecord);
  
  @Delete({"DELETE FROM request_frequencies WHERE tenants_id = #{tenantId} AND type = #{type} AND request_id = #{requestId}"})
  void deleteRequestFrequencyRecordsByUserId(@Param("tenantId") UUID paramUUID, @Param("type") RateLimitedRequestType paramRateLimitedRequestType, @Param("requestId") String paramString);
  
  @Delete({"DELETE FROM request_frequencies WHERE tenants_id = #{tenantId} AND type = #{type} AND last_update_instant < #{cutoff}"})
  int deleteRequestFrequencyRecordsOlderThan(@Param("tenantId") UUID paramUUID, @Param("type") RateLimitedRequestType paramRateLimitedRequestType, @Param("cutoff") long paramLong);
  
  RequestFrequencyRecord retrieveRequestFrequencyRecord(@Param("tenantId") UUID paramUUID, @Param("type") RateLimitedRequestType paramRateLimitedRequestType, @Param("requestId") String paramString);
  
  RequestFrequencyRecord retrieveRequestFrequencyRecordForUserAndLock(@Param("tenantId") UUID paramUUID, @Param("type") RateLimitedRequestType paramRateLimitedRequestType, @Param("requestId") String paramString);
  
  RequestFrequencyRecord retrieveRequestFrequencyRecordNewerThan(@Param("tenantId") UUID paramUUID, @Param("type") RateLimitedRequestType paramRateLimitedRequestType, @Param("requestId") String paramString, @Param("cutoff") long paramLong);
  
  void updateRequestFrequencyRecord(@Param("record") RequestFrequencyRecord paramRequestFrequencyRecord);
  
  void upsertRequestFrequencyRecord(@Param("record") RequestFrequencyRecord paramRequestFrequencyRecord, @Param("timePeriodInSeconds") int paramInt);
}
