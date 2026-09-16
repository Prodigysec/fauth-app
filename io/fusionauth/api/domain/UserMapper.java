package io.fusionauth.api.domain;

import com.inversoft.util.Pair;
import io.fusionauth.api.domain.mybatis._UserRegistration;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.BreachedPasswordStatus;
import io.fusionauth.domain.ChangePasswordReason;
import io.fusionauth.domain.ContentStatus;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.IdentityVerifiedReason;
import io.fusionauth.domain.SecureIdentity;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserRegistration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.SequencedMap;
import java.util.Set;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UserMapper {
  void addRolesToUserRegistration(@Param("userRegistrationId") UUID paramUUID, @Param("roles") Collection<ApplicationRole> paramCollection);
  
  void addRolesToUserRegistrationBulk(@Param("roleMap") Collection<Pair<UUID, UUID>> paramCollection);
  
  void create(@Param("user") User paramUser);
  
  void createBulk(@Param("users") Collection<User> paramCollection);
  
  void createIdentityBulk(@Param("identities") Collection<UserIdentity> paramCollection, @Param("status") UserIdentityStatus paramUserIdentityStatus);
  
  void createPreviousPassword(PreviousPassword paramPreviousPassword);
  
  void createPreviousPasswordBulk(@Param("passwords") Collection<PreviousPassword> paramCollection);
  
  void createRegistration(@Param("registration") UserRegistration paramUserRegistration, @Param("application") Application paramApplication, @Param("user") User paramUser);
  
  void createRegistrationBulk(@Param("pairs") List<Pair<UserRegistration, User>> paramList);
  
  @Delete({"DELETE FROM users WHERE id = #{userId}"})
  int delete(@Param("userId") UUID paramUUID);
  
  @Update({"UPDATE users SET breached_password_status = NULL, breached_password_last_checked_instant = NULL WHERE id = #{userId}"})
  void deleteBreachStatus(@Param("userId") UUID paramUUID);
  
  @Delete({"DELETE FROM identities WHERE id = #{id}"})
  void deleteIdentityById(long paramLong);
  
  @Delete({"DELETE FROM identities WHERE users_id = #{userId}"})
  void deleteIdentityByUserId(UUID paramUUID);
  
  int deletePreviousPasswordsByInsertInstants(@Param("userId") UUID paramUUID, @Param("insertInstants") Collection<ZonedDateTime> paramCollection);
  
  @Delete({"DELETE FROM previous_passwords WHERE users_id = #{userId}"})
  void deletePreviousPasswordsByUserId(@Param("userId") UUID paramUUID);
  
  @Delete({"DELETE FROM user_registrations WHERE id = #{registrationId}"})
  int deleteRegistration(@Param("registrationId") UUID paramUUID);
  
  @Delete({"DELETE FROM user_registrations WHERE users_id = #{userId}"})
  void deleteRegistrations(@Param("userId") UUID paramUUID);
  
  @Delete({"DELETE FROM user_registrations WHERE applications_id = #{applicationId}"})
  void deleteRegistrationsForApplication(@Param("applicationId") UUID paramUUID);
  
  void deleteRolesForUser(@Param("userId") UUID paramUUID);
  
  void deleteRolesFromRegistration(@Param("registrationId") UUID paramUUID);
  
  Integer existsByEmail(@Param("tenantId") UUID paramUUID, @Param("email") String paramString);
  
  Integer existsById(@Param("tenantId") UUID paramUUID1, @Param("userId") UUID paramUUID2);
  
  Integer existsByLegacyIdentifier(@Param("legacyIdentifier") String paramString, @Param("tenantId") UUID paramUUID1, @Param("userId") UUID paramUUID2);
  
  Integer existsByPhoneNumber(@Param("tenantId") UUID paramUUID, @Param("phoneNumber") String paramString);
  
  Integer existsByUsername(@Param("tenantId") UUID paramUUID, @Param("username") String paramString);
  
  @Select({"SELECT id FROM users\nWHERE id = #{userId}\nFOR UPDATE\n"})
  UUID lockUserForMigration(@Param("userId") UUID paramUUID);
  
  @Update({"UPDATE identities SET\n  value = email,\n  type = 0,\n  email = NULL,\n  is_primary = TRUE,\n  username = NULL,\n  username_index = NULL,\n  username_status = NULL,\n  verified = #{verified},\n  verified_instant = #{verifiedInstant},\n  verified_reason = NULL,\n  -- Clear columns moved to users\n  breached_password_last_checked_instant = NULL,\n  breached_password_status = NULL,\n  connectors_id = NULL,\n  encryption_scheme = NULL,\n  factor = NULL,\n  password = NULL,\n  password_change_required = NULL,\n  password_last_update_instant = NULL,\n  salt = NULL\nWHERE users_id = #{user.id}\n"})
  void migrateIdentityToEmail(@Param("user") User paramUser, @Param("verified") boolean paramBoolean, @Param("verifiedInstant") ZonedDateTime paramZonedDateTime);
  
  @Update({"UPDATE identities SET\n  display_value = username,\n  value = username_index,\n  type = 1,\n  email = NULL,\n  is_primary = TRUE,\n  username = NULL,\n  username_index = NULL,\n  verified = FALSE,\n  verified_instant = NULL,\n  verified_reason = 2,\n  -- Clear columns moved to users\n  breached_password_last_checked_instant = NULL,\n  breached_password_status = NULL,\n  connectors_id = NULL,\n  encryption_scheme = NULL,\n  factor = NULL,\n  password = NULL,\n  password_change_required = NULL,\n  password_last_update_instant = NULL,\n  salt = NULL\nWHERE users_id = #{user.id}\n"})
  void migrateIdentityToUsername(@Param("user") User paramUser);
  
  @Update({"UPDATE users SET\n  breached_password_last_checked_instant = #{user.breachedPasswordLastCheckedInstant},\n  breached_password_status = #{user.breachedPasswordStatus},\n  connectors_id = #{user.connectorId},\n  last_login_instant = #{user.lastLoginInstant},\n  password = #{user.password},\n  password_change_reason = #{user.passwordChangeReason},\n  password_change_required = #{user.passwordChangeRequired},\n  password_encryption_scheme = #{user.encryptionScheme},\n  password_factor = #{user.factor},\n  password_last_update_instant = #{user.passwordLastUpdateInstant},\n  password_salt = #{user.salt},\n  verified_instant = #{verifiedInstant}\nWHERE id = #{user.id}\n"})
  void migrateUpdateUserFields(@Param("user") User paramUser, @Param("verifiedInstant") ZonedDateTime paramZonedDateTime);
  
  void removeParentEmail(@Param("id") UUID paramUUID);
  
  List<User> retrieveBreachedUsersPendingAction(@Param("tenantId") UUID paramUUID, @Param("limit") int paramInt, @Param("offset") long paramLong);
  
  long retrieveBreachedUsersPendingActionCount(@Param("tenantId") UUID paramUUID);
  
  User retrieveById(@Param("tenantId") UUID paramUUID1, @Param("id") UUID paramUUID2);
  
  List<User> retrieveByIds(@Param("tenantId") UUID paramUUID, @Param("ids") List<UUID> paramList);
  
  List<User> retrieveByParentEmail(@Param("tenantId") UUID paramUUID, @Param("parentEmail") String paramString);
  
  User retrieveByUserRegistrationId(@Param("userRegistrationId") UUID paramUUID);
  
  @Select({"SELECT count(id) FROM users"})
  long retrieveCount();
  
  int retrieveCountByTenantId(@Param("tenantId") UUID paramUUID);
  
  User retrieveExisting(@Param("tenantId") UUID paramUUID1, @Param("userId") UUID paramUUID2, @Param("loginId") String paramString, @Param("identityType") IdentityType paramIdentityType);
  
  List<User> retrieveLimitOffset(@Param("limit") int paramInt, @Param("offset") long paramLong);
  
  @Select({"SELECT * FROM previous_passwords WHERE users_id = #{userId} ORDER BY insert_instant DESC"})
  List<PreviousPassword> retrievePreviousPasswordsByUserId(@Param("userId") UUID paramUUID);
  
  UserRegistration retrieveRegistration(@Param("tenantId") UUID paramUUID1, @Param("userId") UUID paramUUID2, @Param("applicationId") UUID paramUUID3);
  
  UserRegistration retrieveRegistrationById(@Param("tenantId") UUID paramUUID1, @Param("id") UUID paramUUID2);
  
  List<_UserRegistration> retrieveRegistrationsByUserIds(@Param("userIds") List<UUID> paramList);
  
  List<UserRegistration> retrieveUniversalRegistrationsByUserId(@Param("userId") UUID paramUUID);
  
  List<User> retrieveUnverifiedChildrenForReaping(@Param("tenantId") UUID paramUUID, @Param("cutoff") ZonedDateTime paramZonedDateTime);
  
  List<_UserRegistration> retrieveUnverifiedRegistrationsForReaping(@Param("applicationId") UUID paramUUID, @Param("start") ZonedDateTime paramZonedDateTime1, @Param("cutoff") ZonedDateTime paramZonedDateTime2);
  
  List<User> retrieveUnverifiedUsersForReaping(@Param("tenantId") UUID paramUUID, @Param("start") ZonedDateTime paramZonedDateTime1, @Param("cutoff") ZonedDateTime paramZonedDateTime2);
  
  User retrieveUserByIdentityType(@Param("tenantId") UUID paramUUID, @Param("canonicalLoginIdsByType") SequencedMap<IdentityType, String> paramSequencedMap);
  
  User retrieveUserByLinkUserId(@Param("tenantId") UUID paramUUID1, @Param("id") UUID paramUUID2);
  
  List<UUID> searchByTerm(@Param("tenantId") UUID paramUUID, @Param("searchTerm") String paramString1, @Param("limit") int paramInt1, @Param("offset") int paramInt2, @Param("orderByOuter") String paramString2, @Param("orderByAggregate") String paramString3);
  
  long searchByTermCount(@Param("tenantId") UUID paramUUID, @Param("searchTerm") String paramString);
  
  @Update({"UPDATE users SET active = #{active} WHERE id = #{userId}"})
  int setActive(@Param("userId") UUID paramUUID, @Param("active") boolean paramBoolean);
  
  @Update({"UPDATE identities SET verified = TRUE,\n                      verified_reason = #{reason},\n                      verified_instant = COALESCE(verified_instant, #{verifiedInstant})\n                  WHERE users_id = #{userId} AND value = #{canonicalLoginId} AND type = #{identityType}\n"})
  void setIdentityVerified(@Param("userId") UUID paramUUID, @Param("canonicalLoginId") String paramString, @Param("identityType") IdentityType paramIdentityType, @Param("verifiedInstant") ZonedDateTime paramZonedDateTime, @Param("reason") IdentityVerifiedReason paramIdentityVerifiedReason);
  
  @Update({"UPDATE identities SET verified_reason = #{reason}\n                  WHERE users_id = #{userId} AND value = #{canonicalLoginId} AND type = #{identityType}\n"})
  void setIdentityVerifiedReason(@Param("userId") UUID paramUUID, @Param("canonicalLoginId") String paramString, @Param("identityType") IdentityType paramIdentityType, @Param("reason") IdentityVerifiedReason paramIdentityVerifiedReason);
  
  int update(User paramUser);
  
  @Update({"UPDATE users SET breached_password_status = #{breachStatus}, breached_password_last_checked_instant = #{now} WHERE id = #{userId}"})
  void updateBreachStatus(@Param("userId") UUID paramUUID, @Param("breachStatus") BreachedPasswordStatus paramBreachedPasswordStatus, @Param("now") ZonedDateTime paramZonedDateTime);
  
  @Update({"UPDATE users SET password_change_reason = #{changeReason}, password_change_required = #{changeRequired} WHERE id = #{userId}"})
  void updatePasswordChangeRequired(@Param("userId") UUID paramUUID, @Param("changeReason") ChangePasswordReason paramChangePasswordReason, @Param("changeRequired") boolean paramBoolean);
  
  @Update({"UPDATE users SET password_encryption_scheme = #{identity.encryptionScheme}, password_factor = #{identity.factor}, password_salt = #{identity.salt}, password = #{identity.password}, password_last_update_instant = #{identity.passwordLastUpdateInstant}, password_change_required = #{identity.passwordChangeRequired}, password_change_reason = #{identity.passwordChangeReason} WHERE id = #{identity.id}"})
  void updatePasswordFields(@Param("identity") SecureIdentity paramSecureIdentity);
  
  void updateRecoveryCodes(@Param("user") User paramUser);
  
  int updateRegistration(UserRegistration paramUserRegistration);
  
  void updateRegistrationUsernameStatuses(@Param("cleanSpeakIds") Set<UUID> paramSet, @Param("contentStatus") ContentStatus paramContentStatus);
  
  void updateUserVerifiedInstant(@Param("userId") UUID paramUUID, @Param("verifiedInstant") ZonedDateTime paramZonedDateTime);
  
  void updateUsernameStatuses(@Param("cleanSpeakIds") Set<UUID> paramSet, @Param("contentStatus") ContentStatus paramContentStatus);
  
  void upsertIdentity(@Param("tenantId") UUID paramUUID1, @Param("userId") UUID paramUUID2, @Param("identity") UserIdentity paramUserIdentity, @Param("status") UserIdentityStatus paramUserIdentityStatus);
  
  @Update({"UPDATE user_registrations SET verified = TRUE, verified_instant = COALESCE(verified_instant, #{verifiedInstant}) WHERE id = #{registrationId}"})
  void verifyRegistration(@Param("registrationId") UUID paramUUID, @Param("verifiedInstant") ZonedDateTime paramZonedDateTime);
}
