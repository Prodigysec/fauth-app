package io.fusionauth.api.service.group;

import com.google.inject.Inject;
import io.fusionauth.api.domain.GroupMapper;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.search.GroupMemberSearchCriteria;
import io.fusionauth.domain.search.GroupSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;

public class DefaultGroupReaderService implements GroupReaderService {
  private final GroupMapper groupMapper;
  
  @Inject
  public DefaultGroupReaderService(GroupMapper paramGroupMapper) {
    this.groupMapper = paramGroupMapper;
  }
  
  public List<Group> retrieveAll(UUID paramUUID) {
    return this.groupMapper.retrieveAll(paramUUID);
  }
  
  public Group retrieveById(UUID paramUUID1, UUID paramUUID2) {
    return this.groupMapper.retrieveById(paramUUID1, paramUUID2);
  }
  
  public List<GroupMember> retrieveMembershipsByUserId(UUID paramUUID1, UUID paramUUID2) {
    return this.groupMapper.retrieveMembersByUserId(paramUUID1, paramUUID2);
  }
  
  public SearchResults<Group> search(GroupSearchCriteria paramGroupSearchCriteria) {
    return search(paramGroupSearchCriteria, false);
  }
  
  public SearchResults<Group> search(GroupSearchCriteria paramGroupSearchCriteria, boolean paramBoolean) {
    int i = this.groupMapper.retrieveCountByCriteria(paramGroupSearchCriteria, paramBoolean);
    List<Group> list = (i > 0) ? this.groupMapper.retrieveByCriteria(paramGroupSearchCriteria, paramBoolean) : List.of();
    return new SearchResults<>(list, i);
  }
  
  public SearchResults<GroupMember> search(GroupMemberSearchCriteria paramGroupMemberSearchCriteria) {
    int i = this.groupMapper.retrieveMembersCountByCriteria(paramGroupMemberSearchCriteria);
    List<GroupMember> list = (i > 0) ? this.groupMapper.retrieveMembersByCriteria(paramGroupMemberSearchCriteria) : List.of();
    return new SearchResults<>(list, i);
  }
}
