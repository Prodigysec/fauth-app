package io.fusionauth.domain.search;

import io.fusionauth.domain.ThemeType;
import io.fusionauth.domain.util.SQLTools;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ThemeSearchCriteria extends BaseSearchCriteria {
  public static final Map<String, String> SortableFields = new LinkedHashMap<>();
  
  public String name;
  
  public ThemeType type;
  
  public BaseSearchCriteria prepare() {
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
    return "insertInstant ASC";
  }
  
  static {
    SortableFields.put("id", "id");
    SortableFields.put("insertInstant", "insert_instant");
    SortableFields.put("name", "name");
  }
}
