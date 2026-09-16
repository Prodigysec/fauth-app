package io.fusionauth.api.service.search;

import com.google.inject.Inject;
import io.fusionauth.api.domain.EntityMapper;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.search.Sort;
import io.fusionauth.domain.search.SortField;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DatabaseEntitySearchEngine extends BaseDatabaseSearchEngine implements EntitySearchEngine {
  private static final Map<String, String> FIELD_MAPPINGS = new HashMap<>();
  
  private final EntityMapper entityMapper;
  
  @Inject
  public DatabaseEntitySearchEngine(EntityMapper paramEntityMapper) {
    this.entityMapper = paramEntityMapper;
  }
  
  public void deleteByType(UUID paramUUID) {}
  
  public void index(Collection<Entity> paramCollection) {}
  
  protected List<SortField> defaultOrderBy() {
    return List.of(new SortField("name", Sort.asc));
  }
  
  protected Map<String, String> fieldMappings() {
    return FIELD_MAPPINGS;
  }
  
  protected List<UUID> searchByTerm(UUID paramUUID, String paramString, int paramInt1, int paramInt2, List<SortField> paramList) {
    Map<String, String> map = fieldMappings();
    if (paramList == null || paramList.isEmpty())
      paramList = defaultOrderBy(); 
    String str = paramList.stream().map(paramSortField -> {
          String str = (String)paramMap.get(paramSortField.name);
          if (str == null)
            throw new IllegalStateException("Missing database column mapping for sort field [" + paramSortField.name + "]"); 
          return str + " " + str;
        }).collect(Collectors.joining(", "));
    return this.entityMapper.searchByTerm(paramUUID, paramString, paramInt1, paramInt2, str);
  }
  
  protected long searchByTermCount(UUID paramUUID, String paramString) {
    return this.entityMapper.searchByTermCount(paramUUID, paramString);
  }
  
  static {
    FIELD_MAPPINGS.put("clientId", "e.client_id");
    FIELD_MAPPINGS.put("id", "e.email");
    FIELD_MAPPINGS.put("insertInstant", "e.insert_instant");
    FIELD_MAPPINGS.put("lastUpdateInstant", "e.last_update_instant");
    FIELD_MAPPINGS.put("name", "e.name");
    FIELD_MAPPINGS.put("parentId", "e.parent_id");
    FIELD_MAPPINGS.put("tenantId", "e.tenants_id");
    FIELD_MAPPINGS.put("typeId", "et.id");
  }
}
