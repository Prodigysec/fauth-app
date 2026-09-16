package io.fusionauth.domain.search;

import io.fusionauth.domain.Buildable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class EntitySearchCriteria extends BaseElasticSearchCriteria implements Buildable<EntitySearchCriteria> {
  public static final Set<String> SortableFields = new LinkedHashSet<>(Arrays.asList(new String[] { "clientId", "id", "insertInstant", "lastUpdateInstant", "name", "parentId", "tenantId", "typeId" }));
  
  public EntitySearchCriteria prepare() {
    if (this.orderBy == null)
      this.orderBy = defaultOrderBy(); 
    return this;
  }
  
  protected String defaultOrderBy() {
    return "name ASC";
  }
}
