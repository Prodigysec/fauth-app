package io.fusionauth.domain.search;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.util.SQLTools;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class EventLogSearchCriteria extends BaseSearchCriteria {
  public static final Map<String, String> SortableFields = new LinkedHashMap<>();
  
  public ZonedDateTime end;
  
  public String message;
  
  public ZonedDateTime start;
  
  public EventLogType type;
  
  @JacksonConstructor
  public EventLogSearchCriteria() {}
  
  public EventLogSearchCriteria(String paramString1, EventLogType paramEventLogType, ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2, int paramInt1, int paramInt2, String paramString2) {
    this.end = paramZonedDateTime2;
    this.message = paramString1;
    this.numberOfResults = paramInt2;
    this.orderBy = paramString2;
    this.start = paramZonedDateTime1;
    this.startRow = paramInt1;
    this.type = paramEventLogType;
  }
  
  public EventLogSearchCriteria(int paramInt1, int paramInt2) {
    prepare();
    this.numberOfResults = paramInt2;
    this.startRow = paramInt1;
  }
  
  public EventLogSearchCriteria prepare() {
    if (this.orderBy == null)
      this.orderBy = defaultOrderBy(); 
    this.orderBy = SQLTools.normalizeOrderBy(this.orderBy, SortableFields);
    this.message = SQLTools.toSearchString(this.message);
    return this;
  }
  
  public Set<String> supportedOrderByColumns() {
    return SortableFields.keySet();
  }
  
  protected String defaultOrderBy() {
    return "insertInstant DESC, id DESC";
  }
  
  static {
    SortableFields.put("id", "id");
    SortableFields.put("insertInstant", "insert_instant");
    SortableFields.put("message", "message");
    SortableFields.put("type", "type");
  }
}
