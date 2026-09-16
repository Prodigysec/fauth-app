package io.fusionauth.api.domain;

import io.fusionauth.domain.UserAction;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

public interface UserActionMapper {
  @Insert({"INSERT INTO user_actions (id, active, localized_names, name, insert_instant, last_update_instant, options, prevent_login, send_end_event, temporal, transaction_type, user_notifications_enabled, user_emailing_enabled, start_email_templates_id, modify_email_templates_id, cancel_email_templates_id, end_email_templates_id, include_email_in_event_json) VALUES (#{id}, #{active}, #{localizedNames}, #{name}, #{insertInstant}, #{lastUpdateInstant}, #{options}, #{preventLogin}, #{sendEndEvent}, #{temporal}, #{transactionType}, #{userNotificationsEnabled}, #{userEmailingEnabled}, #{startEmailTemplateId}, #{modifyEmailTemplateId}, #{cancelEmailTemplateId}, #{endEmailTemplateId}, #{includeEmailInEventJSON})"})
  void create(UserAction paramUserAction);
  
  @Update({"UPDATE user_actions SET active = FALSE WHERE id = #{id}"})
  int deactivate(UUID paramUUID);
  
  @Delete({"DELETE FROM user_actions WHERE id = #{id}"})
  int delete(UUID paramUUID);
  
  @Select({"SELECT * FROM user_actions WHERE active = TRUE"})
  @ResultMap({"UserAction"})
  List<UserAction> retrieveAll();
  
  @Select({"SELECT ua.* FROM user_actions AS ua INNER JOIN user_action_logs AS ual ON ual.user_actions_id = ua.id WHERE ual.expiry > #{now} AND ual.actionee_users_id = #{id}"})
  @ResultMap({"UserAction"})
  List<UserAction> retrieveAllCurrentForUser(@Param("id") UUID paramUUID, @Param("now") ZonedDateTime paramZonedDateTime);
  
  @Select({"SELECT * FROM user_actions WHERE active = FALSE"})
  @ResultMap({"UserAction"})
  List<UserAction> retrieveAllInactive();
  
  @Select({"SELECT * FROM user_actions WHERE id = #{id} AND active = TRUE"})
  @ResultMap({"UserAction"})
  UserAction retrieveById(UUID paramUUID);
  
  @Select({"SELECT * FROM user_actions WHERE id = #{id}"})
  @ResultMap({"UserAction"})
  UserAction retrieveByIdIgnoreActive(UUID paramUUID);
  
  @Select({"SELECT * FROM user_actions WHERE name = #{name} AND active = TRUE"})
  @ResultMap({"UserAction"})
  UserAction retrieveByName(String paramString);
  
  @Select({"SELECT * FROM user_actions WHERE name = #{name}"})
  @ResultMap({"UserAction"})
  UserAction retrieveByNameIgnoreActive(String paramString);
  
  @SelectProvider(type = SQL.class, method = "retrieveExisting")
  @ResultMap({"UserAction"})
  UserAction retrieveExisting(UserAction paramUserAction);
  
  @Update({"UPDATE user_actions SET\nactive = #{active},\nlocalized_names = #{localizedNames},\nname = #{name},\nlast_update_instant = #{lastUpdateInstant},\noptions = #{options},\nprevent_login = #{preventLogin},\nsend_end_event = #{sendEndEvent},\ntemporal = #{temporal},\ntransaction_type = #{transactionType},\nuser_notifications_enabled = #{userNotificationsEnabled},\nuser_emailing_enabled = #{userEmailingEnabled},\nstart_email_templates_id = #{startEmailTemplateId},\nmodify_email_templates_id = #{modifyEmailTemplateId},\ncancel_email_templates_id = #{cancelEmailTemplateId},\nend_email_templates_id = #{endEmailTemplateId},\ninclude_email_in_event_json = #{includeEmailInEventJSON}\nWHERE id = #{id}"})
  int update(UserAction paramUserAction);
  
  public static class SQL {
    public String retrieveExisting(UserAction param1UserAction) {
      if (param1UserAction.id != null)
        return "SELECT * FROM user_actions WHERE id != #{id} AND name = #{name}"; 
      return "SELECT * FROM user_actions WHERE name = #{name}";
    }
  }
}
