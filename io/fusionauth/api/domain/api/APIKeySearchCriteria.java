package io.fusionauth.api.domain.api;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.util.SQLTools;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class APIKeySearchCriteria extends BaseSearchCriteria implements Buildable<APIKeySearchCriteria> {
  public static final Map<String, String> SortableFields = new LinkedHashMap<>();
  
  public ZonedDateTime expirationEnd;
  
  public ZonedDateTime expirationStart;
  
  public String key;
  
  public Boolean keyManager;
  
  public String nameOrDescription;
  
  public UUID tenantId;
  
  public APIKeySearchCriteria() {}
  
  public APIKeySearchCriteria(APIKeySearchCriteria paramAPIKeySearchCriteria) {
    this.expirationEnd = paramAPIKeySearchCriteria.expirationEnd;
    this.expirationStart = paramAPIKeySearchCriteria.expirationStart;
    this.key = paramAPIKeySearchCriteria.key;
    this.keyManager = paramAPIKeySearchCriteria.keyManager;
    this.nameOrDescription = paramAPIKeySearchCriteria.nameOrDescription;
    this.tenantId = paramAPIKeySearchCriteria.tenantId;
    this.numberOfResults = paramAPIKeySearchCriteria.numberOfResults;
    this.orderBy = paramAPIKeySearchCriteria.orderBy;
    this.startRow = paramAPIKeySearchCriteria.startRow;
  }
  
  public BaseSearchCriteria prepare() {
    if (this.orderBy == null)
      this.orderBy = defaultOrderBy(); 
    this.orderBy = SQLTools.normalizeOrderBy(this.orderBy, SortableFields);
    this.key = SQLTools.toSearchString(this.key);
    this.nameOrDescription = SQLTools.toSearchString(this.nameOrDescription);
    return this;
  }
  
  public Set<String> supportedOrderByColumns() {
    return SortableFields.keySet();
  }
  
  protected String defaultOrderBy() {
    return "insertInstant ASC";
  }
  
  static {
    SortableFields.put("expirationInstant", "k.expiration_instant");
    SortableFields.put("id", "k.id");
    SortableFields.put("insertInstant", "k.insert_instant");
    SortableFields.put("keyManager", "k.key_manager");
    SortableFields.put("key", "k.key_value");
    SortableFields.put("name", "k.name");
  }
}
