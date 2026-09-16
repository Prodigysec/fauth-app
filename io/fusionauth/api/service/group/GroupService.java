package io.fusionauth.api.service.group;

import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GroupService {
  void addMembers(Map<UUID, Tenant> paramMap, Map<UUID, Group> paramMap1, Map<UUID, List<GroupMember>> paramMap2, Map<UUID, User> paramMap3, boolean paramBoolean, EventInfo paramEventInfo);
  
  void create(Tenant paramTenant, Group paramGroup, List<ApplicationRole> paramList, boolean paramBoolean, EventInfo paramEventInfo);
  
  void delete(Tenant paramTenant, Group paramGroup, EventInfo paramEventInfo);
  
  void deleteAllByTenantId(Tenant paramTenant, EventInfo paramEventInfo);
  
  void removeAllMembers(Tenant paramTenant, Group paramGroup, EventInfo paramEventInfo);
  
  void removeMember(Tenant paramTenant, Group paramGroup, GroupMember paramGroupMember, EventInfo paramEventInfo);
  
  void removeMembers(Map<UUID, Tenant> paramMap, Map<UUID, Group> paramMap1, Map<UUID, User> paramMap2, Map<UUID, List<GroupMember>> paramMap3, EventInfo paramEventInfo);
  
  void removeUserMemberships(Tenant paramTenant, User paramUser, EventInfo paramEventInfo);
  
  void update(Tenant paramTenant, Group paramGroup1, Group paramGroup2, List<ApplicationRole> paramList, boolean paramBoolean, EventInfo paramEventInfo);
  
  void updateMembers(Map<UUID, Tenant> paramMap, Map<UUID, Group> paramMap1, Map<UUID, User> paramMap2, Map<UUID, List<GroupMember>> paramMap3, boolean paramBoolean, EventInfo paramEventInfo);
  
  ValidationResult validateAddOrUpdateMembers(Tenant paramTenant, Map<UUID, List<GroupMember>> paramMap);
  
  ValidationResult validateCreate(Tenant paramTenant, Group paramGroup, List<UUID> paramList);
  
  ValidationResult validateDelete(Tenant paramTenant, UUID paramUUID);
  
  ValidationResult validateRemoveMemberById(Tenant paramTenant, UUID paramUUID);
  
  ValidationResult validateRemoveMemberByUserAndGroup(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2);
  
  ValidationResult validateRemoveMembers(Tenant paramTenant, Map<UUID, List<UUID>> paramMap);
  
  ValidationResult validateRemoveMembersByGroupId(Tenant paramTenant, UUID paramUUID);
  
  ValidationResult validateRemoveMembersByIds(Tenant paramTenant, List<UUID> paramList);
  
  ValidationResult validateRetrieve(Tenant paramTenant, UUID paramUUID);
  
  ValidationResult validateUpdate(Tenant paramTenant, Group paramGroup, List<UUID> paramList);
  
  public static class ValidationResult extends BaseValidationResult {
    public Group existing;
    
    public Group group;
    
    public Map<UUID, Group> groups;
    
    public GroupMember member;
    
    public Map<UUID, List<GroupMember>> members;
    
    public List<ApplicationRole> roles;
    
    public Tenant tenant;
    
    public Map<UUID, Tenant> tenants;
    
    public Map<UUID, User> users;
  }
}
