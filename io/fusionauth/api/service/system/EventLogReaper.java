package io.fusionauth.api.service.system;

import com.google.inject.Inject;

public class EventLogReaper implements Runnable {
  private final EventLogService eventLogService;
  
  @Inject
  public EventLogReaper(EventLogService paramEventLogService) {
    this.eventLogService = paramEventLogService;
  }
  
  public void run() {
    this.eventLogService.deleteOld();
  }
}
