package io.fusionauth.api.service.reindex;

import io.fusionauth.api.service.search.SearchEngine;
import io.fusionauth.api.util.NumberTools;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexQueueWorker<T> implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(ReindexQueueWorker.class);
  
  private final SearchEngine<T> searchEngine;
  
  private final ReindexQueue<T> userQueue;
  
  public ReindexQueueWorker(ReindexQueue<T> paramReindexQueue, SearchEngine<T> paramSearchEngine) {
    this.searchEngine = paramSearchEngine;
    this.userQueue = paramReindexQueue;
  }
  
  public void run() {
    String str = Thread.currentThread().getName();
    logger.debug("[{}] Started", str);
    while (true) {
      Instant instant = Instant.now();
      List<T> list = this.userQueue.poll();
      Duration duration = Duration.between(instant, Instant.now());
      if (list == null) {
        logger.debug("[{}] Complete", str);
        return;
      } 
      if (list.size() > 0) {
        logger.debug("[{}] Took [{}] off of the queue to reindex in [{}] ms.", new Object[] { str, NumberTools.format(list.size()), NumberTools.format(duration.toMillis()) });
        Instant instant1 = Instant.now();
        this.searchEngine.index(list);
        Duration duration1 = Duration.between(instant1, Instant.now());
        logger.debug("[{}] Search index request took [{}] ms", str, NumberTools.format(duration1.toMillis()));
        continue;
      } 
      logger.error("[{}] Batch size retrieved is 0, but we aren't finished.", str);
    } 
  }
}
