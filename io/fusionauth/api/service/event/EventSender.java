package io.fusionauth.api.service.event;

import io.fusionauth.domain.event.BaseEvent;

public interface EventSender {
  EventSenderResult send(BaseEvent paramBaseEvent);
}
