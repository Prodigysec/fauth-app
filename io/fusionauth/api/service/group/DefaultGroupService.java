package io.fusionauth.api.service.group;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.GroupMapper;
import io.fusionauth.api.domain.SCIMMapper;
import io.fusionauth.api.service.search.UserSearchEngine;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Tenantable;
import io.fusionauth.domain.User;
import io.fusionauth.domain.event.BaseEvent;
import io.fusionauth.domain.event.GroupCreateCompleteEvent;
import io.fusionauth.domain.event.GroupCreateEvent;
import io.fusionauth.domain.event.GroupDeleteCompleteEvent;
import io.fusionauth.domain.event.GroupDeleteEvent;
import io.fusionauth.domain.event.GroupMemberAddCompleteEvent;
import io.fusionauth.domain.event.GroupMemberAddEvent;
import io.fusionauth.domain.event.GroupMemberRemoveCompleteEvent;
import io.fusionauth.domain.event.GroupMemberRemoveEvent;
import io.fusionauth.domain.event.GroupMemberUpdateCompleteEvent;
import io.fusionauth.domain.event.GroupMemberUpdateEvent;
import io.fusionauth.domain.event.GroupUpdateCompleteEvent;
import io.fusionauth.domain.event.GroupUpdateEvent;
import io.fusionauth.domain.search.GroupMemberSearchCriteria;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.mybatis.guice.transactional.Transactional;

public class DefaultGroupService implements GroupService {
  private final ApplicationReaderService applicationReader;
  
  private final GroupMapper groupMapper;
  
  private final SCIMMapper scimMapper;
  
  private final UserSearchEngine searchEngine;
  
  private final TenantReaderService tenantReader;
  
  private final UserReaderService userReader;
  
  @Inject
  public DefaultGroupService(GroupMapper paramGroupMapper, SCIMMapper paramSCIMMapper, UserSearchEngine paramUserSearchEngine, TenantReaderService paramTenantReaderService, UserReaderService paramUserReaderService, ApplicationReaderService paramApplicationReaderService) {
    this.groupMapper = paramGroupMapper;
    this.scimMapper = paramSCIMMapper;
    this.searchEngine = paramUserSearchEngine;
    this.tenantReader = paramTenantReaderService;
    this.userReader = paramUserReaderService;
    this.applicationReader = paramApplicationReaderService;
  }
  
  @Transactional
  public void _addGroupMembers(Map<UUID, Tenant> paramMap, Map<UUID, Group> paramMap1, Map<UUID, List<GroupMember>> paramMap2, Map<UUID, User> paramMap3, BiFunction<Group, List<GroupMember>, ? extends BaseEvent> paramBiFunction) {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    ArrayList<?> arrayList = new ArrayList(paramMap2.size());
    paramMap2.forEach((paramUUID, paramList2) -> paramList2.forEach(()));
    Objects.requireNonNull(this.groupMapper);
    MapperTools.safeCreateUpdate(5000, arrayList, this.groupMapper::createMembers);
    for (Group group : paramMap1.values()) {
      List<GroupMember> list = (List)((List)paramMap2.get(group.id)).stream().map(paramGroupMember -> (new GroupMember(paramGroupMember)).with(())).collect(Collectors.toList());
      EventHelper.send(paramMap.get(group.tenantId), null, paramBiFunction.apply(group, list));
    } 
  }
  
  @Transactional
  public void _create(Tenant paramTenant, Group paramGroup, List<ApplicationRole> paramList, EventInfo paramEventInfo) {
    if (paramGroup.id == null)
      paramGroup.id = UUID.randomUUID(); 
    paramGroup.tenantId = paramTenant.id;
    paramGroup.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramGroup.lastUpdateInstant = paramGroup.insertInstant;
    this.groupMapper.create(paramGroup);
    addApplicationRoles(paramGroup, paramList);
    EventHelper.send(paramTenant, null, new GroupCreateEvent(paramEventInfo, (new Group(paramGroup)).sort()));
  }
  
