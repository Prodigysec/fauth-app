package io.fusionauth.domain.search;

import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.util.SQLTools;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class IdentityProviderSearchCriteria extends BaseSearchCriteria {
  public static final Map<String, String> SortableFields = new LinkedHashMap<>();
  
  public UUID applicationId;
  
  public String name;
  
  public String source;
  
  public UUID tenantId;
  
  public IdentityProviderType type;
  
  public IdentityProviderSearchCriteria prepare() {
    if (this.orderBy == null)
      this.orderBy = defaultOrderBy(); 
    this.orderBy = SQLTools.normalizeOrderBy(this.orderBy, SortableFields);
    this.name = SQLTools.toSearchString(this.name);
    this.source = SQLTools.toSearchString(this.source);
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
    SortableFields.put("enabled", "enabled");
    SortableFields.put("source", "source");
    SortableFields.put("tenantId", "tenants_id");
    SortableFields.put("type", "type");
  }
}
