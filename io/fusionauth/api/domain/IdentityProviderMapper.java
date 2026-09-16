package io.fusionauth.api.domain;

import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.BaseIdentityProviderApplicationConfiguration;
import io.fusionauth.domain.provider.IdentityProviderTenantConfiguration;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.search.IdentityProviderSearchCriteria;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface IdentityProviderMapper {
  void create(BaseIdentityProvider<?> paramBaseIdentityProvider);
  
  void createApplicationConfiguration(@Param("id") UUID paramUUID, @Param("applicationConfigurations") Map<UUID, ? extends BaseIdentityProviderApplicationConfiguration> paramMap, @Param("type") IdentityProviderType paramIdentityProviderType);
  
  void createFederatedDomains(@Param("id") UUID paramUUID1, @Param("domains") Collection<String> paramCollection, @Nullable @Param("tenantId") UUID paramUUID2);
  
  void createTenantConfiguration(@Param("id") UUID paramUUID, @Param("tenantConfigurations") Map<UUID, IdentityProviderTenantConfiguration> paramMap);
  
  void createVerificationKeys(@Param("identityProviderId") UUID paramUUID, @Param("type") String paramString, @Param("keyIds") List<UUID> paramList);
  
  @Delete({"DELETE FROM identity_providers WHERE id = #{id}"})
  int delete(UUID paramUUID);
  
  @Delete({"DELETE FROM identity_providers_applications WHERE applications_id = #{applicationId}"})
  void deleteApplicationConfigurationsByApplicationId(UUID paramUUID);
  
  @Delete({"DELETE FROM identity_providers_applications WHERE identity_providers_id = #{id}"})
  void deleteApplicationConfigurationsByIdentityProviderId(UUID paramUUID);
  
  @Delete({"DELETE FROM federated_domains WHERE identity_providers_id = #{id}"})
  int deleteFederatedDomains(UUID paramUUID);
  
  @Delete({"DELETE FROM identity_providers_tenants WHERE identity_providers_id = #{id}"})
  void deleteTenantConfigurationsByIdentityProviderId(UUID paramUUID);
  
  @Delete({"DELETE FROM identity_providers_tenants WHERE tenants_id = #{tenantId}"})
  void deleteTenantConfigurationsByTenantId(UUID paramUUID);
  
  @Delete({"DELETE FROM identity_providers_verification_keys WHERE identity_providers_id = #{identityProviderId}"})
  void deleteVerificationKeys(@Param("identityProviderId") UUID paramUUID);
  
  List<BaseIdentityProvider<?>> retrieveAll(@Nullable @Param("tenantId") UUID paramUUID);
  
  List<BaseIdentityProvider<?>> retrieveByCriteria(IdentityProviderSearchCriteria paramIdentityProviderSearchCriteria);
  
  BaseIdentityProvider<?> retrieveById(@Nullable @Param("tenantId") UUID paramUUID1, @Param("id") UUID paramUUID2);
  
  BaseIdentityProvider<?> retrieveByIdForUpdate(@Nullable @Param("tenantId") UUID paramUUID1, @Param("id") UUID paramUUID2, @Nullable @Param("existingLastUpdateInstant") ZonedDateTime paramZonedDateTime);
  
  BaseIdentityProvider<?> retrieveByName(@Nullable @Param("tenantId") UUID paramUUID, @Param("name") String paramString);
  
  List<BaseIdentityProvider<?>> retrieveByType(@Nullable @Param("tenantId") UUID paramUUID, @Param("type") IdentityProviderType paramIdentityProviderType);
  
  int retrieveCountByCriteria(IdentityProviderSearchCriteria paramIdentityProviderSearchCriteria);
  
  BaseIdentityProvider<?> retrieveExisting(@Nullable @Param("tenantId") UUID paramUUID1, @Param("id") UUID paramUUID2, @Param("name") String paramString);
  
  List<String> retrieveExistingDomains(@Param("domains") Collection<String> paramCollection, @Nullable @Param("id") UUID paramUUID1, @Nullable @Param("tenantId") UUID paramUUID2);
  
  @Select({"SELECT idp.id AS id, idp.name AS name FROM identity_providers_verification_keys AS ipvk JOIN identity_providers AS idp ON idp.id = ipvk.identity_providers_id WHERE ipvk.keys_id = #{keyId}"})
  List<IdentityProviderId> retrieveIdentityProvidersUsingVerificationKey(@Param("keyId") UUID paramUUID);
  
  int update(BaseIdentityProvider<?> paramBaseIdentityProvider);
  
  public static class IdentityProviderId {
    public UUID id;
    
    public String name;
  }
}
