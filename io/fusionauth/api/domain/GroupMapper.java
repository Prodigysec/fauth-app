package io.fusionauth.api.domain;

import io.fusionauth.domain.Group;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.search.GroupMemberSearchCriteria;
import io.fusionauth.domain.search.GroupSearchCriteria;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;

public interface GroupMapper {
  void create(Group paramGroup);
  
  void createApplicationRoles(@Param("groupId") UUID paramUUID, @Param("roleIds") Collection<UUID> paramCollection);
  
  void createMembers(@Param("members") Collection<GroupMember> paramCollection);
  
  int delete(UUID paramUUID);
  
  void deleteAllMembershipsByUserId(@Param("userId") UUID paramUUID);
  
  int deleteApplicationRolesByGroupId(@Param("groupId") UUID paramUUID);
  
  int deleteApplicationRolesByRoleIds(@Param("roleIds") Collection<UUID> paramCollection);
  
  void deleteMemberById(UUID paramUUID);
  
  void deleteMembersByGroupId(UUID paramUUID);
  
  void deleteMembersById(@Param("tenantId") UUID paramUUID, @Param("memberIds") Collection<UUID> paramCollection);
  
  List<Group> retrieveAll(@Param("tenantId") UUID paramUUID);
  
  List<Group> retrieveByCriteria(@Param("criteria") GroupSearchCriteria paramGroupSearchCriteria, @Param("exact") boolean paramBoolean);
  
  Group retrieveById(@Param("tenantId") UUID paramUUID1, @Param("id") UUID paramUUID2);
  
  Group retrieveByName(@Param("tenantId") UUID paramUUID, @Param("name") String paramString);
  
  int retrieveCountByCriteria(@Param("criteria") GroupSearchCriteria paramGroupSearchCriteria, @Param("exact") boolean paramBoolean);
  
  Group retrieveExisting(@Param("tenantId") UUID paramUUID1, @Param("id") UUID paramUUID2, @Param("name") String paramString);
  
  GroupMember retrieveMemberById(@Param("tenantId") UUID paramUUID1, @Param("membershipId") UUID paramUUID2);
  
  GroupMember retrieveMemberByUserId(@Param("tenantId") UUID paramUUID1, @Param("groupId") UUID paramUUID2, @Param("userId") UUID paramUUID3);
  
  int retrieveMemberCountByGroupId(@Param("groupId") UUID paramUUID);
  
  List<GroupMember> retrieveMembersByCriteria(GroupMemberSearchCriteria paramGroupMemberSearchCriteria);
  
  List<GroupMember> retrieveMembersByUserId(@Param("tenantId") UUID paramUUID1, @Param("userId") UUID paramUUID2);
  
  List<GroupMember> retrieveMembersByUserIds(@Param("userIds") List<UUID> paramList);
  
  int retrieveMembersCountByCriteria(GroupMemberSearchCriteria paramGroupMemberSearchCriteria);
  
  List<UUID> retrieveUserIdsByGroupId(@Param("groupId") UUID paramUUID);
  
  int update(Group paramGroup);
}
