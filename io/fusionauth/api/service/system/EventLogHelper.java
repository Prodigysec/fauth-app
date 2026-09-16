package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import io.fusionauth.domain.EventLog;

public class EventLogHelper {
  @Inject
  private static EventLogService eventLogService;
  
  public static void create(EventLog paramEventLog) {
    eventLogService.create(paramEventLog);
  }
  
  public static void create(EventLog paramEventLog, boolean paramBoolean) {
    eventLogService.create(paramEventLog, paramBoolean);
  }
  
  @Inject
  public void setEventLogService(EventLogService paramEventLogService) {
    eventLogService = paramEventLogService;
  }
}
