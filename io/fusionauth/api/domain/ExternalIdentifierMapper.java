package io.fusionauth.api.domain;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;

public interface ExternalIdentifierMapper {
  void create(ExternalIdentifier paramExternalIdentifier);
  
  void deleteByApplicationId(@Param("applicationId") UUID paramUUID);
  
  void deleteByApplicationIdAndType(@Param("applicationId") UUID paramUUID, @Param("type") ExternalIdentifier.ExternalIdType paramExternalIdType);
  
  int deleteByIds(@Param("externalIds") List<String> paramList);
  
  int deleteByUserId(@Param("userId") UUID paramUUID);
  
  int deleteByUserIdAndType(@Param("userId") UUID paramUUID, @Param("excludeId") String paramString, @Param("type") ExternalIdentifier.ExternalIdType... paramVarArgs);
  
  void deleteByUserIdApplicationIdAndType(@Param("userId") UUID paramUUID1, @Param("applicationId") UUID paramUUID2, @Param("type") ExternalIdentifier.ExternalIdType paramExternalIdType);
  
  int deleteExpired(@Param("now") ZonedDateTime paramZonedDateTime);
  
  int deleteOlderThan(@Param("tenantId") UUID paramUUID, @Param("type") ExternalIdentifier.ExternalIdType paramExternalIdType, @Param("cutoff") long paramLong);
  
  List<ExternalIdentifier> retrieveByApplicationId(@Param("applicationId") UUID paramUUID);
  
  ExternalIdentifier retrieveById(@Param("externalId") String paramString);
  
  ExternalIdentifier retrieveByIdForUpdate(@Param("externalId") String paramString);
  
  ExternalIdentifier retrieveByType(@Param("tenantId") UUID paramUUID, @Param("externalId") String paramString, @Param("type") ExternalIdentifier.ExternalIdType paramExternalIdType);
  
  List<ExternalIdentifier> retrieveByUserIdAndTypes(@Param("userId") UUID paramUUID, @Param("types") ExternalIdentifier.ExternalIdType... paramVarArgs);
  
  List<ExternalIdentifier> retrieveByUserIdTypeAndApplicationId(@Param("userId") UUID paramUUID1, @Param("applicationId") UUID paramUUID2, @Param("type") ExternalIdentifier.ExternalIdType paramExternalIdType);
  
  List<ExternalIdentifier> retrieveListByType(@Param("tenantId") UUID paramUUID, @Param("type") ExternalIdentifier.ExternalIdType paramExternalIdType);
  
  int update(ExternalIdentifier paramExternalIdentifier);
}
