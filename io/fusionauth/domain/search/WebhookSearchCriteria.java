package io.fusionauth.domain.search;

import io.fusionauth.domain.util.SQLTools;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WebhookSearchCriteria extends BaseSearchCriteria {
  public static final Map<String, String> SortableFields = new LinkedHashMap<>();
  
  public String description;
  
  public UUID tenantId;
  
  public String url;
  
  public WebhookSearchCriteria prepare() {
    if (this.orderBy == null)
      this.orderBy = defaultOrderBy(); 
    this.orderBy = SQLTools.normalizeOrderBy(this.orderBy, SortableFields);
    this.description = SQLTools.toSearchString(this.description);
    this.url = SQLTools.toSearchString(this.url);
    return this;
  }
  
  public Set<String> supportedOrderByColumns() {
    return SortableFields.keySet();
  }
  
  protected String defaultOrderBy() {
    return "insertInstant DESC, id DESC";
  }
  
  static {
    SortableFields.put("id", "w.id");
    SortableFields.put("insertInstant", "w.insert_instant");
    SortableFields.put("description", "w.description");
    SortableFields.put("url", "w.url");
  }
}
