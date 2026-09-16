package io.fusionauth.api.domain;

import io.fusionauth.api.service.authentication.LoginQueue;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Delete.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Update.List;

public interface LastLoginMapper {
  @Insert({"<script>\nINSERT INTO last_login_instants (users_id, user_last_login_instant, applications_id, registration_last_login_instant, identities_value, identities_type) VALUES\n<foreach collection=\"rawLogins\" item=\"rawLogin\" separator=\",\">\n  (#{rawLogin.userId}, #{rawLogin.instant}, #{rawLogin.applicationId}, #{rawLogin.registrationLastLoginInstant}, <choose><when test=\"rawLogin.loginIdType.name.equals('username')\">UPPER(#{rawLogin.loginId})</when><otherwise>#{rawLogin.loginId}</otherwise></choose>, #{rawLogin.loginIdType})\n</foreach>\n</script>\n"})
  void bulkInsert(@Param("rawLogins") List<LoginQueue.LoginQueueRawLogin> paramList);
  
  @List({@Delete(value = {"TRUNCATE TABLE last_login_instants;"}, databaseId = "mysql"), @Delete(value = {"DELETE FROM last_login_instants;"}, databaseId = "postgresql")})
  void deleteAll();
  
  @List({@Update(value = {"UPDATE identities i\nINNER JOIN (\n  SELECT users_id, identities_type, identities_value, MAX(user_last_login_instant) AS user_last_login_instant\n  FROM last_login_instants\n  GROUP BY users_id, identities_type, identities_value\n  ORDER BY users_id\n) mi ON (i.users_id = mi.users_id AND i.type = mi.identities_type AND mi.identities_value = i.value)\nSET i.last_login_instant = mi.user_last_login_instant\nWHERE i.last_login_instant IS NULL OR mi.user_last_login_instant > i.last_login_instant\n"}, databaseId = "mysql"), @Update(value = {"WITH max_instants AS (\n  SELECT users_id, identities_type, identities_value, max(user_last_login_instant) AS user_last_login_instant\n  FROM last_login_instants\n  GROUP BY users_id, identities_type, identities_value\n  ORDER BY users_id\n)\nUPDATE identities i\nSET last_login_instant = mi.user_last_login_instant\nFROM max_instants mi\nWHERE mi.users_id = i.users_id AND i.type = mi.identities_type AND mi.identities_value = i.value\nAND (i.last_login_instant IS NULL OR mi.user_last_login_instant > i.last_login_instant)\n"}, databaseId = "postgresql")})
  void updateLastLoginIdentities();
  
  @List({@Update(value = {"UPDATE users u\nINNER JOIN (SELECT users_id, MAX(user_last_login_instant) AS user_last_login_instant\n            FROM last_login_instants\n            GROUP BY users_id\n            ORDER BY users_id) mi\n            ON u.id = mi.users_id\nSET u.last_login_instant = mi.user_last_login_instant\nWHERE u.last_login_instant IS NULL OR mi.user_last_login_instant > u.last_login_instant\n"}, databaseId = "mysql"), @Update(value = {"WITH max_instants AS (\n  SELECT users_id, max(user_last_login_instant) AS user_last_login_instant\n  FROM last_login_instants\n  GROUP BY users_id\n  ORDER BY users_id\n)\nUPDATE users u\nSET last_login_instant = mi.user_last_login_instant\nFROM max_instants mi\nWHERE mi.users_id = u.id\nAND (u.last_login_instant IS NULL OR mi.user_last_login_instant > u.last_login_instant)\n"}, databaseId = "postgresql")})
  void updateLastLoginUsers();
  
  @List({@Update(value = {"UPDATE user_registrations ur\nINNER JOIN (SELECT users_id, applications_id, MAX(registration_last_login_instant) AS registration_last_login_instant\n            FROM last_login_instants\n            GROUP BY users_id, applications_id\n            ORDER BY users_id, applications_id) mi\n           ON ur.users_id = mi.users_id AND ur.applications_id = mi.applications_id\nSET ur.last_login_instant = mi.registration_last_login_instant\nWHERE ur.last_login_instant IS NULL OR mi.registration_last_login_instant > ur.last_login_instant\n"}, databaseId = "mysql"), @Update(value = {"WITH max_instants AS (\n  SELECT users_id, applications_id, max(registration_last_login_instant) AS registration_last_login_instant\n  FROM last_login_instants GROUP BY users_id, applications_id ORDER BY users_id, applications_id\n)\nUPDATE user_registrations ur\nSET last_login_instant = mi.registration_last_login_instant\nFROM max_instants mi\nWHERE mi.users_id = ur.users_id\nAND mi.applications_id = ur.applications_id\nAND (ur.last_login_instant IS NULL OR mi.registration_last_login_instant > ur.last_login_instant)\n"}, databaseId = "postgresql")})
  void updateRegistrationLastLogin();
}
