package io.fusionauth.api.domain;

import io.fusionauth.domain.UserActionReason;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UserActionReasonMapper {
  @Insert({"INSERT INTO user_action_reasons (id, localized_texts, text, code, insert_instant, last_update_instant) VALUES (#{id}, #{localizedTexts}, #{text}, #{code}, #{insertInstant}, #{lastUpdateInstant})"})
  void create(UserActionReason paramUserActionReason);
  
  @Delete({"DELETE FROM user_action_reasons WHERE id = #{id}"})
  int delete(UUID paramUUID);
  
  @Select({"SELECT * FROM user_action_reasons"})
  List<UserActionReason> retrieveAll();
  
  @Select({"SELECT * FROM user_action_reasons WHERE code = #{code}"})
  UserActionReason retrieveByCode(String paramString);
  
  @Select({"SELECT * FROM user_action_reasons WHERE id = #{id}"})
  UserActionReason retrieveById(UUID paramUUID);
  
  @Select({"SELECT * FROM user_action_reasons WHERE text = #{text}"})
  UserActionReason retrieveByText(String paramString);
  
  @Select({"SELECT * FROM user_action_reasons WHERE id != #{id} AND code = #{code}"})
  UserActionReason retrieveExistingByCode(UserActionReason paramUserActionReason);
  
  @Select({"SELECT * FROM user_action_reasons WHERE id != #{id} AND text = #{text}"})
  UserActionReason retrieveExistingByText(UserActionReason paramUserActionReason);
  
  @Update({"UPDATE user_action_reasons SET localized_texts = #{localizedTexts}, text = #{text}, code = #{code}, last_update_instant = #{lastUpdateInstant} WHERE id = #{id}"})
  int update(UserActionReason paramUserActionReason);
}
