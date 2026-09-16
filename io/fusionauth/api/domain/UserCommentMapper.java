package io.fusionauth.api.domain;

import io.fusionauth.domain.UserComment;
import io.fusionauth.domain.search.UserCommentSearchCriteria;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.ResultMap;

public interface UserCommentMapper {
  @Insert({"INSERT INTO user_comments (id, users_id, commenter_id, comment, insert_instant) VALUES (#{id}, #{userId}, #{commenterId}, #{comment}, #{insertInstant})"})
  void create(UserComment paramUserComment);
  
  @Delete({"DELETE FROM user_comments WHERE commenter_id = #{id}"})
  void deleteForCommenter(UUID paramUUID);
  
  @Delete({"DELETE FROM user_comments WHERE users_id = #{id}"})
  void deleteForUser(UUID paramUUID);
  
  @ResultMap({"UserComment"})
  List<UserComment> retrieveAllForUser(UUID paramUUID);
  
  List<UserComment> retrieveByCriteria(UserCommentSearchCriteria paramUserCommentSearchCriteria);
  
  int retrieveCountByCriteria(UserCommentSearchCriteria paramUserCommentSearchCriteria);
}
