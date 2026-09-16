package io.fusionauth.api.domain;

import io.fusionauth.domain.UserActionLog;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface UserActionLogMapper {
  @Insert({"INSERT INTO user_action_logs (id, actioner_users_id, actionee_users_id, comment, insert_instant, email_user_on_end, end_event_sent, expiry, history, localized_name, localized_option, localized_reason, name, notify_user_on_end, option_name, reason, reason_code, user_actions_id) VALUES (#{id}, #{actionerUserId}, #{actioneeUserId}, #{comment}, #{insertInstant}, #{emailUserOnEnd}, #{endEventSent}, #{expiry}, #{history}, #{localizedName}, #{localizedOption}, #{localizedReason}, #{name}, #{notifyUserOnEnd}, #{option}, #{reason}, #{reasonCode}, #{userActionId})"})
  void create(UserActionLog paramUserActionLog);
  
  void createApplicationAssociations(@Param("id") UUID paramUUID, @Param("applicationIds") List<UUID> paramList);
  
  void deleteApplicationAssociationsForActionee(UUID paramUUID);
  
  void deleteApplicationAssociationsForActioner(UUID paramUUID);
  
  @Delete({"DELETE FROM user_action_logs_applications WHERE applications_id = #{id}"})
  void deleteApplicationAssociationsForApplication(UUID paramUUID);
  
  @Delete({"DELETE FROM user_action_logs WHERE actionee_users_id = #{id}"})
  void deleteForActionee(UUID paramUUID);
  
  @Delete({"DELETE FROM user_action_logs WHERE actioner_users_id = #{id}"})
  void deleteForActioner(UUID paramUUID);
  
  @Delete({"DELETE FROM user_action_logs WHERE user_actions_id = #{id}"})
  void deleteLogsForAction(UUID paramUUID);
  
  @Update({"UPDATE user_action_logs SET end_event_sent = TRUE WHERE id = #{id}"})
  void markEndEventSent(UserActionLog paramUserActionLog);
  
  List<UserActionLog> retrieveAllCurrentPreventLoginActionLogsForUser(@Param("id") UUID paramUUID, @Param("now") ZonedDateTime paramZonedDateTime);
  
  List<UserActionLog> retrieveAllForActioner(UUID paramUUID);
  
  List<UserActionLog> retrieveAllForUser(UUID paramUUID);
  
  UserActionLog retrieveById(UUID paramUUID);
  
  UserActionLog retrieveCurrent(@Param("actioneeUserId") UUID paramUUID1, @Param("actionId") UUID paramUUID2, @Param("now") ZonedDateTime paramZonedDateTime);
  
  List<UserActionLog> retrieveExpired(ZonedDateTime paramZonedDateTime);
  
  @Update({"UPDATE user_action_logs SET actioner_users_id = #{actionerUserId}, comment = #{comment}, insert_instant = #{insertInstant}, email_user_on_end = #{emailUserOnEnd}, expiry = #{expiry}, history = #{history}, notify_user_on_end = #{notifyUserOnEnd} WHERE id = #{id}"})
  void update(UserActionLog paramUserActionLog);
}
