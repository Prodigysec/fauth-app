package io.fusionauth.domain.search;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.util.SQLTools;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class LambdaSearchCriteria extends BaseSearchCriteria implements Buildable<LambdaSearchCriteria> {
  public static final Map<String, String> SortableFields = new LinkedHashMap<>();
  
  public String body;
  
  public String name;
  
  public LambdaType type;
  
  public LambdaSearchCriteria prepare() {
    if (this.orderBy == null)
      this.orderBy = defaultOrderBy(); 
    this.orderBy = SQLTools.normalizeOrderBy(this.orderBy, SortableFields);
    this.body = SQLTools.toSearchString(this.body);
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
    SortableFields.put("id", "id");
    SortableFields.put("insertInstant", "insert_instant");
    SortableFields.put("name", "name");
    SortableFields.put("engineType", "engine_type");
  }
}
