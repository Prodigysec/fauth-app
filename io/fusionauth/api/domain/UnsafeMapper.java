package io.fusionauth.api.domain;

import com.inversoft.jdbc.CurrentDatabaseEngine;
import com.inversoft.migration.domain.MigrationRecord;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.IdentityVerifiedReason;
import io.fusionauth.domain.User;
import io.fusionauth.domain.jwt.RefreshToken;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UnsafeMapper {
  static String deleteGroupsSQL() {
    if (CurrentDatabaseEngine.current == CurrentDatabaseEngine.DatabaseEngine.mysql)
      return "DELETE FROM `groups`"; 
    return "DELETE FROM groups";
  }
  
  static String deleteKeysSQL(UUID paramUUID) {
    if (CurrentDatabaseEngine.current == CurrentDatabaseEngine.DatabaseEngine.mysql)
      return "DELETE FROM `keys` WHERE id != #{id} AND NOT (type = 'HMAC' AND secret IS NULL) AND name != 'Tenant manager signing key'"; 
    return "DELETE FROM keys WHERE id != #{id} AND NOT (type = 'HMAC' AND secret IS NULL) AND name != 'Tenant manager signing key'";
  }
  
  @Insert({"  INSERT INTO identities (users_id, breached_password_last_checked_instant, breached_password_status, connectors_id, email, username, username_index, username_status, encryption_scheme, factor, insert_instant, last_update_instant,\n               last_login_instant, password, password_change_required, password_change_reason, password_last_update_instant, salt, status, tenants_id, verified, verified_instant)\n  VALUES (#{user.id}, #{user.breachedPasswordLastCheckedInstant}, #{user.breachedPasswordStatus}, #{user.connectorId}, #{email}, #{username}, UPPER(#{usernameIndex}), #{user.usernameStatus}, #{user.encryptionScheme}, #{user.factor},\n         #{user.insertInstant}, #{user.lastUpdateInstant}, #{user.lastLoginInstant}, #{user.password}, #{user.passwordChangeRequired}, #{user.passwordChangeReason}, #{user.passwordLastUpdateInstant},\n         #{user.salt}, 0, #{user.tenantId}, #{verified}, #{verifiedInstant})\n"})
  void createLegacyIdentity(@Param("user") User paramUser, @Param("email") String paramString1, @Param("username") String paramString2, @Param("usernameIndex") String paramString3, @Param("verified") boolean paramBoolean, @Param("verifiedInstant") ZonedDateTime paramZonedDateTime);
  
  @Insert({"INSERT INTO users (id, active, birth_date, clean_speak_id, data, expiry, first_name, full_name, image_url, insert_instant, last_name, last_update_instant, middle_name, mobile_phone, parent_email, tenants_id, timezone)\n VALUES (#{user.id}, #{user.active}, #{user.birthDate}, #{user.cleanSpeakId}, #{user.dataToDatabase}, #{user.expiry}, #{user.firstName}, #{user.fullName}, #{user.imageUrl}, #{user.insertInstant}, #{user.lastName}, #{user.lastUpdateInstant}, #{user.middleName}, #{user.mobilePhone}, #{user.parentEmail}, #{user.tenantId}, #{user.timezone})\n"})
  void createLegacyUser(@Param("user") User paramUser);
  
  @Insert({"  INSERT INTO refresh_tokens (id, tenants_id, token, users_id, applications_id, insert_instant, data, start_instant)\n  VALUES (#{refreshToken.id}, #{refreshToken.tenantId}, #{refreshToken.token}, #{refreshToken.userId}, #{refreshToken.applicationId}, #{refreshToken.insertInstant}, #{refreshToken.dataToDatabase}, #{refreshToken.startInstant})\n"})
  void createRefreshTokenWithStartInstant(@Param("refreshToken") RefreshToken paramRefreshToken);
  
  @Delete({"DELETE FROM application_daily_active_users"})
  void deleteApplicationDailyActiveUsers();
  
  @Delete({"DELETE FROM application_monthly_active_users"})
  void deleteApplicationMonthlyActiveUsers();
  
  @Delete({"DELETE FROM application_registration_counts"})
  void deleteApplicationRegistrationCounts();
  
  @Delete({"DELETE FROM applications_verification_keys"})
  void deleteApplicationVerificationKeys();
  
  @Delete({"<script>\nDELETE FROM applications WHERE id NOT IN (\n  <foreach collection=\"ids\" item=\"id\" separator=\",\">\n    #{id}\n  </foreach>\n)\n</script>\n"})
  void deleteApplicationsExceptFor(@Param("ids") UUID... paramVarArgs);
  
  @Delete({"DELETE FROM application_roles WHERE applications_id != #{applicationId}"})
  void deleteApplicationsRolesExceptFor(UUID paramUUID);
  
  @Delete({"DELETE FROM asynchronous_tasks"})
  void deleteAsyncTasks();
  
  @Delete({"DELETE FROM audit_logs"})
  void deleteAuditLogs();
  
  @Delete({"DELETE FROM authentication_keys WHERE id != (SELECT internal_authentication_keys_id FROM instance LIMIT 1)"})
  void deleteAuthenticationKeysExceptInternalKey();
  
  @Delete({"DELETE FROM breached_password_metrics"})
  void deleteBreachedPasswordMetrics();
  
  @Delete({"DELETE FROM clean_speak_applications"})
  void deleteCleanSpeakApplications();
  
  @Delete({"DELETE FROM common_breached_passwords"})
  void deleteCommonBreachedPasswords();
  
  @Delete({"DELETE FROM data_sets WHERE name = 'CommonPasswords'"})
  void deleteCommonPasswordsDatasets();
  
  @Delete({"DELETE FROM connectors WHERE id != #{connectorId}"})
  void deleteConnectorsExceptFor(UUID paramUUID);
  
  @Delete({"DELETE FROM connectors_tenants"})
  void deleteConnectorsTenants();
  
  @Delete({"DELETE FROM consents"})
  void deleteConsents();
  
  @Delete({"DELETE FROM current_usage_stats"})
  void deleteCurrentUsageStats();
  
  @Delete({"DELETE FROM user_consents_email_plus"})
  void deleteEmailPlusUserConsents();
  
  @Delete({"DELETE FROM email_templates"})
  void deleteEmailTemplates();
  
  @Delete({"DELETE FROM entities"})
  void deleteEntities();
  
  @Delete({"DELETE FROM entity_type_verification_keys"})
  void deleteEntityTypeVerificationKeys();
  
  @Delete({"DELETE FROM entity_type_permissions"})
  void deleteEntityTypePermissions();
  
  @Delete({"DELETE FROM entity_types"})
  void deleteEntityTypes();
  
  @Delete({"DELETE FROM event_logs"})
  void deleteEventLogs();
  
  @Delete({"DELETE FROM external_identifiers"})
  void deleteExternalIdentifiers();
  
  @Delete({"DELETE FROM families"})
  void deleteFamilies();
  
  @Delete({"DELETE FROM federated_domains"})
  void deleteFederatedDomains();
  
  @Delete({"DELETE FROM form_fields"})
  void deleteFormFields();
  
  @Delete({"DELETE FROM form_steps"})
  void deleteFormSteps();
  
  @Delete({"<script>\nDELETE FROM forms WHERE id NOT IN (\n  <foreach collection=\"ids\" item=\"id\" separator=\",\">\n    #{id}\n  </foreach>\n)\n</script>\n"})
  void deleteFormsExceptFor(@Param("ids") UUID... paramVarArgs);
  
  @Delete({"DELETE FROM user_action_logs_applications"})
  void deleteFromUserActionLogsForApplications();
  
  @Delete({"DELETE FROM global_daily_active_users"})
  void deleteGlobalDailyActiveUsers();
  
  @Delete({"DELETE FROM global_monthly_active_users"})
  void deleteGlobalMonthlyActiveUsers();
  
  @Delete({"DELETE FROM global_registration_counts"})
  void deleteGlobalRegistrationCounts();
  
  @Delete({"DELETE FROM group_application_roles"})
  void deleteGroupApplicationRoles();
  
  @Delete({"DELETE FROM group_members"})
  void deleteGroupMemberships();
  
  @Delete({"DELETE FROM scim_external_id_groups"})
  void deleteGroupSCIMExternalIds();
  
  @DeleteProvider(type = UnsafeMapper.class, method = "deleteGroupsSQL")
  void deleteGroups();
  
  @Delete({"DELETE FROM hourly_logins"})
  void deleteHourlyLogins();
  
  @Delete({"DELETE FROM ip_access_control_lists"})
  void deleteIPAccessControlLists();
  
  @Delete({"DELETE FROM ip_location_database"})
  void deleteIPLocationData();
  
  @Delete({"DELETE FROM ip_location_meta_data"})
  void deleteIPLocationMetaData();
  
  @Delete({"DELETE FROM identities"})
  void deleteIdentities();
  
  @Delete({"DELETE FROM identity_providers_applications"})
  void deleteIdentityProviderApplicationConfigurations();
  
  @Delete({"DELETE FROM identity_provider_links"})
  void deleteIdentityProviderLinks();
  
  @Delete({"DELETE FROM identity_providers_tenants"})
  void deleteIdentityProviderTenantConfigurations();
  
  @Delete({"DELETE FROM identity_providers_verification_keys"})
  void deleteIdentityProviderVerificationKeys();
  
  @Delete({"DELETE FROM identity_providers"})
  void deleteIdentityProviders();
  
  @Delete({"DELETE FROM ip_reputation_data"})
  void deleteIpReputationData();
  
  @Delete({"DELETE FROM ip_reputation_meta_data"})
  void deleteIpReputationMetaData();
  
  @DeleteProvider(type = UnsafeMapper.class, method = "deleteKeysSQL")
  void deleteKeysExceptForId(UUID paramUUID);
  
  @Delete({"DELETE FROM kickstart_files"})
  void deleteKickstartFiles();
  
  @Delete({"DELETE FROM lambdas"})
  void deleteLambdas();
  
  @Delete({"DELETE FROM message_templates"})
  void deleteMessageTemplates();
  
  @Delete({"DELETE FROM messengers"})
  void deleteMessengers();
  
  @Delete({"DELETE FROM mfa_metrics"})
  void deleteMfaMetrics();
  
  @Delete({"DELETE FROM nodes"})
  void deleteNodes();
  
  @Delete({"DELETE FROM application_oauth_scopes"})
  void deleteOAuthScopes();
  
  @Delete({"DELETE FROM previous_passwords"})
  void deletePreviousPasswords();
  
  @Delete({"DELETE FROM raw_application_daily_active_users"})
  void deleteRawApplicationDailyActiveUsers();
  
  @Delete({"DELETE FROM raw_application_monthly_active_users"})
  void deleteRawApplicationMonthlyActiveUsers();
  
  @Delete({"DELETE FROM raw_application_registration_counts"})
  void deleteRawApplicationRegistrationCounts();
  
  @Delete({"DELETE FROM raw_global_daily_active_users"})
  void deleteRawGlobalDailyActiveUsers();
  
  @Delete({"DELETE FROM raw_global_monthly_active_users"})
  void deleteRawGlobalMonthlyActiveUsers();
  
  @Delete({"DELETE FROM raw_global_registration_counts"})
  void deleteRawGlobalRegistrationCounts();
  
  @Delete({"DELETE FROM raw_logins"})
  void deleteRawLogins();
  
  @Delete({"DELETE FROM refresh_tokens"})
  void deleteRefreshTokens();
  
  @Delete({"DELETE FROM request_frequencies"})
  void deleteRequestFrequencies();
  
  @Delete({"DELETE FROM tenant_manager_identity_provider_type_configurations"})
  void deleteTenantManagerIdentityProviderTypeConfigurations();
  
  @Delete({"DELETE FROM tenants_verification_keys"})
  void deleteTenantVerificationKeys();
  
  @Delete({"DELETE FROM themes WHERE id != #{themeId}"})
  void deleteThemesExceptFor(UUID paramUUID);
  
  @Delete({"DELETE FROM usage_stats"})
  void deleteUsageStats();
  
  @Delete({"DELETE FROM user_action_logs"})
  void deleteUserActionLogs();
  
  @Delete({"DELETE FROM user_action_reasons"})
  void deleteUserActionReasons();
  
  @Delete({"DELETE FROM user_actions"})
  void deleteUserActions();
  
  @Delete({"DELETE FROM user_agent_reputation_data"})
  void deleteUserAgentReputationData();
  
  @Delete({"DELETE FROM user_agent_reputation_meta_data"})
  void deleteUserAgentReputationMetaData();
  
  @Delete({"DELETE FROM user_comments"})
  void deleteUserComments();
  
  @Delete({"DELETE FROM user_consents"})
  void deleteUserConsents();
  
  @Delete({"DELETE FROM user_registrations_application_roles"})
  void deleteUserRegistrationApplicationRoles();
  
  @Delete({"DELETE FROM user_registrations"})
  void deleteUserRegistrations();
  
  @Delete({"DELETE FROM scim_external_id_users"})
  void deleteUserSCIMExternalIds();
  
  @Delete({"DELETE FROM users"})
  void deleteUsers();
  
  @Delete({"DELETE FROM webauthn_credentials"})
  void deleteWebAuthnCredentials();
  
  @Delete({"DELETE FROM webhook_attempt_logs"})
  void deleteWebhookAttemptLogs();
  
  @Delete({"DELETE FROM webhook_event_logs"})
  void deleteWebhookEventLogs();
  
  @Delete({"DELETE FROM webhooks_tenants"})
  void deleteWebhookTenantMappings();
  
  @Delete({"DELETE FROM webhooks"})
  void deleteWebhooks();
  
  @Select({"SELECT reactor_health_checks FROM instance"})
  String getReactorHealthChecksData();
  
  @Update({"UPDATE instance SET activate_instant = NULL, license = NULL, license_id = NULL, data = NULL"})
  void resetInstance();
  
  @Update({"UPDATE themes SET name = 'FusionAuth' WHERE id = #{themeId}"})
  void resetThemeName(UUID paramUUID);
  
  @Select({"SELECT name, run_instant FROM migrations WHERE name = #{name}"})
  MigrationRecord retrieveJavaMigration(String paramString);
  
  @Select({"SELECT data FROM tenants WHERE id = #{id}"})
  String selectDataFromTenants(@Param("id") UUID paramUUID);
  
  @Update({"UPDATE identities SET verified = FALSE, verified_instant = NULL, verified_reason = #{reason} WHERE users_id = #{userId} AND value = #{loginId} AND type = #{identityType}"})
  void setUserIdentityUnverified(@Param("userId") UUID paramUUID, @Param("loginId") String paramString, @Param("identityType") IdentityType paramIdentityType, @Param("reason") IdentityVerifiedReason paramIdentityVerifiedReason);
  
  @Update({" <script>\n <if test=\"_databaseId == 'mysql'\">\n    UPDATE applications\n    SET data = JSON_SET(CAST(COALESCE(data, '{}') AS JSON),\n     '$.registrationDeletePolicy.unverified.enabledInstant', #{unverifiedEnabledInstant})\n    WHERE id = #{applicationId}\n </if>\n <if test=\"_databaseId == 'postgresql'\">\n   UPDATE applications\n   SET data = JSONB_SET(data::JSONB,\n    '{registrationDeletePolicy, unverified, enabledInstant}',\n    TO_JSONB(#{unverifiedEnabledInstant}))\n   WHERE id = #{applicationId}\n </if>\n </script>\n"})
  void updateApplicationUnverifiedEnabledInstant(@Param("applicationId") UUID paramUUID, @Param("unverifiedEnabledInstant") ZonedDateTime paramZonedDateTime);
  
  @Update({"UPDATE instance SET data = #{data}"})
  void updateInstanceData(String paramString);
  
  @Update({"UPDATE users SET password_last_update_instant = #{lastUpdateInstant} WHERE id = #{userId}"})
  void updatePasswordLastUpdate(@Param("userId") UUID paramUUID, @Param("lastUpdateInstant") ZonedDateTime paramZonedDateTime);
  
  @Update({"UPDATE instance SET reactor_health_checks = #{data}"})
  void updateReactorHealthChecksData(String paramString);
  
  @Update({"UPDATE refresh_tokens SET insert_instant = #{insertInstant} WHERE id = #{id}"})
  void updateRefreshTokenInsertInstant(@Param("id") UUID paramUUID, @Param("insertInstant") ZonedDateTime paramZonedDateTime);
  
  @Update({"UPDATE refresh_tokens SET start_instant = #{startInstant} WHERE id = #{id}"})
  void updateRefreshTokenStartInstant(@Param("id") UUID paramUUID, @Param("startInstant") ZonedDateTime paramZonedDateTime);
  
  @Update({" <script>\n <if test=\"_databaseId == 'mysql'\">\n    UPDATE tenants\n    SET data = JSON_SET(CAST(COALESCE(data, '{}') AS JSON),\n     '$.userDeletePolicy.unverified.enabledInstant', #{unverifiedEnabledInstant})\n    WHERE id = #{tenantId}\n </if>\n <if test=\"_databaseId == 'postgresql'\">\n   UPDATE tenants\n   SET data = JSONB_SET(data::JSONB,\n    '{userDeletePolicy, unverified, enabledInstant}',\n    TO_JSONB(#{unverifiedEnabledInstant}))\n   WHERE id = #{tenantId}\n </if>\n </script>\n"})
  void updateTenantUnverifiedEnabledInstant(@Param("tenantId") UUID paramUUID, @Param("unverifiedEnabledInstant") ZonedDateTime paramZonedDateTime);
  
  @Update({"UPDATE users SET insert_instant = #{insertInstant} WHERE id = #{id}"})
  void updateUserInsertInstant(@Param("id") UUID paramUUID, @Param("insertInstant") ZonedDateTime paramZonedDateTime);
}
