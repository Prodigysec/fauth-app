package io.fusionauth.api.service.event;

import io.fusionauth.domain.event.BaseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class EventRetryQueue {
  private final Queue<RetryPayload> queue = new ConcurrentLinkedDeque<>();
  
  public void add(RetryPayload paramRetryPayload) {
    this.queue.add(paramRetryPayload);
  }
  
  public void clear() {
    this.queue.clear();
  }
  
  public Collection<RetryPayload> popAll() {
    ArrayList<RetryPayload> arrayList = new ArrayList();
    while (this.queue.size() > 0)
      arrayList.add(this.queue.poll()); 
    return arrayList;
  }
  
  public static class RetryPayload {
    public final BaseEvent event;
    
    public final EventSender eventSender;
    
    public int retryCount;
    
    public RetryPayload(BaseEvent param1BaseEvent, EventSender param1EventSender) {
      this.event = param1BaseEvent;
      this.eventSender = param1EventSender;
    }
    
    public boolean retry() {
      return (this.eventSender.send(this.event)).success;
    }
  }
}
