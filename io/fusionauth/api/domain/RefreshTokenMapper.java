package io.fusionauth.api.domain;

import io.fusionauth.api.domain.mybatis._RefreshToken;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.domain.jwt.RefreshToken;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface RefreshTokenMapper {
  void create(@Param("refreshToken") RefreshTokenService.HashedRefreshToken paramHashedRefreshToken);
  
  void createBulk(@Param("refreshTokens") Collection<RefreshTokenService.HashedRefreshToken> paramCollection);
  
  int delete(@Param("hash") String paramString1, @Param("token") String paramString2);
  
  @Delete({"DELETE FROM refresh_tokens WHERE applications_id = #{applicationId}"})
  int deleteByApplicationId(@Param("applicationId") UUID paramUUID);
  
  @Delete({"DELETE FROM refresh_tokens WHERE id = #{tokenId}"})
  void deleteById(@Param("tokenId") UUID paramUUID);
  
  @Delete({"DELETE FROM refresh_tokens WHERE applications_id = #{applicationId} AND users_id = #{userId}"})
  int deleteByUserAndApplicationId(@Param("userId") UUID paramUUID1, @Param("applicationId") UUID paramUUID2);
  
  @Delete({"DELETE FROM refresh_tokens WHERE users_id = #{userId}"})
  int deleteByUserId(@Param("userId") UUID paramUUID);
  
  @Delete({"DELETE FROM refresh_tokens WHERE applications_id = #{applicationId} AND start_instant < #{cutoff}"})
  int deleteOlderThanForApplicationId(@Param("applicationId") UUID paramUUID, @Param("cutoff") long paramLong);
  
  @Delete({"DELETE FROM refresh_tokens WHERE tenants_id = #{tenantId} AND applications_id IS NULL AND start_instant < #{cutoff}"})
  int deleteSingleSignOnTokensOlderThan(@Param("tenantId") UUID paramUUID, @Param("cutoff") long paramLong);
  
  @Delete({"DELETE FROM refresh_tokens WHERE tenants_id IS NULL AND applications_id IS NULL AND start_instant < #{cutoff}"})
  int legacyDeleteSingleSignOnTokensOlderThan(@Param("cutoff") long paramLong);
  
  RefreshToken retrieve(@Param("hash") String paramString1, @Param("token") String paramString2);
  
  Set<UUID> retrieveApplicationIdsByUserId(@Param("userId") UUID paramUUID);
  
  List<RefreshToken> retrieveByApplicationId(@Param("applicationId") UUID paramUUID);
  
  RefreshToken retrieveById(@Param("tokenId") UUID paramUUID);
  
  List<RefreshToken> retrieveByUserId(@Param("userId") UUID paramUUID, @Param("numberOfResults") int paramInt);
  
  List<RefreshToken> retrieveByUserIdAndApplicationId(@Param("userId") UUID paramUUID1, @Param("applicationId") UUID paramUUID2);
  
  _RefreshToken retrieveForUpdateUsingSeed(@Param("hash") String paramString1, @Param("token") String paramString2);
  
  _RefreshToken retrieveUsingSeed(@Param("hash") String paramString1, @Param("token") String paramString2);
  
  void update(@Param("refreshToken") RefreshTokenService.HashedRefreshToken paramHashedRefreshToken, @Param("keepLastHash") boolean paramBoolean);
}
