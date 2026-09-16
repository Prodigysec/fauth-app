package io.fusionauth.api.domain;

import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationOAuthScope;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.search.ApplicationSearchCriteria;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ApplicationMapper {
  @Select({"SELECT COUNT(*) FROM applications_verification_keys WHERE applications_id = #{applicationId} AND type = #{type} AND is_default = true"})
  int countDefaultVerificationKeys(@Param("applicationId") UUID paramUUID, @Param("type") String paramString);
  
  void create(Application paramApplication);
  
  void createCleanSpeakApplicationIds(@Param("applicationId") UUID paramUUID, @Param("cleanSpeakApplicationIds") List<UUID> paramList);
  
  void createOAuthScopes(@Param("scopes") Collection<ApplicationOAuthScope> paramCollection);
  
  void createRoles(@Param("roles") Collection<ApplicationRole> paramCollection);
  
  void createVerificationKeys(@Param("applicationId") UUID paramUUID, @Param("type") String paramString, @Param("keyIds") List<UUID> paramList, @Param("markDefault") boolean paramBoolean);
  
  int deactivate(@Param("applicationId") UUID paramUUID);
  
  @Delete({"DELETE FROM applications WHERE id = #{id}"})
  int delete(UUID paramUUID);
  
  @Delete({"DELETE FROM application_oauth_scopes WHERE applications_id = #{id}"})
  void deleteAllOAuthScopesFromApplication(UUID paramUUID);
  
  @Delete({"DELETE FROM application_roles WHERE applications_id = #{id}"})
  void deleteAllRolesFromApplication(UUID paramUUID);
  
  @Delete({"DELETE FROM clean_speak_applications WHERE applications_id = #{id}"})
  void deleteCleanSpeakApplicationIds(UUID paramUUID);
  
  @Delete({"DELETE FROM application_oauth_scopes WHERE id = #{scopeId}"})
  void deleteOAuthScope(@Param("scopeId") UUID paramUUID);
  
  @Delete({"DELETE FROM application_roles WHERE id = #{roleId}"})
  void deleteRole(@Param("roleId") UUID paramUUID);
  
  int deleteRolesFromUsers(@Param("roleIds") Collection<UUID> paramCollection);
  
  @Delete({"DELETE FROM applications_verification_keys WHERE applications_id = #{applicationId}"})
  void deleteVerificationKeys(@Param("applicationId") UUID paramUUID);
  
  int reactivate(@Param("applicationId") UUID paramUUID);
  
  List<Application> retrieveAll(@Param("tenantId") UUID paramUUID);
  
  List<Application> retrieveAllByRegistrationFormId(@Param("formId") UUID paramUUID);
  
  List<Application> retrieveAllExistingByName(@Param("tenantId") UUID paramUUID1, @Param("name") String paramString, @Param("id") UUID paramUUID2);
  
  List<Application> retrieveAllIgnoreActive(@Param("tenantId") UUID paramUUID);
  
  List<Application> retrieveAllInactive(@Param("tenantId") UUID paramUUID);
  
  @Select({"SELECT id, name FROM applications WHERE ui_ip_access_control_lists_id = #{ipAccessControlListId}\n"})
  List<ApplicationId> retrieveAllUsingIPAccessControlList(@Param("ipAccessControlListId") UUID paramUUID);
  
  List<UUID> retrieveApplicationIdsUsingFormById(@Param("formId") UUID paramUUID);
  
  @Select({"SELECT a.id AS id, a.name AS name FROM applications_verification_keys AS avk JOIN applications AS a ON a.id = avk.applications_id WHERE avk.keys_id = #{keyId}"})
  List<ApplicationId> retrieveApplicationsUsingVerificationKey(@Param("keyId") UUID paramUUID);
  
  List<Application> retrieveByCriteria(ApplicationSearchCriteria paramApplicationSearchCriteria);
  
  Application retrieveById(@Param("tenantId") UUID paramUUID1, @Param("id") UUID paramUUID2);
  
  Application retrieveByIdIgnoreActive(@Param("tenantId") UUID paramUUID1, @Param("id") UUID paramUUID2);
  
  List<Application> retrieveByIds(@Param("tenantId") UUID paramUUID, @Param("applicationIds") List<UUID> paramList);
  
  Application retrieveByName(@Param("tenantId") UUID paramUUID, @Param("name") String paramString);
  
  Application retrieveBySAMLv2Issuer(@Param("tenantId") UUID paramUUID, @Param("issuer") String paramString);
  
  @Select({"SELECT clean_speak_application_id FROM clean_speak_applications WHERE applications_id = #{id}"})
  List<UUID> retrieveCleanSpeakApplicationIds(UUID paramUUID);
  
  int retrieveCountByCriteria(ApplicationSearchCriteria paramApplicationSearchCriteria);
  
  @Select({"SELECT COUNT(id) FROM applications WHERE themes_id = #{themeId}"})
  int retrieveCountByThemeId(UUID paramUUID);
  
  Application retrieveExisting(@Param("tenantId") UUID paramUUID1, @Param("name") String paramString, @Param("id") UUID paramUUID2);
  
  Application retrieveExistingBySAMLv2Issuer(@Param("tenantId") UUID paramUUID1, @Param("issuer") String paramString, @Param("id") UUID paramUUID2);
  
  Application retrieveExistingUniversal(@Param("name") String paramString, @Param("id") UUID paramUUID);
  
  List<UUID> retrieveIdsByCleanSpeakIds(@Param("cleanSpeakApplicationIds") List<UUID> paramList);
  
  ApplicationOAuthScope retrieveOAuthScopeById(@Param("tenantId") UUID paramUUID1, @Param("applicationId") UUID paramUUID2, @Param("scopeId") UUID paramUUID3);
  
  ApplicationOAuthScope retrieveOAuthScopeByName(@Param("tenantId") UUID paramUUID1, @Param("applicationId") UUID paramUUID2, @Param("scopeName") String paramString);
  
  List<ApplicationOAuthScope> retrieveOAuthScopesByApplicationIds(@Param("tenantId") UUID paramUUID, @Param("applicationIds") List<UUID> paramList);
  
  ApplicationRole retrieveRoleById(@Param("tenantId") UUID paramUUID1, @Param("applicationId") UUID paramUUID2, @Param("roleId") UUID paramUUID3);
  
  ApplicationRole retrieveRoleByName(@Param("tenantId") UUID paramUUID1, @Param("applicationId") UUID paramUUID2, @Param("name") String paramString);
  
  List<ApplicationRole> retrieveRolesByApplicationIds(@Param("applicationIds") List<UUID> paramList);
  
  List<ApplicationRole> retrieveRolesByIds(@Param("tenantId") UUID paramUUID, @Param("roleIds") Collection<UUID> paramCollection);
  
  List<ApplicationRole> retrieveRolesByNames(@Param("tenantId") UUID paramUUID1, @Param("applicationId") UUID paramUUID2, @Param("names") List<String> paramList);
  
  Application retrieveTenantManagerApplication();
  
  List<UUID> retrieveValidIds(@Param("ids") List<UUID> paramList);
  
  int update(Application paramApplication);
  
  int updateOAuthScope(ApplicationOAuthScope paramApplicationOAuthScope);
  
  int updateRole(ApplicationRole paramApplicationRole);
  
  public static class ApplicationId {
    public UUID id;
    
    public String name;
  }
}
