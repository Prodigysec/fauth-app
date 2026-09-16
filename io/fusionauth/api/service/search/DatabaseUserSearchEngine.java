package io.fusionauth.api.service.search;

import com.google.inject.Inject;
import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.domain.User;
import io.fusionauth.domain.search.Sort;
import io.fusionauth.domain.search.SortField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DatabaseUserSearchEngine extends BaseDatabaseSearchEngine implements UserSearchEngine {
  public static final String USERS_ID_COLUMN = "id";
  
  private static final Map<String, String> FIELD_MAPPINGS = new HashMap<>();
  
  private static final String USERS_TENANTS_ID_COLUMN = "tenants_id";
  
  private final UserMapper userMapper;
  
  @Inject
  public DatabaseUserSearchEngine(UserMapper paramUserMapper) {
    this.userMapper = paramUserMapper;
  }
  
  public void index(Collection<User> paramCollection) {}
  
  public SearchEngine.SearchEngineStatus status() {
    return null;
  }
  
  protected List<SortField> defaultOrderBy() {
    return List.of(new SortField("email", Sort.asc));
  }
  
  protected Map<String, String> fieldMappings() {
    return FIELD_MAPPINGS;
  }
  
  protected List<UUID> searchByTerm(UUID paramUUID, String paramString, int paramInt1, int paramInt2, List<SortField> paramList) {
    Map<String, String> map = fieldMappings();
    if (paramList == null || paramList.isEmpty())
      paramList = defaultOrderBy(); 
    ArrayList<String> arrayList1 = new ArrayList();
    ArrayList<String> arrayList2 = new ArrayList();
    for (SortField sortField : paramList) {
      String str3, str1 = map.get(sortField.name);
      if (str1 == null)
        throw new IllegalStateException("Missing database column mapping for sort field [" + sortField.name + "]"); 
      String str2 = "subquery_%s".formatted(new Object[] { str1 });
      if (str1.equals("tenants_id") || str1.equals("id")) {
        str3 = String.format("%s AS %s", new Object[] { str1, str2 });
      } else {
        String str = (sortField.order == Sort.asc) ? "MIN" : "MAX";
        str3 = String.format("%s(%s) AS %s", new Object[] { str, str1, str2 });
      } 
      arrayList2.add(str3);
      arrayList1.add(str2 + " " + str2);
    } 
    return this.userMapper.searchByTerm(paramUUID, paramString, paramInt1, paramInt2, 
        String.join(", ", (Iterable)arrayList1), 
        String.join(", ", (Iterable)arrayList2));
  }
  
  protected long searchByTermCount(UUID paramUUID, String paramString) {
    return this.userMapper.searchByTermCount(paramUUID, paramString);
  }
  
  static {
    FIELD_MAPPINGS.put("birthDate", "birth_date");
    FIELD_MAPPINGS.put("email", "email");
    FIELD_MAPPINGS.put("fullName", "full_name");
    FIELD_MAPPINGS.put("id", "id");
    FIELD_MAPPINGS.put("insertInstant", "insert_instant");
    FIELD_MAPPINGS.put("lastLoginInstant", "last_login_instant");
    FIELD_MAPPINGS.put("login", "login");
    FIELD_MAPPINGS.put("phoneNumber", "phone_number");
    FIELD_MAPPINGS.put("tenantId", "tenants_id");
    FIELD_MAPPINGS.put("username", "username");
  }
}