  @Transactional
  public void _delete(Tenant paramTenant, Group paramGroup, EventInfo paramEventInfo) {
    this.groupMapper.deleteMembersByGroupId(paramGroup.id);
    this.groupMapper.deleteApplicationRolesByGroupId(paramGroup.id);
    this.scimMapper.deleteSCIMGroupExternalIdsByGroupId(paramGroup.id);
    this.groupMapper.delete(paramGroup.id);
    EventHelper.send(paramTenant, null, new GroupDeleteEvent(paramEventInfo, (new Group(paramGroup)).sort()));
  }
  
  @Transactional
  public void _removeAllMembers(Tenant paramTenant, Group paramGroup, EventInfo paramEventInfo) {
    this.groupMapper.deleteMembersByGroupId(paramGroup.id);
    EventHelper.send(paramTenant, null, new GroupMemberUpdateEvent(paramEventInfo, (new Group(paramGroup)).sort(), List.of()));
  }
  
  @Transactional
  public void _removeMember(Tenant paramTenant, Group paramGroup, GroupMember paramGroupMember, EventInfo paramEventInfo) {
    this.groupMapper.deleteMemberById(paramGroupMember.id);
    EventHelper.send(paramTenant, null, new GroupMemberRemoveEvent(paramEventInfo, (new Group(paramGroup)).sort(), List.of(paramGroupMember)));
  }
  
  @Transactional
  public void _removeMembers(Map<UUID, Tenant> paramMap, Map<UUID, Group> paramMap1, Map<UUID, List<GroupMember>> paramMap2, EventInfo paramEventInfo) {
    for (Group group : paramMap1.values()) {
      List<GroupMember> list = paramMap2.get(group.id);
      if (list != null && !list.isEmpty()) {
        list = (List)((List)paramMap2.get(group.id)).stream().map(paramGroupMember -> (new GroupMember(paramGroupMember)).with(())).collect(Collectors.toList());
        MapperTools.safeCreateUpdate(32000, (Collection)list.stream().map(paramGroupMember -> paramGroupMember.id).collect(Collectors.toList()), paramList -> this.groupMapper.deleteMembersById(paramGroup.tenantId, paramList));
        EventHelper.send(paramMap.get(group.tenantId), null, new GroupMemberRemoveEvent(paramEventInfo, (new Group(group)).sort(), list));
      } 
    } 
  }
  
  @Transactional
  public void _update(Tenant paramTenant, Group paramGroup1, Group paramGroup2, List<ApplicationRole> paramList, EventInfo paramEventInfo) {
    paramGroup2.tenantId = paramGroup1.tenantId;
    paramGroup2.insertInstant = paramGroup1.insertInstant;
    paramGroup2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.groupMapper.update(paramGroup2);
    this.groupMapper.deleteApplicationRolesByGroupId(paramGroup2.id);
    paramGroup2.roles.clear();
    addApplicationRoles(paramGroup2, paramList);
    EventHelper.send(paramTenant, null, new GroupUpdateEvent(paramEventInfo, (new Group(paramGroup1)).sort(), (new Group(paramGroup2)).sort()));
  }
  
  @Transactional
  public void _updateMembers(Map<UUID, Tenant> paramMap, Map<UUID, Group> paramMap1, Map<UUID, List<GroupMember>> paramMap2, Map<UUID, User> paramMap3, Set<UUID> paramSet, EventInfo paramEventInfo) {
    for (UUID uUID : paramMap2.keySet()) {
      paramSet.addAll(this.groupMapper.retrieveUserIdsByGroupId(uUID));
      this.groupMapper.deleteMembersByGroupId(uUID);
    } 
    _addGroupMembers(paramMap, paramMap1, paramMap2, paramMap3, (paramGroup, paramList) -> new GroupMemberUpdateEvent(paramEventInfo, (new Group(paramGroup)).sort(), paramList));
  }
  
