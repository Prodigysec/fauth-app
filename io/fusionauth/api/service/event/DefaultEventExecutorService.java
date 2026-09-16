package io.fusionauth.api.service.event;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.fusionauth.domain.event.BaseEvent;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Singleton
public class DefaultEventExecutorService implements EventExecutorService, Closeable {
  private final ExecutorService executorService;
  
  @Inject
  public DefaultEventExecutorService(@Named("EventExecutorService") ExecutorService paramExecutorService) {
    this.executorService = paramExecutorService;
  }
  
  public void close() {
    this.executorService.shutdownNow();
  }
  
  public List<Future<EventSenderResult>> send(BaseEvent paramBaseEvent, List<EventSender> paramList) {
    return paramList.stream()
      .map(paramEventSender -> this.executorService.submit(new SenderTask(paramEventSender, paramBaseEvent)))
      .toList();
  }
  
  private static class SenderTask implements Callable<EventSenderResult> {
    private final BaseEvent event;
    
    private final EventSender sender;
    
    public SenderTask(EventSender param1EventSender, BaseEvent param1BaseEvent) {
      this.sender = param1EventSender;
      this.event = param1BaseEvent;
    }
    
    public EventSenderResult call() {
      return this.sender.send(this.event);
    }
  }
}
