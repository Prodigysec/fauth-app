package io.fusionauth.domain.search;

import io.fusionauth.domain.util.SQLTools;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UserCommentSearchCriteria extends BaseSearchCriteria {
  public static final Map<String, String> SortableFields = new LinkedHashMap<>();
  
  public String comment;
  
  public UUID commenterId;
  
  public UUID tenantId;
  
  public UUID userId;
  
  public UserCommentSearchCriteria prepare() {
    if (this.orderBy == null)
      this.orderBy = defaultOrderBy(); 
    this.orderBy = SQLTools.normalizeOrderBy(this.orderBy, SortableFields);
    this.comment = SQLTools.toSearchString(this.comment);
    return this;
  }
  
  public Set<String> supportedOrderByColumns() {
    return SortableFields.keySet();
  }
  
  protected String defaultOrderBy() {
    return "insertInstant DESC";
  }
  
  static {
    SortableFields.put("id", "u.id");
    SortableFields.put("commenterId", "u.commenter_id");
    SortableFields.put("insertInstant", "u.insert_instant");
    SortableFields.put("tenantId", "uu.tenants_id");
    SortableFields.put("userId", "u.users_id");
    SortableFields.put("comment", "u.comment");
  }
}
