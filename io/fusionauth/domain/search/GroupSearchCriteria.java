package io.fusionauth.domain.search;

import io.fusionauth.domain.util.SQLTools;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GroupSearchCriteria extends BaseSearchCriteria {
  public static final Map<String, String> SortableFields = new LinkedHashMap<>();
  
  public String name;
  
  public UUID tenantId;
  
  public GroupSearchCriteria prepare() {
    if (this.orderBy == null)
      this.orderBy = defaultOrderBy(); 
    this.orderBy = SQLTools.normalizeOrderBy(this.orderBy, SortableFields);
    this.name = SQLTools.toSearchString(this.name);
    return this;
  }
  
  public Set<String> supportedOrderByColumns() {
    return SortableFields.keySet();
  }
  
  protected String defaultOrderBy() {
    return "name ASC";
  }
  
  static {
    SortableFields.put("id", "g.id");
    SortableFields.put("insertInstant", "g.insert_instant");
    SortableFields.put("name", "g.name");
    SortableFields.put("tenant", "t.name");
  }
}
