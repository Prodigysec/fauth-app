package io.fusionauth.api.domain;

import io.fusionauth.domain.Theme;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.usagestats.shared.domain.UsageStats;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Select.List;

public interface UsageStatsCollectorMapper {
  @Select({"SELECT count(*) FROM authentication_keys WHERE expiration_instant IS NOT NULL\n"})
  Long apiKeyExpiringEnabled();
  
  @Select({"SELECT count(*) FROM authentication_keys WHERE key_format != #{hashedNone}\n"})
  Long apiKeyHashedEnabled(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE a.access_token_populate_lambdas_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationAccessTokenPopulateLambda(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationActiveLoggedIntoNumber(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications WHERE active = TRUE\n"})
  Long applicationActiveNumber();
  
  @Select({"        SELECT count(*) FROM application_roles r, applications a WHERE r.applications_id = a.id AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationActiveRoleNumber(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE data->>'$.samlv2Configuration.enabled' = 'true' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE data::JSON->'samlv2Configuration'->>'enabled' = 'true' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationActsAsSamlv2Idp(int paramInt);
  
  @List({@Select(value = {"SELECT count FROM application_monthly_active_users WHERE applications_id = 0x3C219E58ED0E4B18AD48F4F92793AE32 ORDER BY month DESC LIMIT 1\n"}, databaseId = "mysql"), @Select(value = {"SELECT count FROM application_monthly_active_users WHERE applications_id = '3c219e58-ed0e-4b18-ad48-f4f92793ae32' ORDER BY month DESC LIMIT 1\n"}, databaseId = "postgresql")})
  Long applicationAdminMonthlyActiveUsers();
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE a.data->>'$.registrationConfiguration.type' = 'advanced' AND a.data->>'$.registrationConfiguration.enabled' = 'true' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE a.data::JSON->'registrationConfiguration'->>'type' = 'advanced' AND a.data::JSON->'registrationConfiguration'->>'enabled' = 'true' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationAdvancedSelfServiceRegistrationEnabled(int paramInt);
  
  @List({@Select(value = {"SELECT COUNT(DISTINCT f.id)\n      FROM forms f\n      INNER JOIN form_steps fs ON f.id = fs.forms_id\n      INNER JOIN applications a ON f.id = (a.data::JSON->'registrationConfiguration'->>'formId')::UUID\nWHERE f.type = 0\nAND fs.type IN (1,2)\nAND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql"), @Select(value = {"SELECT COUNT(DISTINCT f.id)\n      FROM forms f\n      INNER JOIN form_steps fs ON f.id = fs.forms_id\n      INNER JOIN applications a ON f.id = UNHEX(REPLACE(a.data->>'$.registrationConfiguration.formId', '-', ''))\nWHERE f.type = 0\nAND fs.type IN (1,2)\nAND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql")})
  Long applicationAdvancedSelfServiceRegistrationPreVerified(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE a.data->>'$.registrationConfiguration.type' = 'basic' AND a.data->>'$.registrationConfiguration.enabled' = 'true' AND a.data->>'$.registrationConfiguration.loginIdType' = 'phoneNumber' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE a.data::JSON->'registrationConfiguration'->>'type' = 'basic' AND a.data::JSON->'registrationConfiguration'->>'enabled' = 'true' AND a.data::JSON->'registrationConfiguration'->>'loginIdType' = 'phoneNumber' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationBasicSelfServicePhoneRegistrationEnabled(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE a.data->>'$.registrationConfiguration.type' = 'basic' AND a.data->>'$.registrationConfiguration.enabled' = 'true' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE a.data::JSON->'registrationConfiguration'->>'type' = 'basic' AND a.data::JSON->'registrationConfiguration'->>'enabled' = 'true' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationBasicSelfServiceRegistrationEnabled(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE self_service_user_forms_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationCustomSelfServiceAccountForm(int paramInt);
  
  default Long applicationCustomThemeUsed(int paramInt) {
    return applicationCustomThemeUsedDb(Theme.FUSIONAUTH_THEME_ID, paramInt);
  }
  
  @Select({"        SELECT count(*) FROM applications a WHERE themes_id IS NOT NULL AND themes_id != #{fusionauthThemeId} AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationCustomThemeUsedDb(@Param("fusionauthThemeId") UUID paramUUID, @Param("dayCutoff") int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE email_update_email_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationEmailUpdateTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE email_verification_email_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationEmailVerificationTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE email_verified_email_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationEmailVerifiedTemplate(int paramInt);
  
  @Select({"        SELECT ip.type, count(*) FROM applications a, identity_providers_applications ipa, identity_providers ip WHERE ipa.applications_id = a.id AND ipa.enabled = TRUE AND ip.id = ipa.identity_providers_id AND ip.tenants_id IS NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} ) GROUP BY ip.type ORDER BY ip.type ASC\n"})
  List<UsageStats.IdpCount> applicationEnabledIdentityProviders(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a, identity_providers_applications ipa, identity_providers ip WHERE ipa.applications_id = a.id AND ipa.enabled = TRUE AND reconcile_lambdas_id IS NOT NULL AND ip.id = ipa.identity_providers_id AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationEnabledIdentityProvidersWithLambdas(int paramInt);
  
  @Select({"        SELECT ip.type, count(*) FROM applications a, identity_providers_applications ipa, identity_providers ip WHERE ipa.applications_id = a.id AND ipa.enabled = TRUE AND ip.id = ipa.identity_providers_id AND ip.tenants_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} ) GROUP BY ip.type ORDER BY ip.type ASC\n"})
  List<UsageStats.IdpCount> applicationEnabledTenantIdentityProviders(int paramInt);
  
  @Select({"    SELECT count(*) FROM applications a WHERE phone_configuration_forgot_password_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationForgotPasswordPhoneTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE forgot_password_email_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationForgotPasswordTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE a.id_token_populate_lambdas_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationIdTokenPopulateLambda(int paramInt);
  
  @Select({"    SELECT count(*) FROM applications a WHERE phone_configuration_identity_update_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationIdentityUpdatePhoneTemplate(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE themes_id IN ( SELECT id FROM themes WHERE data->>'$.type' = 'simple' ) AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE themes_id IN ( SELECT id FROM themes WHERE data::JSON->>'type' = 'simple' ) AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationLoggedInApplicationsUsingSimpleThemes(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE data->>'$.loginConfiguration.requireAuthentication' = 'false' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE data::JSON->'loginConfiguration'->>'requireAuthentication' = 'false' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationLoginApiAuthenticationDisabled(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE data->>'$.loginConfiguration.generateRefreshTokens' = 'true' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE data::JSON->'loginConfiguration'->>'generateRefreshTokens' = 'true' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationLoginApiRefreshTokensEnabled(int paramInt);
  
  @Select({"    SELECT count(*) FROM applications a WHERE phone_configuration_login_id_in_use_on_create_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationLoginIdInUseOnCreatePhoneTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE login_id_in_use_on_create_email_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationLoginIdInUseOnCreateTemplate(int paramInt);
  
  @Select({"    SELECT count(*) FROM applications a WHERE phone_configuration_login_id_in_use_on_update_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationLoginIdInUseOnUpdatePhoneTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE login_id_in_use_on_update_email_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationLoginIdInUseOnUpdateTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE login_new_device_email_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationLoginNewDevice(int paramInt);
  
  @Select({"    SELECT count(*) FROM applications a WHERE phone_configuration_login_new_device_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationLoginNewDevicePhoneTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE login_suspicious_email_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationLoginSuspiciousEmailTemplate(int paramInt);
  
  @Select({"    SELECT count(*) FROM applications a WHERE phone_configuration_login_suspicious_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationLoginSuspiciousPhoneTemplate(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE data->>'$.passwordlessConfiguration.enabled' = 'true' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE data::JSON->'passwordlessConfiguration'->>'enabled' = 'true' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationMagicLinks(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE a.multi_factor_requirement_lambdas_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationMfaLambdasConfigured(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE data->>'$.multiFactorConfiguration.loginPolicy' IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE data::JSON->'multiFactorConfiguration'->'loginPolicy' IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationMfaPolicy(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE data->>'$.multiFactorConfiguration.loginPolicy' = 'ChallengeOnHighRisk' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE data::JSON->'multiFactorConfiguration'->>'loginPolicy' = 'ChallengeOnHighRisk' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationMfaPolicyChallengeOnHighRisk(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE data->>'$.multiFactorConfiguration.loginPolicy' = 'ChallengeOnMediumRisk' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE data::JSON->'multiFactorConfiguration'->>'loginPolicy' = 'ChallengeOnMediumRisk' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationMfaPolicyChallengeOnMediumRisk(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE data->>'$.multiFactorConfiguration.loginPolicy' = 'Disabled' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE data::JSON->'multiFactorConfiguration'->>'loginPolicy' = 'Disabled' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationMfaPolicyDisabled(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE data->>'$.multiFactorConfiguration.loginPolicy' = 'Enabled' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE data::JSON->'multiFactorConfiguration'->>'loginPolicy' = 'Enabled' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationMfaPolicyEnabled(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE data->>'$.multiFactorConfiguration.loginPolicy' = 'Required' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE data::JSON->'multiFactorConfiguration'->>'loginPolicy' = 'Required' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationMfaPolicyRequired(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE multi_factor_email_message_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationMultiFactorEmailMessageTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE multi_factor_sms_message_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationMultiFactorSmsMessageTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications\n"})
  Long applicationNumber();
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a, application_oauth_scopes oas WHERE oas.applications_id = a.id AND oas.data->>'$.required' = 'false' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a, application_oauth_scopes oas WHERE oas.applications_id = a.id AND oas.data::JSON->>'required' = 'false' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationOptionalScopes(int paramInt);
  
  @List({@Select(value = {"SELECT MAX(count)\nFROM application_monthly_active_users\nWHERE applications_id <> 0x3C219E58ED0E4B18AD48F4F92793AE32 AND applications_id <> (SELECT tenant_manager_applications_id FROM instance)\nGROUP BY month\nORDER BY month DESC\nLIMIT 1\n"}, databaseId = "mysql"), @Select(value = {"SELECT MAX(count)\nFROM application_monthly_active_users\nWHERE applications_id <> '3c219e58-ed0e-4b18-ad48-f4f92793ae32' AND applications_id <> (SELECT tenant_manager_applications_id FROM instance)\nGROUP BY month\nORDER BY month DESC\nLIMIT 1\n"}, databaseId = "postgresql")})
  Long applicationOtherMonthlyActiveUsers();
  
  @Select({"    SELECT count(*) FROM applications a WHERE phone_configuration_password_reset_success_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationPasswordResetSuccessPhoneTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE password_reset_success_email_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationPasswordResetSuccessTemplate(int paramInt);
  
  @Select({"    SELECT count(*) FROM applications a WHERE phone_configuration_password_update_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationPasswordUpdatePhoneTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE password_update_email_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationPasswordUpdateTemplate(int paramInt);
  
  @Select({"    SELECT count(*) FROM applications a WHERE phone_configuration_passwordless_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationPasswordlessPhoneTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE passwordless_email_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationPasswordlessTemplate(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff}) AND a.data->>'$.jwtConfiguration.refreshTokenOneTimeUseConfiguration.gracePeriodInSeconds' > 0 ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff}) AND (a.data::JSON->'jwtConfiguration'->'refreshTokenOneTimeUseConfiguration'->>'gracePeriodInSeconds')::INT > 0 ;\n"}, databaseId = "postgresql")})
  Long applicationRefreshTokenGracePeriodEnabled(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a, application_oauth_scopes oas WHERE oas.applications_id = a.id AND oas.data->>'$.required' = 'true' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a, application_oauth_scopes oas WHERE oas.applications_id = a.id AND oas.data::JSON->>'required' = 'true' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationRequiredScopes(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE a.samlv2_populate_lambdas_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationSamlv2PopulateLambda(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE a.self_service_registration_validation_lambdas_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationSelfServiceRegistrationValidationLambda(int paramInt);
  
  @Select({"    SELECT count(*) FROM applications a WHERE phone_configuration_set_password_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationSetPasswordPhoneTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE set_password_email_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationSetPasswordTemplate(int paramInt);
  
  @Select({"    SELECT count FROM application_monthly_active_users WHERE applications_id = (SELECT tenant_manager_applications_id FROM instance) ORDER BY month DESC LIMIT 1\n"})
  Long applicationTenantManagerMonthlyActiveUsers();
  
  @List({@Select(value = {"        SELECT count(*) FROM applications a WHERE data->>'$.oauthConfiguration.relationship' = 'ThirdParty' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM applications a WHERE data::JSON->'oauthConfiguration'->>'relationship' = 'ThirdParty' AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long applicationThirdParty(int paramInt);
  
  @Select({"    SELECT count(*) FROM applications a WHERE phone_configuration_two_factor_method_add_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationTwoFactorMethodAddPhoneTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE two_factor_method_add_email_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationTwoFactorMethodAddTemplate(int paramInt);
  
  @Select({"    SELECT count(*) FROM applications a WHERE phone_configuration_two_factor_method_remove_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationTwoFactorMethodRemovePhoneTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE two_factor_method_remove_email_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationTwoFactorMethodRemoveTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE a.userinfo_populate_lambdas_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationUserinfoPopulateLambda(int paramInt);
  
  @Select({"    SELECT count(*) FROM applications a WHERE phone_configuration_verification_complete_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationVerificationCompletePhoneTemplate(int paramInt);
  
  @Select({"    SELECT count(*) FROM applications a WHERE phone_configuration_verification_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationVerificationPhoneTemplate(int paramInt);
  
  @Select({"        SELECT count(*) FROM applications a WHERE verification_email_templates_id IS NOT NULL AND a.active = TRUE AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long applicationVerificationTemplate(int paramInt);
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.captchaConfiguration.enabled' = 'true'\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'captchaConfiguration'->>'enabled' = 'true'\n"}, databaseId = "postgresql")})
  Long atdCaptcha();
  
  @Select({"        SELECT count(*) FROM ip_access_control_lists\n"})
  Long atdIpAcls();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE (data->>'$.rateLimitConfiguration') LIKE '%enabled\": true%'\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE (data::JSON->'rateLimitConfiguration')::TEXT LIKE '%enabled\":true%'\n"}, databaseId = "postgresql")})
  Long atdRateLimiting();
  
  @Select({"        SELECT count(*) FROM entity_entity_grants\n"})
  Long entitiesEntityGrants();
  
  @Select({"        SELECT count(*) FROM entity_type_permissions\n"})
  Long entitiesPermissions();
  
  @Select({"        SELECT count(*) FROM entity_types\n"})
  Long entitiesTypeNumber();
  
  @Select({"SELECT count(*) FROM group_application_roles gar, application_roles ar, applications a WHERE a.id = ar.applications_id AND ar.id = gar.application_roles_id AND a.active = TRUE AND a.id IN ( SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"})
  Long groupApplicationRoles(int paramInt);
  
  @Select({"        SELECT g.count FROM global_daily_active_users g ORDER BY g.day DESC LIMIT 1;\n"})
  Long instanceDailyActiveUsers();
  
  @List({@Select(value = {"        SELECT  COALESCE(data->>'$.firstTimeSetup.emailConfigured', 'false') = 'true' AS emailConfigured, `instance`.data->>'$.firstTimeSetup.applicationId' IS NOT NULL AS applicationConfigured, `instance`.data->>'$.firstTimeSetup.apiKeyId' IS NOT NULL AS apiKeyConfigured, COALESCE(`instance`.data->>'$.firstTimeSetup.licenseActivated', 'false') = 'true' AS licenseActivated  FROM `instance`\n"}, databaseId = "mysql"), @Select(value = {"        SELECT  COALESCE(\"instance\".data::JSON->'firstTimeSetup'->>'emailConfigured', 'false') = 'true' AS emailConfigured, \"instance\".data::JSON->'firstTimeSetup'->>'applicationId' IS NOT NULL AS applicationConfigured, \"instance\".data::JSON->'firstTimeSetup'->>'apiKeyId' IS NOT NULL AS apiKeyConfigured, COALESCE(\"instance\".data::JSON->'firstTimeSetup'->>'licenseActivated', 'false') = 'true' AS licenseActivated  FROM \"instance\"\n"}, databaseId = "postgresql")})
  UsageStats.FirstTimeSetupProgress instanceFirstTimeSetupProgress();
  
  @Select({"        SELECT count(*) FROM nodes;\n"})
  Long instanceNodes();
  
  @List({@Select(value = {"        SELECT EXISTS (SELECT 1 FROM integrations WHERE integrations.data->>'$.cleanspeak.enabled' = 'true');\n"}, databaseId = "mysql"), @Select(value = {"        SELECT EXISTS (SELECT 1 FROM integrations WHERE integrations.data::JSON->'cleanspeak'->>'enabled' = 'true');\n"}, databaseId = "postgresql")})
  boolean integrationsCleanspeakEnabled();
  
  @List({@Select(value = {"        SELECT EXISTS (SELECT 1 FROM integrations WHERE integrations.data->>'$.kafka.enabled' = 'true');\n"}, databaseId = "mysql"), @Select(value = {"        SELECT EXISTS (SELECT 1 FROM integrations WHERE integrations.data::JSON->'kafka'->>'enabled' = 'true');\n"}, databaseId = "postgresql")})
  boolean integrationsKafkaEnabled();
  
  @Select({"        SELECT count(*) FROM messengers WHERE type = 0\n"})
  Long messengersGeneric();
  
  @Select({"        SELECT count(*) FROM messengers WHERE type = 1\n"})
  Long messengersKafka();
  
  @Select({"        SELECT count(*) FROM messengers WHERE type = 2\n"})
  Long messengersTwilio();
  
  @Select({"        SELECT coalesce(SUM(challenge_count), 0) FROM mfa_metrics\n"})
  Long mfaChallenge();
  
  @Select({"        SELECT coalesce(SUM(failed_attempt_count), 0) FROM mfa_metrics\n"})
  Long mfaFailedAttempt();
  
  @Select({"        SELECT coalesce(SUM(success_count), 0) FROM mfa_metrics\n"})
  Long mfaSuccess();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.scimServerConfiguration.enabled' = 'true'\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'scimServerConfiguration'->>'enabled' = 'true'\n"}, databaseId = "postgresql")})
  Long scimEnabled();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.passwordValidationRules.breachDetection.\"enabled\"' = 'true'\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'passwordValidationRules'->'breachDetection'->>'enabled' = 'true'\n"}, databaseId = "postgresql")})
  Long tenantBreachedPasswordDetection();
  
  @Select({"        SELECT count(*) FROM tenants WHERE client_credentials_access_token_populate_lambdas_id IS NOT NULL\n"})
  Long tenantClientCredentialsLambdaEnabled();
  
  @Select({"        SELECT count(*) FROM tenants WHERE confirm_child_email_templates_id IS NOT NULL;\n"})
  Long tenantConfirmChildTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE admin_user_forms_id NOT IN (SELECT id FROM forms WHERE name LIKE '%provided by FusionAuth')\n"})
  Long tenantCustomAdminUserForm();
  
  default Long tenantCustomConnectors() {
    return tenantCustomConnectorsDb(BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID);
  }
  
  @Select({"        SELECT count(*) FROM connectors_tenants WHERE connectors_id != #{fusionauthConnectorId}\n"})
  Long tenantCustomConnectorsDb(UUID paramUUID);
  
  default Long tenantCustomThemeUsed() {
    return tenantCustomThemeUsedDb(Theme.FUSIONAUTH_THEME_ID);
  }
  
  @Select({"        SELECT count(*) FROM tenants WHERE themes_id != #{fusionauthThemeId}\n"})
  Long tenantCustomThemeUsedDb(UUID paramUUID);
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.multiFactorConfiguration.email.\"enabled\"' = 'true'\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'multiFactorConfiguration'->'email'->>'enabled' = 'true'\n"}, databaseId = "postgresql")})
  Long tenantEmailMfaEnabled();
  
  @Select({"        SELECT count(*) FROM tenants WHERE email_update_email_templates_id IS NOT NULL;\n"})
  Long tenantEmailUpdateTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE email_verified_email_templates_id IS NOT NULL;\n"})
  Long tenantEmailVerifiedTemplate();
  
  @List({@Select(value = {"            SELECT COUNT(*) FROM tenants WHERE data->>'$.familyConfiguration.enabled' = 'true'\n"}, databaseId = "mysql"), @Select(value = {"            SELECT COUNT(*) FROM tenants WHERE data::JSON->'familyConfiguration'->>'enabled' = 'true'\n"}, databaseId = "postgresql")})
  Long tenantFamilyEnabledTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE family_request_email_templates_id IS NOT NULL;\n"})
  Long tenantFamilyRequestTemplate();
  
  @Select({"    SELECT count(*) FROM tenants WHERE phone_configuration_forgot_password_templates_id IS NOT NULL;\n"})
  Long tenantForgotPasswordPhoneTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE forgot_password_email_templates_id IS NOT NULL;\n"})
  Long tenantForgotPasswordTemplate();
  
  @Select({"    SELECT count(*) FROM tenants WHERE phone_configuration_identity_update_templates_id IS NOT NULL;\n"})
  Long tenantIdentityUpdatePhoneTemplate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants t, applications a WHERE t.themes_id IN ( SELECT id FROM themes WHERE data->>'$.type' = 'simple' ) AND a.tenants_id = t.id AND a.active = TRUE AND a.themes_id IS NULL AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants t, applications a WHERE t.themes_id IN ( SELECT id FROM themes WHERE data::JSON->>'type' = 'simple' ) AND a.tenants_id = t.id AND a.active = TRUE AND a.themes_id IS NULL AND a.id IN (SELECT applications_id FROM application_daily_active_users WHERE application_daily_active_users.day > #{dayCutoff} )\n"}, databaseId = "postgresql")})
  Long tenantLoggedInApplicationsUsingSimpleThemesFromTenant(int paramInt);
  
  @Select({"    SELECT count(*) FROM tenants WHERE phone_configuration_login_id_in_use_on_create_templates_id IS NOT NULL;\n"})
  Long tenantLoginIdInUseOnCreatePhoneTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE login_id_in_use_on_create_email_templates_id IS NOT NULL;\n"})
  Long tenantLoginIdInUseOnCreateTemplate();
  
  @Select({"    SELECT count(*) FROM tenants WHERE phone_configuration_login_id_in_use_on_update_templates_id IS NOT NULL;\n"})
  Long tenantLoginIdInUseOnUpdatePhoneTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE login_id_in_use_on_update_email_templates_id IS NOT NULL;\n"})
  Long tenantLoginIdInUseOnUpdateTemplate();
  
  @Select({"    SELECT count(*) FROM tenants WHERE phone_configuration_login_new_device_templates_id IS NOT NULL;\n"})
  Long tenantLoginNewDevicePhoneTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE login_new_device_email_templates_id IS NOT NULL;\n"})
  Long tenantLoginNewDeviceTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE login_suspicious_email_templates_id IS NOT NULL;\n"})
  Long tenantLoginSuspiciousEmailTemplate();
  
  @Select({"    SELECT count(*) FROM tenants WHERE phone_configuration_login_suspicious_templates_id IS NOT NULL;\n"})
  Long tenantLoginSuspiciousPhoneTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE multi_factor_requirement_lambdas_id IS NOT NULL;\n"})
  Long tenantMfaLambdasConfigured();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.multiFactorConfiguration.loginPolicy' = 'ChallengeOnHighRisk';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'multiFactorConfiguration'->>'loginPolicy' = 'ChallengeOnHighRisk';\n"}, databaseId = "postgresql")})
  Long tenantMfaPolicyChallengeOnHighRisk();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.multiFactorConfiguration.loginPolicy' = 'ChallengeOnMediumRisk';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'multiFactorConfiguration'->>'loginPolicy' = 'ChallengeOnMediumRisk';\n"}, databaseId = "postgresql")})
  Long tenantMfaPolicyChallengeOnMediumRisk();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.multiFactorConfiguration.loginPolicy' = 'Disabled';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'multiFactorConfiguration'->>'loginPolicy' = 'Disabled';\n"}, databaseId = "postgresql")})
  Long tenantMfaPolicyDisabled();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.multiFactorConfiguration.loginPolicy' = 'Enabled';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'multiFactorConfiguration'->>'loginPolicy' = 'Enabled';\n"}, databaseId = "postgresql")})
  Long tenantMfaPolicyEnabled();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.multiFactorConfiguration.loginPolicy' = 'Required';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'multiFactorConfiguration'->>'loginPolicy' = 'Required';\n"}, databaseId = "postgresql")})
  Long tenantMfaPolicyRequired();
  
  @Select({"        SELECT count(*) FROM tenants WHERE multi_factor_email_message_templates_id IS NOT NULL;\n"})
  Long tenantMultiFactorEmailMessageTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE multi_factor_sms_message_templates_id IS NOT NULL;\n"})
  Long tenantMultiFactorSmsMessageTemplate();
  
  @Select({"        SELECT count(*) FROM tenants\n"})
  Long tenantNumber();
  
  @Select({"        SELECT count(*) FROM tenants WHERE parent_registration_email_templates_id IS NOT NULL;\n"})
  Long tenantParentRegistrationTemplate();
  
  @Select({"    SELECT count(*) FROM tenants WHERE phone_configuration_password_reset_success_templates_id IS NOT NULL;\n"})
  Long tenantPasswordResetSuccessPhoneTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE password_reset_success_email_templates_id IS NOT NULL;\n"})
  Long tenantPasswordResetSuccessTemplate();
  
  @Select({"    SELECT count(*) FROM tenants WHERE phone_configuration_password_update_templates_id IS NOT NULL;\n"})
  Long tenantPasswordUpdatePhoneTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE password_update_email_templates_id IS NOT NULL;\n"})
  Long tenantPasswordUpdateTemplate();
  
  @Select({"    SELECT count(*) FROM tenants WHERE phone_configuration_passwordless_templates_id IS NOT NULL;\n"})
  Long tenantPasswordlessPhoneTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE passwordless_email_templates_id IS NOT NULL;\n"})
  Long tenantPasswordlessTemplate();
  
  @Select({"    SELECT count(*) FROM tenants WHERE phone_configuration_messengers_id IS NOT NULL;\n"})
  Long tenantPhoneConfigurationMessengers();
  
  @Select({"    SELECT count(*) FROM tenants WHERE phone_configuration_set_password_templates_id IS NOT NULL;\n"})
  Long tenantSetPasswordPhoneTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE set_password_email_templates_id IS NOT NULL;\n"})
  Long tenantSetPasswordTemplate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.multiFactorConfiguration.sms.\"enabled\"' = 'true'\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'multiFactorConfiguration'->'sms'->>'enabled' = 'true'\n"}, databaseId = "postgresql")})
  Long tenantSmsMfaEnabled();
  
  @List({@Select(value = {"        SELECT count(DISTINCT t.id) FROM tenants t, applications a WHERE t.id = a.tenants_id AND a.active = TRUE AND t.data->>'$.ssoConfiguration.allowAccessTokenBootstrap' = 'true'\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(DISTINCT t.id) FROM tenants t, applications a WHERE t.id = a.tenants_id AND a.active = TRUE AND t.data::JSON ->'ssoConfiguration'->>'allowAccessTokenBootstrap' = 'true'\n"}, databaseId = "postgresql")})
  Long tenantSsoSessionBootstrapEnabled();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.multiFactorConfiguration.authenticator.\"enabled\"' = 'true'\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'multiFactorConfiguration'->'authenticator'->>'enabled' = 'true'\n"}, databaseId = "postgresql")})
  Long tenantTotpMfaEnabled();
  
  @Select({"    SELECT count(*) FROM tenants WHERE phone_configuration_two_factor_method_add_templates_id IS NOT NULL;\n"})
  Long tenantTwoFactorMethodAddPhoneTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE two_factor_method_add_email_templates_id IS NOT NULL;\n"})
  Long tenantTwoFactorMethodAddTemplate();
  
  @Select({"    SELECT count(*) FROM tenants WHERE phone_configuration_two_factor_method_remove_templates_id IS NOT NULL;\n"})
  Long tenantTwoFactorMethodRemovePhoneTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE two_factor_method_remove_email_templates_id IS NOT NULL;\n"})
  Long tenantTwoFactorMethodRemoveTemplate();
  
  @Select({"    SELECT count(*) FROM tenants WHERE phone_configuration_verification_complete_templates_id IS NOT NULL;\n"})
  Long tenantVerificationCompletePhoneTemplate();
  
  @Select({"    SELECT count(*) FROM tenants WHERE phone_configuration_verification_templates_id IS NOT NULL;\n"})
  Long tenantVerificationPhoneTemplate();
  
  @Select({"        SELECT count(*) FROM tenants WHERE verification_email_templates_id IS NOT NULL;\n"})
  Long tenantVerificationTemplate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.webAuthnConfiguration.enabled' = 'true'\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'webAuthnConfiguration'->>'enabled' = 'true'\n"}, databaseId = "postgresql")})
  Long tenantWebauthnEnabled();
  
  @Select({"        SELECT count(*) FROM applications WHERE universal = TRUE;\n"})
  Long universalApplicationNumber();
  
  @List({@Select(value = {"        SELECT count(*) FROM webhooks WHERE data->>'$.eventsEnabled.\"audit-log.create\"' = 'true';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM webhooks WHERE data::JSON->'eventsEnabled'->>'audit-log.create' = 'true';\n"}, databaseId = "postgresql")})
  Long webhookAuditLogCreate();
  
  @List({@Select(value = {"        SELECT count(*) FROM webhooks WHERE data->>'$.eventsEnabled.\"event-log.create\"' = 'true';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM webhooks WHERE data::JSON->'eventsEnabled'->>'event-log.create' = 'true';\n"}, databaseId = "postgresql")})
  Long webhookEventLogCreate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"group.create\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'group.create'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookGroupCreate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"group.create.complete\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'group.create.complete'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookGroupCreateComplete();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"group.create\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"group.create\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'group.create'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'group.create'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookGroupCreateTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"group.delete\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'group.delete'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookGroupDelete();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"group.delete.complete\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'group.delete.complete'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookGroupDeleteComplete();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"group.delete\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"group.delete\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'group.delete'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'group.delete'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookGroupDeleteTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"group.member.add\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'group.member.add'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookGroupMemberAdd();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"group.member.add.complete\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'group.member.add.complete'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookGroupMemberAddComplete();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"group.member.add\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"group.member.add\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'group.member.add'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'group.member.add'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookGroupMemberAddTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"group.member.remove\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'group.member.remove'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookGroupMemberRemove();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"group.member.remove.complete\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'group.member.remove.complete'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookGroupMemberRemoveComplete();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"group.member.remove\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"group.member.remove\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'group.member.remove'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'group.member.remove'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookGroupMemberRemoveTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"group.member.update\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'group.member.update'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookGroupMemberUpdate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"group.member.update.complete\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'group.member.update.complete'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookGroupMemberUpdateComplete();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"group.member.update\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"group.member.update\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'group.member.update'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'group.member.update'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookGroupMemberUpdateTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"group.update\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'group.update'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookGroupUpdate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"group.update.complete\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'group.update.complete'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookGroupUpdateComplete();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"group.update\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"group.update\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'group.update'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'group.update'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookGroupUpdateTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"jwt.public-key.update\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'jwt.public-key.update'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookJwtPublicKeyUpdate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"jwt.public-key.update\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"jwt.public-key.update\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'jwt.public-key.update'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'jwt.public-key.update'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookJwtPublicKeyUpdateTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"jwt.refresh\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'jwt.refresh'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookJwtRefresh();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"jwt.refresh-token.revoke\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'jwt.refresh-token.revoke'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookJwtRefreshTokenRevoke();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"jwt.refresh-token.revoke\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"jwt.refresh-token.revoke\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'jwt.refresh-token.revoke'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'jwt.refresh-token.revoke'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookJwtRefreshTokenRevokeTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"jwt.refresh\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"jwt.refresh\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'jwt.refresh'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'jwt.refresh'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookJwtRefreshTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM webhooks WHERE data->>'$.eventsEnabled.\"kickstart.success\"' = 'true';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM webhooks WHERE data::JSON->'eventsEnabled'->>'kickstart.success' = 'true';\n"}, databaseId = "postgresql")})
  Long webhookKickstartSuccess();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.action\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.action'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserAction();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.bulk.create\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.bulk.create'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserBulkCreate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.bulk.create\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.bulk.create\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.bulk.create'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.bulk.create'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserBulkCreateTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.create\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.create'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserCreate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.create.complete\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.create.complete'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserCreateComplete();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.create\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.create\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.create'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.create'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserCreateTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.deactivate\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.deactivate'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserDeactivate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.deactivate\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.deactivate\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.deactivate'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.deactivate'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserDeactivateTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.delete\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.delete'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserDelete();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.delete.complete\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.delete.complete'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserDeleteComplete();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.delete\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.delete\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.delete'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.delete'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserDeleteTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.email.update\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.email.update'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserEmailUpdate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.email.verified\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.email.verified'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserEmailVerified();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.email.verified\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.email.verified\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.email.verified'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.email.verified'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserEmailVerifiedTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.identity-provider.link\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.identity-provider.link'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserIdentityProviderLink();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.identity-provider.unlink\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.identity-provider.unlink'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserIdentityProviderUnlink();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.login.failed\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.login.failed'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserLoginFailed();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.login.failed\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.login.failed\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.login.failed'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.login.failed'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserLoginFailedTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.loginId.duplicate.create\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.loginId.duplicate.create'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserLoginIdDuplicateCreate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.loginId.duplicate.update\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.loginId.duplicate.update'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserLoginIdDuplicateUpdate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.login.new-device\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.login.new-device'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserLoginNewDevice();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.login.new-device\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.login.new-device\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.login.new-device'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.login.new-device'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserLoginNewDeviceTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.login.success\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.login.success'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserLoginSuccess();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.login.success\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.login.success\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.login.success'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.login.success'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserLoginSuccessTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.login.suspicious\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.login.suspicious'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserLoginSuspicious();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.login.suspicious\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.login.suspicious\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.login.suspicious'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.login.suspicious'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserLoginSuspiciousTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.two-factor.challenge\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.two-factor.challenge'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserTwoFactorChallenge();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.two-factor.failed-attempt\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.two-factor.failed-attempt'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserTwoFactorFailedAttempt();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.two-factor.success\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.two-factor.success'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserTwoFactorSuccess();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.password.breach\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.password.breach'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserPasswordBreach();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.password.breach\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.password.breach\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.password.breach'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.password.breach'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserPasswordBreachTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.password.reset.send\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.password.reset.send'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserPasswordResetSend();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.password.reset.start\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.password.reset.start'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserPasswordResetStart();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.password.reset.success\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.password.reset.success'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserPasswordResetSuccess();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.password.update\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.password.update'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserPasswordUpdate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.reactivate\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.reactivate'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserReactivate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.reactivate\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.reactivate\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.reactivate'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.reactivate'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserReactivateTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.registration.create\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.registration.create'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserRegistrationCreate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.registration.create.complete\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.registration.create.complete'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserRegistrationCreateComplete();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.registration.create\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.registration.create\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.registration.create'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.registration.create'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserRegistrationCreateTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.registration.delete\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.registration.delete'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserRegistrationDelete();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.registration.delete.complete\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.registration.delete.complete'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserRegistrationDeleteComplete();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.registration.delete\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.registration.delete\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.registration.delete'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.registration.delete'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserRegistrationDeleteTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.registration.update\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.registration.update'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserRegistrationUpdate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.registration.update.complete\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.registration.update.complete'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserRegistrationUpdateComplete();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.registration.update\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.registration.update\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.registration.update'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.registration.update'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserRegistrationUpdateTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.registration.verified\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.registration.verified'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserRegistrationVerified();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.registration.verified\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.registration.verified\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.registration.verified'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.registration.verified'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserRegistrationVerifiedTransactional();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.two-factor.method.add\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.two-factor.method.add'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserTwoFactorMethodAdd();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.two-factor.method.remove\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.two-factor.method.remove'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserTwoFactorMethodRemove();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.update\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.update'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserUpdate();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE data->>'$.eventConfiguration.events.\"user.update.complete\".enabled' = 'true' ;\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE data::JSON->'eventConfiguration'->'events'->'user.update.complete'->>'enabled' = 'true' ;\n"}, databaseId = "postgresql")})
  Long webhookUserUpdateComplete();
  
  @List({@Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data->>'$.eventConfiguration.events.\"user.update\".enabled' = 'true' AND tenants.data->>'$.eventConfiguration.events.\"user.update\".transactionType' != 'None';\n"}, databaseId = "mysql"), @Select(value = {"        SELECT count(*) FROM tenants WHERE tenants.data::JSON->'eventConfiguration'->'events'->'user.update'->>'enabled' = 'true' AND tenants.data::JSON->'eventConfiguration'->'events'->'user.update'->>'transactionType' != 'None';\n"}, databaseId = "postgresql")})
  Long webhookUserUpdateTransactional();
}
