package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import io.fusionauth.api.domain.AsyncTask;
import io.fusionauth.api.domain.AsyncTaskMapper;
import io.fusionauth.api.domain.FusionAuthNodeMapper;
import io.fusionauth.api.domain.LockType;
import io.fusionauth.api.domain.TenantMapper;
import io.fusionauth.api.domain.guice.mybatis.UseDataSource;
import io.fusionauth.api.service.lock.LockService;
import io.fusionauth.api.service.system.task.DeleteTenantTask;
import io.fusionauth.api.service.system.task.FailSafeRunnable;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.ObjectState;
import java.io.Closeable;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAsyncTaskManager implements Closeable, Runnable, AsyncTaskManager {
  private static final int TaskTimeoutMinutes = 20;
  
  private static final Logger logger = LoggerFactory.getLogger(DefaultAsyncTaskManager.class);
  
  protected final AsyncTaskMapper backgroundAsyncTaskMapper;
  
  protected final LockService lockService;
  
  protected final Map<UUID, Thread> tasks = new HashMap<>();
  
  private final ScheduledExecutorService executorService;
  
  private final Injector injector;
  
  private volatile boolean running = true;
  
  @Inject
  public DefaultAsyncTaskManager(@Named("background") AsyncTaskMapper paramAsyncTaskMapper, Injector paramInjector, LockService paramLockService) {
    this.backgroundAsyncTaskMapper = paramAsyncTaskMapper;
    this.injector = paramInjector;
    this.lockService = paramLockService;
    this.executorService = Executors.newSingleThreadScheduledExecutor(paramRunnable -> {
          Thread thread = new Thread(paramRunnable, "AsyncTaskManager");
          thread.setDaemon(true);
          return thread;
        });
    this.executorService.schedule(this, getExecutorDelay().toMillis(), TimeUnit.MILLISECONDS);
  }
  
  @Transactional
  @UseDataSource("background")
  public void _doRun() {
    this.lockService.acquireLock(LockType.AsyncTaskManager);
    List<AsyncTask> list1 = this.backgroundAsyncTaskMapper.retrieveByStatus(AsyncTask.AsyncTaskStatus.Pending);
    processPendingTasks(list1);
    List<AsyncTask> list2 = this.backgroundAsyncTaskMapper.retrieveByStatus(AsyncTask.AsyncTaskStatus.Running);
    for (AsyncTask asyncTask : list2) {
      if (asyncTask.nodeId == null) {
        logger.info("Node running [{}] [{}] task for [{}] is no longer active. Resetting task status to [{}].", new Object[] { asyncTask.id, asyncTask.type, asyncTask.entityId, AsyncTask.AsyncTaskStatus.Pending });
        asyncTask.status = AsyncTask.AsyncTaskStatus.Pending;
        asyncTask.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
        this.backgroundAsyncTaskMapper.update(asyncTask);
        continue;
      } 
      if (!asyncTask.nodeId.equals(FusionAuthNodeMapper.FusionAuthNode.ID)) {
        if (asyncTask.lastUpdateInstant.isBefore(ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(20L))) {
          logger.error("Node [{}] running [{}] [{}] task for [{}] is no longer available. Resetting entity state and removing task.", new Object[] { asyncTask.nodeId, asyncTask.id, asyncTask.type, asyncTask.entityId });
          resetEntityState(asyncTask);
          deleteTask(asyncTask);
          continue;
        } 
        logger.debug("[{}] [{}] task for [{}] is running. Did not find a local job, another node is managing this task.", new Object[] { asyncTask.id, asyncTask.type, asyncTask.entityId });
        continue;
      } 
      Thread thread = this.tasks.get(asyncTask.id);
      if (thread == null) {
        logger.error("Node [{}] running [{}] [{}] task for [{}] is missing reference to processing thread. Resetting entity state and removing task.", new Object[] { asyncTask.nodeId, asyncTask.id, asyncTask.type, asyncTask.entityId });
        resetEntityState(asyncTask);
        deleteTask(asyncTask);
        continue;
      } 
      if (thread.isAlive()) {
        asyncTask.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
        this.backgroundAsyncTaskMapper.update(asyncTask);
        continue;
      } 
      if (isTaskComplete(asyncTask)) {
        logger.info("[{}] [{}] task for [{}] has completed successfully.", new Object[] { asyncTask.id, asyncTask.type, asyncTask.entityId });
      } else {
        logger.warn("[{}] [{}] task for [{}] failed to complete. You may need to retry the request. Please view the EventLog for additional information.", new Object[] { asyncTask.id, asyncTask.type, asyncTask.entityId });
      } 
      this.tasks.remove(asyncTask.id);
      deleteTask(asyncTask);
    } 
    Set set = (Set)Stream.concat(list1.stream(), list2.stream()).map(paramAsyncTask -> paramAsyncTask.id).collect(Collectors.toSet());
    this.tasks.entrySet().removeIf(paramEntry -> (!paramSet.contains(paramEntry.getKey()) && !((Thread)paramEntry.getValue()).isAlive()));
  }
  
  public void close() {
    logger.info("Shutting down the AsyncTaskManager.");
    this.running = false;
    List<AsyncTask> list = this.backgroundAsyncTaskMapper.retrieveByStatus(AsyncTask.AsyncTaskStatus.Running);
    for (AsyncTask asyncTask : list) {
      Thread thread = this.tasks.get(asyncTask.id);
      if (thread != null) {
        asyncTask.status = AsyncTask.AsyncTaskStatus.Pending;
        this.backgroundAsyncTaskMapper.update(asyncTask);
      } 
    } 
    this.executorService.shutdownNow();
  }
  
  @Transactional
  @UseDataSource("background")
  public boolean offer(AsyncTask paramAsyncTask) {
    this.lockService.acquireLock(LockType.AsyncTaskManager);
    AsyncTask asyncTask = this.backgroundAsyncTaskMapper.retrieveByEntityId(paramAsyncTask.entityId);
    if (asyncTask != null)
      return false; 
    updateEntityState(paramAsyncTask);
    paramAsyncTask.id = UUID.randomUUID();
    paramAsyncTask.status = AsyncTask.AsyncTaskStatus.Pending;
    paramAsyncTask.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramAsyncTask.lastUpdateInstant = paramAsyncTask.insertInstant;
    this.backgroundAsyncTaskMapper.create(paramAsyncTask);
    TaskManagerSignal.signal();
    return true;
  }
  
  public void run() {
    while (this.running) {
      try {
        _doRun();
        TaskManagerSignal.await(31L);
      } catch (Throwable throwable) {
        try {
          logger.error("An exception occurred while managing an async task.", throwable);
          EventLogHelper.create(new EventLog(EventLogType.Error, "An exception occurred while managing an async task.", throwable));
        } catch (Throwable throwable1) {
          logger.error("Failed to create an event log to indicate why an async task failed.", throwable1);
        } 
      } 
    } 
  }
  
  protected Runnable buildRunnable(AsyncTask paramAsyncTask) {
    switch (paramAsyncTask.type) {
      default:
        throw new MatchException(null, null);
      case DeleteTenant:
        break;
    } 
    Runnable runnable = (
      (DeleteTenantTask)this.injector.getInstance(DeleteTenantTask.class)).get(paramAsyncTask);
    if (runnable == null)
      return null; 
    return new FailSafeRunnable(this.injector, runnable, paramAsyncTask);
  }
  
  protected void deleteTask(AsyncTask paramAsyncTask) {
    this.backgroundAsyncTaskMapper.delete(paramAsyncTask);
  }
  
  protected Duration getExecutorDelay() {
    return Duration.ofSeconds(60L);
  }
  
  protected boolean isTaskComplete(AsyncTask paramAsyncTask) {
    switch (paramAsyncTask.type) {
      default:
        throw new MatchException(null, null);
      case DeleteTenant:
        break;
    } 
    return (
      (DeleteTenantTask)this.injector.getInstance(DeleteTenantTask.class)).isComplete(paramAsyncTask);
  }
  
  protected void processPendingTasks(List<AsyncTask> paramList) {
    for (AsyncTask asyncTask : paramList) {
      Runnable runnable = buildRunnable(asyncTask);
      if (runnable == null) {
        logger.error("[{}] [{}] task for [{}]. No runnable returned.", new Object[] { asyncTask.id, asyncTask.type, asyncTask.entityId });
        deleteTask(asyncTask);
        continue;
      } 
      submitTask(asyncTask, runnable);
    } 
  }
  
  protected void resetEntityState(AsyncTask paramAsyncTask) {
    switch (paramAsyncTask.type) {
      case DeleteTenant:
        ((DeleteTenantTask)this.injector.getInstance(DeleteTenantTask.class)).resetEntityState(paramAsyncTask);
        break;
    } 
  }
  
  protected void submitTask(AsyncTask paramAsyncTask, Runnable paramRunnable) {
    logger.info("[{}] Submit [{}] task for [{}].", new Object[] { paramAsyncTask.id, paramAsyncTask.type, paramAsyncTask.entityId });
    Thread thread = new Thread(paramRunnable);
    thread.setName("Async [" + String.valueOf(paramAsyncTask.type) + "] task for [" + String.valueOf(paramAsyncTask.entityId) + "].");
    thread.setDaemon(true);
    thread.start();
    this.tasks.put(paramAsyncTask.id, thread);
    paramAsyncTask.status = AsyncTask.AsyncTaskStatus.Running;
    paramAsyncTask.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramAsyncTask.nodeId = FusionAuthNodeMapper.FusionAuthNode.ID;
    this.backgroundAsyncTaskMapper.update(paramAsyncTask);
  }
  
  private void updateEntityState(AsyncTask paramAsyncTask) {
    switch (paramAsyncTask.type) {
      case DeleteTenant:
        ((TenantMapper)this.injector.getInstance(TenantMapper.class)).updateState(paramAsyncTask.entityId, ObjectState.PendingDelete);
        break;
    } 
  }
  
  public static final class TaskManagerSignal {
    private static final Object SIGNAL = new Object();
    
    public static void await(long param1Long) {
      synchronized (SIGNAL) {
        try {
          SIGNAL.wait(TimeUnit.SECONDS.toMillis(param1Long));
        } catch (InterruptedException interruptedException) {}
      } 
    }
    
    public static void signal() {
      synchronized (SIGNAL) {
        SIGNAL.notify();
      } 
    }
  }
}
