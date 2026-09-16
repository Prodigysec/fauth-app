package io.fusionauth.api.service.event;

import io.fusionauth.domain.event.BaseEvent;
import java.util.List;
import java.util.concurrent.Future;

public interface EventExecutorService {
  List<Future<EventSenderResult>> send(BaseEvent paramBaseEvent, List<EventSender> paramList);
}