  public void addMembers(Map<UUID, Tenant> paramMap, Map<UUID, Group> paramMap1, Map<UUID, List<GroupMember>> paramMap2, Map<UUID, User> paramMap3, boolean paramBoolean, EventInfo paramEventInfo) {
    _addGroupMembers(paramMap, paramMap1, paramMap2, paramMap3, (paramGroup, paramList) -> new GroupMemberAddEvent(paramEventInfo, (new Group(paramGroup)).sort(), paramList));
    updateSearchIndex(paramMap3.values());
    if (paramBoolean)
      for (Group group : paramMap1.values()) {
        List<GroupMember> list = paramMap2.get(group.id);
        EventHelper.send(paramMap.get(group.tenantId), null, new GroupMemberAddCompleteEvent(paramEventInfo, (new Group(group)).sort(), list));
      }  
  }
  
  public void create(Tenant paramTenant, Group paramGroup, List<ApplicationRole> paramList, boolean paramBoolean, EventInfo paramEventInfo) {
    _create(paramTenant, paramGroup, paramList, paramEventInfo);
    if (paramBoolean)
      EventHelper.send(paramTenant, null, new GroupCreateCompleteEvent(paramEventInfo, (new Group(paramGroup)).sort())); 
  }
  
  public void delete(Tenant paramTenant, Group paramGroup, EventInfo paramEventInfo) {
    List<UUID> list = this.groupMapper.retrieveUserIdsByGroupId(paramGroup.id);
    _delete(paramTenant, paramGroup, paramEventInfo);
    updateSearchIndexByUserIds(list);
    EventHelper.send(paramTenant, null, new GroupDeleteCompleteEvent(paramEventInfo, (new Group(paramGroup)).sort()));
  }
  
  public void deleteAllByTenantId(Tenant paramTenant, EventInfo paramEventInfo) {
    if (paramTenant == null)
      return; 
    this.groupMapper.retrieveAll(paramTenant.id).forEach(paramGroup -> delete(paramTenant, paramGroup, paramEventInfo));
  }
  
  public void removeAllMembers(Tenant paramTenant, Group paramGroup, EventInfo paramEventInfo) {
    _removeAllMembers(paramTenant, paramGroup, paramEventInfo);
    updateSearchIndex(paramGroup.id);
    EventHelper.send(paramTenant, null, new GroupMemberUpdateCompleteEvent(paramEventInfo, (new Group(paramGroup)).sort(), List.of()));
  }
  
  public void removeMember(Tenant paramTenant, Group paramGroup, GroupMember paramGroupMember, EventInfo paramEventInfo) {
    GroupMember groupMember = (new GroupMember(paramGroupMember)).with(paramGroupMember -> paramGroupMember.groupId = null);
    _removeMember(paramTenant, paramGroup, groupMember, paramEventInfo);
    updateSearchIndexByUserIds(List.of(groupMember.userId));
    EventHelper.send(paramTenant, null, new GroupMemberRemoveCompleteEvent(paramEventInfo, (new Group(paramGroup)).sort(), List.of(groupMember)));
  }
  
  public void removeMembers(Map<UUID, Tenant> paramMap, Map<UUID, Group> paramMap1, Map<UUID, User> paramMap2, Map<UUID, List<GroupMember>> paramMap3, EventInfo paramEventInfo) {
    paramMap3.forEach((paramUUID, paramList) -> paramList.forEach(()));
    _removeMembers(paramMap, paramMap1, paramMap3, paramEventInfo);
    updateSearchIndex(paramMap2.values());
    for (Group group : paramMap1.values()) {
      List<GroupMember> list = paramMap3.get(group.id);
      EventHelper.send(paramMap.get(group.tenantId), null, new GroupMemberRemoveCompleteEvent(paramEventInfo, (new Group(group)).sort(), list));
    } 
  }
  
