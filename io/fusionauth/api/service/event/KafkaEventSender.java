package io.fusionauth.api.service.event;

import io.fusionauth.api.service.messaging.KafkaService;
import io.fusionauth.domain.event.BaseEvent;

public class KafkaEventSender implements EventSender {
  public final KafkaService kafkaService;
  
  public KafkaEventSender(KafkaService paramKafkaService) {
    this.kafkaService = paramKafkaService;
  }
  
  public EventSenderResult send(BaseEvent paramBaseEvent) {
    EventSenderResult eventSenderResult = this.kafkaService.sendEvent(paramBaseEvent);
    eventSenderResult.sender = this;
    return eventSenderResult;
  }
}
