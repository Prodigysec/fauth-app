package io.fusionauth.api.service.reindex;

import com.google.inject.Injector;
import com.inversoft.cache.CacheNotifier;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.EntityMapper;
import io.fusionauth.api.domain.RegistrationCountMapper;
import io.fusionauth.api.service.lock.ReindexDistributedLock;
import io.fusionauth.api.service.search.ReindexElasticsearchEntitySearchEngine;
import io.fusionauth.api.service.search.ReindexElasticsearchUserSearchEngine;
import io.fusionauth.api.service.search.SearchEngine;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.util.NumberTools;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.User;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexRunner<T> implements Runnable, Buildable<ReindexRunner<T>> {
  private static final Logger logger = LoggerFactory.getLogger(ReindexRunner.class);
  
  private final SqlSessionFactory backgroundSQLSessionFactory;
  
  private final int batchSize;
  
  private final Class<T> clazz;
  
  private final FusionAuthConfiguration configuration;
  
  private final Injector injector;
  
  private final ReindexDistributedLock reindexDistributedLock;
  
  private final int threadCount;
  
  public ReindexRunner(FusionAuthConfiguration paramFusionAuthConfiguration, SqlSessionFactory paramSqlSessionFactory, Injector paramInjector, ReindexDistributedLock paramReindexDistributedLock, Class<T> paramClass) {
    this.backgroundSQLSessionFactory = paramSqlSessionFactory;
    this.batchSize = paramFusionAuthConfiguration.reindexBatchSize();
    this.configuration = paramFusionAuthConfiguration;
    this.injector = paramInjector;
    this.threadCount = paramFusionAuthConfiguration.reindexThreadCount();
    this.reindexDistributedLock = paramReindexDistributedLock;
    this.clazz = paramClass;
  }
  
  public void run() {
    CacheNotifier cacheNotifier = (CacheNotifier)this.injector.getInstance(CacheNotifier.class);
    ArrayList<Closeable> arrayList = new ArrayList(2);
    SqlSession sqlSession = this.backgroundSQLSessionFactory.openSession(false);
    try {
      ReindexDistributedLock reindexDistributedLock = this.reindexDistributedLock.lock();
      try {
        String str2 = this.clazz.isAssignableFrom(User.class) ? "Users" : "Entities";
        if (reindexDistributedLock == null) {
          logger.debug("{} reindex attempted to start but the lock could not be obtained. Perhaps another re-index is already in progress.", str2);
          if (reindexDistributedLock != null)
            reindexDistributedLock.close(); 
          if (sqlSession != null)
            sqlSession.close(); 
          return;
        } 
        logger.info("Start {} reindex. Batch size [{}] Thread Count [{}]", new Object[] { str2, NumberTools.format(this.batchSize), Integer.valueOf(this.threadCount) });
        Instant instant = Instant.now();
        boolean bool = false;
        Closeable closeable = (Closeable)(this.clazz.isAssignableFrom(User.class) ? new ReindexElasticsearchUserSearchEngine(this.configuration, this.configuration.userSearchIndexName()) : new ReindexElasticsearchEntitySearchEngine(this.configuration, this.configuration.entitySearchIndexName()));
        arrayList.add(closeable);
        String str1 = closeable.createIndex();
        closeable = this.clazz.isAssignableFrom(User.class) ? new ReindexElasticsearchUserSearchEngine(this.configuration, str1) : new ReindexElasticsearchEntitySearchEngine(this.configuration, str1);
        arrayList.add(closeable);
        cacheNotifier.reload("SearchIndexNames");
        String str3 = closeable.getIndexRefreshInterval();
        String str4 = "60s";
        logger.info("Increase the Elasticsearch refresh interval to [" + str4 + "] from [" + str3 + "] during reindex.");
        closeable.setIndexRefreshInterval(str4);
        AtomicInteger atomicInteger = new AtomicInteger();
        long l = this.clazz.isAssignableFrom(User.class) ? ((RegistrationCountMapper)sqlSession.getMapper(RegistrationCountMapper.class)).retrieveGlobalCurrentTotal() : ((EntityMapper)sqlSession.getMapper(EntityMapper.class)).retrieveCountForReindex();
        try {
          ReindexQueue<T> reindexQueue = new ReindexQueue(this.threadCount);
          String str = this.clazz.isAssignableFrom(User.class) ? "retrieveUsersForReindex" : "retrieveEntitiesForReindex";
          Thread thread = new Thread(new ReindexQueueLoader<>(this.batchSize, this.clazz, atomicInteger, this.injector, reindexDistributedLock, reindexQueue, sqlSession, str, l), ReindexQueueLoader.class.getSimpleName());
          thread.setDaemon(true);
          thread.start();
          try {
            Thread.sleep(5000L);
          } catch (InterruptedException interruptedException) {}
          ArrayList<Thread> arrayList1 = new ArrayList();
          for (byte b = 0; b < this.threadCount; b++) {
            Thread thread1 = new Thread(new ReindexQueueWorker<>(reindexQueue, (SearchEngine<T>)closeable), "Worker " + b + 1);
            thread1.start();
            arrayList1.add(thread1);
          } 
          try {
            for (Thread thread1 : arrayList1)
              thread1.join(); 
          } catch (Exception exception) {
            logger.error("Unable to complete search engine reindex.", exception);
          } 
          logger.debug("All threads have completed.");
          bool = true;
          Instant instant2 = Instant.now();
          Duration duration2 = Duration.between(instant, instant2);
          createEventLog(EventLogType.Information, str2 + " reindex completed.", instant, instant2, duration2, atomicInteger, str2);
        } finally {
          logger.info("Set Elasticsearch refresh interval to default [" + this.configuration.searchEngineDefaultRefreshInterval() + "]");
          closeable.setIndexRefreshInterval(this.configuration.searchEngineDefaultRefreshInterval());
          Instant instant1 = Instant.now();
          Duration duration = Duration.between(instant, instant1);
          if (bool) {
            closeable.updateAliasDeleteOldIndex();
            logger.info("{} reindex completed in [{}] ms or [{}] seconds.", new Object[] { str2, NumberTools.format(duration.toMillis()), NumberTools.format(TimeUnit.MILLISECONDS.toSeconds(duration.toMillis())) });
          } else {
            if (str1 != null)
              closeable.deleteIndex(str1); 
            logger.error("{} reindex failed to complete. {} indexed [{}] of an expected count of [{}]. See event log for additional details.", new Object[] { str2, this.clazz.getSimpleName(), NumberTools.format(atomicInteger.get()), NumberTools.format(l) });
            createEventLog(EventLogType.Error, str2 + " reindex did not complete successfully.", instant, instant1, duration, atomicInteger, str2);
          } 
          cacheNotifier.reload("SearchIndexNames");
          closeable.refresh();
          for (Closeable closeable1 : arrayList) {
            try {
              closeable1.close();
            } catch (IOException iOException) {}
          } 
        } 
        if (reindexDistributedLock != null)
          reindexDistributedLock.close(); 
      } catch (Throwable throwable) {
        if (reindexDistributedLock != null)
          try {
            reindexDistributedLock.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
      if (sqlSession != null)
        sqlSession.close(); 
    } catch (Throwable throwable) {
      if (sqlSession != null)
        try {
          sqlSession.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  private void createEventLog(EventLogType paramEventLogType, String paramString1, Instant paramInstant1, Instant paramInstant2, Duration paramDuration, AtomicInteger paramAtomicInteger, String paramString2) {
    EventLogHelper.create(new EventLog(paramEventLogType, "${heading}\n\nBatch size: ${batchSize}\nQueue workers: ${workerCount}\n\nStart instant: ${start}\nEnd instant: ${end}\n\nTotal time in ms: ${duration}\n${reindexType} re-indexed: ${totalDocuments}\n"










          
          .replace("${heading}", paramString1)
          .replace("${batchSize}", NumberTools.format(this.batchSize))
          .replace("${workerCount}", "" + this.threadCount)
          .replace("${start}", "" + paramInstant1.toEpochMilli())
          .replace("${end}", "" + paramInstant2.toEpochMilli())
          .replace("${duration}", NumberTools.format(paramDuration.toMillis()))
          .replace("${reindexType}", paramString2)
          .replace("${totalDocuments}", NumberTools.format(paramAtomicInteger.get()))));
  }
}
