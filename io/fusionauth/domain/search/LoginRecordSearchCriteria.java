package io.fusionauth.domain.search;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class LoginRecordSearchCriteria extends BaseSearchCriteria implements Buildable<LoginRecordSearchCriteria> {
  public UUID applicationId;
  
  public ZonedDateTime end;
  
  public ZonedDateTime start;
  
  public UUID userId;
  
  @JacksonConstructor
  public LoginRecordSearchCriteria() {
    this.orderBy = null;
  }
  
  public LoginRecordSearchCriteria(UUID paramUUID1, UUID paramUUID2, ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2) {
    this.userId = paramUUID1;
    this.applicationId = paramUUID2;
    this.start = paramZonedDateTime1;
    this.end = paramZonedDateTime2;
    this.orderBy = null;
  }
  
  public LoginRecordSearchCriteria(UUID paramUUID1, UUID paramUUID2, ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2, int paramInt1, int paramInt2) {
    this.userId = paramUUID1;
    this.applicationId = paramUUID2;
    this.start = paramZonedDateTime1;
    this.end = paramZonedDateTime2;
    this.startRow = paramInt1;
    this.numberOfResults = paramInt2;
    this.orderBy = null;
  }
  
  public LoginRecordSearchCriteria prepare() {
    this.orderBy = null;
    return this;
  }
  
  public Set<String> supportedOrderByColumns() {
    return Collections.emptySet();
  }
}
