package io.fusionauth.api.service.group;

import io.fusionauth.domain.Group;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.search.GroupMemberSearchCriteria;
import io.fusionauth.domain.search.GroupSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;

public interface GroupReaderService {
  List<Group> retrieveAll(UUID paramUUID);
  
  Group retrieveById(UUID paramUUID1, UUID paramUUID2);
  
  List<GroupMember> retrieveMembershipsByUserId(UUID paramUUID1, UUID paramUUID2);
  
  SearchResults<Group> search(GroupSearchCriteria paramGroupSearchCriteria);
  
  SearchResults<Group> search(GroupSearchCriteria paramGroupSearchCriteria, boolean paramBoolean);
  
  SearchResults<GroupMember> search(GroupMemberSearchCriteria paramGroupMemberSearchCriteria);
}
