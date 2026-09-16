package io.fusionauth.api.service.reindex;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.GroupMapper;
import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.api.service.lock.ReindexDistributedLock;
import io.fusionauth.api.service.user.DefaultUserReaderService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.util.NumberTools;
import io.fusionauth.domain.User;
import java.lang.annotation.Annotation;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexQueueLoader<T> implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(ReindexQueueLoader.class);
  
  private final SqlSession backgroundSQLSession;
  
  private final int batchSize;
  
  private final Class<T> clazz;
  
  private final AtomicInteger counter;
  
  private final Injector injector;
  
  private final ReindexDistributedLock lock;
  
  private final ReindexQueue<T> queue;
  
  private final String sqlStatement;
  
  private final long totalCountExpected;
  
  public ReindexQueueLoader(int paramInt, Class<T> paramClass, AtomicInteger paramAtomicInteger, Injector paramInjector, ReindexDistributedLock paramReindexDistributedLock, ReindexQueue<T> paramReindexQueue, SqlSession paramSqlSession, String paramString, long paramLong) {
    this.backgroundSQLSession = paramSqlSession;
    this.batchSize = paramInt;
    this.clazz = paramClass;
    this.counter = paramAtomicInteger;
    this.injector = paramInjector;
    this.lock = paramReindexDistributedLock;
    this.queue = paramReindexQueue;
    this.sqlStatement = paramString;
    this.totalCountExpected = paramLong;
  }
  
  public void run() {
    List[] arrayOfList = { new ArrayList(this.batchSize) };
    Instant instant = Instant.now();
    Instant[] arrayOfInstant1 = { Instant.now() };
    Instant[] arrayOfInstant2 = { Instant.now() };
    Instant[] arrayOfInstant3 = { Instant.now() };
    String str = this.clazz.isAssignableFrom(User.class) ? "users" : "entities";
    UserMapper userMapper = str.equals("users") ? (UserMapper)this.injector.getInstance(Key.get(UserMapper.class, (Annotation)Names.named("background"))) : null;
    GroupMapper groupMapper = str.equals("users") ? (GroupMapper)this.injector.getInstance(Key.get(GroupMapper.class, (Annotation)Names.named("background"))) : null;
    FusionAuthConfiguration fusionAuthConfiguration = str.equals("users") ? (FusionAuthConfiguration)this.injector.getInstance(FusionAuthConfiguration.class) : null;
    try {
      logger.info("0% complete. Processed [0] of [{}] {}.", NumberTools.format(this.totalCountExpected), str);
      this.backgroundSQLSession.select(this.sqlStatement, paramResultContext -> {
            Object object = paramResultContext.getResultObject();
            if (paramString.equals("users")) {
              User user = (User)object;
              DefaultUserReaderService.fixLegacyIdentity(user);
            } 
            paramArrayOfList[0].add(object);
            this.counter.getAndIncrement();
            if (paramArrayOfList[0].size() >= this.batchSize) {
              if (paramString.equals("users"))
                paramArrayOfList[0] = DefaultUserReaderService.expand(paramArrayOfList[0], UserReaderService.UserExpansion.all(), paramFusionAuthConfiguration, paramUserMapper, paramGroupMapper); 
              Duration duration = Duration.between(paramArrayOfInstant1[0], Instant.now());
              logger.debug("Add [{}] {} to the queue, took [{}]. Remaining queue capacity [{}].", new Object[] { paramString, NumberTools.format(this.batchSize), NumberTools.format(duration.toMillis()), Integer.valueOf(this.queue.getRemainingCapacity()) });
              this.queue.add(paramArrayOfList[0]);
              paramArrayOfList[0] = new ArrayList(this.batchSize);
              Thread.yield();
              paramArrayOfInstant1[0] = Instant.now();
            } 
            if (paramArrayOfInstant2[0].plus(15L, ChronoUnit.SECONDS).isBefore(Instant.now())) {
              this.lock.keepAlive();
              paramArrayOfInstant2[0] = Instant.now();
            } 
            if (paramArrayOfInstant3[0].plus(1L, ChronoUnit.MINUTES).isBefore(Instant.now())) {
              float f = this.counter.get() * 100.0F / (float)this.totalCountExpected;
              logger.info("{}% complete. Processed [{}] of [{}] {}.", new Object[] { String.format("%.1f", new Object[] { Float.valueOf(f) }), NumberTools.format(this.counter.get()), NumberTools.format(this.totalCountExpected), paramString });
              paramArrayOfInstant3[0] = Instant.now();
            } 
          });
      if (!arrayOfList[0].isEmpty()) {
        if (str.equals("users"))
          arrayOfList[0] = DefaultUserReaderService.expand(arrayOfList[0], UserReaderService.UserExpansion.all(), fusionAuthConfiguration, userMapper, groupMapper); 
        logger.debug("Add remaining [{}] {} to the queue.", NumberTools.format(arrayOfList[0].size()), str);
        this.queue.add(arrayOfList[0]);
      } 
      logger.info("100% complete. Processed [{}] {} in [{}] ms.", new Object[] { NumberTools.format(this.counter.get()), str, NumberTools.format(Duration.between(instant, Instant.now()).toMillis()) });
    } catch (Throwable throwable) {
      float f = this.counter.get() * 100.0F / (float)this.totalCountExpected;
      logger.error(String.format("Failed to complete. %s%% complete. Processed [%s] of [%s] %s.", new Object[] { String.format("%.1f", new Object[] { Float.valueOf(f) }), NumberTools.format(this.counter.get()), NumberTools.format(this.totalCountExpected), str }), throwable);
    } finally {
      this.queue.finished();
    } 
  }
}