  public void removeUserMemberships(Tenant paramTenant, User paramUser, EventInfo paramEventInfo) {
    GroupMemberSearchCriteria groupMemberSearchCriteria = new GroupMemberSearchCriteria();
    groupMemberSearchCriteria.userId = paramUser.id;
    groupMemberSearchCriteria.tenantId = paramTenant.id;
    List<GroupMember> list = this.groupMapper.retrieveMembersByCriteria(groupMemberSearchCriteria);
    User user = new User(paramUser);
    for (GroupMember groupMember1 : list) {
      Group group = this.groupMapper.retrieveById(paramTenant.id, groupMember1.groupId);
      GroupMember groupMember2 = (new GroupMember(groupMember1)).with(paramGroupMember -> paramGroupMember.groupId = null);
      _removeMember(paramTenant, group, groupMember2, paramEventInfo);
      EventHelper.send(paramTenant, null, new GroupMemberRemoveCompleteEvent(paramEventInfo, (new Group(group)).sort(), List.of(groupMember2)));
      user.removeMembershipById(group.id);
    } 
    this.searchEngine.index(Collections.singletonList(user));
  }
  
  public void update(Tenant paramTenant, Group paramGroup1, Group paramGroup2, List<ApplicationRole> paramList, boolean paramBoolean, EventInfo paramEventInfo) {
    _update(paramTenant, paramGroup1, paramGroup2, paramList, paramEventInfo);
    if (paramBoolean)
      EventHelper.send(paramTenant, null, new GroupUpdateCompleteEvent(paramEventInfo, (new Group(paramGroup1)).sort(), (new Group(paramGroup2)).sort())); 
  }
  
  public void updateMembers(Map<UUID, Tenant> paramMap, Map<UUID, Group> paramMap1, Map<UUID, User> paramMap2, Map<UUID, List<GroupMember>> paramMap3, boolean paramBoolean, EventInfo paramEventInfo) {
    HashSet<UUID> hashSet = new HashSet();
    _updateMembers(paramMap, paramMap1, paramMap3, paramMap2, hashSet, paramEventInfo);
    updateSearchIndex(paramMap2.values());
    Set set = (Set)paramMap2.values().stream().map(paramUser -> paramUser.id).collect(Collectors.toSet());
    List<UUID> list = (List)hashSet.stream().filter(paramUUID -> !paramSet.contains(paramUUID)).collect(Collectors.toList());
    updateSearchIndexByUserIds(list);
    if (paramBoolean)
      for (Group group : paramMap1.values()) {
        List<GroupMember> list1 = paramMap3.get(group.id);
        if (list1 != null && !list1.isEmpty()) {
          list1 = (List)list1.stream().map(paramGroupMember -> (new GroupMember(paramGroupMember)).with(())).collect(Collectors.toList());
          EventHelper.send(paramMap.get(group.tenantId), null, new GroupMemberUpdateCompleteEvent(paramEventInfo, (new Group(group)).sort(), list1));
        } 
      }  
  }
  
