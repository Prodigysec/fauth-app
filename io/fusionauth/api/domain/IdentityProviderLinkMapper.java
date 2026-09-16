package io.fusionauth.api.domain;

import io.fusionauth.domain.IdentityProviderLink;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Insert.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface IdentityProviderLinkMapper {
  public static final String SELECT_IDP_USER_LINK = "SELECT\n  idp_u.data                       AS idp_u_data,\n  idp_u.identity_providers_id      AS idp_u_identity_providers_id,\n  idp.name                         AS idp_identity_providers_name,\n  idp.type                         AS idp_identity_providers_type,\n  idp_u.identity_providers_user_id AS idp_u_identity_providers_user_id,\n  idp_u.insert_instant             AS idp_u_insert_instant,\n  idp_u.last_login_instant         AS idp_u_last_login_instant,\n  idp_u.tenants_id                 AS idp_u_tenants_id,\n  idp_u.users_id                   AS idp_u_users_id\n  FROM identity_provider_links AS idp_u\n  INNER JOIN identity_providers AS idp ON idp_u.identity_providers_id = idp.id\n";
  
  @Delete({"DELETE FROM identity_provider_links\nWHERE\nidentity_providers_id = #{identityProviderId} AND\nidentity_providers_user_id = #{identityProviderUserId} AND\ntenants_id = #{tenantId}"})
  void deleteIdentityProviderLink(@Param("tenantId") UUID paramUUID1, @Param("identityProviderId") UUID paramUUID2, @Param("identityProviderUserId") String paramString);
  
  @Delete({"DELETE FROM identity_provider_links\nWHERE\nidentity_providers_id = #{identityProviderId}"})
  void deleteIdentityProviderLinksByIdentityProviderId(UUID paramUUID);
  
  @Delete({"DELETE FROM identity_provider_links\nWHERE\nusers_id = #{userId}"})
  void deleteIdentityProviderLinksByUserId(@Param("userId") UUID paramUUID);
  
  @Select({"<script>SELECT\n  idp_u.data                       AS idp_u_data,\n  idp_u.identity_providers_id      AS idp_u_identity_providers_id,\n  idp.name                         AS idp_identity_providers_name,\n  idp.type                         AS idp_identity_providers_type,\n  idp_u.identity_providers_user_id AS idp_u_identity_providers_user_id,\n  idp_u.insert_instant             AS idp_u_insert_instant,\n  idp_u.last_login_instant         AS idp_u_last_login_instant,\n  idp_u.tenants_id                 AS idp_u_tenants_id,\n  idp_u.users_id                   AS idp_u_users_id\n  FROM identity_provider_links AS idp_u\n  INNER JOIN identity_providers AS idp ON idp_u.identity_providers_id = idp.id\nWHERE idp_u.identity_providers_id = #{identityProviderId} AND idp_u.identity_providers_user_id = #{identityProviderUserId}\n<if test=\"userId != null\">\n  AND idp_u.users_id = #{userId}\n</if>\n<if test=\"tenantId != null\">\n  AND idp_u.tenants_id = #{tenantId}\n</if>\n</script>\n"})
  @ResultMap({"IdentityProviderLink"})
  IdentityProviderLink retrieveIdentityProviderLink(@Param("tenantId") UUID paramUUID1, @Param("identityProviderId") UUID paramUUID2, @Param("identityProviderUserId") String paramString, @Param("userId") UUID paramUUID3);
  
  @Select({"<script>SELECT\n  idp_u.data                       AS idp_u_data,\n  idp_u.identity_providers_id      AS idp_u_identity_providers_id,\n  idp.name                         AS idp_identity_providers_name,\n  idp.type                         AS idp_identity_providers_type,\n  idp_u.identity_providers_user_id AS idp_u_identity_providers_user_id,\n  idp_u.insert_instant             AS idp_u_insert_instant,\n  idp_u.last_login_instant         AS idp_u_last_login_instant,\n  idp_u.tenants_id                 AS idp_u_tenants_id,\n  idp_u.users_id                   AS idp_u_users_id\n  FROM identity_provider_links AS idp_u\n  INNER JOIN identity_providers AS idp ON idp_u.identity_providers_id = idp.id\nWHERE idp_u.users_id = #{userId}\n<if test=\"tenantId != null\">\n  AND idp_u.tenants_id = #{tenantId}\n</if>\n<if test=\"identityProviderId != null\">\n  AND idp_u.identity_providers_id = #{identityProviderId}\n</if>\nORDER BY idp_u.insert_instant, idp_u_identity_providers_user_id\n</script>\n"})
  @ResultMap({"IdentityProviderLink"})
  List<IdentityProviderLink> retrieveIdentityProviderLinksByUserId(@Param("tenantId") UUID paramUUID1, @Param("identityProviderId") UUID paramUUID2, @Param("userId") UUID paramUUID3);
  
  @List({@Insert(value = {"INSERT INTO identity_provider_links (data, identity_providers_id, identity_providers_user_id, insert_instant, last_login_instant, tenants_id, users_id)\nVALUES (#{dataToDatabase}, #{identityProviderId}, #{identityProviderUserId}, #{insertInstant}, #{lastLoginInstant}, #{tenantId}, #{userId})\nON DUPLICATE KEY UPDATE data = #{dataToDatabase}, last_login_instant = #{lastLoginInstant}\n"}, databaseId = "mysql"), @Insert(value = {"INSERT INTO identity_provider_links (data, identity_providers_id, identity_providers_user_id, insert_instant, last_login_instant, tenants_id, users_id)\nVALUES (#{dataToDatabase}, #{identityProviderId}, #{identityProviderUserId}, #{insertInstant}, #{lastLoginInstant}, #{tenantId}, #{userId})\nON CONFLICT (identity_providers_id, identity_providers_user_id, tenants_id) DO UPDATE SET data = EXCLUDED.data, last_login_instant = EXCLUDED.last_login_instant\n"}, databaseId = "postgresql")})
  void upsertIdentityProviderLink(IdentityProviderLink paramIdentityProviderLink);
}
