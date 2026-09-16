package io.fusionauth.domain.search;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.util.SQLTools;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GroupMemberSearchCriteria extends BaseSearchCriteria implements Buildable<GroupMemberSearchCriteria> {
  public static final Map<String, String> SortableFields = new LinkedHashMap<>();
  
  public UUID groupId;
  
  public UUID tenantId;
  
  public UUID userId;
  
  public GroupMemberSearchCriteria prepare() {
    if (this.orderBy == null)
      this.orderBy = defaultOrderBy(); 
    this.orderBy = SQLTools.normalizeOrderBy(this.orderBy, SortableFields);
    return this;
  }
  
  public Set<String> supportedOrderByColumns() {
    return SortableFields.keySet();
  }
  
  protected String defaultOrderBy() {
    return "insertInstant ASC, userId ASC, groupId ASC";
  }
  
  static {
    SortableFields.put("id", "gm.id");
    SortableFields.put("insertInstant", "gm.insert_instant");
    SortableFields.put("groupId", "gm.groups_id");
    SortableFields.put("tenantId", "g.tenants_id");
    SortableFields.put("userId", "gm.users_id");
  }
}
