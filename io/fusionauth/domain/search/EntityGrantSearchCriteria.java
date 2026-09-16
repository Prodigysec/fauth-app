package io.fusionauth.domain.search;

import io.fusionauth.domain.util.SQLTools;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EntityGrantSearchCriteria extends BaseSearchCriteria {
  public static final Map<String, String> SortableFields = new LinkedHashMap<>();
  
  public UUID entityId;
  
  public String name;
  
  public UUID userId;
  
  public EntityGrantSearchCriteria prepare() {
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
    SortableFields.put("insertInstant", "eg.insert_instant");
    SortableFields.put("name", "name");
  }
}
