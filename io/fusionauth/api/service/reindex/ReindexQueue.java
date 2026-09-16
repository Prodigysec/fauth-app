package io.fusionauth.api.service.reindex;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexQueue<T> {
  private static final Logger logger = LoggerFactory.getLogger(ReindexQueue.class);
  
  private final BlockingQueue<List<T>> queue;
  
  private final int queuePollTimeout = 10;
  
  private boolean finished;
  
  public ReindexQueue(int paramInt) {
    this.queue = new LinkedBlockingDeque<>(paramInt);
  }
  
  public void add(List<T> paramList) {
    while (true) {
      try {
        this.queue.put(paramList);
        return;
      } catch (InterruptedException interruptedException) {
        String str = Thread.currentThread().getName();
        logger.warn("[{}] Queue add caught InterruptedException.", str);
      } 
    } 
  }
  
  public synchronized void finished() {
    this.finished = true;
  }
  
  public int getRemainingCapacity() {
    return this.queue.remainingCapacity();
  }
  
  public List<T> poll() {
    while (!this.finished || this.queue.size() > 0) {
      String str = Thread.currentThread().getName();
      try {
        List<T> list = this.queue.poll(10L, TimeUnit.SECONDS);
        if (list != null)
          return list; 
        logger.debug("[{}] Timed out waiting to poll. Waited [{}] s. {}.", new Object[] { str, Integer.valueOf(10), this.finished ? "Done" : " Retry" });
      } catch (InterruptedException interruptedException) {
        logger.warn("[{}] Queue poll caught InterruptedException.", str);
      } 
    } 
    return null;
  }
}
