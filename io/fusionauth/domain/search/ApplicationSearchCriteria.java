package io.fusionauth.domain.search;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.ObjectState;
import io.fusionauth.domain.util.SQLTools;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ApplicationSearchCriteria extends BaseSearchCriteria implements Buildable<ApplicationSearchCriteria> {
  public static final Set<String> NullableFields = new HashSet<>();
  
  public static final Map<String, String> SortableFields = new LinkedHashMap<>();
  
  public String name;
  
  public ObjectState state;
  
  public UUID tenantId;
  
  public Boolean universal;
  
  public ApplicationSearchCriteria prepare() {
    if (this.orderBy == null)
      this.orderBy = defaultOrderBy(); 
    this.orderBy = SQLTools.normalizeOrderBy(this.orderBy, SortableFields, NullableFields);
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
    NullableFields.add("tenant");
    SortableFields.put("id", "a.id");
    SortableFields.put("insertInstant", "a.insert_instant");
    SortableFields.put("name", "a.name");
    SortableFields.put("tenant", "t.name");
  }
}
