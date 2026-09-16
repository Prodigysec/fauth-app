package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import io.fusionauth.api.service.event.EventService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.event.BaseEvent;

public class EventHelper {
  @Inject
  private static EventService eventService;
  
  public static void send(Tenant paramTenant, Application paramApplication, BaseEvent paramBaseEvent) {
    eventService.send(paramTenant, paramApplication, paramBaseEvent);
  }
  
  @Inject
  public void setEventService(EventService paramEventService) {
    eventService = paramEventService;
  }
}
