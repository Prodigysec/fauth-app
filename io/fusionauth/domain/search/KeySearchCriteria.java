package io.fusionauth.domain.search;

import io.fusionauth.domain.Key;
import io.fusionauth.domain.util.SQLTools;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class KeySearchCriteria extends BaseSearchCriteria {
  public static final Set<String> NullableFields = new HashSet<>();
  
  public static final Map<String, String> SortableFields = new LinkedHashMap<>();
  
  public Key.KeyAlgorithm algorithm;
  
  public String name;
  
  public Key.KeyType type;
  
  public KeySearchCriteria prepare() {
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
    NullableFields.add("algorithm");
    NullableFields.add("expiration");
    SortableFields.put("id", "k.id");
    SortableFields.put("name", "k.name");
    SortableFields.put("type", "k.type");
    SortableFields.put("algorithm", "k.algorithm");
    SortableFields.put("expiration", "k.expiration_instant");
    SortableFields.put("insertInstant", "k.insert_instant");
  }
}
