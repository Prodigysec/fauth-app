package io.fusionauth.api.domain;

import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.tenantManager.TenantManagerApplicationConfiguration;
import io.fusionauth.domain.tenantManager.TenantManagerConfiguration;
import io.fusionauth.domain.tenantManager.TenantManagerIdentityProviderTypeConfiguration;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface TenantManagerMapper {
  void createTenantManagerApplicationConfigurations(@Param("applicationConfigurations") List<TenantManagerApplicationConfiguration> paramList);
  
  void createTenantManagerIdentityProviderTypeConfiguration(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration);
  
  @Delete({"DELETE FROM tenant_manager_applications"})
  void deleteAllTenantManagerApplicationConfigurations();
  
  @Delete({"DELETE FROM tenant_manager_identity_provider_type_configurations WHERE type = #{type}"})
  void deleteTenantManagerIdentityProviderTypeConfiguration(@Param("type") IdentityProviderType paramIdentityProviderType);
  
  @Select({"SELECT * FROM tenant_manager_configuration"})
  @ResultMap({"TenantManagerConfiguration"})
  TenantManagerConfiguration retrieve();
  
  List<TenantManagerApplicationConfiguration> retrieveTenantManagerApplicationConfigurations();
  
  @Select({"SELECT count(*) FROM tenant_manager_configuration WHERE tenant_manager_attribute_forms_id = #{formId}"})
  int retrieveTenantManagerConfigurationCountUsingFormById(@Param("formId") UUID paramUUID);
  
  TenantManagerIdentityProviderTypeConfiguration retrieveTenantManagerIdentityProviderTypeConfiguration(@Param("type") IdentityProviderType paramIdentityProviderType);
  
  List<TenantManagerIdentityProviderTypeConfiguration> retrieveTenantManagerIdentityProviderTypeConfigurations();
  
  @Update({"UPDATE tenant_manager_configuration SET data = #{dataToDatabase}, last_update_instant = #{lastUpdateInstant}, tenant_manager_attribute_forms_id = #{attributeFormId}"})
  void update(TenantManagerConfiguration paramTenantManagerConfiguration);
  
  void updateTenantManagerIdentityProviderTypeConfiguration(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration);
}
