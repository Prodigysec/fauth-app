package io.fusionauth.domain.search;

import io.fusionauth.api.domain.ZonedDateTimeWrapper;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.WebhookEventResult;
import io.fusionauth.domain.event.EventType;
import io.fusionauth.domain.util.SQLTools;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class WebhookEventLogSearchCriteria extends BaseSearchCriteria implements Buildable<WebhookEventLogSearchCriteria> {
  public static final Map<String, String> SortableFields = new LinkedHashMap<>();
  
  public ZonedDateTime end = ZonedDateTimeWrapper.now(ZoneOffset.UTC).plusMinutes(1L).truncatedTo(ChronoUnit.MINUTES);
  
  public String event;
  
  public WebhookEventResult eventResult;
  
  public EventType eventType;
  
  public ZonedDateTime start = ZonedDateTimeWrapper.now(ZoneOffset.UTC).minusHours(1L).truncatedTo(ChronoUnit.MINUTES);
  
  public WebhookEventLogSearchCriteria prepare() {
    if (this.orderBy == null)
      this.orderBy = defaultOrderBy(); 
    this.orderBy = SQLTools.normalizeOrderBy(this.orderBy, SortableFields);
    this.event = SQLTools.toSearchString(this.event);
    return this;
  }
  
  public Set<String> supportedOrderByColumns() {
    return SortableFields.keySet();
  }
  
  protected String defaultOrderBy() {
    return "insertInstant DESC";
  }
  
  static {
    SortableFields.put("eventResult", "w.event_result");
    SortableFields.put("eventType", "w.event_type");
    SortableFields.put("id", "w.id");
    SortableFields.put("insertInstant", "w.insert_instant");
    SortableFields.put("lastAttemptInstant", "w.last_attempt_instant");
    SortableFields.put("linkedObjectId", "w.linked_object_id");
    SortableFields.put("sequence", "w.sequence");
  }
}
