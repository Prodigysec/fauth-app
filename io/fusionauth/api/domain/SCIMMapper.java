package io.fusionauth.api.domain;

import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SCIMMapper {
  @Insert({"INSERT INTO scim_external_id_groups (entities_id, external_id, groups_id)\nVALUES (#{entityId}, #{externalId}, #{groupId})\n"})
  void addSCIMGroupExternalId(@Param("entityId") UUID paramUUID1, @Param("externalId") String paramString, @Param("groupId") UUID paramUUID2);
  
  @Insert({"INSERT INTO\nscim_external_id_users (entities_id, external_id, users_id)\nVALUES (#{entityId}, #{externalId}, #{userId})\n"})
  void addSCIMUserExternalId(@Param("entityId") UUID paramUUID1, @Param("externalId") String paramString, @Param("userId") UUID paramUUID2);
  
  @Delete({"DELETE FROM scim_external_id_groups where entities_id = #{entityId} AND external_id = #{externalId}"})
  void deleteSCIMGroupExternalIdById(@Param("entityId") UUID paramUUID, @Param("externalId") String paramString);
  
  @Delete({"DELETE FROM scim_external_id_groups where groups_id = #{groupId}"})
  void deleteSCIMGroupExternalIdsByGroupId(@Param("groupId") UUID paramUUID);
  
  @Delete({"DELETE FROM scim_external_id_users where entities_id = #{entityId} AND external_id = #{externalId}"})
  void deleteSCIMUserExternalIdById(@Param("entityId") UUID paramUUID, @Param("externalId") String paramString);
  
  @Delete({"DELETE FROM scim_external_id_users where users_id = #{userId}"})
  void deleteSCIMUserExternalIdsByUserId(@Param("userId") UUID paramUUID);
  
  @Select({"SELECT EXISTS (SELECT entities_id FROM scim_external_id_groups WHERE entities_id = #{id} LIMIT 1)\n"})
  boolean isEntityIdInUseByGroups(UUID paramUUID);
  
  @Select({"SELECT EXISTS (SELECT entities_id FROM scim_external_id_users WHERE entities_id = #{id} LIMIT 1)\n"})
  boolean isEntityIdInUseByUsers(UUID paramUUID);
  
  @Select({"SELECT groups_id\nFROM scim_external_id_groups\nWHERE entities_id = #{entityId} AND external_id = #{externalId}\n"})
  UUID retrieveGroupIdBySCIMExternalId(@Param("entityId") UUID paramUUID, @Param("externalId") String paramString);
  
  @Select({"SELECT external_id\nFROM scim_external_id_groups\nWHERE entities_id = #{entityId} AND groups_id = #{groupId}\n"})
  String retrieveSCIMGroupExternalIdByGroupId(@Param("entityId") UUID paramUUID1, @Param("groupId") UUID paramUUID2);
  
  @Select({"SELECT external_id\nFROM scim_external_id_users\nWHERE entities_id = #{entityId} AND users_id = #{userId}\n"})
  String retrieveSCIMUserExternalIdByUserId(@Param("entityId") UUID paramUUID1, @Param("userId") UUID paramUUID2);
  
  @Select({"SELECT users_id\nFROM scim_external_id_users\nWHERE entities_id = #{entityId} AND external_id = #{externalId}\n"})
  UUID retrieveUserIdBySCIMExternalId(@Param("entityId") UUID paramUUID, @Param("externalId") String paramString);
}
