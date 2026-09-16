package io.fusionauth.api.domain;

import io.fusionauth.domain.ObjectState;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.connector.ConnectorPolicy;
import io.fusionauth.domain.search.TenantSearchCriteria;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface TenantMapper {
  @Select({"SELECT COUNT(*) FROM tenants_verification_keys WHERE tenants_id = #{tenantId} AND type = #{type} AND is_default = true"})
  int countDefaultVerificationKeys(@Param("tenantId") UUID paramUUID, @Param("type") String paramString);
  
  void create(Tenant paramTenant);
  
  void createConnectorPolicies(@Param("tenantId") UUID paramUUID, @Param("connectorPolicies") List<ConnectorPolicy> paramList);
  
  void createVerificationKeys(@Param("tenantId") UUID paramUUID, @Param("type") String paramString, @Param("keyIds") List<UUID> paramList, @Param("markDefault") boolean paramBoolean);
  
  void delete(@Param("id") UUID paramUUID);
  
  void deleteConnectorPolicies(@Param("tenantId") UUID paramUUID);
  
  @Delete({"DELETE FROM tenants_verification_keys WHERE tenants_id = #{tenantId}"})
  void deleteVerificationKeys(@Param("tenantId") UUID paramUUID);
  
  Integer existsById(@Param("id") UUID paramUUID);
  
  List<Tenant> retrieveAll();
  
  @Select({"SELECT id, name FROM tenants WHERE ui_ip_access_control_lists_id = #{ipAccessControlListId}\n"})
  List<TenantId> retrieveAllIdsUsingIPAccessControlList(@Param("ipAccessControlListId") UUID paramUUID);
  
  List<Tenant> retrieveAllUsingEntityTypes();
  
  List<Tenant> retrieveByCriteria(TenantSearchCriteria paramTenantSearchCriteria);
  
  Tenant retrieveById(@Param("id") UUID paramUUID);
  
  Tenant retrieveByName(@Param("name") String paramString);
  
  List<ConnectorPolicy> retrieveConnectorPolicies(@Param("tenantId") UUID paramUUID);
  
  int retrieveCount();
  
  int retrieveCountByCriteria(TenantSearchCriteria paramTenantSearchCriteria);
  
  @Select({"SELECT COUNT(id) FROM tenants WHERE themes_id = #{themeId}"})
  int retrieveCountByThemeId(UUID paramUUID);
  
  Tenant retrieveExistingByName(@Param("id") UUID paramUUID, @Param("name") String paramString);
  
  Tenant retrieveTenantByApplicationId(@Param("applicationId") UUID paramUUID);
  
  List<UUID> retrieveTenantIdByConnectorId(@Param("connectorId") UUID paramUUID);
  
  List<UUID> retrieveTenantIdsUsingFormById(@Param("formId") UUID paramUUID);
  
  List<UUID> retrieveTenantIdsUsingMessengerById(@Param("messengerId") UUID paramUUID);
  
  List<UUID> retrieveTenantIdsUsingSMSMessengerById(@Param("messengerId") UUID paramUUID);
  
  List<UUID> retrieveTenantIdsUsingVoiceMessengerById(@Param("messengerId") UUID paramUUID);
  
  @Select({"SELECT t.id AS id, t.name AS name FROM tenants_verification_keys AS tvk JOIN tenants AS t ON t.id = tvk.tenants_id WHERE tvk.keys_id = #{keyId}"})
  List<TenantId> retrieveTenantsUsingVerificationKey(@Param("keyId") UUID paramUUID);
  
  void update(@Param("tenant") Tenant paramTenant);
  
  void updateState(@Param("id") UUID paramUUID, @Param("state") ObjectState paramObjectState);
  
  public static class TenantId {
    public UUID id;
    
    public String name;
  }
}
