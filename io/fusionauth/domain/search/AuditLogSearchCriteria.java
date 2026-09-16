package io.fusionauth.domain.search;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.util.SQLTools;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AuditLogSearchCriteria extends BaseSearchCriteria implements Buildable<AuditLogSearchCriteria> {
  public static final Set<String> NullableFields = new HashSet<>();
  
  public static final Map<String, String> SortableFields = new LinkedHashMap<>();
  
  public ZonedDateTime end;
  
  public String message;
  
  public String newValue;
  
  public String oldValue;
  
  public String reason;
  
  public ZonedDateTime start;
  
  public UUID tenantId;
  
  public String user;
  
  @JacksonConstructor
  public AuditLogSearchCriteria() {}
  
  public AuditLogSearchCriteria(UUID paramUUID, String paramString1, String paramString2, ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2, String paramString3) {
    this.end = paramZonedDateTime2;
    this.message = paramString1;
    this.start = paramZonedDateTime1;
    this.tenantId = paramUUID;
    this.user = paramString2;
    this.orderBy = paramString3;
  }
  
  public AuditLogSearchCriteria(String paramString1, String paramString2, ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2, int paramInt1, int paramInt2, String paramString3) {
    this.end = paramZonedDateTime2;
    this.message = paramString1;
    this.start = paramZonedDateTime1;
    this.user = paramString2;
    this.startRow = paramInt1;
    this.numberOfResults = paramInt2;
    this.orderBy = paramString3;
  }
  
  public AuditLogSearchCriteria prepare() {
    if (this.orderBy == null)
      this.orderBy = defaultOrderBy(); 
    this.orderBy = SQLTools.normalizeOrderBy(this.orderBy, SortableFields, NullableFields);
    this.user = SQLTools.toSearchString(this.user);
    this.message = SQLTools.toSearchString(this.message);
    this.newValue = SQLTools.toSearchString(this.newValue);
    this.oldValue = SQLTools.toSearchString(this.oldValue);
    this.reason = SQLTools.toSearchString(this.reason);
    return this;
  }
  
  public Set<String> supportedOrderByColumns() {
    return SortableFields.keySet();
  }
  
  protected String defaultOrderBy() {
    return "insertInstant DESC";
  }
  
  static {
    NullableFields.add("tenant");
    SortableFields.put("insertInstant", "al.insert_instant");
    SortableFields.put("insertUser", "al.insert_user");
    SortableFields.put("message", "al.message");
    SortableFields.put("tenant", "t.name");
  }
}
