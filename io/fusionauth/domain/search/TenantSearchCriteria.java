package io.fusionauth.domain.search;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.util.SQLTools;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TenantSearchCriteria extends BaseSearchCriteria implements Buildable<TenantSearchCriteria> {
  public static final Map<String, String> SortableFields = new LinkedHashMap<>();
  
  public String name;
  
  public TenantSearchCriteria prepare() {
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
    SortableFields.put("id", "t.id");
    SortableFields.put("insertInstant", "t.insert_instant");
    SortableFields.put("name", "t.name");
  }
}