  public GroupService.ValidationResult validateAddOrUpdateMembers(Tenant paramTenant, Map<UUID, List<GroupMember>> paramMap) {
    GroupService.ValidationResult validationResult = new GroupService.ValidationResult();
    Set<?> set = (Set)paramMap.values().stream().flatMap(Collection::stream).filter(paramGroupMember -> (paramGroupMember.userId != null)).map(paramGroupMember -> paramGroupMember.userId).collect(Collectors.toSet());
    List<?> list = MapperTools.safeRetrieve(5000, set, paramList -> this.userReader.retrieveByIds((paramTenant != null) ? paramTenant.id : null, paramList, UserReaderService.UserExpansion.all()));
    validationResult.users = (Map<UUID, User>)list.stream().collect(Collectors.toMap(paramUser -> paramUser.id, paramUser -> paramUser));
    validationResult



      
      .groups = (Map<UUID, Group>)paramMap.keySet().stream().map(paramUUID -> this.groupMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID)).filter(Objects::nonNull).collect(Collectors.toMap(paramGroup -> paramGroup.id, paramGroup -> paramGroup));
    validationResult.tenant = paramTenant;
    Objects.requireNonNull(this.tenantReader);
    validationResult.tenants = (Map<UUID, Tenant>)validationResult.groups.values().stream().map(paramGroup -> paramGroup.tenantId).distinct().map(this.tenantReader::retrieveById).collect(Collectors.toMap(paramTenant -> paramTenant.id, paramTenant -> paramTenant));
    validationResult
















      
      .errors = (new Validator()).forEach(paramMap.entrySet(), (paramValidator, paramEntry, paramInteger) -> paramValidator.forEach((Iterable)paramEntry.getValue(), ()).validObjectWithCode(paramValidationResult.groups.get(paramEntry.getKey()), "members[" + String.valueOf(paramEntry.getKey()) + "]", "[invalid]members", new Object[] { paramEntry.getKey() })).done();
    return validationResult;
  }
  
  public GroupService.ValidationResult validateCreate(Tenant paramTenant, Group paramGroup, List<UUID> paramList) {
    Map<UUID, ApplicationRole> map = loadRoles(paramTenant, paramList);
    GroupService.ValidationResult validationResult = new GroupService.ValidationResult();
    validationResult.roles = (paramList != null) ? this.applicationReader.retrieveRolesByIds(paramTenant.id, paramList) : Collections.<ApplicationRole>emptyList();
    validationResult




      
      .errors = (new Validator()).notBlank(paramGroup.name, "group.name", new Object[0]).ifTrue((paramGroup.id != null), paramValidator -> paramValidator.notDuplicate(this.groupMapper.retrieveById(null, paramGroup.id), "groupId", new Object[] { paramGroup.id })).ifLastCheckHadNoError(paramValidator -> paramValidator.notDuplicate(this.groupMapper.retrieveByName(paramTenant.id, paramGroup.name), "group.name", new Object[] { paramGroup.name })).forEach(paramList, (paramValidator, paramUUID, paramInteger) -> paramValidator.validObject(paramMap.get(paramUUID), "roleIds[" + paramInteger + "]", new Object[] { paramUUID })).done();
    return validationResult;
  }
  
  public GroupService.ValidationResult validateDelete(Tenant paramTenant, UUID paramUUID) {
    GroupService.ValidationResult validationResult = new GroupService.ValidationResult();
    validationResult.existing = this.groupMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID);
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.existing });
    validationResult.errors = new Errors();
    return validationResult;
  }
  
  public GroupService.ValidationResult validateRemoveMemberById(Tenant paramTenant, UUID paramUUID) {
    GroupService.ValidationResult validationResult = new GroupService.ValidationResult();
    validationResult.member = this.groupMapper.retrieveMemberById((paramTenant != null) ? paramTenant.id : null, paramUUID);
    validationResult.group = (validationResult.member != null) ? this.groupMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, validationResult.member.groupId) : null;
    User user = (validationResult.member != null) ? this.userReader.retrieveById((paramTenant != null) ? paramTenant.id : null, validationResult.member.userId) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { user });
    return validationResult;
  }
  
  public GroupService.ValidationResult validateRemoveMemberByUserAndGroup(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2) {
    GroupService.ValidationResult validationResult = new GroupService.ValidationResult();
    validationResult.member = this.groupMapper.retrieveMemberByUserId((paramTenant != null) ? paramTenant.id : null, paramUUID1, paramUUID2);
    validationResult.group = (paramUUID1 != null) ? this.groupMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID1) : null;
    User user = (paramUUID2 != null) ? this.userReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID2) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.group, user });
    return validationResult;
  }
  
  public GroupService.ValidationResult validateRemoveMembers(Tenant paramTenant, Map<UUID, List<UUID>> paramMap) {
    GroupService.ValidationResult validationResult = new GroupService.ValidationResult();
    Set<?> set = (Set)paramMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    validationResult.members = new HashMap<>();
    for (UUID uUID : paramMap.keySet())
      ((List)paramMap.get(uUID)).stream()
        .map(paramUUID2 -> this.groupMapper.retrieveMemberByUserId((paramTenant != null) ? paramTenant.id : null, paramUUID1, paramUUID2))
        .filter(Objects::nonNull)
        .forEach(paramGroupMember -> ((List<GroupMember>)paramValidationResult.members.computeIfAbsent(paramGroupMember.groupId, ())).add(paramGroupMember)); 
    List<?> list = MapperTools.safeRetrieve(5000, set, paramList -> this.userReader.retrieveByIds((paramTenant != null) ? paramTenant.id : null, paramList, UserReaderService.UserExpansion.all()));
    validationResult.users = (Map<UUID, User>)list.stream().collect(Collectors.toMap(paramUser -> paramUser.id, paramUser -> paramUser));
    validationResult



      
      .groups = (Map<UUID, Group>)paramMap.keySet().stream().map(paramUUID -> this.groupMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID)).filter(Objects::nonNull).collect(Collectors.toMap(paramGroup -> paramGroup.id, paramGroup -> paramGroup));
    Objects.requireNonNull(this.tenantReader);
    validationResult.tenants = (Map<UUID, Tenant>)validationResult.groups.values().stream().map(paramGroup -> paramGroup.tenantId).distinct().map(this.tenantReader::retrieveById).filter(Objects::nonNull).collect(Collectors.toMap(paramTenant -> paramTenant.id, paramTenant -> paramTenant));
    validationResult























      
      .errors = (new Validator()).forEach(paramMap.keySet(), (paramValidator, paramUUID, paramInteger) -> paramValidator.validObjectWithCode(paramValidationResult.groups.get(paramUUID), "members[" + String.valueOf(paramUUID) + "]", "[invalid]members", new Object[] { paramUUID })).forEach(paramMap.entrySet(), (paramValidator, paramEntry, paramInteger) -> paramValidator.ifTrue((paramValidationResult.groups.get(paramEntry.getKey()) != null), ())).done();
    return validationResult;
  }
  
  public GroupService.ValidationResult validateRemoveMembersByGroupId(Tenant paramTenant, UUID paramUUID) {
    GroupService.ValidationResult validationResult = new GroupService.ValidationResult();
    validationResult.group = this.groupMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID);
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.group });
    return validationResult;
  }
  
  public GroupService.ValidationResult validateRemoveMembersByIds(Tenant paramTenant, List<UUID> paramList) {
    GroupService.ValidationResult validationResult = new GroupService.ValidationResult();
    validationResult.members = new HashMap<>();
    paramList.stream()
      .map(paramUUID -> this.groupMapper.retrieveMemberById((paramTenant != null) ? paramTenant.id : null, paramUUID))
      .forEach(paramGroupMember -> ((List<GroupMember>)paramValidationResult.members.computeIfAbsent(paramGroupMember.groupId, ())).add(paramGroupMember));
    List<?> list1 = (List)validationResult.members.values().stream().flatMap(Collection::stream).map(paramGroupMember -> paramGroupMember.userId).distinct().collect(Collectors.toList());
    List<?> list2 = MapperTools.safeRetrieve(5000, list1, paramList -> this.userReader.retrieveByIds((paramTenant != null) ? paramTenant.id : null, paramList, UserReaderService.UserExpansion.all()));
    validationResult.users = (Map<UUID, User>)list2.stream().collect(Collectors.toMap(paramUser -> paramUser.id, paramUser -> paramUser));
    validationResult






      
      .groups = (Map<UUID, Group>)validationResult.members.values().stream().flatMap(Collection::stream).map(paramGroupMember -> paramGroupMember.groupId).distinct().map(paramUUID -> this.groupMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID)).filter(Objects::nonNull).collect(Collectors.toMap(paramGroup -> paramGroup.id, paramGroup -> paramGroup));
    Objects.requireNonNull(this.tenantReader);
    validationResult.tenants = (Map<UUID, Tenant>)validationResult.groups.values().stream().map(paramGroup -> paramGroup.tenantId).distinct().map(this.tenantReader::retrieveById).collect(Collectors.toMap(paramTenant -> paramTenant.id, paramTenant -> paramTenant));
    return validationResult;
  }
  
  public GroupService.ValidationResult validateRetrieve(Tenant paramTenant, UUID paramUUID) {
    GroupService.ValidationResult validationResult = new GroupService.ValidationResult();
    validationResult.group = (paramUUID != null) ? this.groupMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.group });
    return validationResult;
  }
  
  public GroupService.ValidationResult validateUpdate(Tenant paramTenant, Group paramGroup, List<UUID> paramList) {
    GroupService.ValidationResult validationResult = new GroupService.ValidationResult();
    UUID uUID = (paramTenant != null) ? paramTenant.id : null;
    validationResult.existing = (paramGroup.id != null) ? this.groupMapper.retrieveById(uUID, paramGroup.id) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.existing });
    if (validationResult.existing == null)
      return validationResult; 
    Map<UUID, ApplicationRole> map = loadRoles(validationResult.tenant, paramList);
    validationResult.roles = new ArrayList<>(map.values());
    validationResult








      
      .errors = (new Validator()).notBlank(paramGroup.name, "group.name", new Object[0]).notMissing(paramGroup.id, "groupId", new Object[0]).notDuplicate(this.groupMapper.retrieveExisting(validationResult.tenant.id, paramGroup.id, paramGroup.name), "group.name", new Object[] { paramGroup.name }).forEach(paramList, (paramValidator, paramUUID, paramInteger) -> paramValidator.validObject(paramMap.get(paramUUID), "roleIds[" + paramInteger + "]", new Object[] { paramUUID })).done();
    return validationResult;
  }
  
  private void addApplicationRoles(Group paramGroup, List<ApplicationRole> paramList) {
    if (paramList == null || paramList.isEmpty())
      return; 
    MapperTools.safeCreateUpdate(5000, (Collection)paramList.stream().map(paramApplicationRole -> paramApplicationRole.id).distinct().collect(Collectors.toList()), paramList -> this.groupMapper.createApplicationRoles(paramGroup.id, paramList));
    paramList.forEach(paramApplicationRole -> ((List<ApplicationRole>)paramGroup.roles.computeIfAbsent(paramApplicationRole.applicationId, ())).add(paramApplicationRole));
  }
  
  private Map<UUID, ApplicationRole> loadRoles(Tenant paramTenant, List<UUID> paramList) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    List<?> list = MapperTools.safeRetrieve(1000, paramList, paramList -> this.applicationReader.retrieveRolesByIds(paramTenant.id, paramList));
    list.forEach(paramApplicationRole -> paramMap.put(paramApplicationRole.id, paramApplicationRole));
    return (Map)hashMap;
  }
  
  private void updateSearchIndex(UUID paramUUID) {
    List<UUID> list = this.groupMapper.retrieveUserIdsByGroupId(paramUUID);
    updateSearchIndexByUserIds(list);
  }
  
  private void updateSearchIndex(Collection<User> paramCollection) {
    char c = 'ú';
    Iterator<User> iterator = paramCollection.iterator();
    ArrayList<User> arrayList = new ArrayList();
    while (iterator.hasNext()) {
      if (arrayList.size() == c) {
        this.searchEngine.index(arrayList);
        arrayList.clear();
      } 
      arrayList.add(iterator.next());
    } 
    if (arrayList.size() > 0)
      this.searchEngine.index(arrayList); 
  }
  
  private void updateSearchIndexByUserIds(List<UUID> paramList) {
    int i = 0;
    char c = 'ú';
    while (i < paramList.size()) {
      List<User> list = this.userReader.retrieveByIds(null, paramList.subList(i, Math.min(paramList.size(), i + c)), UserReaderService.UserExpansion.all());
      this.searchEngine.index(list);
      i += c;
    } 
  }
}
